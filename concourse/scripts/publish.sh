#!/bin/sh

set -eou

# setup

echo 'setup git'
export DEBIAN_FRONTEND=noninteractive
apt-get -yqq update
apt-get -yqq install git

cd ./fauna-jvm-repository

PACKAGE_VERSION=$(./gradlew -q printVersion | tail -n 1)

# publish

echo "publishing version: $PACKAGE_VERSION"

export ORG_GRADLE_PROJECT_mavenCentralUsername="$MAVENCENTRAL_USER"
export ORG_GRADLE_PROJECT_mavenCentralPassword="$MAVENCENTRAL_PASSWORD"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$SIGNING_KEY"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="$SIGNING_KEYID"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="$SIGNING_KEYPASSWORD"

./gradlew publishToMavenCentral

# release tag

echo "tagging release-$PACKAGE_VERSION"
cd ../
git clone fauna-jvm-repository fauna-jvm-repository-tagged
cd ./fauna-jvm-repository-tagged

git config --global user.email "production@fauna.com"
git config --global user.name "Fauna Engineering"
git tag "release-$PACKAGE_VERSION"

echo "fauna-jvm@$PACKAGE_VERSION has been published. (Can be delayed showing up in maven central.)" > ../slack-message/publish
