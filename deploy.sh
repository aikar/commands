#!/usr/bin/env bash
if [ ! -z "$1" ]; then
	cd $1 || exit 1
fi
mvn -T 4 clean deploy || exit 1
mvn -T1 javadoc:jar || exit 1
if [ ! -z "$1" ]; then
	cd - || exit 1
fi
git co docs/**/overview-summary.html docs/**/index.html
git add docs
git commit docs -m "(DEPLOYED ACF) Updated JavaDocs"
