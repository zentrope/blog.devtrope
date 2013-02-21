# TO DO

  * <strike>~~Archives page~~</strike>

  * Consider making date stuff finer grained so a template can do
    interesting things with it.

  * <strike>~~Use the markdown file name itself as the
    title.~~</strike>

  * Allow "html" articles as well.

  * <strike>~~Document directory structure.~~</strike>

  * Document template variables.

  * Document how minimalist this really is.

  * Add an "auto" facility that watches files and regenerates the site
    on every change. If the change is to an asset or article, just
    copy those over. If it's to a template, re-gen the whole thing.

  * <strike>~~Make this a one-pass system. Load everything into a data
    structure, then pass it through transforms. No need to
    differentiate between "posts" (shown in the index page) and
    "articles" (each on their own page), etc.~~</strike>

  * <strike>~~Generalize template interpolation: just a map and a blob
    of text. Replace occurances of keys in blob with values in
    map.~~</strike>

  * <strike>~~Rationalize the template var names and the template file
    names.~~</strike>

  * Add a "serve" facility so you can run this on localhost:port and
    thus not have to use explicit "index.html" references if you don't
    want to.

  * Allow static pages (not articles, e.g., contact, about) to be
    markdown, with a template for them as well.

  * <strike>~~Computed pages should be resources (/contact) not file
    (contact.html).~~</strike>

  * Recognizable assets should have :site-url interploated. (js, css,
    html).
