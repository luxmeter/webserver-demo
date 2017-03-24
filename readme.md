# Java Webserver Demo

This is a demo of a simple webserver being only capable of serving static files from a user defined directory.
It is not meant to be used in any environment. *Its only purpose is to show good coding practices*.

**Features**:
* Understands GET, HEAD and POST methods
* Understands ETag, If-Non-Match, If-Modified-Since header-fields
* Can list all files and subdirectories in a particular directory


## Usage

`java -jar webserver.jar start`

Executing this command will start the server on **port 8080** using the current working directory as root.
Use the `--rootdir` option if you want another directory to be root.
Without any options the help page will be printed (same as with `-h` or `--help`) 
showing a complete list of all available commands and options.

## Rquest Examples

`curl --verbose --get http://localhost:8080/index.html`

Serves index.html if the file is available and shows a 404 error otherwise.