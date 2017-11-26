#!/usr/bin/env bash
if [ ! -z "$1" ]; then
	cd $1 || exit 1
fi
mvn clean javadoc:jar deploy || exit 1
if [ ! -z "$1" ]; then
	cd - || exit 1
fi
git add docs
git commit docs -m "Updated JavaDocs"
