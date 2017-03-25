#!/bin/env sh

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
EXECUTABLE=$(find $SCRIPTPATH/target/*-with-dependencies.jar 2> /dev/null)

if [[ $? != 0 ]]; then
	echo "Executable not found :("
else
	echo "Executing $EXECUTABLE"
	java -jar $EXECUTABLE
fi
