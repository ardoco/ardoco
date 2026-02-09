#!/bin/bash
# Push subdirectories to their respective repositories
if [ -z "$1" ]; then
    echo "Please provide the branch name as the first parameter"
    exit 1
fi

BRANCH="$1"
REPOS=("core" "tlr" "inconsistency-detection")
BASE_URL="git@github.com:ardoco"
MONOREPO_ROOT="$(pwd)"
TMP_DIR="/tmp/ardoco-push-$$"

trap "rm -rf '$TMP_DIR'" EXIT
mkdir -p "$TMP_DIR"

for REPO in "${REPOS[@]}"; do
    echo "Pushing $REPO to branch $BRANCH..."
    
    REPO_URL="$BASE_URL/$REPO.git"
    REPO_PATH="$TMP_DIR/$REPO"
    
    # Clone the repository
    git clone --branch "$BRANCH" "$REPO_URL" "$REPO_PATH" 2>/dev/null || {
        git clone "$REPO_URL" "$REPO_PATH"
        cd "$REPO_PATH"
        git checkout "$BRANCH" 2>/dev/null || git checkout -b "$BRANCH"
        cd - > /dev/null
    }
    
    # Clean the repo using git (handles case sensitivity properly)
    cd "$REPO_PATH"
    GIT_PAGER=cat git rm -r --quiet . 2>/dev/null || true
    cd - > /dev/null
    
    # Copy content from monorepo
    rsync -av --exclude='.git' "$MONOREPO_ROOT/$REPO/" "$REPO_PATH/"
    
    # Commit and push
    cd "$REPO_PATH"
    git config user.email "$(git -C "$MONOREPO_ROOT" config user.email)"
    git config user.name "$(git -C "$MONOREPO_ROOT" config user.name)"
    git config core.ignorecase false
    git add -A
    git diff --cached --name-only
    GIT_PAGER=cat git commit -m "Sync from monorepo - $BRANCH" || echo "No changes for $REPO"
    git push origin "$BRANCH" -u || echo "Failed to push $REPO"
    cd - > /dev/null
done

rm -rf "$TMP_DIR"
echo "Done!"
