from pathlib import Path
from subprocess import check_output, check_call
from typing import List
from urllib.request import urlopen, Request
from utils import *
import os, re, json

SPECIAL_PARTS = {
    "dev": -1, # dev is smaller than any other non-numeric part, even non-special parts
    "rc": 1,
    "snapshot": 2,
    "final": 3,
    "ga": 4,
    "release": 5,
    "sp": 6,
}

TOKEN_RE = re.compile(r"[0-9]+|[a-zA-Z]+")

def gradle_version_key(version: str):
    key = []
    for token in TOKEN_RE.findall(version):
        if token.isdigit():
            # Numeric
            key.append((1, int(token)))
        else:
            # Non-numeric
            # Special non-numeric parts get a rank != 0 and they're case-insensitive
            rank = SPECIAL_PARTS.get(token, 0)
            if rank != 0:
                token = token.lower()
            key.append((0, rank, token))
    return tuple(key)
