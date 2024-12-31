# Project 1 AI-Aided Caches-n-Logs Monitoring-n-Wiping Demon

## Project Overview

The Cache-n-Log Wiper is a platform-independent tool designed to monitor and manage the cache and log files on your
computer. With the help of AI, it analyzes each file to determine its purpose and whether it should be wiped or deleted.
This project aims to help users regain control over their computer's file system and enhance privacy.

## Features

- **Search**: Allows users to search for log/cache-files or specific files with a given pattern within the file system.
- **Monitoring**: Tracks cache and log files within the file system.
- **AI Integration**: Analyzes files to understand their utility and whether deletion is recommended.
- **File Wiping/Deletion**: Securely wipes or deletes files based on AI recommendations and user approval.

## Installation

As a prerequisite, a Java runtime environment must already be installed on the corresponding device.

### Linux

1. Browse the latest artifact built by the main branch from this repository and download
   the `target/tracesentry-<version>-submission.zip` to your machine.
2. Unpack the archive to your desired installation directory for the TraceSentry.
3. Define the following environment variables in the `/etc/environment` file:
    ```
   JAVA_HOME=<absolute path to jdk folder>
   TRACE_SENTRY_DIR=<absolute path to installation dir>
   ```
   And add the `TRACE_SENTRY_DIR` variable to the `PATH` at the end of the `/etc/profile` file:
   ```
   PATH=$PATH:$TRACE_SENTRY_DIR
   ```
4. If you want to use the inspection feature, create an OpenAI API key by following the instructions
   at [OpenAI API keys](https://platform.openai.com/settings/organization/api-keys).
   Add credit to your account to use the OpenAI
   API: [OpenAI billing overview](https://platform.openai.com/settings/organization/billing/overview).
   Then set the generated API key as an environment variable at the end of the file `/etc/profile`:
    ```
    export OPENAI_API_KEY=<generated API-KEY>
    ```
   If you skip this part, you won't have access to the inspection feature.
5. If you want that the daemon starts every time automatically in the background when you start your system,
   you can do the following in a terminal:
    ```
   crontab -e
   // Add the following line to the end of the opened file
   @reboot PATH=$JAVA_HOME/bin:$PATH $TRACE_SENTRY_DIR/ts run
    ```
   Then save the file.
6. After a system restart you are ready to use TraceSentry.

### Windows

1. Browse the latest artifact built by the main branch from this repository and download
   the `target/tracesentry-<version>-submission.zip` to your machine.
2. Extract the archive to your desired installation directory for TraceSentry.
3. Set the environment variable `TRACE_SENTRY_DIR` via the Control Panel or via PowerShell:
    ```powershell
    [System.Environment]::SetEnvironmentVariable("TRACE_SENTRY_DIR", "<absolute path to the installation directory>", "User")
    ```
4. To ensure the CLI works correctly, add the installation directory to the `PATH`:
    ```powershell
    $currentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
    [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;<absolute path to the installation directory>", "User")
    ```
5. If you want to use the inspection feature, create an OpenAI API key by following the instructions
   at [OpenAI API keys](https://platform.openai.com/settings/organization/api-keys).
   Add credit to your account to use the OpenAI
   API: [OpenAI billing overview](https://platform.openai.com/settings/organization/billing/overview).
   Then set the generated API key as an environment variable via PowerShell:
    ```powershell
    [System.Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "<generated API key>", "User")
    ```
   If you skip this step, you will not have access to the inspection feature.
6. If you want the daemon to start automatically in the background at system startup, copy the file `ts-daemon.bat` from
   the installation directory to the Startup folder:
    ```powershell
    Copy-Item "$env:TRACE_SENTRY_DIR\ts-daemon.bat" "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup"
    ```
   On the next system startup, the daemon will start automatically.
   The first time you start it, a security warning may appear, in which you will need to uncheck a checkbox to prevent
   it from appearing again in the future.
7. Restart the terminal, and you are ready to use TraceSentry.

### macOS

For simplicity, only configurations in the .zprofile file are used here. The following commands write the configurations
directly to the file.

1. Browse the latest artifact built by the main branch from this repository and download
   the `target/tracesentry-<version>-submission.zip` to your machine.
2. Unpack the archive to your desired installation directory for the TraceSentry.
3. Set the following environment variables in the `~/.zprofile` file:
    ```
   echo '\n# Added for TraceSentry' >> ~/.zprofile
   echo export TRACE_SENTRY_DIR="<absolute path to installation dir>" >> ~/.zprofile
   echo export PATH="\$TRACE_SENTRY_DIR:\$PATH" >> ~/.zprofile
   ```
4. If you want to use the inspection feature, create an OpenAI API key by following the instructions
   at [OpenAI API keys](https://platform.openai.com/settings/organization/api-keys).
   Add credit to your account to use the OpenAI
   API: [OpenAI billing overview](https://platform.openai.com/settings/organization/billing/overview).
   The following command will add the API-KEY to the `~/.zprofile` file:
    ```
    echo export OPENAI_API_KEY=<generated API-KEY> >> ~/.zprofile
    ```
   When you skip this part you won't have access to the inspect feature but the rest will work the same way.
5. If you want that the daemon starts every time automatically in the background when you start your system,
   adjust your `~/.zprofile` file as follows:
   ```
   echo ts run >> ~/.zprofile
   ```
   This is recommended as the most pragmatic solution. Alternatively, you can also use launchd.
6. Restart the terminal. If you followed step 5, the daemon will start automatically. You are now ready to use
   TraceSentry.

## Usage

[CLI Usage](./docs/cli.md)

## Technologies Used

- OpenAI GPT-4o mini
- Java/Spring/Maven
- SQLite/H2

## Contribution

1. Create a new branch, prefixed with `feat/` (`git checkout -b feat/feature-name`)
2. Commit your changes (`git commit -m 'Add feature'`)
3. Push to the branch (`git push origin feat/feature-name`)
4. Create a merge request

## License

This project is licensed under the MIT License: [LICENSE](./LICENSE)

## Contact

- **Advisor**: Dr. Simon Kramer
