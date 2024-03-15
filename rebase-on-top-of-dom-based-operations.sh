#!/bin/bash

initialCommit = $(git log --grep 'Allow resolving compilation unit (DOM) with Javac' --format=%H)
commits = $initialCommit $(git rev-list ${initialCommit}...HEAD | sort -nr)
git fetch	incubator dom-based-operations
git checkout FETCH_HEAD
git cherry-pick $(commits)
git push --force incubator HEAD:dom-with-javac
