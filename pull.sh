#!/bin/bash
# Pull changes from subdirectory repositories into the monorepo
# First Parameter is the branch name .. check that it exists ..
if [ -z "$1" ]; then
    echo "Please provide the branch name as the first parameter"
    exit 1
fi

BRANCH="$1"
REPOS=("core" "tlr" "inconsistency-detection")
BASE_URL="git@github.com:ardoco"
TMP_WORKTREE_DIR=".git/worktrees/pull-temp"

set -e  # Exit on error

for REPO in "${REPOS[@]}"; do
    REPO_URL="$BASE_URL/$REPO.git"
    WORKTREE_PATH="$TMP_WORKTREE_DIR/$REPO"
    
    echo "Pulling $REPO from branch $BRANCH..."
    
    # Create a temporary worktree cloned from the repo
    rm -rf "$WORKTREE_PATH" 2>/dev/null || true
    git clone --branch "$BRANCH" --single-branch "$REPO_URL" "$WORKTREE_PATH" 2>/dev/null || {
        git clone --single-branch "$REPO_URL" "$WORKTREE_PATH"
        cd "$WORKTREE_PATH"
        git checkout -b "$BRANCH" || git checkout "$BRANCH"
        cd - > /dev/null
    }
    
    # Remove the old directory contents and copy new ones
    rm -rf "$REPO"/*
    cp -r "$WORKTREE_PATH/"* "$REPO/" 2>/dev/null || true
    
    # Copy hidden files/dirs except .git
    for item in $(cd "$WORKTREE_PATH" && find . -maxdepth 1 -name '.*' ! -name '.git' -type f -o -name '.*' ! -name '.git' -type d 2>/dev/null); do
        [ "$item" != "." ] && [ "$item" != ".." ] && cp -r "$WORKTREE_PATH/$item" "$REPO/" 2>/dev/null || true
    done
    
    # Clean up
    rm -rf "$WORKTREE_PATH"
done

echo "Successfully pulled all repositories from branch $BRANCH"
echo "Staged changes in: ${REPOS[*]}"
echo "Use 'git add' and 'git commit' to finalize the pull"
