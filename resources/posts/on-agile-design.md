{:type :post
 :post-date "2013-11-09"
 :post-time "3:00"
 :post-slug "design-and-the-agile-process"
 :post-title "On design and the agile process"
 :post-tags "software"
 :post-publish? true}

I think I've finally figured out what bugs me about Agile software
development, or whatever it's called when it's institutionalized at a
company: the fact that it says nothing about the actual design of
software systems, and that because of that, companies ignore design,
and because of _that_, teams get better and better at adding less and
less value and companies get better and better at expecting less and
less of their development teams.

Agile is a tactical approach to managing the day-to-day work of a
development team: breaking down a project into user/customer centric
tasks (which deliver customer value, let's say), figuring out how much
those tasks cost, prioritizing, perhaps working out dependencies, and
then making a commitment for a given iteration. It's also about
managing expectations about what a team can actually accomplish.

Software development is iterative, but businesses are waterfall in the
sense that they conceive, plan, develop and then deploy a product in a
nice straight line, then do it all over again depending on how the
market responds. (A series of waterfalls, which is kind of iterative,
isn't it? I tend to think "waterfall" is a myth at worst, a metaphor
at best for something that doesn't exist, but is useful for a
fear-and-doubt kind of argument.)

Anyway, an Agile process allows a given team to quantify just how
difficult or time consuming a software project is, and to track in
objective-seeming metrics how scope creep affects progress, and so
on. Stuff like that. I'm not so interested in the details of anyone's
specific implementation of the Agile way.

What's _not_ part of Agile is design.

The design of software systems.

I'm not talking about colors, whitespace, borders, gradients, flat or
skeuomorphic or even the user experience.

I'm talking about how a complex software system actually works: its
implementation. Is it a monolithic app with many separately built
components? Is it a message-based distributed system? Does it use a
central data store, or small, domain-specific, distributed data
caches? Can it adapt to change without any one person understanding
the whole? Does it account for what you don't know you don't know
about the problem to be solved? Can lots of developers work in
parallel, or is any given feature tied down by a huge dependency
thread?

Agile doesn't speak to this part of the problem, and I don't think
anyone who understands Agile thinks it does.

How does having a daily stand up prevent you from building technical
debt?

How does a sprint review facilitate a design that mere mortals can
comprehend?

How does sprint planning make a project more maintainable?

Absurd questions, you might think because Agile is about
customer-focussed stories, not about implementation details.

But if Agile says nothing about implementation details, what does an
agile team do about, well, the implementation details they have to
have in order to realize the stories?

In my experience, there are two problems to be solved for a software
system: the implementation design and architecture, focussed the
ultimate business goals and on enabling humans to continue to work on
it (i.e., long term strategy), and team management for working on that
design within a larger organization (short term tactics).

The problem I have is that people _think_ Agile addresses the first of
these problems, the architecture, when it does nothing of the
sort. Or, perhaps more accurately, folks think that Agile addresses
all their software problems, so they "bottom line" Agile as their
solution and stop there.

Thus, sane implementation falls on the floor. Any long term view is
trumped by short-term, sprint-planned tactics.

Because of this, the short-term thinking of the team management part
(daily stand-ups, a backlog of stories with no technical aspects,
tasks, each one of which is treated like an item on a grocery list
that doesn't require the buyer to know what's being cooked, etc, etc),
allows a team to accomplish every task set out for and agreed to by
them and end up with a giant, unmaintainable mess. Such a team will
end up going slower and slower.

When I've participated in such teams, I've been frustrated. I now
understand that personal frustration: without a long-term architecture
and design, all the process seems pointless, and pointless process
then seems to get in the way of coming up with an appropriate design
which provides for the ability to accomplish software, which is the
reason for process. Tactics without strategic understanding is really
hard, at least for me. Such a milieu tells me over and over again to
be and think like a contractor. Like a grunt. To have no real stake in
the project at all. To do things that are not only counter to any
recognizable strategic goals, but prevent future strategic
opportunities. _That's_ what I find frustrating: a process that
prevents me from doing good work.

So, the task for the Agile folks out there is to figure out a way to
raise implementation strategies to the same level as user
stories. Don't mix the two. "Technical" stories aren't
strategy. Instead, find a parallel mode for such things and emphasize
the hell out of it. Please. For all our sakes.
