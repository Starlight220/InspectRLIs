#!/bin/bash

cd /inspector/
echo "$(pwd -P)"

echo "INPUT_ROOT=$INPUT_ROOT)"
echo "INPUT_VERSIONSCHEME=$INPUT_VERSIONSCHEME)"
echo "INPUT_BASEURL=$INPUT_BASEURL)"
echo "INPUT_LATESTVERSION=$INPUT_LATESTVERSION)"
echo "GITHUB_WORKSPACE=$GITHUB_WORKSPACE)"

echo 'REPORT<<EOF' >> $GITHUB_ENV
OUTPUT="$(/inspector/gradlew run -q)"
echo "$OUTPUT" >> $GITHUB_ENV
echo 'EOF' >> $GITHUB_ENV

echo "$OUTPUT"
