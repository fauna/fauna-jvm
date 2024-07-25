#!/bin/sh

export ORG_GRADLE_PROJECT_mavenCentralUsername="$MAVENCENTRAL_USER"
export ORG_GRADLE_PROJECT_mavenCentralPassword="$MAVENCENTRAL_PASSWORD"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$SIGNING_KEY"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="$SIGNING_KEYID"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="$SIGNING_KEYPASSWORD"

./gradlew publishToMavenCentral
