#!/bin/bash

set -eou

# For differentiating output files
export LOG_UNIQUE=$(date +%s%3N)

# Install fauna-shell
apt update -qq
apt install -y -qq npm
npm install --silent -g fauna-shell@^2.0.0

cd repo.git

# Run init.sh to setup database schema and initial data
pushd perf-test-setup
./init.sh

# Build solution and run performance tests
popd
./gradlew clean
./gradlew test -i --tests '*PerformanceTest*'

# Test run should output stats.txt, cat it to the slack-message output for display in Slack
echo ":stopwatch: *Perf test results for _<https://github.com/fauna/fauna-jvm|fauna-jvm>_* ($FAUNA_ENVIRONMENT)" > ../slack-message/perf-stats
echo '_(non-query time in milliseconds)_' >> ../slack-message/perf-stats
echo '```' >> ../slack-message/perf-stats
cat ./stats_$LOG_UNIQUE.txt >> ../slack-message/perf-stats
echo '```' >> ../slack-message/perf-stats
echo "_<$CONCOURSE_URL|Concourse job>_" >> ../slack-message/perf-stats

# Run teardown.sh to delete the test collections
pushd perf-test-setup
./teardown.sh
