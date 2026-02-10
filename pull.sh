#!/bin/bash
# Pull changes from subdirectory repositories into the monorepo
if [ -z "$1" ]; then
    echo "Please provide the branch name as the first parameter"
    exit 1
fi

BRANCH="$1"
REPOS=("core" "tlr" "inconsistency-detection")
BASE_URL="git@github.com:ardoco"
TMP_DIR="/tmp/ardoco-pull-$$"

trap "rm -rf '$TMP_DIR'" EXIT
mkdir -p "$TMP_DIR"

for REPO in "${REPOS[@]}"; do
    echo "Pulling $REPO from branch $BRANCH..."
    
    REPO_URL="$BASE_URL/$REPO.git"
    REPO_PATH="$TMP_DIR/$REPO"
    
    # Clone the repository
    git clone --branch "$BRANCH" "$REPO_URL" "$REPO_PATH" 2>/dev/null || {
        git clone "$REPO_URL" "$REPO_PATH"
        cd "$REPO_PATH"
        git checkout "$BRANCH" 2>/dev/null || git checkout -b "$BRANCH"
        cd - > /dev/null
    }
    
    # Delete everything except .git in monorepo subdirectory
    find "$REPO" ! -path "$REPO/.git*" -type f -delete 2>/dev/null || true
    find "$REPO" ! -path "$REPO/.git*" -type d -empty -delete 2>/dev/null || true
    
    # Copy content from cloned repo to monorepo
    rsync -av --delete --exclude='.git' "$REPO_PATH/" "$REPO/"
done

rm -rf "$TMP_DIR"
echo "Done! Use 'git add' and 'git commit' to finalize the pull"
