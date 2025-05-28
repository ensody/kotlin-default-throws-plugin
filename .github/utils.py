from pathlib import Path
from subprocess import check_output, check_call
from typing import List
from urllib.request import urlopen, Request
import os, re, json

ROOT = Path(__file__).parent.parent
VERSIONS_PATH = ROOT / "gradle" / "libs.versions.toml"
version_re = re.compile(r'^(kotlin = ")(.*)(")', re.MULTILINE)

shell_extra_env = {}

def shell(*args, **kwargs):
    kwargs["env"] = dict(kwargs.get("env") or {}, **shell_extra_env)
    return check_call(*args, shell=True, **kwargs)

def shell_output(*args, **kwargs):
    kwargs["env"] = dict(kwargs.get("env") or {}, **shell_extra_env)
    return check_output(*args, shell=True, **kwargs).decode()

def request_json(url: str):
    headers = {}
    token = os.environ.get("GITHUB_TOKEN")
    if token:
        print("Using GITHUB_TOKEN")
        headers["Authorization"] = f"Bearer {token}"
    return json.load(urlopen(Request(url, headers=headers)))

def get_kotlin_version(json_data) -> str:
    result = get_version_from_tag(json_data["tag_name"])
    assert result
    return result

def get_version_from_tag(tag: str) -> str:
    return tag.lstrip("v-")

def set_kotlin_version(kotlin_version: str):
    VERSIONS_PATH.write_text(version_re.sub(f"\\g<1>{kotlin_version}\\g<3>", VERSIONS_PATH.read_text()))

def get_all_versions() -> List[str]:
    return [get_version_from_tag(tag) for tag in shell_output(f"git tag --list 'v-*'").strip().splitlines()]

def get_latest_version() -> str:
    return get_version_from_tag(shell_output(f"git describe --tags --abbrev=0 --match 'v-*'").strip())

def commit(message: str, all_files: bool = True):
    if all_files:
        shell(f"git add -A .")
    shell(f"git commit -m '{message}'")

def git_tag(name: str):
    shell(f"git tag '{name}'")

def git_push(tags_only: bool = False):
    if not tags_only:
        shell(f"git push")
    shell(f"git push --tags")
