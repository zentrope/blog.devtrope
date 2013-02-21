# Zentrope Static Blog

In Development

Basic idea is to create a command line tool. You tell it where the
"source" is, where the "target" ought to be, what the site URL should
be, and then let it rip.

No plugins, no build file, no config file.

**If you're reading this disclaimer, it means you're seeing this
because I'm saving an incomplete README file. The rest of this README
is a work-in-progress, a rough draft, a partially completed
free-write. None of it is knowingly wrong. All of it us discouragingly
inadequate.**

## Build

Initially:

  * Install [leiningen](http://leiningen.org).
  * Install [git](http://git-scm.com).

Then:

    $ cd ~/blog
    $ git clone git@github.com:zentrope/zentrope-sb.git zentrope-sb
    $ cd zentrope-sb
    $ lein uberjar
    $ cp target/zsb.jar ~/bin

As of this writing, building the app from source is the only way to
get it.

## Usage

This just about covers is:

    $ cd ~/blog
    $ mkdir source
    $ mkdir target

    $ java -jar ~/bin/zsb.jar -s source -t target -u ""

You'll end up with a blog in `target` in which the links have a prefix
of `/` if you're going to deploy to the root of your domain.

Use:

    -u /blog

if you're going to deploy into a sub-directory on your website, such
as:

    http://bandyblatt.com/blog

If you do NOT use a `-u` parameter, the blog will prepend links with a
file URL:

    file://Users/you/blog/target

so that you can open the site locally via your browser.

The idea is that you can use this software as part of some larger
work flow. It is something you plug in to an external hook. It does
_not_ contain hooks so you can the simple processing the app does.

You can also run the application from source if you want:

    $ lein run -- -s ../source -t ../target -u ""

That's what I do. ;)

Only this _doesn't_ really cover it.

The bulk of the work is in your templates, your CSS, your HTML layout
skills, and so on. This app does nothing for you along those lines. On
the other hand, it doesn't prevent you from doing interesting things,
either.

## Blog Source Directory

The source directory contains everything the software uses to generate
a web site.

Let's start with an example:

    .
    ├── articles
    │   └── 2013
    │       ├── 01
    │       │   └── 15
    │       │       └── welcome-all
    │       │           └── Welcome to my Blog.md
    │       │   └── 28
    │       │       └── origin-stories
    │       │           └── My Favorite Comic-Based Movies.md
    │       └── 02
    │           └── 05
    │               └── shell-scripts
    │                   └── I've Discovered the Find Command.md
    ├── assets
    │   ├── pages
    │   │   └── contact.html
    │   │   └── about.html
    │   │   └── disclaimer.html
    │   ├── images
    │   │   ├── masthead.png
    │   │   ├── favicon.ico
    │   │   ├── logo.png
    │   │   └── ultimate-spider-man-cover.png
    │   ├── scripts
    │   │   ├── special-fx.js
    │   └── styles
    │       └── site.css
    └── templates
        ├── archive-article.html
        ├── archive.html
        ├── article.html
        ├── feed-article.rss
        ├── feed.rss
        ├── home.html
        └── home-article.html

There are three main directories:

  * **articles:**<br/>
    A tree of markdown files representing, hopefully, the
    reason you're interested in blogging in the first place.

  * **assets:**<br/>
    All the stuff that makes up your site, potentially referred
    to by your articles or by the templates. Images, for instance, and
    scripts, stylesheets, podcasts, zip files: the works. Everything
    that's not an article and not a template.

  * **templates:**<br/>
    Conventionally named templates used to turn your assets and
    articles into a web site.

Let's go through each one these, saving the "templates" for last, as
they're the most complicated part of this whole thing (outside of of
HTML/CSS and actually writing actual articles).

### Articles

There are two aspects to the way you write articles for this
particular minimalist engine:

  * the path to the article
  * the article

Let's use the following (from the above) as an example:

    │   └── 2013
    │       ├── 01
    │       │   └── 15
    │       │       └── welcome-all
    │       │           └── Welcome to my Blog.md

The **Article path** contains all of the metadata about the article,
including:

  * The url: _domain.com/2013/01/15/welcome-all_

  * The article's date: _2013-01-15_ (sorry, no timestamp)

  * The article's title: _Welcome to my Blog_

The **Article** itself is just a simple text file written in
[markdown](http://en.wikipedia.org/wiki/Markdown) format. When you
generate the static website, the markdown is translated into HTML,
then deposited into a document structure implementing the URL
indicated by the path to the markdown file, like so:

    <document-root>/2013/01/15/welcome-all/index.html

Some caveats:

  * You probably don't need an `h1` level head line at the beginning
    because the software will create one for you based on the name of
    the file (with the `md` extension removed).

  * You can use the template variable `:site-url` in your markdown to
    refer to the blog's document root so that you can link to images
    and other assets.

        I really enjoyed Ultimate Spider-Man:

         ![Cover](:site-url/images/ultimate-spider-man-cover.png)

        because it focused on Peter Parker more than
        Spider-Man. Parker is the heart and soul of that story.

That hardest part of all this is setting up the HTML templates. Once
that's done, actually putting down new content to be published should
be as easy is creating date-based sub-directories and then editing
plain test files.

### Assets

In general, assets are just raw files copied to your document root
without malice aforethought. They should be arranged just as you want
them on your real web site and you can refer to them in your templates
and markdown files via the `:site-url` template variable.

For instance, the following in a template file:

    <link rel="stylesheet" type="text/css" href=":site-url/styles/main.css"/>

implies that you have the following in your asset directory:

    <source-root>/assets/styles/main.css

This will be copied to your document root:

    <document-root>/styles/main.css

You can arrange things any way you want inside the asset directory. As
long as you use `:site-url`, you should be able to create links
pointing to the assets as needed.

**NOTE:** If your assets end in `.js` or `.css` or `.html`, the
software will merge references to `:site-url`. This is so that you can
use other assets inside your static content.

For example, a css fragment:

```css
    html, body {
      background: url(:site-url/images/bg.png);
    }
```

This is also useful for pages you want to have that aren't generated,
such as a `contact.html` page, or an `about.html` page in which you
might have (say) a navigation area:

```html
    <li><a href=":site-url">home</a></li>
    <li><a href=":site-url/contact.html">contact</a></li>
```

or something similar.

### Templates

Random notes....

- home.html:<br/>
  The main page containing the full test of your
  most recent articles.

    - home-article.html: An HTML fragment representing a single article as
      it appears when it's merged into _index.html_.

- archive.html: <br/>A single linking to all of your articles.

    - archive-article.html: An HTML fragment representing a single article
      as it appears when it's merged into the _archive.html_.

- article.html: <br/>
  A template for single-page, stand-alone articles
  (linked to from the _archive.html_ and _index.html_ pages.)

- feed.rss:<br/>
  The format for your RSS 2.0 feed.

    - feed-article.rss: The RSS 2.0 "item" XML template for how you
      want your blog to be seen via RSS.

### Template Variables Reference

There are three types of template vars:

  * **metadata** (:site-url, :pub-machine-date)<br/>Usable on any page
    for any reason.

  * **posts** (:article-title, :article-text (etc))<br/>For merging into
    templates representing a single post (item.rss, article.html,
    post.html).

  * **aggregates** (:article-list, :feed-items)<br/> For injecting a
    collection of posts (html fragments) into a larger container page
    (index.html, archive.html, feed.rss).

The details:

  * `:site-url` <br/>
    The web site's document root. Normally blank if you
    deploy to the top of your domain (e.g., _domain.com/index.html_), or
    a subdirectory _/blog_, if not (e.g., _domain.com/**blog**/index.html_).

  * `:article-title`
  * `:article-date`
  * `:article-text`
  * `:article-url`
  * `:article-machine-date` [feed]
  * `:build-machine-date` [feed]
  * `:pub-machine-date` [feed]

The following are for interpolating a list of articles, formatted by a
sub-template into a list pages, such as an RSS feed, the home page, or
the archive listing.

  * `:article-list`     --> (home page, feed xml, archive page)

## License

Copyright © 2013 Keith Irwin

Distributed under the Eclipse Public License, the same as Clojure.
