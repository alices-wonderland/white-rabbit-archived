#!/usr/bin/env python3

import os
import sys
from typing import Final

if len(sys.argv) != 3:
    raise Exception("You should provide branch name and project version")

branch, version = sys.argv[1], sys.argv[2]

run_id: str = os.environ.get('GITHUB_RUN_NUMBER')
if run_id is None:
    raise Exception("GITHUB_RUN_NUMBER must be specified")

BRANCH_FEATURE_PREFIX: Final = "refs/heads/feature/"
BRANCH_RELEASE_PREFIX: Final = "refs/heads/release/"
BRANCH_HOTFIX_PREFIX: Final = "refs/heads/hotfix/"
BRANCH_BUGFIX_PREFIX: Final = "refs/heads/bugfix/"
TAG_PREFIX: Final = "refs/tags/"
if branch == "refs/heads/develop":
    print(f"{version}-staging-{run_id}")
elif branch.startswith(BRANCH_FEATURE_PREFIX):
    name = branch[len(BRANCH_FEATURE_PREFIX):]
    print(f"{version}-f-{name}-{run_id}")
elif branch.startswith(BRANCH_BUGFIX_PREFIX):
    name = branch[len(BRANCH_BUGFIX_PREFIX):]
    print(f"{version}-bug-{name}-{run_id}")
elif branch.startswith(BRANCH_RELEASE_PREFIX) or branch.startswith(BRANCH_HOTFIX_PREFIX):
    print(f"{version}-rc-{run_id}")
elif branch.startswith(TAG_PREFIX):
    name = branch[len(TAG_PREFIX):]
    if name != version:
        raise Exception("Tag name and the project version should be the same")
    print(f"{version}")
else:
    raise Exception(f"Branch {branch} should not be packaged as docker images")
