{:type :post
 :when "2012-09-02 13:01"
 :slug "emacs-and-extensions"
 :title "On committing to Emacs"
 :tags "emacs"
 :publish? true}

I like to use lots of programming editors and never end up sticking
with any particular one for long. All of them are interesting in one
way or another and my curiosity gets the better of me.

Thing is, people who _do_ stick with a given editor get really good at
them, doing all kinds of automated things that I do by hand, playing
their editors like accomplished musicians. Take [Emacs Rocks][er] for
example. Geek performance art.

It's time for me to stick with something.

[Emacs][em].

When I have a choice, I write software in in Clojure. Emacs coupled
with [Slime][sl]/[Swank][sw] or [nREPL][cnr]/[nrepl][nr] just can't be
beat. (There seems to be a lot of activity in the Vim universe along
these lines. I might give that a tourist try someday.)

Part of adopting a programming editor is to learn about its
extensions, and Emacs is extensible by its very definition (Editor
MacroS). So far, here's what I've found useful:

**[auto-complete][ac]**

This does exactly what it says it does. When you start typing a
symbol, Emacs suggests how to complete it. Press return, Emacs does
the rest. Sometimes this is quite annoying because it breaks your
flow, especially if you slow down enough that the pop up breaks in,
and then (at least I) spend too much time trying to make the widget it
go away. On the whole, though, it's pretty neat, and it's certainly no
worse than you average IDE. As should be obvious, the completions are
based on pattern matching, not a semantic understanding of your
application.

**[flyspell][fs]**

One of the downsides of using Emacs (and, I imagine, Vim) on the Mac
is that it doesn't participate in the generic spell-checking that
every Cocoa app gets for free. Enter flyspell. You can check spelling
"on the fly" as you type or you can run it once to find all the
misspellings in your buffer. Downside: it's just not as automated or
fast as the native OSX stuff.

**[interactive do][ido]**

Emacs seems really focused on the one-visible-file-at-a-time sort of
editing. You can have multiple panes, sure, but tabbed panes (like in
web browsers) are (ahem) painful to manage if you have a lot of them
and the speed-bar (and other file listing options) are also
sub-standard (even if kind of neat in their own way). Enter
Interactive Do. This feature allows you to "fuzzy" search for files in
the mini-buffer, a kind of command-line completion sort of thing. It's
smart enough to remember files you've edited before. If I'm deep into
a directory structure for some project or other, I can still C-x f to
the mini-buffer, type init.el, wait a moment, and Ido figures out
where init.el is and lets me load it up. The end result is that I
start to see this as a solution for the problem of giant trees of
files in a side bar, or way-too-many tabs across the top of a
window. Just punt and use search instead. (I also use ido-ubiquitous
with this stuff.)

**[magit][mg]**

Git. Who's not using it these days? I'm one of those types who likes
to keep text editing inside text editors, and command-line stuff at
the terminal. But this mode is pretty nice. All I ever really do with
git, or other VCSs, is push, pull, commit, browse logs. Sometimes I
look at diffs. This app-within-emacs works really well for that sort
of thing. Here's a nice little tutorial in two pars: [part 1][mg1]
[part 2][mg2]. Could use more colorful diff output.

**[mark-multiple][mm]**

IntelliJ and Eclipse have these modes where when you highlight a
symbol in an editor, the IDE selects all similar words. When you
change the first word, all the rest of them are changed, too. A bit
safer than "search and replace". This minor mode lets you do that as
well. Worth checking out!

**[package][ep]**

The package extension (default in Emacs 24+) allows you to install all
of the rest of these extensions automatically. It's about time! Very
nicely done. Downsides: There doesn't seem to be a nice automated way
for Emacs to tell you that there are new versions of the extensions
available. At least not that I've noticed. If you use this, you should
add in the [Marmalade repo][marm].

**[paraedit][pe]**

A specialized mode I use for Clojure, mainly. Seems to work reasonably
well in Javascript, but I tend to use Autopair for that. Clojure is a
lisp which means it's just symbols and parenthesis (and brackets and
braces). Paredit keeps track of all that plus has a lot of handy stuff
for moving Lisp expressions around. I've barely scratched the surface,
frankly, and I still find it terribly useful.

**[smex][sx]**

Fuzzy-matching for Emacs commands. When you type "M-x" you're put into
the mini-buffer where you can enter a command (such as
"string-replace") or countless others. With smex enabled, you can type
in characters that are somehow part of a given command and be
presented with a list of matches. This is worth the price of
admission. For some reason, I find this much easier than
[TextMate][tm]'s or [Sublime Text 2][st]'s versions of the same
thing. Not sure why.

**[wrap-region][ww]**

Discovered this while working on this very, overlong blog post. I
installed it via the Emacs package management system, made sure it was
enabled for all modes. Now I can highlight a word or phrase, type a
brace or parent or quote and have the selection wrapped in that
character.

A lot of the above brings parity between Emacs and other editors, such
as the venerable (alas) TextMate. Some things I'm never likely to use,
such as snippets.

So, there you have it. ;)

[ac]: http://cx4a.org/software/auto-complete/ "Autocomplete Mode"
[cnr]: https://github.com/clojure/tools.nrepl "Clojure nREPL"
[em]: http://emacsformacosx.com "Emacs for Mac OSX"
[ep]: https://github.com/technomancy/package.el "Package"
[er]: http://emacsrocks.com "Emacs Rocks!"
[fs]: http://goo.gl/cPcED "Flyspell"
[ido]: http://emacswiki.org/emacs/InteractivelyDoThings "Ido"
[marm]: http://marmalade-repo.org "Marmalade Repo"
[md]: http://daringfireball.net/projects/markdown/ "Markdown"
[mg1]: http://goo.gl/LmPmi "Magit Tutorial 1"
[mg2]: http://goo.gl/KLrhk "Magit Tutorial 2"
[mg]: https://github.com/magit/magit "Magit"
[mm]: https://github.com/magnars/mark-multiple.el "Mark Multiple"
[nr]: https://github.com/kingtim/nrepl.el "Nrepl"
[pe]: http://emacswiki.org/emacs/ParEdit "Paredit"
[sl]: http://common-lisp.net/project/slime/ "Superior Lisp Interactive Mode"
[st]: http://www.sublimetext.com "Sublime Text"
[sw]: https://github.com/technomancy/swank-clojure "Swank"
[sx]: https://github.com/nonsequitur/smex "Smex"
[tm]: https://github.com/textmate/textmate "TextMate"
[ww]: https://github.com/rejeep/wrap-region "Wrap Region"
