#!/bin/bash

set -e

./gradlew jar

COMMIT=$(git rev-parse --short HEAD)
echo "Commit: $COMMIT"

REMOTE_PATH="/www/builds/$COMMIT"

ssh host "mkdir -p $REMOTE_PATH"

scp build/libs/plugin.jar host:$REMOTE_PATH/plugin.jar

ssh host "echo $COMMIT > /www/builds/latest.txt"

echo "Done: $REMOTE_PATH (latest = $COMMIT)"
