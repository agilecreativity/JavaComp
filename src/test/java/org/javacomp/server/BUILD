java_library(
    name = "tests",
    testonly = 1,
    srcs = glob(["*.java"]),
    data = glob(["testdata/*"]),
    deps = [
        "//src/main/java/org/javacomp/protocol",
        "//src/main/java/org/javacomp/server:errorcode",
        "//src/main/java/org/javacomp/server:gsonutils",
        "//src/main/java/org/javacomp/server:request",
        "//src/main/java/org/javacomp/server:requestexception",
        "//src/main/java/org/javacomp/server:response",
        "//src/main/java/org/javacomp/server:serverimpl",
        "//src/main/java/org/javacomp/server/handler",
        "//src/main/java/org/javacomp/server/io",
        "//third_party:gson",
        "//third_party:guava",
        "//third_party:jsr305",
        "//third_party:junit4",
        "//third_party:mockito",
        "//third_party:truth",
    ],
)

java_test(
    name = "GsonUtilsTest",
    size = "small",
    runtime_deps = [
        ":tests",
    ],
)

java_test(
    name = "RequestParserTest",
    size = "small",
    runtime_deps = [
        ":tests",
    ],
)

java_test(
    name = "RequestDispatcherTest",
    size = "small",
    runtime_deps = [
        ":tests",
    ],
)
