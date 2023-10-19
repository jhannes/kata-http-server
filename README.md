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

KRITISK: index.html
KRITISK: favicons.ico
KRITISK: GET ../../../README.md
ALVORLIG: form processing
ALVORLIG: mer enn én cookie
VIKTIG: URL encoding + utf 8
NYTTIG: Redirect

