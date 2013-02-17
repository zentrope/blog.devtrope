# TO DO

  * Archives page

  * Consider making date stuff finer grained so a template can do
    interesting things with it.

  * Use the markdown file name itself as the title.

  * Allow "html" articles as well.

  * Document directory structure.

  * Document template variables.

  * Document how minimalist this really is.

  * Add an "auto" facility that watches files and regenerates the site
    on every change. If the change is to an asset or article, just
    copy those over. If it's to a template, re-gen the whole thing.

  * Make this a one-pass system. Load everything into a data
    structure, then pass it through transforms. No need to
    differentiate between "posts" (shown in the index page) and
    "articles" (each on their own page), etc.

  * Generalize template interpolation: just a map and a blob of
    text. Replace occurances of keys in blob with values in map.
