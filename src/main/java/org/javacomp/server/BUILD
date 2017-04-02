package(
    default_visibility = [":subpackages"],
)

package_group(
    name = "subpackages",
    packages = [
        "//src/main/java/org/javacomp/server/...",
        "//src/test/java/org/javacomp/server/...",
    ],
)

java_library(
    name = "server",
    srcs = ["Server.java"],
)

java_library(
    name = "serverimpl",
    srcs = glob(
        ["*.java"],
        exclude = [
            "ErrorCode.java",
            "GsonUtils.java",
            "Request.java",
            "RequestException.java",
            "Response.java",
            "Server.java",
        ],
    ),
    deps = [
        ":errorcode",
        ":gsonutils",
        ":request",
        ":requestexception",
        ":response",
        ":server",
        "//external:guava",
        "//external:jsr305",
        "//src/main/java/org/javacomp/logging",
        "//src/main/java/org/javacomp/server/io",
        "//src/main/java/org/javacomp/server/protocol",
        "//third_party:auto_value",
        "//third_party:gson",
    ],
)

java_library(
    name = "errorcode",
    srcs = ["ErrorCode.java"],
)

java_library(
    name = "gsonutils",
    srcs = ["GsonUtils.java"],
    deps = [
        "//third_party:gson",
    ],
)

java_library(
    name = "request",
    srcs = ["Request.java"],
    deps = [
        "//external:guava",
        "//external:jsr305",
        "//src/main/java/org/javacomp/server/protocol:nullparams",
        "//src/main/java/org/javacomp/server/protocol:requestparams",
        "//third_party:auto_value",
    ],
)

java_library(
    name = "requestexception",
    srcs = ["RequestException.java"],
    deps = [
        ":errorcode",
    ],
)

java_library(
    name = "response",
    srcs = ["Response.java"],
    deps = [
        ":errorcode",
        "//external:guava",
        "//external:jsr305",
    ],
)

java_binary(
    name = "JavaComp",
    main_class = "org.javacomp.server.JavaComp",
    runtime_deps = [":serverimpl"],
)