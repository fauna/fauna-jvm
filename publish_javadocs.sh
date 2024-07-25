#!/bin/bash

set -euo pipefail

ORIGINAL_DIR=$(pwd)

PACKAGE_VERSION=$(./gradlew -q printVersion | tail -n 1)

./gradlew clean javadoc

echo "Current docs version: $PACKAGE_VERSION"

# Check if the Javadoc directory exists
if [ ! -d "$ORIGINAL_DIR/build/docs/javadoc" ]; then
    echo "Error: Javadoc directory not found at $ORIGINAL_DIR/build/docs/javadoc"
    exit 1
fi

TEMP_DIR=$(mktemp -d -t fauna-jvm-docs.XXXXXX)

REPO_URL=$(git config --get remote.origin.url)

git clone "$REPO_URL" "$TEMP_DIR"

cd "$TEMP_DIR"

git checkout gh-pages

if [ -d "$PACKAGE_VERSION" ]; then
    rm -rf "$PACKAGE_VERSION"
    echo "Existing $PACKAGE_VERSION directory removed."
fi

mkdir -p "$PACKAGE_VERSION"

cp -R "$ORIGINAL_DIR/build/docs/javadoc/"* "$PACKAGE_VERSION/"

git add "$PACKAGE_VERSION"

git commit -m "Updated docs to version: $PACKAGE_VERSION"

git push origin gh-pages

cd "$ORIGINAL_DIR"

rm -rf "$TEMP_DIR"

echo "Updated docs to version: $PACKAGE_VERSION"
