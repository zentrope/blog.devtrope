{:type :post
 :post-date "2009-09-20"
 :post-time "13:01"
 :post-slug "dev-without-requirements"
 :post-title "On development without clear requirements"
 :post-tags "software"
 :post-publish? true}

## a bit of history...

Sometime in early 2004 an awful project I was on got cancelled. It was
awful for lots of reasons, none of them especially technical. I was
almost the only developer on the project. Normally, this is great for
me. I get to write a lot of code and see the whole thing out the door
from top to bottom. But not in this case. One of the main issues I had
to contend with were constantly shifting requirements, due, in large
part, to the fact that the project involved many stakeholders, each
one in a bid to see their particular vision as the right one.

To get the job done (a web application), I used a company customized
version of Struts to develop an application running on a cluster of
WebLogic servers. Struts is not the most agile of technologies,
obviously enough, but it's not the worst, either. In fact, I got quite
good at getting things done with it.

Nevertheless, the constant stream of requirements changes, some of
them sprung on me after I'd finished the application, some of them due
to my own misinterpretation of what was said in meetings meant that I
could not keep up with the code. We brought on another developer, who
didn't help much because he was completely unfamiliar with Struts, web
applications in general, and all the discussions that had happened
before.

(I won't even bring up the nightmare that was integration via
<small>SOAP</small> when you don't control both sides of the
interface. Shudder.)

After the project was cancelled, I had a few months in which I didn't
work on any project at all. In that time, I studied Lisp, Scheme,
Python and asynchronous messaging systems and key/value store
databases.  When I finally got another project to work on, it was a
video conferencing project.

I and another developer used Python. I wrote all the front end bits
(controlling scheduling and the user interface), and he wrote the
backend bits (controlling hardware).

At first we tried to bind our code together, his as a library to my
application, but there were just too many issues, mostly involving the
whacky Python threading model (at that time), so we used a message
bus, inspired by IRC, separate processes communicating to a central
broker.

Our collaboration using this technology was extremely successful: we
implemented a massive amount of functionality in parallel in a very
short time using a surprisingly small amount of code. (It took the
subsequent team about a year to rebuild the functionality we had. They
ported the code to Java, using a distributed object model, rather than
messaging. And a much bigger team. They even had a full time build
guy!)

Comparing the success of the new project, and the failure of the old
one, I came up with a few notes (around late 2004) I'd like to share
now. I recently found these notes, and what I noticed about them is
that I've been trying to get these ideas allowed into my daily
practice ever since. I'm pleased to say that when I did manage to get
my team members to use these sorts of things, the projects were on
time, required fewer people, and were generally easy to change and
always adjusted well to constantly changing requirements.

Anyway, here are the notes which were a sketch of a presentation I
gave at a Developer Days conference (sponsored by our group at that
company):

(_Italics_ denote my current commentary on my ancient notes.)

## frustrations

_As we all know, requirements help us figure out how to get done what
we need to get done and when. Here's a list of frustrations you're
likely to encounter even if things are going wonderfully well:_

  * "That's not what we meant."

  * "That's cool, but now, let's change it."

  * "We appreciate the work you've put in, but we've been discussing
    this among ourselves. Let me tell you what we've concluded while
    you were working on that."

  * Cross-team stake-holders:

     * Partner 1: Basically, it all comes down to A.

     * Partner 2: Basically, it all comes down to B.

     * A completely contradicts B.

     * (Basically, all vision is Parallax vision.)

  * "Well, in a few months, we're going to hire some developers."

  * "Let's use our differing assumptions about what the customers want
    to justify our disagreements, given that we can't talk to the
    actual customers."

  * "That solution seems simple enough, but we want to use buzzword X
    with buzzword Y because:

     * We've heard everyone's going that way,

     * We've heard it's Best Practices,

     * The company is standardizing on that technology,

     * Your choice is not company approved."

_I think I've heard each of those at status meetings and demos when I
thought the work was essentially done._

## problems

_As a developer, you need to make engineering choices that help you
deal with the following problems:_

  * No requirements, or not enough to justify one technology choice
    _or design_ over another.

  * Frequent "sea" changes, requiring a rethink of the basic software
    architecture (or ought to), or at least changes from the database
    on up through to the UI.

  * One day your "model" (code organization, problem breakdown) is
    what you need, the next day it seems like it was a bad choice:
    recipe for spaghetti because it's too hard to start over.

## solutions

_The following was my assessment, in 2004, about what features a
solution to the above problem should have in order to have a chance at
success:_

  * Make it as easy to change code as it is to change a paragraph in a
    requirements doc, a slide, or a diagramming tool.

  * Discover requirements and be able to adjust to them.

  * Discover constraints, and be able to live within them.

_Note: Most folks think that you need to have all the requirements
down and locked in stone at least for an initial implementation. I've
never experienced this, and given that projects live of die based on
how fast you can bootstrap them, I've come to believe that one gathers
requirements via implementing ideas. The goal, then, is to figure out
how to embrace that and stop doing the things that make it difficult._

_If the basic precondition of much software methodology and technology
choices is a good set of requirements, then we need a different set of
methodologies and technologies when that precondition cannot be met._

## technology goals

_What we need are tools, philosophies, and techniques encouraging:_

  * Ability to get stuff done quickly based on little information,
    guesses, proposals, etc.

  * Guessing wrong should be cheap.

  * Ability to change practically everything without too much cost.

  * Writing executable requirements.

## keeping it simple

  * Python, Ruby, Lisp, Scheme, Groovy, PHP, Erlang

  * Avoid complex build systems (_i.e., ant &gt; maven, or projects
    depending on other projects in an elaborate tree of
    dependencies_).

  * Avoid complex data / business models (_data pipeline and
    transformation over elaborate relational state, if possible_).

  * Ability to change things while the app is running.

  * No need to re-compile, re-deploy.

  * Extreme-programming techniques, as much as makes sense, but unit
    tests if nothing else (esp. with late-bound, dynamic languages).

  * Super decoupled application architecture: talking separate
    processes for layers, not just object interfaces. Write code to
    network interface specs, not giant libraries which hide the
    details. (_Think HTTP/Rest vs SOAP._)

  * Prefer asynchronous to synchronous everywhere possible.

  * In-memory DB, then flat-file object persistence, then RDBMS. (_An
    RDBMS should always be your last choice, not your first. Do you
    really, really need one for your app?_).

  * Prefer computed HTML over templates.

  * If using PHP, embed _everything_ in each page: no fancy MVC
    framework: too hard to debug, too hard to maintain, and really
    difficult for future maintainers to unravel.

_I'd add that AJAX or Javascript clients are preferred over computed
HTML or template languages of any sort. Keeps the backend very clean,
and it's very easy to change Javascript quickly as requirements shift
over time._

## question frameworks

  * Fear frameworks: don't trade the possible complexity of the
    problem for the definite complexity of the framework.

  * Prefer straight SQL to object/relational mapping frameworks.

  * Recognize that frameworks often exist to overcome the shortcomings
    of less dynamic languages.

## documentation

  * Generated from code if possible.

  * The code, and what it does, _is_ the documentation.

  * We can help best by documenting the problems we're solving rather
    than the implementation. Rather than an SQL E-R diagram (for
    instance), a description of the type of information and the basic
    domain entities is more import.

  * Always document for future re-implementors and re-writers, not for
    future maintainers.

## and so it goes...

A lot of what I wrote up back in 2004 still seems controversial among
a lot of people I work with. I find, though, that the controversy
usually breaks down to a single point: requirements.

When you work in an environment where you start with vague ideas and
write code in order to solidify those ideas, to discover the
requirements as you evolve a system, the above makes a lot of sense
(or would, if I fleshed them out a bit more). This kind of environment
is much more on the artistic, intuitive side of software development,
the side that acknowledges that every new project is a "first time"
situation. If the solution already existed, we'd just buy it, so we
might as well embrace the uncertainty and develop techniques to
minimize bad choices.

When you work in an environment where the outcome of a given project
is absolutely clear, then I think most of the above is not
necessary. It's easy enough to go with the waterfall method, or at
least to start there, by gathering all the requirements, making sure
they're written down, and then using those requirements to schedule
and scope. In such a case, you can use any technology you want because
you can know up front if you're going down the wrong path simply by
looking at your requirements document.

A comfortable world, if you can get it, and one that, I'm convinced,
no longer exists. In fact, I bet it never existed.
