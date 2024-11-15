#!/bin/bash
if ! netstat -tuln | grep -q ":8087"; then
    echo "Daemon not running starting..."
    java -jar daemon/target/daemon-0.0.1-SNAPSHOT.jar > /dev/null &
fi
java -jar "cli/target/cli-0.0.1-SNAPSHOT.jar" "$@"