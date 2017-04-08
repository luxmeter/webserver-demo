# Java Webserver Demo

This is a demo of a simple webserver being only capable of serving static files from a user defined directory.
It is not meant to be used in any real environment. *It exists only for educational purpose*.

**Features**:
* Can process GET and HEAD requests
    * Lists recursively all files and subdirectories if the URL points to a directory
* Can process If-Match, If-Non-Match and If-Modified-Since header-fields *for all static files*
* Can keep the connection alive

## Technical Design
It was required for this demo to implement the webserver without the use of third party libraries 
dedicated for this usecase. However, it was allowed to use libraries like Apache Commons for the sake of convenience.
Fortunately, the JDK provides a simple HTTP server (HttpServer) which means it is not needed to start from scratch.
It allows the developer to focus on the HTTP request processing rather than the TCP/IP handling and request parsing.

### Key components
* Application - The entry point of application providing the CLI interface for user interaction.
* HttpServer - A simple server implementation from the JDK handling the connections and the HTML parsing.
* Filter - Filterer can be attached to the HttpServer to process sequential the request before any Handler comes into play.
They can also abort further processing if necessary.
* Handler - Handles finally the filtered request (s. DefaultHandler).

Besides that, there have been left several TODOs and comments in the code to give a hint of the thought process. 

## Usage

`java -jar webserver.jar`

Executing this command will start the server on **port 8080** using the current working directory as root.
Use the `--rootdir` option if you want to change the directory.
Use `-h` or `--help` to get a complete list of all available commands and options.

## Rquest Examples

`curl --verbose --get http://localhost:8080/index.html`

Serves index.html if the file is available and shows a 404 error otherwise.

**List files**

```
curl -v 'http://localhost:8080/src/test/resources' \
-H 'Connection: close'
```

**If-None-Match: HTTP/1.1 304 Not Modified**

```
curl -v 'http://localhost:8080/src/test/resources/some_file.md' \
-H 'If-None-Match: 86FB269D190D2C85F6E0468CECA42A20' \
-H 'Connection: close'
```

**If-None-Match: HTTP/1.1 200 OK**

```
curl -v 'http://localhost:8080/src/test/resources/some_file.md' \
-H 'If-None-Match: abc' \
-H 'Connection: close'
```

**If-Match fails: HTTP/1.1 412 Precondition Failed**

```
curl -v 'http://localhost:8080/src/test/resources/some_file.md' \
-H 'If-Match: abc' \
-H 'Connection: close'
```

**If-Modified-Since: HTTP/1.1 304 Not Modified**

```
curl -v 'http://localhost:8080/src/test/resources/some_file.md' \
-H 'If-Modified-Since: Wed, 21 Oct 2099 07:28:00 GMT' \
-H 'Connection: close'
```


**If-Modified-Since: HTTP/1.1 200 OK**

```
curl -v 'http://localhost:8080/src/test/resources/some_file.md' \
-H 'If-Modified-Since: Wed, 02 Jul 2014 07:28:00 GMT' \
-H 'Connection: keep-alive'
```
