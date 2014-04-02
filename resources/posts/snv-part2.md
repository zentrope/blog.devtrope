{:type :post
 :date "2009-07-15 13:01"
 :when "case-study-asynchronous-rest-2"
 :title "On the solution to the problem (2 of 3)"
 :tags "software"
 :publish? true}

## overview

This is part 2 of a 3 part series about an asynchronous web service I
worked on a few years ago which lead to a lot of the ideas I now hold
about how to design distributed systems. In [part 1][part1], I
talked about the problem we had to solve, which was:

  * create a web service to validate serial numbers, and

  * figure out how to negotiate numerous internal resources, not all
    of which are available all of the time, and most of which were
    expected to change over time.

We ended up deciding to solve the problem by creating an asynchronous
web service with the following interaction (from the client's point of
view):

* A client submits a job containing one or more serial numbers for
  validation.

* At a later point in time, the client retrieves the results by using
  the individual serial numbers to compute a <small>URL</small>.

In other words, the client polls for the results and may resubmit
numbers if it feels that the result has not appeared in a reasonable
amount of time.

## areas of concern

The first thing we did in figuring out how to build the application
was to figure out what problems we had to solve, or what areas of
concern we had as far as solving the problem. Here's what we came up
with:

* Accepting serial numbers from external clients for validation.

* Publishing validation results.

* Querying the Web Service resource for serial number data.

* Querying the Oracle Database resource for serial number data.

* Given all the results, refining an appropriate answer.

What we ended up with was a rough pre-design as follows:

![Areas of Concern](:site-url/pix/areas-of-concern.png "Areas of Concern")

As you can see, these areas of concern line up pretty obviously along
the lines of what external resources they interact with, or clients
they serve.

The dashed lines represent the division between the solution space in
which we implement pieces, and the partners with whom we need to
integrate.

The submitter and publisher line up with the client: they're client
interfaces and most of their concern is made up of how best to
interface with a remote client, the Rebate Processing company.

The Oracle and Web Service Query concerns line up with the services
they consult. Most of their concern is with how to contact,
authenticate, query for and process the results of the data.

It doesn't take much of a leap of the imagination to see the above
five areas of concerns as five separate services within a distributed
application. You could also see them as five separate modules in a
single monolithic application. (Or even, say, five different Web
Applications in a Web Container.)

## why separate services?

Based on my own experience writing monolithic apps in the presence of
ever changing requirements, integration touch points, and
implementation technologies was quite painful in that the nature of
what it takes to manage separate concerns in the same code base slowed
me down considerably.

Therefore, I advocated strongly for maintaining separate applications
for each area of concern.

It's not that monolithic applications are all that bad (oh, all right,
they are), it's just that by merging all five concerns into a single
application, one ends up introducing all kinds of additional
abstractions in order to manage substantially different tasks.

For instance, just about everyone is tempted to treat the results of
the Web Service in a way similar to the results of the Oracle SQL
result set in some data abstraction layer that's really, really cool
to implement, but becomes the absolutely wrong thing to do when you
need to adjust to a new requirement. And don't get me started on
elaborate XML meta/domain-specific languages meant to bind disparate
concerns together into a single binary in hopes of creating something
easy to refactor.

Finally, if the implementation of one of your areas of concern is a
bit wonky, or uses memory-leaking libraries, it'll take down the whole
app and you'll never figure out why. Is it due to the implementation
of one of the concerns, or due to the impact of one concern's
implementation against another concerns when they're running in the
same address space?

By splitting the app into five separate services, you can at least
rule out the other four areas if something goes wrong.

## pass 1: a vague architecture

Okay, so we decided to write a bunch of stand-alone services rather
than a single application.

Here's what we ended up with:

![Vague Architecture](:site-url/pix/vague-architecture.png)

Each area of concern became a new service in the distributed
architecture. Each service can be optimized and designed according to
its specific concern. For instance, the Submitter Service is good at
accepting client connections and validating data without having any
part if its code base have to deal with Oracle <small>JDBC</small>
drivers. It can implement caching schemes, thread pools, and so on,
depending on how the service needs to work under load.

The biggest win for this kind of separation is how much easier it
makes any given developer's life. If the author of the Submit Service
wrote a lot of ad hoc, not-well-planned, first-draft code, well, no
big deal for any subsequent maintainer. Because the code only does one
thing, even the worst code ends up being easier to figure out.

The question the above illustration brought up next was how these
applications were going to communicate with each other. Back then,
<small>SOAP</small> was on the way out. Even when you own all sides of
a given distributed application, <small>SOAP</small> proves to be just
too much book-keeping all the way around.

That left "simple" sockets and a custom protocol, or
<small>HTTP</small>, or something asynchronous, like a
<small>JMS</small> provider, which is what we went with.

## pass 2: asynchronous messaging

The things we liked about the <small>JMS</small> /
asynchronous-messaging approach were:

* **emphasis on interfaces**: In a message-based system, _the messages
    are the architecture_. As long as the messages are
    self-describing, complete, autonomous data _qua_ data, nouns
    instead of verbs, the application becomes easy to document and
    easy to understand for the people who have to maintain it. In
    other words, given a certain message going in, and another message
    coming out, you can pretty much deduce what the service does
    without any documentation at all. This is a good thing. Burying
    interface decisions in shared-libraries (say) of your own
    composing, or available via app stacks, such as
    <small>J2EE</small> or .Net, often hide how things are done and
    thus make debugging and integration difficult.

* **decoupled concerns**: With asynchronous messaging (using topics
    rather than queues), your interfaces are decoupled even from the
    other services making up your application. A given service
    publishes data to a topic and doesn't need to be concerned if, or
    where, a given consumer of those messages resides, or what its
    purpose is. (With <small>HTTP</small>, you have to know the URL to
    post to, and that <small>URL</small> has to be up. If it's not,
    you have to manage fault tolerance yourself.) The service consumes
    messages in the same way. It's as close as you can get to a
    `standard-in`, `standard-out` kind of <small>UNIX</small>
    command-line filter.

* **event driven**: With such a system, individual services can be
    event driven. A message comes in, it gets processed, and then it
    gets posted to another topic. Very clean, especially given that
    messaging infrastructure, in our case, ActiveMQ, provides all
    fault-tolerant communication for you. Writing the individual
    services in such a distributed application becomes similar to the
    callback methodologies in <small>GUI</small> programming (though
    I'd not want to press on that analogy too much).

* **easy to evolve**: If all interprocess communication (except leaf
    nodes, e.g., the interfaces to the outside world) use topics (the
    <small>JMS</small> version of the blackboard metaphor), you can
    hook additional clients to those topics to expand functionality
    without having to change any of the existing components. This
    comes in handy for monitoring and metrics, especially during
    development. Given that we weren't sure what additional internal
    resources we might need to consult as the project matured, being
    easy to evolve with as little code change as possible was a very
    good thing for us.

* **hot upgrades**: If you're okay with the external interfaces going
    down for brief moments, you can re-install all the components
    making up a message based system without taking special care to be
    "down for maintenance." As one service shuts down, the message
    broker keeps the messages in its local store. When the message
    broker itself goes down, each producer client blocks until it
    comes up again.

Another aspect of the technical choice we made was that a
message-based system was new to most of us, and its good to gain a
much broader perspective on the types of architecture one can use to
solve problems. The appeal of trying something new rather than
suffering from the same old problems is not something to be shrugged
off. We're all human and software is an art and a craft. Sure, it
makes use of some engineering principals, some science, some
mathematics, and even rules of thumb, but so does any fine art. We
wanted to recognize the need to explore alternatives and embrace
rather than deny it under the rubric of traditional "best practices".

If the above seems kind of sketchy for justification, chalk it up
partly to my faulty memory, and also to the fact that an architecture
that embraces the asynchronous style everywhere it can is best
justified by how easy it is to maintain, how little code you have to
write to support it, and how simple it is to understand and
trouble-shoot. These are experiential justifications which are hard to
justify via diagrams or the simple three-tier design that so many
managers and stake holders and operational staffs are familiar with.

## pass 3: how it turned out

Given the above diagram as a starting point, and the notion that we
wanted to use asynchronous, topic-based messaging as our data
pipeline, all we had to do was place a topic between each stand-alone
service along all the internal interfaces:

![Topics](:site-url/pix/async-architecture.png)

The above illustrates the one big drawback to messaging systems:
they're hard to draw in such a way that a managers or architects don't
rub their eyes and mutter, "too complicated," or, as I translate it,
"too many notes."

The most complicated part of all of the above is the Refiner Service
which has a lot of inputs and outputs.

Here's a narrative of how the Refiner worked, which should give you a
flavor of how easy it is to think about something rather complicated:

* The Refiner receives a message from the **job-submit** topic,
  unpacks it, crafts up a serial-number message, and posts it to the
  **web-service-query** topic, and then it's ready for the next
  message.

* The Refiner receives a message from the **query-service-result**
  topic, unpacks the message, and examines it. If the serial number is
  validated, it posts the message to the **job-complete** topic. If it
  is NOT validated, it posts the message to the **database-query**
  topic. And then it's done (or, rather, is ready to process the next
  message).

* The refiner receives a message from the **database-query-result**
  topic, examines all the results available, figures out how to
  describe the result (good, shaky, invalid), appends that data to the
  message, and writes it to the **job-complete** topic, and we're
  done.

With not much imagination, you can see how each of these flows can be
organized as a "plugin" floating in the Refiner Service, with an
outbox publisher and an inbox subscriber object with appropriate
callbacks. Need to query additional resources? Just add more plugins
and adjust the existing inboxes or outboxes as necessary. (And
remember, changing topic names in your code is a lot easier than
changing <small>XML</small> bindings in three config files.)

Don't like how things are going? You might choose to rewrite the
Refiner Service, or split it into three services. Regardless, the
Submitter, Publisher, and both Resource Query services all remain
untouched.

That is, unless you change the message format. But, again, changing
the message format is changing the architecture, and even that's
pretty easy (if you use, say, <small>XML</small> and XPath, or even
<small>JSON</small>, in which case adding new elements does not
require immediate changes if the consuming services don't need the
extra data).

The upshot of all this is not that there won't be change over time, or
that a particular change might not have to be done in multiple places,
but that _it's always clear what the impact of any change will be_,
and, because each service in the application is small and
single-focussed, _it's easy to assess the impact of the change on any
given subsystem_.

I cannot over-emphasize how important this kind of architecture turned
out to be for managing change over time with only one or two
developers and an extremely over-worked operations staff.

## operational details

In the [next part][part3] of this long, long essay, I'd like to
discuss some of the operational details that the messaging backbone
afforded us, and how we deployed and maintained the application as a
series of services running on a linux VMWare instance, and, finally,
what happened to the service when the maintainers were forced to move
it to a <small>J2EE</small> WebLogic cluster solution.

[part1]: :site-url/articles/2009/07/11/case-study-asynchronous-rest-1
[part2]: :site-url/articles/2009/07/15/case-study-asynchronous-rest-2
[part3]: :site-url/articles/2009/07/23/case-study-asynchronous-rest-3
