# HTTP server kata

A demonstration of how to build an http server

* [x] What does the browser say?
* [x] What does the server say?
* [x] Structure the server to be testable
* [x] Unit test: A totally empty server
* [x] Unit test: Responding with a file
* [x] Demo: Responding with a file
* [x] Unit test: Responding more than once
* [x] Demo: Logging in and showing the user
* [x] Unit test: 401 if there's no cookie
* [x] Unit test: Respond with name if there is a cookie
* [x] Demo: Setting the cookie in the development tools (and logging out)
* [x] Unit test: Setting the cookie
* [x] Demo: Logging in and showing the user

### Obvious flaws

* [ ] Silly: Refactor http server with new methods
* [ ] Fatal: respond to dir requests with welcome-files (instead of crashing!)
* [ ] Fatal: don't crash on binary files
* [ ] Fatal: don't serve files outside of the contentRoot
* [ ] Critical: Decode more than one form parameter
* [ ] Critical: Support having more than one cookie in the browser
* [ ] Serious: URL encoding (including UTF-8)
* [ ] Useful: Redirect after login

