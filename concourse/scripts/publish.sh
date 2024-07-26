#!/bin/sh

set -eou

cd ./fauna-jvm-repository

PACKAGE_VERSION=`grep version gradle.properties | cut -c 9-`

echo "Going to publish version: $PACKAGE_VERSION"

export ORG_GRADLE_PROJECT_mavenCentralUsername="$MAVENCENTRAL_USER"
export ORG_GRADLE_PROJECT_mavenCentralPassword="$MAVENCENTRAL_PASSWORD"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$SIGNING_KEY"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="$SIGNING_KEYID"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="$SIGNING_KEYPASSWORD"

./gradlew publishToMavenCentral

echo "fauna-jvm@$PACKAGE_VERSION has been published. (Can be delayed showing up in maven central.)" > ../slack-message/publish
