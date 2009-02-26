#!/bin/bash

usage() {
    echo "run.sh <libdir>"
    echo "    run.sh must be above ./bin/"
    echo "    libdir is relative to run.sh dir"
}

if [ -z "$1" ]; then
    usage >&2
    exit 1
fi
LIBDIR="$1"

if [ ! -d "./bin" ]; then
    echo `pwd`"/bin doesn't exist. Did you run make yet?" >&2
    exit 1
fi
cd ./bin

java -cp "$CLASSPATH:$LIBDIR/junit-4.5.jar:$LIBDIR/commons-codec-1.3/commons-codec-1.3.jar:$LIBDIR/commons-httpclient-3.1/commons-httpclient-3.1.jar:$LIBDIR/commons-logging-1.1.1/commons-logging-1.1.1.jar:$LIBDIR/commons-lang-2.4/commons-lang-2.4.jar:$LIBDIR/apache-log4j-1.2.15/log4j-1.2.15.jar:$LIBDIR/lucene-2.4.0/lucene-core-2.4.0.jar" coms6111/proj1/RunnerGUI
