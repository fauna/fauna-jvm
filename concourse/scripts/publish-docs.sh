#!/bin/sh

set -eou

# setup

echo 'setup git'
export DEBIAN_FRONTEND=noninteractive
apt-get -yqq update
apt-get -yqq install git

cd ./fauna-jvm-repository

PACKAGE_VERSION=$(./gradlew -q printVersion | tail -n 1)

# generate docs

./gradlew javadoc

echo "Current docs version: $PACKAGE_VERSION"

if [ ! -d "./build/docs/javadoc" ]; then
    echo "Error: Javadoc directory not found at build/docs/javadoc"
    exit 1
fi

cd ../
git clone fauna-jvm-repository-docs fauna-jvm-repository-updated-docs
cd fauna-jvm-repository-updated-docs

if [ -d "$PACKAGE_VERSION" ]; then
    rm -rf "$PACKAGE_VERSION"
    echo "Existing $PACKAGE_VERSION directory removed."
fi

cp -R "../fauna-jvm-repository/build/docs/javadoc" "$PACKAGE_VERSION"

echo "Adding google manager tag to head..."

HEAD_GTM=$(cat ../fauna-jvm-repository/concourse/scripts/head_gtm.dat)
sed -i.bak "0,/<\/title>/{s/<\/title>/<\/title>${HEAD_GTM}/}" ./$PACKAGE_VERSION/index.html

echo "Adding google manager tag to body..."

BODY_GTM=$(cat ../fauna-jvm-repository/concourse/scripts/body_gtm.dat)
sed -i.bak "0,/<body>/{s/<body>/<body>${BODY_GTM}/}" ./$PACKAGE_VERSION/index.html

rm ./$PACKAGE_VERSION/index.html.bak

echo "Updating 'latest' symlink to point to $PACKAGE_VERSION"
ln -sfn "$PACKAGE_VERSION" latest

git config --global user.email "nobody@fauna.com"
git config --global user.name "Fauna, Inc"

git add -A
git commit -m "Update docs to version: $PACKAGE_VERSION"

echo "Updated docs to version: $PACKAGE_VERSION"
