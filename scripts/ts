#!/bin/bash

cd "$(dirname "$0")" || exit 1

# check if the first argument is empty
if [ $# -eq 0 ]; then
    java -Dlogging.level.root=OFF -Dspring.profiles.active=dev -jar "cli/target/cli-0.0.1-SNAPSHOT.jar"
else
    java -Dlogging.level.root=OFF -jar "cli/target/cli-0.0.1-SNAPSHOT.jar" "$@"
fi
