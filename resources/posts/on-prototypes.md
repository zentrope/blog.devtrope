{:type :post
 :when "2013-03-15 12:45"
 :slug "prototype-the-production-of-software"
 :title "On prototyping the production of software"
 :tags "software"
 :publish? true}

When you want to put something in front of a customer or stake holder,
you make a mock-up or a functioning prototype. The prototype teaches
you, the engineer, about a lot of things you'll need to consider, both
hidden, in terms of the engineering challenges and visible, in terms
of the customers' expectations and the users' experience of the "flow"
of the application.

Fine, right?

No duh, right?

Right.

The rightness is that you're working on something the world has never
seen before and you want it in front of the customer as early and as
cheaply as possible. Prototyping is the process of discovery, of
charting a route from where you are to where you want to be. At its
best, prototyping leads you to a new and better end-point you wouldn't
have been able to think of without taking those first few steps. You
want your customer to end up there on the West Coast where you both
knew you were heading, rather than in South Florida, swatting
mosquitos in lieu of each other, wondering how your best intentions
lead you so far astray.

What I want to suggest, though, is that prototyping shouldn't stop
with what it's possible to put in front of the customer. Any user
experience is the just the tip of the iceberg[^ice], after all,
especially in the world of network based services and applications.

[^ice]: Sorry for the cliché, but it's so apt. Examples abound:
Twitter, Facebook, Google Search, Amazon, Apple's iTunes and App
Stores, but even small e-commerce sites for mom-n-pop businesses.

Before any new software project, we ought to prototype every part of
the process that's going to go into building and maintaining the
project over the long haul. Projects aren't just about the customer,
they're about the team that needs to fix problems, support the
customer, build enhancements and new features, bring new employees up
to speed, inform the business of the impact of those changes and so on
and so forth.

Sure, the initial customer experience is great, but customers are
fickle, competitors don't stand still, and the first version is never
the complete version, often by design. Change is inevitable and if you
don't anticipate and embrace it, you'll find yourself in an even worse
situation: unable to change.

So, why not prototype the whole production experience as well as the
user experience? Why not consider the engine getting you where you
want to go as worthy of as much attention as the destination itself?
You can't have one without the other.

Let's take the idea of a spin across a few of the hidden concerns
making up the production of software.


  * **version control**<br/>
    Why not prototype a workflow using different kinds of version
    control systems? How are you going to manage branches and
    releases? How well does your choice integrate with build servers
    (if you want them) or shell scripts or projects that want to use
    the code as code (such as package deployers or automated
    integration testers)? What about developer tools? You could use
    SVN, for instance, which keeps a centralized server model, or you
    could use BZR which tracks branches as separate directories on a
    file system, or GIT, which is very will supported, but has a
    higher initial learning curve. What are the long term
    implications? How easy is it to export change history from one
    system to another? Do you want a distributed work flow with
    patches flowing to a designated "owner" of a module, or do you
    want a central source-of-truth everyone pushes to and pulls from?
    Do these tools work well on the kinds of platforms the developers
    are likely to want to use for hacking? It's always easy to go with
    the dominant choice of the day (CVS back when, GIT or SVN now), or
    with whatever the corporate mandate is. With a prototyped
    workflow, you'll be informed enough to make a compelling case if
    you feel you need to.


  * **source code**<br/>
    Personally, I think it's worth prototyping different ways of
    organizing source code, especially if your project is going to be
    large enough to require a sizable team. Do you want to use a
    library model in which sub-components reside in separate code
    repositories to be brought together via a dependency management
    system? Maybe a distributed architecture is the way to go such
    that each "concern" (or service) of the application is at the end
    of a network pipe. In that kind of system, you can get away with
    no shared code at all. (If you're tempted to share code, it means
    you're sharing a concern, which means, well, refactor until the
    need goes away.) Or might it make sense to bundle all the code
    into one big repository organized by subdirectory? Easier to
    change multiple concerns if (say) the data format shared between
    them changes, but a lot of files are a lot of files, no matter how
    you organize them.[^ls] Try these things out. Distributed source
    code (one project, one service) might be frustrating at the
    beginning, but it might pay off big time as the project matures
    and more and more people get involved. A new person can master a
    small code base much more quickly and confidently (and thus feel
    good about being on the team) than mastering a small subset of a
    huge set of files, no matter how well organized. Doubt it? Try out
    a prototype. Prototype something that's the exact opposite of what
    you think best and see if your misgivings are confirmed, or if
    (you'll hate to admit), the paradigm, tried in anger, is
    compelling. Think of other developers as your customers. What's
    going to be easiest for them? Small, single purpose code bases, or
    a large, organized, comprehensive code base? Which would you
    prefer on the crankiest day of your life?

[^ls]: Library science and taxonomy standards exist for a reason, I
imagine. This stuff is hard. The part of you that files things into
categories doesn't seem to be the same part of you that looks for
things. Once you get past a certain point, all you can really do is
search à la Google.


  * **build system**<br/> Are you going to use a simple build system
    that only knows how to actually build the application, or do you
    want one that can package it for deployment, run integration
    tests, generate merge, test, contributor and dependency reports?
    What are the strengths and weaknesses of the approach? Do you want
    the build system to be dependent on multiple source code repos? Or
    do you want component "integration" at some other stage, such as
    deployment? Maybe try using a simple build, then using other
    prototypes to see how easy (or problematic) it is to leverage that
    system from afar.

  * **deployment prototype**<br/> Given a "hello world" of some sort,
    prototype a way to deploy it. Is it an application running in an
    app server? Does it run as a process hosted on a Unix OS? Windows?
    Both? Maybe your prototype knows how to pull down the source code,
    invoke the simplified build script, then generate an installable
    package for your target OS. If you deploy to multiple platforms,
    do you make a single project that can generate all the installers
    or do you have separate, small platform-specific deployment
    projects? Do you want to use this system as a precondition of your
    testing apparatus? Is it easier for developers to come up to speed
    on a monolithic "create an installer" project, or a sequence of
    platform-based projects?

  * **proxy server vs app-server prototype**<br/> Suppose you're
    working on a system made up of separate but related components
    making up a web-based service of some sort. One component might be
    the UI, another a directory-server, yet another a background
    data-import service. One way to put these together is to deploy
    them to a single application server. Another way is to stand each
    service up as an OS process and use a proxy server to route
    requests. In effect, you're turning a normal OS into a kind of app
    server. You've decided you like this approach because it enables
    you to spread the load over multiple host machines if you find you
    need to scale in certain directions. No one believes you? Push
    back from folks? Write a prototype! Stand up a few hello-world
    apps and put them behind a proxy server and show how they appear
    to be different aspects of the same app. What are the UI
    implications? Make a single installer that can deploy the proxy
    server configuration and the individual services all via a one
    button click or shell command. Prototype the use of OS tools to
    track the performance of the various services and the stats on the
    proxy server itself. Make one of the services blow up and tank due
    to an out-of-memory condition and show how the rest of the
    services keep going. Show how you can replace one of the
    components and introduce a new one without having to change the
    existing components. Add a whole new host and integrate it via a
    simple proxy-server update (automated, of course). Prototype this
    with an emphasis on ease of maintenance. You might decide you
    don't like it, but you'll make that decision based on actual
    experience. Try and take the long view. What options does this
    give in the far future? What options are precluded by NOT doing
    it? It only took you a couple of days to get the whole thing
    working. Think of the other developers on your team. Is this kind
    of thing too complicated for them? A nice separation of concerns?
    Let's them work on services without worrying about their impact on
    peers or various "security" concerns at the proxy layer? Easy to
    fold into an "integration" stack?

  * **black box testing prototype**<br/> What might it be like for a
    QA team to test your application? Can you get a project going
    independent of your hello-world prototype's source code, yet can
    test it when it's hooked up to the real resources its going to
    need? Better to find out now. You can get away with manual testing
    for quite a while but why not prototype an automated testing
    system and see what it would look like before you get too far down
    the implementation road? Unit tests are one thing, but external,
    black-box testing is what you need for multi-process systems. If
    you have a database, you have more than one process. At the very
    least, you'll keep that black-box testing prototype in mind when
    designing the real product and that can't be anything but good.

  * **system updates / change over time / operations**<br/> You've
    prototyped your code management organization, your deployment
    system, your black-box testing system. The real question is how
    this helps you introduce change over time. How do you deploy new
    code over the top of old code? How do you migrate data formats
    from one version to the next? If you want to update one thing do
    you have to update everything? How hard is it to introduce a
    spelling fix? Can an operations team understand the risks of every
    move you make? If you move from MySQL to MongoDB, how much of an
    impact is that on your entire stack? How hard is that to test? How
    hard is it to back out if it all goes wrong? If you prototype the
    full "production line" (so to speak)[^disc] you can answer a lot
    of these questions while you still have time to make
    adjustments. Technical debt consists of the problems you require a
    "business decision" about "priorities" to fix rather than problems
    you can just get done in the course of producing new features and
    enhancements. If you build a system that allows change over time,
    that minimizes the affects of any change on the whole system (from
    source code all the way up to customer experience), then you'll
    have produced an engine that'll enable an even better customer
    experience than you imagined a that start because you'll be able
    to act and act quickly. Sure, there will be surprises, but you
    want those surprises to be _true_ surprises, not "we'll figure
    that out later" kinds of surprises.

[^disc]: I don't ever want to suggest that software engineering is
anything like manufacturing. I just don't believe it and a lot of the
frustration I and others have experienced is due to the imposition of
such methods on what's more akin to movie making than gadget
manufacturing.

These prototypes don't have to be perfect implementations, they just
have to teach you enough to know if a given approach is going to lead
to road-blocks or unintended complexity in the future. For instance,
if you know that a simple build system that _only_ builds (doesn't
make deployable packages, or do integration testing, etc) is going to
work for you, you can always use a different (but equally simple)
build system in the future, or fix up your sloppy build file, or even
be in place to use an entirely different technology stack. But the
"idea" of the build system (that it's simple and that other
traditional build-time tasks are handled elsewhere) is what you've
really worked out. The internal details can ebb and flow.

I'm really talking about interfaces here, I think, from the source
code on up. All these prototypes lead to well-defined interfaces. And
interfaces define the system, not their implementations.

Dedicating the idea of prototyping for the production of software as
well as the software itself is really just prototyping the customers
experience for a whole other set of customers: the engineers who are
going to have to work on the project for (hopefully) years to come,
including yourself. Sure, the bottom line is always going to be the
customer experience and the actual product. The application needs to
do what you say it's going to do and what the stake holder needs for
it to do. Who can argue with that? But you want to be able to grow and
change the product and that's going to be a lot easier to do when your
tools and processes are as thoughtfully designed as the end result.
