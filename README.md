# HTTP server kata

A demonstration of how to build a HTTP server

browser Hello World
TEST: 404
TEST: 200
browser index.html
TEST: 404 x 2
browser style.css
TEST: 401
TEST: Cookie -> 200
browser > Application > Cookies (NB: Path)
TEST: Set-Cookie
browser > Application > Remove cookie
browser > index.html -> login -> show user
GIVE IT UP FOR Johannes Brodwall i Fredrikstad

Ting som mangler:

* [x] LURT: refactoring
* [x] KRITISK: index.html
* [x] KRITISK: favicons.ico
* [x] KRITISK: GET ../../../README.md
* [x] ALVORLIG: form processing
* [x] ALVORLIG: mer enn én cookie
* [x] VIKTIG: URL encoding + utf 8
* [x] NYTTIG: Redirect

