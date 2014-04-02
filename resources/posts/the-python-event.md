{:type :post
 :when "2013-02-03"
 :slug "the-python-event"
 :title "On discovering a message-based architecture"
 :tags "software"
 :publish? true}

This is the story of how I became interested in basing all distributed
systems on asynchronous messaging.

## the problem

Some time ago I worked on a project to bootstrap a video conferencing
system. The hardware for the system was largely done, but the software
wasn't much more than a few Perl scripts the hardware folks threw
together to show off their gear.

After some analysis, a colleague and I determined that the work to be
done broke down into two concerns:

  - A user interface allowing video conference room participants to
    turn on a document camera, connect to a room in another city, view
    a catalog of available rooms. (I also added an administrative user
    interface.)

  - A library for interacting with the rooms themselves. Each room had
    several machines with network interfaces. To connect a room in one
    city to a room in another city, you had to send signals to both
    rooms telling them to reach out and touch each other.

A colleague and I split up the work. I took the "UI" portion (which
involved a lot of data modeling and usability guesses). He took the
library concern. He loved the problem of brokering socket connections
to RS-232 bridges. Me? Not so much.

We decided to write the whole thing in Python rather than the _de
rigueur_ Java or .NET. He'd work on his code separately, I'd work on
mine, and then we'd hook them up to get this sort of functionality:

  * When a user pressed a remote-city's button on the in-room GUI, I'd
    record that connection in the database, then call my colleague's
    library to make the hardware perform the actual sound / video
    connections.

  * When a user pressed a document camera button, I'd record that it
    was on (so that UIs would reflect this on both sides), then call
    his library to do the work.

My colleague's library was pretty cool. He had configuration files for
each room such that we could connect them appropriately. We talked a
bit about the interface, how to import one side into the other, etc.

## recap

So, what we developed and tested separately was:

  - An admin UI and video conference room UI written in Python, backed
    by a Postgres database.

  - A system interface library, also written in Python, with public
    methods for connecting and disconnection rooms, turning on lights
    and cameras, and so on.

The thing is, we used threads, a _felix culpa_ if there ever was one.

## threads

The web server used threads, of course, to handle simultaneous HTTP
requests.[^async]

The system interface library used threads to manage communication
channels to all the devices at the core of each room participating in
a conference.

When we merged the code together (the web app pulling in the system
interface library), nothing worked. I suspect this was the result of
Python's [Global Interpreter Lock][gil].

To tell you the truth, I can't remember the exact details of the
problem because the solution was so much more entertaining and (for me
at least) paradigm shifting.

## messages

I'd been interested for a long time in the idea of applications
"chatting" to each other in the same way that bots on IRC or Jabber
chatted with users, or each other, or, well, in the way that viruses
installed IRC bots on individual machines and had them communicate
back to an IRC room to await nefarious instructions. I just liked the
whole idea of "chatty" apps, how easy they were to write, and how a
single person could write a bot, yet participate in a rich,
greater-than-the-whole system.

So, to solve our concurrency problem, I proposed we use a message bus
so that our two layers could communicate without having to run in the
same process space.

My colleague was all for this because he wanted to have complete
freedom to change his code in the same way I did. As long as we agreed
on a simple, high-level message format, we could have complete freedom
on either side of it.

This kind of thing makes for very happy developers. You know it's
true.

## options

Why not have both sides implement a REST style web service and just
have calls back and forth?

  - REST was very new at that time and not quite yet the stultifying
    silver-bullet it has become today.

  - REST interfaces are good for leaf nodes, but for inter-system
    communication, a message bus in which publishers don't care who
    consumes, and consumers don't care who produces, seems far, far
    more flexible and far less prone to technical debt.

  - In a distributed world, managing the configuration and tracking of
    all the end points across multiple possible deployment scenarios
    wasn't really the best use of our time.

  - A messaging system facilitates the "symantics" of event driven
    software, which seemed just write for a video conferencing
    system. Sure, you can do that with Web Services, but it's not a
    natural fit and is thus easy to break discipline and incur technical
    debt.

  - Fire and Forget 1: I wanted my UI to tell the system interface
    library to "connect room-a room-b" and then assume that it
    happened. I don't need a response back, because the connection
    would be obvious to the users of the room.

  - Fire and Forget 2: If the system interface lib needed to, it could
    send me a message back, something like, "disconnect room-a room-b"
    and I'd be able to adjust the state of the conferencing system
    accordingly. I could even make a request for "give me everything"
    and then adjust my state accordingly.

  - Messaging Systems are cool.[^cool]

So, that's what we did.

## simple messaging irc style

I wrote a message queue server in about 150 lines of Python and the
Twisted Framework, implenting a line-oriented protocol on a TCP
socket. The protocol was so simple you could log in via Telnet and
test things out.

For example:

    login <user> <pass>
    recv <topic>
    leave <topic>
    send <topic> <message>
    logoff

and so on. Truthfully, I don't remember much of the protocol anymore
but it was about as simple as the above, or maybe simpler. (If I can
make a message protocol simpler, I will). Any connection joining a
topic would receive all lines sent from other clients sending to that
topic.

Sending messages was as simple as writing a line to a socket:

    send syslib connect room1 room2

If you're thinking this is just like IRC, then you get it.

This worked really well for lots of reasons:

  - My colleague used the message bus to "backup" his data sets by
    sending messages to a separate listening process, making up his
    own protocol messages as needed.

  - We didn't have to deploy the UI concerns on the same host as the
    system interface library, each of which had (potentially)
    different performance needs.

  - _Not_ sharing code made each of our code bases much simpler and
    language/technique/architecture agnostic.

  - We could fire up telnet and verify that my code was actually
    issuing commands.

  - We could fire up telnet and send messages to the UI to make sure
    it did what we thought it would do.

  - When running in production, we could log every message and have a
    pretty good idea of what went on.

  - The code was small, easy to understand, and the line-oriented
    protocol resisted the propensity for developers to over-engineer
    messages. The lack of persistent queues never re-inforced the
    notion that message busses are for moving data, not storing it.

  - Text-based messages were easy to inspect and required nothing more
    than basic Unix utils.

But the main thing was that we could work together without any
personal or technical conflicts and with a sense of efficacy. The
architecture itself reflected the nature of how we work as human
beings, especially experienced, opinionated human beings.

## the beginning

This asynchronous message-based architecture worked so well I started
to wonder where else the pattern could be used.

For instance, what about shared databases? These are always a problem.
If the developer of one application needs to make a change to the
database schema, he'll have to negotiate with all the other developers
of the other applications using that same database. If those
developers report to different organizations, things get even more
complicated and slow --- and political.

A good way to deal with this is to put some sort of service interface
in front of the database. A REST web service, for instance.

But what if you used some form of messaging to transmit copies of
database from one system, or "concern" to another? What if every
change to a database was broadcast such that any interested parties
could slurp up the change, ignore what it didn't care about, and write
down the rest in a way that made sense for that particular concern?

As typical software developers, the idea of having "copies" of
something smells a lot like cutting and pasting. The thing is, the
needs of big distributed systems are not the same as a small
application in which you implement algorithms once and then
re-use. Right? And we're talking about data replication, not
functionality.

Anyway, all of this lead me to investigate JMS in the Java
ecosystem[^amqp], but once I got into Erlang (in which there is _no_
shared state or mutable data), I pretty much moved into the mode in
which I could no longer be satisfied with typical server-side
Corporate Development and the Best Practices it tends to s[t]olidify.

That's when I became the guy for whom bozo bits are flipped.

[gil]: http://en.wikipedia.org/wiki/Global_Interpreter_Lock

[^async]: This was in the days before async socket based web servers
were all the hype. I suspect it wouldn't have made much difference,
though.

[^cool]: I've gotten so much value at conceiving of a system of
applications as agents communicating via asychronous messaging that I
think the burdern of proof is on the designer who wants to do
something else. By which I mean, it's difficult for me to recover the
persuasive arguments in the face of such self-evident goodness.

[^amqp]: Since trumped by the various AMQP-like implementations.
