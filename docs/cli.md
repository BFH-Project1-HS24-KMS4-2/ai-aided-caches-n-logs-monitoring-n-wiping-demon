
# CLI Documentation

## Commands
1. [run](#1-run)
2. [search](#2-search)

### 1. `run`
This command starts the daemon process by specifying the path to the JAR file if it is not already running. If the daemon is already running, it will not be restarted.

#### Usage:
```bash
tbd run <path>
```

#### Parameters:
- **`path`**: The full path to the daemon JAR file.  Either the relative path from the current directory or the absolute path can be used.

#### Example:
```bash
tbd run ./path/to/daemon.jar # relative path unix 
tbd run C:\\Path\\To\\Daemon.jar # absolute path windows
```
Success message:
```
Daemon started successfully.
```
Already running message:
```
Daemon is already running.
```
Error message:
```
Failed to start daemon.
```
---

### 2. `search`
This command initiates a search operation in the specified directory. The search mode can be customized, and subdirectory scanning is enabled by default unless the `--no-subdirs` flag is set. Additionally, a regex pattern can be used to filter files when the `pattern` mode is selected.

#### Usage:
```bash
tbd search <path> [--mode <log|cache|full|pattern>] [--pattern <regex>] [--no-subdirs]
```

#### Parameters:
- **`path`**: The full path to the directory you want to search. Either the relative path from the current directory or the absolute path can be used.
- **`--mode`**: Defines the search mode:
  - `log`: Search for log files.
  - `cache`: Search for cache files.
  - `full`: Search for both log and cache files (default).
  - `pattern`: Search for files that match a custom regex pattern (requires `--pattern` parameter).
- **`--pattern`**: If the mode is set to `pattern`, this parameter defines the regular expression to match files.
- **`--no-subdirs`**: If this flag is present, the search will not include subdirectories (default is to search subdirectories).

#### Example:
- Search a directory in full mode (default):
  ```bash
  tbd search /etc/path/to/directory # absolute path unix
  tbd search Path\\To\\Directory # relative path windows
  ```
- Search only for log files without scanning subdirectories:
  ```bash
  tbd search /path/to/directory --mode log --no-subdirs
  ```
- Search using a custom regex pattern:
  ```bash
  tbd search /path/to/directory --mode pattern --pattern ".*\.log$"
  ```
Successful search:
```
Listing 2 files in /path/to/directory...
relative/path/from/search/directory/file1.log
relative/path/from/search/directory/file2.log
```
Error message:
```
Failed to search directory.
```
---

## Notes:
- Ensure that the path you provide is absolute or relative to the current working directory.
- If no mode is specified, the search defaults to `full` mode.
- The `pattern` mode requires the `--pattern` parameter to define the regex for matching files.
