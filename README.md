# HTTP server kata

This project contains the result and notes for Johannes Brodwall's presentation on Building an HTTP Server with Nothing Up My Sleeves.

The presentation builds a (more or less) working HTTP server from scratch in one hour with no real dependencies. It only uses the following:

* The Java 19 programming language
* java.lang, java.util, java.nio and java.io packages
* java.net.Socket and java.net.ServerSocket
* java.net.URL for testing
* JUnit 5 for testing

We use [RFC 7230](https://www.rfc-editor.org/rfc/rfc7230) for reference (it's not the newest, but it's the one I know best)

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

## Programmer tips

I often get asked how to learn how to work effectively with IntelliJ. Like everything, it's always a matter of practice, but I have a few recommendations as to what to focus on when you practice

### IntellJ shortcuts

These are some of the most versatile keyboard shortcuts in IntelliJ. There are many more, but learning these 12 will really speed up your code

| Shortcut (Windows)   | Shortcut (Mac)      | Command                                          |
|----------------------|---------------------|--------------------------------------------------|
| alt-enter            | opt-enter           | Show content action (quick fix)                  |
| ctrl-alt-shift-t     | ctrl-t              | Refactor this (show refactor menu)               |
| alt-insert           | cmd-n               | New... (add some content)                        |
| ctrl-w               | opt-up              | Expand selection (add shift to go the other way) |
| shift-alt-f10        | ctrl-opt-r          | Run....                                          |
| shift-alt-f9         | ctrl-opt-d          | Debug....                                        |
| shift-f10            | ctrl-d              | Rerun last....                                   |
| ctrl-b               | cmd-b               | Navigate to symbol                               |
| alt-ctrl-left        | cmd-opt-b           | Navigate back                                    |
| alt-j                | ctrl-g              | Add next match to selection (multi-cursor)       |
| shift-ctrl-backspace | shift-cmd-backspace | Goto last edit location                          |
| shift, shift         | shift, shift        | Search anywhere                                  |

Make yourself familiar with `Refactor this` (ctrl-alt-shift-t / ctrl-t) and use it to learn the shortcut keys for your favorite refactorings like Extract method, Rename and Inline. Also, make sure you explore what's available on the Content Action (alt-enter).

Also remember basic cursor navigation like ctrl-left and ctrl-right to jump one word at a time, home and end and holding shift to expand selection while you move the cursor.

### IntelliJ Live Templates

Less used than the shortcuts, these shorthand ways of writing common bits of Java code can save a bit of time. Write the name of the code template in the right spot and press Tab to have IntelliJ expand it

| Template    | Result                                     |
|-------------|--------------------------------------------|
| `fori`      | `for (int i=0; i<...; i++) {}`             |
| `main`      | `public static void main(String[] args) {` |
| `sout`      | `System.out.println();`                    |

