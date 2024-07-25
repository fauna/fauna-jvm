#!/bin/sh

set -eou

cd ./fauna-jvm-repository

PACKAGE_VERSION=`grep version gradle.properties | cut -c 9-`

./gradlew javadoc

echo "Current docs version: $PACKAGE_VERSION"

cd ../
git clone fauna-jvm-repository-docs fauna-jvm-repository-updated-docs

cd fauna-jvm-repository-updated-docs

mkdir "${PACKAGE_VERSION}"
cd "${PACKAGE_VERSION}"

echo "Copying..."

mkdir api
cp -R "../../fauna-jvm-repository/build/docs/javadoc" api

echo "Adding google manager tag to head..."

HEAD_GTM=$(cat ../../fauna-jvm-repository/concourse/scripts/head_gtm.dat)
sed -i '' "0,/<\/title>/{s/<\/title>/<\/title>${HEAD_GTM}/}" ./api/index.html

echo "Adding google manager tag to body..."

BODY_GTM=$(cat ../../fauna-jvm-repository/concourse/scripts/body_gtm.dat)
sed -i '' "0,/<body>/{s/<body>/<body>${BODY_GTM}/}" ./api/index.html

git config --global user.email "nobody@fauna.com"
git config --global user.name "Fauna, Inc"

git add -A
git commit -m "Update docs to version: $PACKAGE_VERSION"
