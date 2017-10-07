[![Build Status](https://travis-ci.org/tigersoldier/JavaComp.svg)](https://travis-ci.org/tigersoldier/JavaComp)

# JavaComp - a Java Language Server

JavaComp implements [Language Server Protocol][lang-server] for Java language.
It provides language editing features such as code completion, showing method
signatures and jumping to definitions to editors that support the protocol.

## Features

### Code completion

![Code completion screencast][completion-screencast]

### Go to definition

![Go to definition screencast][definition-screencast]

### Symbol documentation

![Symbol documentation screencast][hover-screencast]


## How to install

See the installation guide for your editor:

 * Emacs: [javacomp-el][javacomp-el]
 * Visual Studio Code: [javacomp-vscode][javacomp-vscode]

## Customization

You can put a `javacomp.json` file under the project root directory to customize
the behavior of JavaComp.

Example `javacomp.json` file content:

```json
{
  "logPath": "/tmp/javacomp.log",
  "logLevel": "fine",
  "typeIndexFiles": ["typeindeces/guava.json"],
  "ignorePaths": ["*.bak", ".*"]
}
```

The schema of the JSON is defined by the `InitializationOptions` class in
[InitializeParams.java][InitializeParams.java]. The `initializationOptions`
field of the [`initialize` Request][initialize-request] accepts the same format
as `javacomp.json`. Editor plugins can set `initializationOptions` to customize
JavaComp. If both `javacomp.json` and `initializationOptions` exist,
`initializationOptions` overrides `javacomp.json`.

Below are supported options. The source of truth of the supported options is the

### logPath

(String) Path of the log file. If not set, logs are not written to any file.

### logLevel

(String) The minimum log level. Logs with the level and above will be logged.

Possible values are: `severe`, `warning`, `info`, `fine`, `finer`, `finest`.

### ignorePaths

(String) Pattern of paths that JavaComp should ignore.

The patterns are valid Java path glob patterns defined by the documentation of
`java.nio.file.FileSystem#getPathMatcher` without `glob:` prefix.
     
When determing whether a path of a file or directory should be ignored, the path
is converted into 2 froms: the filename and pseudo absolute path. A pseudo
absolute path is the path without the prefix of project root path. For example,
if a path is `/root/path/foo/bar` and the project root path is `/root/path`, its
pseudo absolute path is `/foo/bar`. If a path is not under project root, its
pseudo absolute path is itself.
     
Both filename of the path and its pseudo absolute path are checked by all ignore path
patterns. If either form matches any of the patterns, the path is ignored.

### typeIndexFiles

(Array of strings) A list of file paths that contains additional type indeces
generated by the Indexer tool.

The path can be either relative to the project root path, or an absolute path.

## Documentation

See our [Wiki page][javacomp-wiki].

[lang-server]: https://github.com/Microsoft/language-server-protocol
[initialize-request]: https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#initialize-request
[InitializeParams.java]: https://github.com/tigersoldier/JavaComp/blob/develop/src/main/java/org/javacomp/server/protocol/InitializeParams.java
[javacomp-el]: https://github.com/tigersoldier/javacomp-el
[javacomp-vscode]: https://github.com/tigersoldier/javacomp-vscode
[javacomp-wiki]: https://github.com/tigersoldier/JavaComp/wiki
[completion-screencast]: https://user-images.githubusercontent.com/226229/31046835-b3a36ba8-a632-11e7-8cd5-d53da7c6a638.gif
[definition-screencast]: https://user-images.githubusercontent.com/226229/31046836-b3c26166-a632-11e7-8601-8c4f368f3f37.gif
[hover-screencast]: https://user-images.githubusercontent.com/226229/31046837-b3c2f220-a632-11e7-84c1-d04260a9ac3d.gif
