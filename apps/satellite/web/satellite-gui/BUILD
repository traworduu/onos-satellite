"""
 Copyright 2019-present Open Networking Foundation

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
"""

"""
    Rules to build the satellite for GUI 2


"""

load("//tools/build/bazel:jdk_genrule.bzl", genrule = "jdk_genrule")

COMPILE_DEPS = CORE_DEPS + JACKSON + KRYO + [
    "@javax_ws_rs_api//jar",
    "@servlet_api//jar",
    "@jetty_websocket//jar",
    "@jetty_util//jar",
    "@jersey_media_multipart//jar",
    "@jersey_server//jar",
    "//utils/rest:onlab-rest",
    "//core/store/serializers:onos-core-serializers",
]

TEST_DEPS = TEST + [
    "//core/api:onos-api-tests",
    "//drivers/default:onos-drivers-default",
]

"""
    Files that get put at the top level of the tar ball
"""

filegroup(
    name = "_root_level_files",
    srcs =
        [
            ":angular.json",
            ":package.json",
            ":package-lock.json",
            ":tsconfig.json",
            ":tslint.json",
        ],
)

filegroup(
    name = "_e2e_test_files",
    srcs = [
        ":e2e/protractor.conf.js",
        ":e2e/src/app.e2e-spec.ts",
        ":e2e/src/app.po.ts",
        ":e2e/tsconfig.e2e.json",
    ],
)

"""
    Run ng build to create satellite GUI 2 library in production mode
    The output file satellite-gui-lib-ver.tgz is in the form that can be uploaded directly to https://www.npmjs.com/
    See bazel-genfiles/apps/satellite/web/satellite-gui/satellite-gui-lib.log for details of the Angular CLI output
"""

genrule(
    name = "satellite-gui-lib-build",
    srcs = [
        "@nodejs//:bin/npm",
        "@nodejs//:bin/node",
        "@nodejs//:bin/nodejs/bin/node",
        "@nodejs//:bin/nodejs/bin/npm",
        "//web/gui2-fw-lib:onos-gui2-fw-npm-install",
        "//web/gui2-fw-lib:onos-gui2-fw-ng-build",
        "//web/gui2-fw-lib:gui2_fw_lib_ext_css",
        ":_root_level_files",
        ":_satellite_lib_src",
        ":_satellite_tester_files",
    ],
    outs = [
        "satellite-gui-lib.log",
        "satellite-gui-lib-ver.tgz",
    ],
    cmd = "ROOT=`pwd` &&" +
          " export HOME=. &&" +
          " export XDG_CONFIG_HOME=$(@D)/config &&" +
          " NODE=$(location @nodejs//:bin/node) &&" +
          " INSTALL_FILES=($(locations //web/gui2-fw-lib:onos-gui2-fw-npm-install)) &&" +  # An array of filenames - sorted by time created
          " FWLIB_FILES=($(locations //web/gui2-fw-lib:onos-gui2-fw-ng-build)) &&" +  # An array of filenames - sorted by time created
          " mkdir -p apps/satellite/web/satellite-gui &&" +
          " cd apps/satellite/web/satellite-gui &&" +
          " jar xf $$ROOT/$${INSTALL_FILES[0]} &&" +
          " tar xf $$ROOT/$${FWLIB_FILES[0]} &&" +
          " mv package/ node_modules/gui2-fw-lib/ &&" +
          " mkdir -p projects/satellite-gui-lib/fw &&" +
          " (cd projects/satellite-gui-lib/fw &&" +
          "  jar xf $$ROOT/$(location //web/gui2-fw-lib:gui2_fw_lib_ext_css)) &&" +
          " chmod +x node_modules/@angular/cli/bin/ng &&" +
          " export PATH=$$ROOT/$$(dirname $${NODE}):$$ROOT/apps/satellite/web/satellite-gui/node_modules/@angular/cli/bin:$$PATH &&" +
          " ng build satellite-gui-lib >> $$ROOT/$(location satellite-gui-lib.log) 2>&1 ||" +
          " if [ $$? -eq 0 ]; then echo 'Successfully ran build';" +
          " else " +
          "   echo 'Error running \'ng build satellite-gui-lib\' on \'//apps/satellite/web/satellite-gui:satellite-gui-lib-build\'. \\\n" +
          "     See bazel-genfiles/apps/satellite/web/satellite-gui/satellite-gui-lib.log for more details' >&2;" +
          #"   tail -n 100 ../../../../$(location onos-gui2-ng-test.log) >&2;" +
          "   exit 1;" +
          " fi;" +
          " cp README.md LICENSE dist/satellite-gui-lib && " +
          " npm pack ./dist/satellite-gui-lib >> $$ROOT/$(location satellite-gui-lib.log) 2>&1 &&" +
          " mv satellite-gui-lib-*.tgz $$ROOT/$(location satellite-gui-lib-ver.tgz) &&" +
          " touch $$ROOT/$(location satellite-gui-lib.log)",  # to get the log always as the 2nd file,
    message = "Angular satellite Lib build",
    visibility = ["//visibility:public"],
)

"""
    Run 'ng test' to run Angular test and 'ng lint' for checkstyle
    See bazel-genfiles/apps/satellite/web/satellite-gui/satellite-gui-lib-lint.log or
    bazel-genfiles/apps/satellite/web/satellite-gui/satellite-gui-lib-test.log for details of the Angular CLI output
"""

genrule(
    name = "_satellite-gui-lib-test-genrule",
    srcs = [
        "@nodejs//:bin/npm",
        "@nodejs//:bin/node",
        "@nodejs//:bin/nodejs/bin/node",
        "@nodejs//:bin/nodejs/bin/npm",
        "//web/gui2-fw-lib:onos-gui2-fw-npm-install",
        "//web/gui2-fw-lib:onos-gui2-fw-ng-build",
        "//web/gui2-fw-lib:gui2_fw_lib_ext_css",
        ":_root_level_files",
        ":_satellite_lib_src",
        ":_satellite_lib_src_tests",
    ],
    outs = [
        "satellite-gui-lib-ver.log",
        "satellite-gui-lib-lint.log",
        "satellite-gui-lib-test.log",
    ],
    cmd = " ROOT=`pwd` &&" +
          " export HOME=. &&" +
          " export XDG_CONFIG_HOME=$(@D)/config &&" +
          " NODE=$(location @nodejs//:bin/node) &&" +
          " INSTALL_FILES=($(locations //web/gui2-fw-lib:onos-gui2-fw-npm-install)) &&" +  # An array of filenames - sorted by time created
          " FWLIB_FILES=($(locations //web/gui2-fw-lib:onos-gui2-fw-ng-build)) &&" +  # An array of filenames - sorted by time created
          " mkdir -p apps/satellite/web/satellite-gui &&" +
          " cd apps/satellite/web/satellite-gui &&" +
          " jar xf ../../../../$(location :_satellite_lib_src_tests) &&" +
          " jar xf $$ROOT/$${INSTALL_FILES[0]} &&" +
          " tar xf $$ROOT/$${FWLIB_FILES[0]} &&" +
          " mv package/ node_modules/gui2-fw-lib/ &&" +
          " mkdir -p projects/satellite-gui-lib/fw &&" +
          " (cd projects/satellite-gui-lib/fw &&" +
          "  jar xf $$ROOT/$(location //web/gui2-fw-lib:gui2_fw_lib_ext_css)) &&" +
          " chmod +x $$ROOT/apps/satellite/web/satellite-gui/node_modules/@angular/cli/bin/ng &&" +
          " export PATH=$$ROOT/$$(dirname $${NODE}):node_modules/@angular/cli/bin:$$PATH &&" +
          " node -v > ../../../../$(location satellite-gui-lib-ver.log) &&" +
          " npm -v >> ../../../../$(location satellite-gui-lib-ver.log) &&" +
          " ng version >> ../../../../$(location satellite-gui-lib-ver.log) &&" +
          " ng lint satellite-gui-lib > ../../../../$(location satellite-gui-lib-lint.log)  2>&1 ||" +
          " if [ $$? -eq 0 ]; then echo 'Successfully ran lint';" +
          " else " +
          "   echo 'Error running \'ng lint\' on \'//apps/satellite/web/satellite-gui:_satellite-gui-lib-test-genrule\'. \\\n" +
          "     See bazel-genfiles/apps/satellite/web/satellite-gui/satellite-gui-lib-lint.log for more details' >&2;" +
          "   exit 1;" +
          " fi;" +
          " echo -e 'Skipping unit tests temporarily for satellite-gui-lib' > ../../../../$(location satellite-gui-lib-test.log);",
    message = "Angular Satellite Lib lint and test",
)

"""
    Make a group of all the webapp files.
"""

filegroup(
    name = "_satellite_lib_src",
    srcs = glob(
        [
            "projects/satellite-gui-lib/**/*",
            "README.md",
            "LICENSE",
        ],
        exclude = [
            "projects/satellite-gui-lib/**/*.spec.*",  # Don't track tests here
            "projects/satellite-gui-lib/karma.conf.js",
            "projects/satellite-gui-lib/src/test.ts",
            "projects/satellite-gui-lib/fw/**/*",
        ],
    ),
)

"""
    Make a group of all the webapp qpp files.
"""

filegroup(
    name = "_satellite_tester_files",
    srcs = glob(
        [
            "src/**/*",
        ],
    ),
)

"""
    Make a jar file of all the webapp test (*.spec.ts) files.
"""

genrule(
    name = "_satellite_lib_src_tests",
    srcs = glob(
        [
            "projects/satellite-gui-lib/karma.conf.js",
            "projects/satellite-gui-lib/src/test.ts",
            "projects/satellite-gui-lib/tsconfig.spec.json",
            "projects/satellite-gui-lib/**/*.spec.ts",
        ],
        exclude = [
            "projects/satellite-gui-lib/ng-package.json",
            "projects/satellite-gui-lib/ng-package.prod.json",
            "projects/satellite-gui-lib/package.json",
            "projects/satellite-gui-lib/tsconfig.lib.json",
            "projects/satellite-gui-lib/tslint.json",
            "projects/satellite-gui-lib/src/public_api.ts",
        ],
    ),
    outs = ["satellite_gui_lib_src_tests.jar"],
    cmd = "cd apps/satellite/web/satellite-gui &&" +
          " jar Mcf ../../../../$@ .",
)

"""
    Make a jar file of all the webapp test (*.spec.ts) files.
"""

genrule(
    name = "_satellite_gui_lib_tests",
    srcs = glob(
        [
            "projects/satellite-gui-lib/**/*.spec.ts",
            "projects/satellite-gui-lib/**/*.spec.json",
        ],
        exclude = [
            "src/main/webapp/tests/**",
            "src/main/webapp/node_modules/**",
            "src/main/webapp/dist/**",
            "src/main/webapp/doc/**",
        ],
    ),
    outs = ["satellite_gui_lib_tests.jar"],
    cmd = "cd apps/satellite/web/satellite-gui &&" +
          " find projects/satellite-gui-lib/src/lib -type f -exec touch -t 201808280000 {} \; &&" +
          " jar Mcf ../../../../$@ projects/satellite-gui-lib/src/lib",
)

"""
    Wrap the genrule for testing in a test
"""

sh_test(
    name = "satellite-gui-lib-tests",
    size = "small",
    srcs = [
        ":ng-test.sh",
    ],
    data = [
        ":_satellite-gui-lib-test-genrule",
    ],
    deps = [
        "@bazel_tools//tools/bash/runfiles",
    ],
)
