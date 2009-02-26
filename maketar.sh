#!/bin/bash

mkdir ans2120-proj1
if [ $? != 0 ]; then
    exit 1
fi

cp -R src Readme.pdf transcript.txt build.sh run.sh maketar.sh \
    Makefile ans2120-proj1

tar czvf ans2120-proj1.tar.gz ans2120-proj1

rm -rf ans2120-proj1
