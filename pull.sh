#!/bin/bash
# First Parameter is the branch name .. check that it exists ..
if [ -z "$1" ]; then
    echo "Please provide the branch name as the first parameter"
    exit 1
fi

for val in core tlr inconsistency-detection ; do
    echo "Pushing $val to branch $1"
    # REPO: Replace - with empty string
    git subtree pull --prefix=$val git@github.com:ArDoCo/${val//-/} "$1"
done
