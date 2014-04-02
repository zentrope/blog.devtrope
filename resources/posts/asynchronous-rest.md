{:type :post
 :when "2009-07-09"
 :slug "initial-thoughts-on-asynchronous-rest"
 :title "On asynchronous REST interfaces"
 :tags "software"
 :publish? true}

Tim Bray's [article][tb] got me thinking about REST and about
synchronous vs asynchronous interfaces.

[tb]: http://www.tbray.org/ongoing/When/200x/2009/07/02/Slow-REST

What really got me interested in asynchronous services, especially
message-based services of the fire-and-forget kind, was how helpful
such things were as you develop and maintain services over time, and
across organizations, or even across the "divide" between one
head-strong developer and another, or between two tasks that are
completely different, but share data.

But that's for another note, another time.

One of the issues I've had with REST is not the style itself, but its
synchronous nature, or at least how it's used in common web-service
style architectures as not much more than a function call. Cleaner
than SOAP, certainly, more maintainable and understandable, but,
basically, a function call.

Nevertheless, a step in the right direction, at least for me, is to be
able to use a REST style HTTP interface in a fire-and-forget kind of
way.

Tim's article is mostly about making HTTP requests which initiate
actions that take longer than a traditional HTTP request should
last. How do you workaround connection timeouts?

I'm interested in a slightly different but related idea: how do you
make a REST request without getting any answer, but then set things up
so that you can get the results of that request at a later time?

## polling

The idea of polling is that you submit a request to a specific
resource, which returns an in-progress result code, and a payload with
a URL providing you with a resource you can consult about the status
of the job.

You can periodically issue a GET on that resource to find out the
status of the job. Presumably, when the job is complete, the poll
request will provide a link to the finished results (if there are
any).

I'd guess that this solution is pretty hard to scale, though that
might be done by returning not only how complete the job is as a
percentage, but an allowance for how many times in a given time frame
a client is allowed to check back. For instance, a client might be
allowed to check back 3 times a minute, or each check might provide a
suggested time for when to check back next (and refuse any checks
earlier than that).

I've actually implemented a polling-style service like this a few
years ago. The client would submit a request to the service with a
payload containing a unique ID. At a later time, the client was
supposed to use that ID to construct a URL to look for the result. It
was considered okay if, after a sufficiently long time without a
result, the client could re-submit the request. As far as I know, the
service is still in production.

## callback

My favorite method is the callback.

When you submit a job, you include in that job a URL to which the
results should be POSTed. Your client can then consider its task of
submitting the job as complete and go on doing other things. Sometime
later, another part of your app, the "server" part, gets a request,
which is the result of the job.

Event driven logic.

Very clean. And works well, if you control both sides of the network,
as in, both sides reside within your data center. Not so good if you
want to make a request from your data center to the external world,
given firewall issues. (Of course, given that the call back is HTTP,
it's probably not as controversial as your average Ops manager might
make you think.)

## thoughts

The really important part of this, though, is to allow for
asynchronous behavior on top of a strategy that (in the pop culture
that rules the tech world), is mostly conceived of as synchronous,
remote-procedure calls.

If you submit a request without requiring an immediate response, that
job can be shipped off to be handled my many internal (and invisible
to you) services, any one of which may fault, or timeout, or throw
exceptions. By requiring an asynchronous methodology, the immediate
transaction, that of connecting to the job-submission service, is very
simple. Either the job was submitted, or it was not.

It's also straight forward to find out if the job succeeded or failed
via the polling or callback methods, at which point you re-submit the
whole thing, or simply log it for later human intervention.

Large grained simplicity over fine-grained complexity.
