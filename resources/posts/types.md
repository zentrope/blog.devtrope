{:type :post
 :when "2014-07-04"
 :slug "types-and-dynamic-languages"
 :title "Types and dynamic languages"
 :tags "software"
 :publish? true}

I wonder if the "types" in dynamic languages are much worse when the
languages are object oriented.

I use a dynamic language very day, Clojure, and just don't experience
problems a static type system would significantly improve.

Maybe that's because in Clojure, (not OO, basically functional), I
end up working with the same types over and over:

 - lists
 - maps
 - strings
 - numbers

Even things like database connections are usually just elements of a
map you pass back and forth to the functions that operate on database
connections.

I don't have to cover types in my unit tests, because I just test the
actual function and when it works, it works and I tend to have a "fail
fast and recover" design mentality.

Of course, the key to any sort of development happiness is to avoid
large code bases. That's the key to everything and as close to a
silver bullet I can imagine.

<div>&nbsp;</div>

A language like Python allows simple functions outside of classes, but
I think the language gets more complicated when you start to leverage
OO. Instead of a few simple types (maps, lists, tuples, numbers,
strings), you start to grow your types (via classes) exponentially. If
you use inheritance, you're quickly into desperate circumstances.

When one class has methods that take parameters typed to another
class, you quickly lose sight of what's what, especially if your
domain isn't a model of "real world" objects.

I've seen a large Java project that used Jython in hopes of allowing
extensions to the app without having to re-deploy the app. Not a bad
idea, but the actual Jython code "inherits" from Java objects and
attempts to "re-use" code via inheritance within the Jython classes
themselves. In other words, the original developers wrote Python the
way they wrote Java and they wrote Java using inheritance (which is
only slightly less problematic in statically typed Java as it is in
Python).

<div>&nbsp;</div>

I don't write Python anymore, but when I did, I kept things pretty
simple because using "inheritance" as a way to share code never really
caught on with me. (I'd read a few articles about the evils of
inheritance and believed them.) I don't work on the kinds of problems
where a deep hierarchy seems like a reasonable choice and I'm a
believer in letting the problem shape the tools rather than the
reverse. Psychologically, my tendency is to deal with complexity by
cutting through it in a Gordian Knot kind of way.

So, here's what I'm saying:

 - If you're inclined to use Haskell to solve the problems you've had
   with dynamic languages, please do that.

 - If you're interested in sticking with a dynamic language, use a
   functional paradigm, and stick with four or five basic types.

I use Clojure all the time. I don't have type error problems for more
than a few minutes at a time. The language has taught me to do the
things that tend to avoid them, such as sticking to the core data
types as mentioned above, and having shallow function hierarchies.

My choice of Clojure over Haskell (currently, for personal projects or
work side projects) has nothing to do with my estimation of the
technical sophistication (or practicality) of Haskell, it's more about
the fact that I just don't have need for elaborate types most of the
time and, to a certain extent, I want to work with tools with the
fewest number of intrusive abstractions. I bet I could get to the
point where Haskell seems "simple" to me. Maybe that's the problem:
using a dynamically typed OO language puts you in the same boat: a
too-tempting facility for adding more and more abstractions that are
more about the language and paradigm you're using than the problem to
be solved. If you can reduce the number of abstractions you introduce
(types via class definitions), things are simpler, right?
