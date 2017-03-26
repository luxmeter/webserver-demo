# Java Webserver Demo

This is a demo of a simple webserver being only capable of serving static files from a user defined directory.
It is not meant to be used in any environment. *Its only purpose is to show good coding practices*.

**Features**:
* Understands GET and HEAD methods
    * Lists recursively all files and subdirectories if the URL points to a directory
* Understands If-Match, If-Non-Match, If-Modified-Since header-fields *for all static files*

## Usage

`java -jar webserver.jar start`

Executing this command will start the server on **port 8080** using the current working directory as root.
Use the `--rootdir` option if you want another directory to be root.
Without any options the help page will be printed (same as with `-h` or `--help`) 
showing a complete list of all available commands and options.

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
-H 'If-None-Match: ECCD66D6803584426248217359708D8C' \
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
-H 'If-Modified-Since: Wed, 21 Oct 2015 07:28:00 GMT' \
-H 'Connection: close'
```


**If-Modified-Since: HTTP/1.1 200 OK**

```
curl -v 'http://localhost:8080/src/test/resources/some_file.md' \
-H 'If-Modified-Since: Wed, 21 Oct 2099 07:28:00 GMT' \
-H 'Connection: keep-alive'
```

## Issues

It is recommended to set the Content-Length equal to the file size even though a HEAD request was made and no response body must be returned.
The current implementation returns the file size and appends a -1:
`Content-length: 12 -1`