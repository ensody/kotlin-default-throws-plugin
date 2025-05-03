#!/bin/sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

cd "$ROOT/.."
./gradlew podspec generateDummyFramework

cd "$ROOT"

if ! command -v xcodegen &> /dev/null; then
    echo "XcodeGen not found. Installing..."
    brew install xcodegen
fi

export PROJECT_NAME="$(basename "$ROOT")"
rm -rf Pods Podfile.lock *.xcodeproj *.xcworkspace
xcodegen generate --use-cache
pod install
