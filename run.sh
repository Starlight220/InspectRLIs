#!/bin/bash

cd /inspect_rli/
echo "$(pwd -P)"

echo "INPUT_ROOT=$($INPUT_ROOT)"
echo "INPUT_VERSIONSCHEME=$($INPUT_VERSIONSCHEME)"
echo "INPUT_BASEURL=$($INPUT_BASEURL)"
echo "INPUT_LATESTVERSION=$($INPUT_LATESTVERSION)"
echo "GITHUB_WORKSPACE=$($GITHUB_WORKSPACE)"

/inspect_rli/gradlew run
