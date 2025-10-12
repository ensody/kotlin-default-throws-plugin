#!/usr/bin/env python3
from utils import *
from typing import List
import sys
import traceback


def main():
    all_versions = get_all_versions()

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
            if unstable_count >= max_unstable or \
                any(get_kotlin_version(release).startswith(base_version)
                    for release in releases) or \
                has_version(base_version, all_versions):
                continue
            unstable_count += 1
        releases.append(release)
        if stable_count >= max_stable:
            break

    errors = []
    for release in reversed(releases):
        kotlin_version = get_kotlin_version(release)
        if not has_version(kotlin_version, all_versions):
            is_stable = is_stable_version(kotlin_version)
            # shell("git switch origin/main -C main")
            kotlin_versions_with_plugin = set(version.rsplit(".", 1)[0] for version in all_versions)
            if not is_stable or \
                any(tuple(version.split(".")) > tuple(kotlin_version.split("."))
                    for version in kotlin_versions_with_plugin if is_stable_version(version)):
                shell("git switch --detach")
            set_kotlin_version(kotlin_version)
            latest_version = get_latest_version().rsplit(".", 1)[1]
            plugin_version = f"{kotlin_version}.{latest_version}"
            shell_extra_env["OVERRIDE_VERSION"] = plugin_version
            try:
                shell(f"./gradlew assemble --stacktrace")
                shell(f"./gradlew testAll --stacktrace")
            except KeyboardInterrupt:
                pass
            except Exception as e:
                print(f"Error building for version {kotlin_version}:")
                traceback.print_exc()
                errors.append(e)
                print("")
                continue

            # Pushing a new tag will start a real publication in a separate CI workflow
            shell(f"git add {VERSIONS_PATH}")
            commit(f"Bumped to Kotlin {kotlin_version}", all_files=False)
            git_tag(f"v-{plugin_version}")
            all_versions.append(plugin_version)

            if is_stable:
                git_push()
            else:
                # Preview releases and old stable releases don't get pushed on main
                git_push(tags_only=True)

    if errors:
        sys.exit(1)


def has_version(kotlin_version: str, all_versions: List[str]) -> bool:
    return any(version.startswith(f"{kotlin_version}.") for version in all_versions)

def is_stable_version(version: str) -> bool:
    return "-" not in version and "+" not in version

if __name__ == "__main__":
    main()
