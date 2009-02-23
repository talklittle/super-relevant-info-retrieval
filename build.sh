#!/bin/bash

PWD=`pwd`
LIBDIR=$PWD/src/lib
if [ ! -e $PWD/bin ]; then
    mkdir $PWD/bin
fi
javac -classpath "$CLASSPATH:$LIBDIR/junit-4.5.jar:$LIBDIR/commons-codec-1.3/commons-codec-1.3.jar:$LIBDIR/commons-httpclient-3.1/commons-httpclient-3.1.jar:$LIBDIR/commons-logging-1.1.1/commons-logging-1.1.1.jar:$LIBDIR/commons-lang-2.4/commons-lang-2.4.jar:$LIBDIR/apache-log4j-1.2.15/log4j-1.2.15.jar:$LIBDIR/lucene-2.4.0/lucene-core-2.4.0.jar" -d $PWD/bin/ $PWD/src/coms6111/proj1/*.java
