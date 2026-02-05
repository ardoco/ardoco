#!/bin/bash
# Push subdirectories to their respective repositories
# First Parameter is the branch name .. check that it exists ..
if [ -z "$1" ]; then
    echo "Please provide the branch name as the first parameter"
    exit 1
fi

BRANCH="$1"
REPOS=("core" "tlr" "inconsistency-detection")
BASE_URL="git@github.com:ardoco"
TMP_WORKTREE_DIR=".git/worktrees/push-temp"
MONOREPO_ROOT="$(pwd)"

set -e  # Exit on error

for REPO in "${REPOS[@]}"; do
    REPO_URL="$BASE_URL/$REPO.git"
    WORKTREE_PATH="$TMP_WORKTREE_DIR/$REPO"
    
    echo "Pushing $REPO to branch $BRANCH..."
    
    # Clean up any existing worktree from previous runs
    git worktree remove --force "$WORKTREE_PATH" 2>/dev/null || true
    
    # Create a temporary worktree for this repo
    git worktree add --detach "$WORKTREE_PATH" HEAD
    
    cd "$WORKTREE_PATH"
    
    # Clean the working directory to avoid conflicts when checking out
    git clean -fd
    git reset --hard
    
    # Configure git for this worktree
    git config user.email "$(git -C ../.. config user.email)"
    git config user.name "$(git -C ../.. config user.name)"
    
    # Add remote if not exists and fetch the target branch
    git remote add "$REPO" "$REPO_URL" 2>/dev/null || git remote set-url "$REPO" "$REPO_URL"
    git fetch "$REPO" 2>/dev/null || true
    
    # Delete any existing temporary branch from previous runs
    git branch -D "tmp-push-$REPO-$BRANCH" 2>/dev/null || true
    
    # Check if the remote branch exists, if not create from HEAD
    if git rev-parse --verify "$REPO/$BRANCH" >/dev/null 2>&1; then
        # Remote branch exists, check it out and track it
        git checkout --track -b "tmp-push-$REPO-$BRANCH" "$REPO/$BRANCH"
    else
        # Remote branch doesn't exist, create new branch
        git checkout -b "tmp-push-$REPO-$BRANCH"
    fi
    
    # Clear the working directory (except .git)
    find . -mindepth 1 -maxdepth 1 ! -name '.git' -exec rm -rf {} + 2>/dev/null || true
    
    # Copy fresh contents from the monorepo subdirectory
    cp -r "$MONOREPO_ROOT/$REPO/"* . 2>/dev/null || true
    
    # Also copy hidden files and directories (except .git)
    for item in $(cd "$MONOREPO_ROOT/$REPO" && find . -maxdepth 1 -name '.*' ! -name '.git' 2>/dev/null); do
        cp -r "$MONOREPO_ROOT/$REPO/$item" . 2>/dev/null || true
    done
    
    # Stage all changes
    git add -A
    
    # Create a single commit with all changes
    git commit -m "Sync from monorepo - $BRANCH" || echo "No changes to commit for $REPO"
    
    # Push to the repository (simple push, no force)
    git push "$REPO" "tmp-push-$REPO-$BRANCH:$BRANCH" -u
    
    cd - > /dev/null
    
    # Clean up worktree
    git worktree remove --force "$WORKTREE_PATH" 2>/dev/null || true
done

echo "Successfully pushed all repositories to branch $BRANCH"
