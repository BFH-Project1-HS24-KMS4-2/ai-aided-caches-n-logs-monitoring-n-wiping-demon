# Project 1 AI-Aided Caches-n-Logs Monitoring-n-Wiping Demon

**License**: FLOSS (Free/Libre and Open Source Software)

## Project Overview

The Cache-n-Log Wiper is a platform-independent tool designed to monitor and manage the cache and log files on your
computer. With the help of AI, it analyzes each file to determine its purpose and whether it should be wiped or deleted.
This project aims to help users regain control over their computer's file system and enhance privacy.

## Features

- **Monitoring**: Tracks cache and log files within the file system.
- **AI Integration**: Analyzes files to understand their utility and whether deletion is recommended.
- **File Wiping/Deletion**: Securely wipes or deletes files based on AI recommendations and user approval.

## Installation

As a prerequisite, a Java runtime environment must already be installed on the corresponding device.

### Unix

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
4. If you want to use the inspect feature create an openai API-KEY following these
   instructions: https://platform.openai.com/settings/organization/billing/overview
   after that set the generated API-KEY as environment variable at the end of the `/etc/profile` file:
    ```
    export OPENAI_API_KEY=<generated API-KEY>
    ```
   When you skip this part you won't have access to the inspect feature but the rest will work the same way.
5. If you want that the daemon starts every time automatically in the background when you start your system,
   you can do the following in a terminal:
    ```
   crontab -e
   // Add the following line to the end of the opened file
   @reboot PATH=$JAVA_HOME/bin:$PATH $TRACE_SENTRY_DIR/ts-daemon
    ```
   Then save the file.
6. After a system restart you are ready to use the TraceSentry.

### Windows

1. Browse the latest artifact built by the main branch from this repository and download
   the `target/tracesentry-<version>-submission.zip` to your machine.
2. Unpack the archive to your desired installation directory for the TraceSentry.
3. Open a terminal and set the following environment variables:
    ```
   setx TRACE_SENTRY_DIR <absolute path to installation dir>
   // restart the terminal
   setx PATH %PATH%;%TRACE_SENTRY_DIR%
   ```
4. If you want to use the inspect feature create an openai API-KEY following these
   instructions: https://platform.openai.com/settings/organization/billing/overview
   after that set the generated API-KEY as environment variable as follows in a terminal:
    ```
    setx OPENAI_API_KEY <generated API-KEY>
    ```
   When you skip this part you won't have access to the inspect feature but the rest will work the same way.
5. If you want that the daemon starts every time automatically in the background when you start your system,
   you can do the following in a terminal:
   ```
   copy %TRACE_SENTRY_DIR%\ts-daemon.bat "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup"
   ```
6. Restart the terminal and you are ready to use the TraceSentry.

## Usage

[CLI Usage](./docs/cli.md)

## Technologies Used

- AI APIs _TBD_ ❗
- File System Management
- Java

## Contribution

1. Create a new branch, prefixed with `feat/` (`git checkout -b feat/feature-name`)
2. Commit your changes (`git commit -m 'Add feature'`)
3. Push to the branch (`git push origin feat/feature-name`)
4. Create a merge request

## License

This project is licensed under the FLOSS License.

## Contact

- **Advisor**: Dr. Simon Kramer
