{:type :post
 :post-date "2009-07-11"
 :post-time "13:01"
 :post-slug "case-study-asynchronous-rest-1"
 :post-title "On the problem to be solved (1 of 3)"
 :post-tags "software"
 :post-publish? true}

## general problem

A company I worked for (let's just call it The Company) sold a lot of
products and offered a lot of rebates. The rebates were processed by a
Rebate Processor company which took in the numbers and other rebate
information from customers (such as product descriptions), did all the
paperwork, sent out the cash, then billed The Company for its efforts.

In other words, The Company outsourced the handling of rebates, as I
imagine many companies do.

The problem, though, was that it was possible for miscreants to
introduce fraud into the system by submitting properly formatted
serial numbers which where, nevertheless, fake.

What The Company wanted to do was offer a service such that the Rebate
Processor could ask us if a given serial number was not only valid
(proper numbers and letters in the right order), but had actually been
issued against a product instance.

(My apologies for the vague language, but hopefully you understand the
legal implications of mentioning anything to anyone. Oy.)

## technical problem

You'd think that the solution should be pretty easy. Just offer a web
service that, when you post a serial number, responds with a "true" or
"false," depending on whether or not the serial number was ever used.

However....

My company did not have a single data source with all the serial
numbers ever used for all the products it sold, or had ever sold.

Why?

  * **Acquisitions**:

    The Company had acquired many other companies, each of which had
    their own methods and data stores dedicated to issuing, managing
    and tracking serial numbers.

  * **Federated Divisions**

    The Company itself had, for a long time, developed a federated
    culture in which each division was locally managed, with only
    minimal oversight from the corporate leadership. Each of those
    divisions represented quite varying products and product families,
    and each one had its own way of managing serial numbers.

Over the years, there were efforts to consolidate this information,
and those efforts were largely successful in that there were just two
sources to consult about the validity of serial numbers:

  * **Web Service**

    A web service, with a complicated <small>XML/HTTP</small>
    interface. Not <small>SOAP</small>, not <small>REST</small>, but
    just <small>XML</small> posted to and retrieved from an
    <small>HTTP</small> endpoint.

  * **Oracle Database**

    An Oracle database. A BIG Oracle database, with lots of views, and
    many tables containing many serial numbers defined in a
    not-readily-discoverable, and potentially ever-changing ways.

These two data source were *internal* data sources, and did not have
particularly stringent service level agreements. If the Oracle
Database needed to go down for maintenance, it went down for
maintenance, users beware. Same with the web service. The potentially
lackadaisical uptime for these services was reasonable, given what
they were normally used for, and given their role in the normal
business operations of the company.

Finally, there was a good change that a perfectly valid serial number
on an actual, physical product was not in either data store.

Yikes!

## summary of complicating factors

Let's summarize the situation:

  * More than one internal service.

  * Unreliable internal services.

  * Both internal services (potentially) must be consulted to resolve
    a question about each submitted serial number.

  * Valid serial numbers might not be found in any internal data source.

  * With new acquisitions, there might be additional data sources to
    be integrated.

  * The two existing internal services might merge, or morph into a
    third, grand-vision, data-warehouse-like thing (which is always
    the threat in a corporation as mind-bogglingly, borg-imitating
    like The Company).

The bottom line is that any design, we thought, would have to
accommodate, maybe even, dare we say it, make it easy to make changes
over time.

## solution space

In attempting to work out what to do about the above, we contemplated
several options, which boil down to the following three approaches:

  * **Synchronous with Synchronized Cache**: Periodically import all
  serial number data from all available systems into a local service
  database, and serve out answers synchronously.

  * **Synchronous, Luck of the Draw**: Each incoming web request
    should consult each internal service in turn, and respond with the
    results, as best it can, even if one or more of them are down.

  * **Asynchronous**: Submit a request asynchronously, and look for
the completed request at a later time. Internally, we move the job
around, consulting each source, make our best guess about the validity
of the number, and "publish" the result for later pickup.

We chose the last option (thankfully, or there'd be no reason to write
this, at least as far as my interests are concerned).

We couldn't use Option 1, in which we'd import all available data for
several reasons: even if we import *only* the serial numbers with no
associated metadata, we'd have more data than our little effort could
sustain, and some Architect who didn't understand that shared state is
bad would see the copy as duplication, rather than caching, and nix
the project. Finally, we had to also import additional metadata so
that we could guess if a given serial number we didn't have is at
least likely to be legitimate. (We'd publish a "confidence factor" if
we couldn't find an exact match.) And, of course, we only had about
two months to develop the entire solution and even if importing data
was fast and easy, procuring enough infrastructure to make it happen
most definitely wasn't.

Option 2 seems, on the surface, the most reasonable, except that one
of the resources we needed to consult was a database with millions of
rows of data. It was unclear that the queries we'd have to run to make
it work could complete before an <small>HTTP</small> request could
complete. Timeouts are unpleasant on either side of a remote procedure
call. Also, the resulting monolithic webapp code would be further
complicated each time we added a new resource to consult, or had to
change our strategy. How would we know if fixing one part of the app
would break the other, seemingly unrelated part? What we needed was a
way to handle a potentially long-running request.

And so, finally, we settled on Option 3, an asynchronous web request
style service, which is maybe another way of saying "batch
processing".

The client would submit a batch of serial numbers and any associated
metadata (such as the model of the product). We'd return with an `OK`
if the batch job was valid, and at some later point in time, the
client would use the numbers in that batch to poll for any results. If
the client could find no results for a specific number, they were free
to re-submit it in another job.

Using the above strategy also allowed us to carry the idea of
asynchronous services behind the scenes and make it the underlying
methodology of the entire supporting architecture.

That architecture, with all the unintended benefits it provided, is
the whole point of this long exposition, and will be the main subject
matter in the [next article][part2].

[part1]: :site-url/articles/2009/07/11/case-study-asynchronous-rest-1
[part2]: :site-url/articles/2009/07/15/case-study-asynchronous-rest-2
[part3]: :site-url/articles/2009/07/23/case-study-asynchronous-rest-3
