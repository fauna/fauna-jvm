# Release process

We publish the JVM driver on maven central. Our pipeline is driven by the
repository itself. The runbook below describes the release process.

1. When issuing the latest mainline release, create branch off of `main`.
2. Update [`gradle.properties`](../gradle.properties) to refer to the proper
   version number, and commit with a message "Release $RELEASE_VERSION". Note
   this commit SHA.
3. Increment the version in `gradle.properties` and append -SNAPSHOT. Add this
   as another commit.
4. Submit a release PR.
5. Once approved, set the remote `release` branch to point to the commit
   generated in step 2 by running `git push --force origin $SHA:release`.
6. The following additional manual step is required for now:
   - Find the staged release artifact at https://central.sonatype.com/publishing and click publish
