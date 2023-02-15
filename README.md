# HTTP server kata

This project contains the result and notes for Johannes Brodwall's presentation on Building an HTTP Server with Nothing Up My Sleeves.

The presentation builds a (more or less) working HTTP server from scratch in one hour with no real dependencies. It only uses the following:

* The Java 19 programming language
* java.lang and java.io packages
* java.net.Socket and java.net.ServerSocket
* java.net.URL for testing
* JUnit 5 for testing

During the presentation we build a server that can:

* Respond to HTTP requests
* Serve files from disk
* Respond to API requests
* Handle login and user sessions
* Unit tests for all the functionality

During the presentation, we highlight many important developer lessons:

* HTTP is only text sent to and from the server - you can parse and build it just like with files
* *How* you approach the task of coding is as important as the code you write
* The structure of code is best grown gradually as the code solves more and more functionality
* The process is most engaging then you minimize the time spent with code that doesn't compile or tests that fail
* Refactoring support in your IDE can increase your productivity incredibly

## Contents

This repository contains several iterations of building the code bases, each living on a separate branch

### Performances

* [Performance starting point](https://github.com/jhannes/kata-http-server/tags)
* [JavaBin Christmas Lecture, 2022](https://github.com/jhannes/kata-http-server/tree/performance/javabin-2022-M12)
* [Sopra Steria DevMeetup, 2022](https://github.com/jhannes/kata-http-server/tree/performance/soprasteria-2023-M2)

### Reference implementations

* [A comprehensive server](https://github.com/jhannes/kata-http-server/tree/reference/comprehensive-server) - includes routing and a `HttpRequestHandler` framework, SSL support, a strong `HttpServerRequest` class with header parsing, request parsing and cookie parsing, and a fluid `HttpServerResponse`. This lets you see a fairly comprehensive result
* [A stepwise commit log](https://github.com/jhannes/kata-http-server/commits/reference/stepwise-commits) with 50 commits showing a step-by-step approach with each new failing test, each completed test and each refactor as a new commit. This lets you follow the process as I build a fairly functioning server.