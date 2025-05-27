#!/usr/bin/env python3
from utils import *
import sys
import traceback

# Collect the last max_stable stable releases and up to max_unstable unstable releases for which there is no
# corresponding stable release yet.
max_stable = 2
max_unstable = 3
releases = []
stable_count = 0
unstable_count = 0
for release in request_json("https://api.github.com/repos/JetBrains/Kotlin/releases"):
    kotlin_version = get_kotlin_version(release)
    base_version = kotlin_version.split("-")[0].split("+")[0]
    is_stable = "-" not in kotlin_version and "+" not in kotlin_version
    if is_stable:
        stable_count += 1
    else:
        if unstable_count >= max_unstable or any(get_kotlin_version(release).startswith(base_version) for release in releases) or has_version_tag(base_version):
            continue
        unstable_count += 1
    releases.append(release)
    if stable_count >= max_stable:
        break

errors = []
for release in reversed(releases):
    kotlin_version = get_kotlin_version(release)
    if not has_version_tag(kotlin_version):
        is_stable = "-" not in kotlin_version and "+" not in kotlin_version
        shell("git switch origin/main -C main")
        if not is_stable:
            shell("git switch --detach")
        set_kotlin_version(kotlin_version)
        latest_version = get_latest_version().rsplit("-plugin.", 1)[1]
        plugin_version = f"{kotlin_version}-plugin.{latest_version}"
        shell_extra_env["OVERRIDE_VERSION"] = plugin_version
        try:
            shell(f"./gradlew assemble --stacktrace")
            shell(f"./gradlew testAll --stacktrace")
        except Exception as e:
            print(f"Error building for version {kotlin_version}:")
            traceback.print_exception(e)
            errors.append(e)
            print("")

        # Pushing a new tag will start a real publication in a separate CI workflow
        shell(f"git add {VERSIONS_PATH}")
        commit(f"Bumped to Kotlin {kotlin_version}", all_files=False)
        tag(f"v-{plugin_version}")

        if is_stable:
            push()
        else:
            # Preview releases don't get pushed on main
            shell(f"git push --tags")

if errors:
    sys.exit(1)
