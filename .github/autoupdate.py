#!/usr/bin/env python3
from utils import *
from gradle_versioning import *
from typing import List, Tuple
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

    # Note: For debugging it might be useful to write the result into a file and afterwards
    # load it from that file instead of doing requests which might hit rate limits.
    releases_json = request_json("https://api.github.com/repos/JetBrains/Kotlin/releases")
    # with open(".releases.json", "w") as fp:
    #     fp.write(json.dumps(releases_json))
    # with open(".releases.json", "r") as fp:
    #     releases_json = json.loads(fp.read())

    for release in releases_json:
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

    all_versions.sort(key=lambda x: gradle_version_key(x))
    errors = []
    for release in reversed(releases):
        kotlin_version = get_kotlin_version(release)
        if not has_version(kotlin_version, all_versions):
            # Take the very latest plugin version and the latest plugin version which came before the given Kotlin
            # version. Try building against both and take the first working version.
            # Imagine we have the plugin versions v-2.3.0_1 and v-2.3.20-Beta_1 tagged in the repo.
            # In Kotlin 2.3.20-Beta a breaking API change happened.
            # If Kotlin 2.3.10-Beta gets released afterwards we'll build a new release starting from the revision
            # v-2.3.20-Beta_1, but the breaking API change is not included in 2.3.10-Beta, so the build will fail.
            # Next, we try building from revision v-2.3.0_1 which should be API-compatible with the 2.3.10 series.
            base_versions = all_versions[-1:]
            base_versions += [version for version in all_versions
                              if gradle_version_key(split_base_kotlin_version(version)[0]) < gradle_version_key(kotlin_version)][-1:]
            sub_errors = []
            for base_version in base_versions:
                plugin_version = kotlin_version
                shell(f"git reset --hard")
                shell(f"git clean --ffdx")
                print(f"base_version = {base_version}")
                print(f"plugin_version = {plugin_version}")
                print(f"set_kotlin_version({kotlin_version})")
                set_kotlin_version(kotlin_version)
                shell_extra_env["OVERRIDE_VERSION"] = plugin_version
                try:
                    shell(f"./gradlew assemble --stacktrace")
                    shell(f"./gradlew testAll --stacktrace")
                except KeyboardInterrupt:
                    pass
                except Exception as e:
                    print(f"Error building for version {kotlin_version} from plugin version {base_version}:")
                    traceback.print_exc()
                    sub_errors.append(e)
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
                # Success! We can exit the loop and ignore any sub_errors (don't make the CI fail).
                break
            else:
                # Building has failed for all potential base versions
                errors += sub_errors

    if errors:
        sys.exit(1)

if __name__ == "__main__":
    main()
