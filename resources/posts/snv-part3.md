{:type :post
 :when "2009-07-23 13:01"
 :slug "case-study-asynchronous-rest-3"
 :title "On the operational details (3 of 3)"
 :tags "software"
 :publish? true}

## overview

There are two other parts to this epic story:

  * [Part 1][part1], about the problem we had to solve (validating
    product serial numbers), and the resources available to us to
    solve the problem, and

  * [Part 2][part2], about our solution: using an asynchronous web
    service as an external interface to our application, and
    asynchronous messaging as the backbone of the internal
    architecture.

This third and final part is a catch all covering some of the
operational details of the service, including build, deployment, and
monitoring, connecting to the outside world, and testing.

## digression on the evils of the "software factory"

Why would I, a developer of this distributed, asynchronous
architecture, have much at all to say about operational details? Let
me begin with a digression:

A lot of my fellow colleagues &mdash; developers, operational staff,
and quality-assurance folks &mdash; tend to think that software can be
done in an assembly line fashion. The developers write the code,
someone else builds it, yet another team tests it, and a final team
deploys it. They see this as a sign of organizational maturity, or
even as part of the maturation of the software industry at large.

Alas, I don't believe the above for a minute. Yes, it _can_ work, but,
in my own experience, it turns a going concern into a slow moving,
classical <small>IT</small> shop, who says "no" to product, marketing
or sales groups, bogging them down in endless progress and process
details. (And I'm not even talking about what it does to developers.)
In fact, I'd say that projects running in Software Factory mode are,
essentially, dead projects. No growth, no change, no evolution, and no
radical discoveries that open up whole new possibilities.

I've found that the more detached a given development team is from
issues of testing and deployment, the more mistakes they make, and the
more mandated policy and management is required, thus causing even
more mistakes. At best, moving things along is slow. At worst,
developers make fundamentally bad designs not because the designs
don't work, but because they're too hard to operate. What makes sense
in a single binary doesn't make sense when an application is spread
over several binaries, and what makes sense on a single workstation
doesn't make sense running on multiple hosts in a data center.

But let me leave all this for another rant. What I'd like to talk
about is how the asynchronous messaging architecture facilitated
operational concerns.

## build & deploy

If you read [Part 2][part2],
you'll remember that we created five services making up the serial
number validation application:


  * **Submitter**: Accepted jobs for validation.

  * **Publisher**: Published results of validation.

  * **Oracle Querier**: Queried a remote Oracle Database for serial
      number data.

  * **Web Service Querier**: Queried a remote Web Service for serial
      number data.

  * **Refiner**: Delegated validation requests to the above query
      services, and assembled results for publication, including
      "fuzzy logic" for "almost" matches.

To build for deployment, we decided on the following principles:

  * Developers should be able to check out each project, compile and
    run it with _no extra environment setup_ on their development
    machines. In other words, projects, as organized in a revision
    control system, should be optimized for developer
    productivity. And by optimize we meant quick edit-compile-test
    cycles, and minimal (or no) documentation about how to set up your
    machine.

  * Production deployment issues should be captured in its own
    project, which knows how to check out the services, build them,
    apply operational details such as configuration,
    production-oriented log4j.properties (say), file locations, init.d
    start/stop scripts, etc.

The guiding principle for all of the above was to separate the issue
of developing the code from the issue of deploying it and then solving
each of the problems according to the problem's specific
requirements. (Using a single build process for both issues makes for
something far more complicated than keeping the concerns separate.)

Each of these services existed in a separate directory in a Subversion
Repository. Each service was build-able on the command line using
`ant`, which created a "target" subdirectory, moved all the
third-party jars, `log4j.properties` configuration and application
classes into that directory, and included a `run.sh` script which
could start the application for testing as you developed code.


Edit, compile, run, test wasn't much more than the following command
line:

    target> `cd .. ; ant ; cd target ; ./run.sh`

After changing the code, you could just hit Control-C, up-arrow (to
get the above line), and return. Experts could refashion the above
command-line to terminate if the compile was unsuccessful rather than
run the code regardless). <small>IDE</small> lovers could configure
their software to do the above, but why bother? Using the command-line
guarenteed that other software (such as the packager or tester) could
also check out and build your app without involving an
<small>IDE</small>.

We created a sixth project directory called the packager, which was
responsible for building the code for deployment. The packager created
<small>RPM</small> packages (our target was a RedHat Linux VMWare
instance). The project contained the production oriented
log4j.properties files, <small>RPM</small> spec files for the
post/pre-install steps, and so on.

On installation or update, the <small>RPM</small> packages:

* created non-shell users for each service,

* installed config files in /etc/,

* installed RedHat style start/stop scripts in /etc/init.d,

* deployed the binaries in /opt/apps/,

* created a data partition for storing published files in /data,

* configured Apache to redirect all port 80 traffic to port 443,

* configured Apache to use mod_jk for proxying to the submitter and publisher,

* managed and rotated the <small>SSL</small> certificate for Apache,

* set up HTTP Basic Authentication,

and so on and so forth. In other words, installing the <small>RPM</small>s turned a commodity, standard-ops RedHat Linux machine into a Serial Number Validation machine without any user intervention.

The slightly-modified RedHat installed by the operations group had `apt-get` installed and pointed to a corporate repository for Linux, and so all we had to do in terms of "manual" configuration was add a line to the `apt-get` config file to point to our own repository.

From then on, deploying code for the first time was a simple:

    apt-get install snv

with `snv` being a meta package which depended on the Apache config package, Apache itself, Java, our services, and so on. The dependencies were arranged such that everything was installed in the correct order.

To upgrade to new versions of the service:

    apt-get dist-upgrade

and that was all there was to it. This worked for test environments, <small>QA</small> environments, and so on.

Because of `apt-get`, we were assured that all dependencies we needed were downloaded and installed, even if we introduced new ones with new versions of the application. It was impossible to install our code if a dependency couldn't be met, and that's exactly what we wanted.

The Ops Staff, overworked, underpaid, and under constant threat of being "right-shored," were very happy about this situation. We developers were happy because our documentation for setting up and maintaining the service, wasn't more than a single page, most of which was letter-head, introductory remarks, contact information, and so on.

## connecting to the outside world

The serial number validation service was in no way a public service,
and was meant, at least initially, to serve only a single client. (We
accounted for the possibility of other clients inside the batch
submission format, the publication <small>URL</small> construction
formula, and other authentication schemes). As such, The Company
insisted on a two-way, <small>SSL</small> certificate authentication
scheme.

What we ended up with was something like the following:

![Front End](:site-url/pix/front-end.png)

The client used a certificate to communicate with a load balanced web
proxy farm running in the data center. The web proxy redirected
traffic over an <small>SSL</small> encrypted socket connection to an
Apache server running as part of our service.  The Apache server only
allowed connections via port 443, using <small>HTTPS</small>, and
redirected all other traffic to an error page. Also, the Apache server
was configured with basic <small>HTTP</small> authentication so as to
protect it from other services also running on the internal network,
of which it was a part.

This is a fairly traditional set up for web services, so I don't
really need to go in to it. The set up was also out of our hands as a
development team. The one thing to note is that we deployed the Apache
set up, including the locally generated certs it needed, as part of
our installation, so it needed no intervention by an Ops staff.

In fact, the Ops staff took a cue from us and began to deploy
certificates via <small>RPM</small>s on most of their other
machines. This made things very easy for them when it came time to
update them.

## monitoring, observing, etc

The one thing we needed to instrument for the first pass of the
application was whether or not the external web interfaces to the
application (Submitter, Publisher) were up or down. The idea was that
the load balancer between the web proxy farm and our application would
detect if the service was down and alert the appropriate support
group.

Rather than afix this concern to either of the web services, we
decided to apply the idea of _ruthlessly separating concerns_ by
creating another service, called the health monitor, which would
monitor all the other services, and publish a static Apache page
containing the status of the given services.

What this required was that each service implement a module which
subscribed to a **ping** topic, and published to a **pong** topic. A
message on the **ping** topic would produce an event that lead to a
message on the **pong** topic. That message contained the name of the
component, its location, and any other details we cared about. For the
first pass, all we sent was the name of the component, which was good
enough.

Here's an illustration of the anatomy of a given service running in
our application:

![Anatomy of a Service](:site-url/pix/service-anatomy.png)

(The above shows how easy it is to write event driven services which
are largely ignorant of the applications feeding them data, and are
also largely clear of complicated, data flow logic.)

The monitor service subscribed to all the **pong** topics, kept track
of the last time it saw a pong for a given service, and displayed an
error message on a web page if it had not seen a message in over a
minute. (In honor of the national security 'color alert' system going
on at the time, we added a colored square next to the name of the
component: with yellow, red, and green, for just how 'late' a pong
notification was.)

We never went any further than this, but it was pretty clear to us
that we could leverage that **ping** topic for all kinds of status
messages, and that we could use a similar set of topics for adjusting
service parameters on the fly. I worked on subsequent services where
we did this, but that story's for another day.

## testing

We created a [Python](http://python.org "Python Home Page") test
script, similar to Junit, but suitable for asynchronous testing. It
could spawn a process to send a serial number to the service, then
wait a bit, then poll for the result, test it, eventually timing out
if something went wrong. A Black Box tester. After I left the group,
another developer rewrote the whole thing in Java because he was more
comfortable with the language and with the threading tools
available. The fact that the testing module was separate from all the
others, that it was just another project within the source code vault,
made it easy to do just this sort of thing. No need to touch all the
other code: just create a new testing module, a better one, ditch the
old one, and there you go.

## conclusion

These three long semi-essays are really all the conclusion I need: I
wouldn't have written this up if I didn't think that designing a
service in just this way, using the underlying principles, anyway, was
just about always the right way to go.

For me, the big win was using topic-based, asynchronous messaging as
the way to do interprocess communication between the components of the
distributed application.

Using topics disassociated consumers from producers, simulating the
adaptability and conceptual simplicity of the `stdin`, `stdout`
filters making up most of the tools we all know and love on the
<small>UNIX</small> command line.

Using an asynchronous mode encourages event-based programming, which
tends to make each component much easier to write and far more fault
tolerant. Actually, I should amend that: asynchronous massing forces
you to deal with fault tolerance as a design issue rather than an
afterthought when you go about making your code production worthy. For
instance, if you ship data off to a topic, and don't know when you're
going to get the results back, the solution of persisting the
intermediate state to disk (say), and then re-loading it when a
message comes back, is both the solution to an asynchronous
request/response pairing, and services the needs of a fault-tolerant
system that might crash (or get re-installed) at any time.

Asynchronous modes are so usable, I think, that they should be the
default for how you design services rather than an exception. You
should only use synchronous calls when there's no way you can get
around it (such as we did for the submitter). And even then, you can
often simulate asynchronicity.

Finally, a big win all the way around is the use of packages (or
installers) native to the Operating System on which you're going to
deploy the distributed application. This encourages automation, gives
you dependency checking for free, reduces the amount of documentation
you have to write, and builds trust between the development and
operations sides of the house. (Any Ops person who's had to read a
five page "cookbook" for installing updates while in a cube being
interrupted by marketing and sales folks will very much appreciate
your diligence.)

I went on to use these techniques in a couple of later, much bigger
projects, and the developers I worked with, once they gave up thinking
I was crazy or self-serving, ended up really liking these
techniques. We were always done way ahead of schedule, never had to
work weekends (at least not because of our own code), and were
generally insufferable in our glee at being ahead of the game.

And what's not to like? You write very simple, single-purpose
applications, and, somehow, as a side effect, you end up with a rich
and complex distributed system.

Complexity is, after all, an emergent property of systems of simple
components. Make those components OS processes, and you've got a
distributed application that works and is easy to evolve. Ideas worth
embracing.

[part1]: :site-url/articles/2009/07/11/case-study-asynchronous-rest-1
[part2]: :site-url/articles/2009/07/15/case-study-asynchronous-rest-2
[part3]: :site-url/articles/2009/07/23/case-study-asynchronous-rest-3
