#!/usr/bin/env python3
from utils import *

kotlin_version = get_kotlin_version(request_json("https://api.github.com/repos/JetBrains/Kotlin/releases")[0])
if not has_version_tag(kotlin_version):
    set_kotlin_version(kotlin_version)
    latest_version = get_latest_version().rsplit("-plugin.", 1)[1]
    plugin_version = f"{kotlin_version}-plugin.{latest_version}"
    shell_extra_env["OVERRIDE_VERSION"] = plugin_version
    shell(f"./gradlew assemble --stacktrace")
    shell(f"./gradlew testAll --stacktrace")

    # Pushing a new tag will start a real publication in a separate CI workflow
    shell(f"git add {VERSIONS_PATH}")
    commit(f"Bumped to Kotlin {kotlin_version}", all_files=False)
    tag(f"v-{plugin_version}")

    if "-" in kotlin_version or "+" in kotlin_version:
        # Preview releases don't get pushed on main
        shell(f"git push --tags")
    else:
        push()
