# HTTP server kata

A demonstration of how to build an http server


## Steps

1. HttpServer creates server socket => connect browser
2. HttpClient connects to http://www.rfc-editor.org/rfc/rfc7230/
   * Redirect
   * SSL
   * 404
3. HttpServer responds to browser
   * Demonstrate UTF problem
4. Refactor HttpServer into constructor - refactor out content to method 
5. HttpServerTest.shouldReturn404ForUnknown (status code)
   * Thread to make it pass
6. HttpServerTest.shouldReturn404ForUnknown (read response body)
   * read first line
   * parse requestTarget
7. HttpServerTest refactor server and openConnection; refactor HttpServerClient
8. HttpServerTest.shouldReturn202ForExistingFile (status code)
9. HttpServerTest.shouldReturn202ForExistingFile (content)
10. Test in browser - notice crashes and single request
11. HttpServerTest.shouldHandleMultipleRequests
12. Server works, but now crashes on favicon.ico => hide it
13. Demonstrate 404 on GET /api/login
14. HttpServerTest.shouldReturn401ForUnauthorizedUser
15. HttpServerTest.shouldReturnAuthorizedUsersName
    * parse header to get cookie
    * split username from cookie
16. Demonstrate problems on POST /api/login
17. HttpServerTest.shouldSetSessionCookieOnLogin
    * read body using 
    * split username from query body
18. Demonstrate in browser

Unsolved mysteries

* favicon.ico
* UTF-8
* query body
* cookie parsing
