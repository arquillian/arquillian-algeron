#!/bin/bash
# ------------------------------------------------------------------
# [Bartosz Majsak] Release automation
#  
# This script takes care of automating following steps:
# - runs mvn release process and push everything (changes and tags) to the repository
# - closes related milestone (named the same as tag/release version) using GitHub API
#
# Important: You should have write rights to the given repository and have authentication token generated.
#
# Prerequisites:
# - GitHub token stored in .github-auth file. @see https://help.github.com/articles/creating-an-access-token-for-command-line-use/
# - jq for parsing json installed. @see https://stedolan.github.io/jq/download/
# ------------------------------------------------------------------

SUBJECT=arq-release-automation
VERSION=0.0.1
USAGE="Usage: $(basename "$0") -hv release_version next_dev_version"

# --- Option processing --------------------------------------------
if [ $# == 0 ] ; then
    echo $USAGE
    exit 1;
fi

while getopts ":vh" optname
  do
    case "$optname" in
      "v")
        echo "Version $VERSION"
        exit 0;
        ;;
      "h")
        echo $USAGE
        exit 0;
        ;;
      "?")
        echo "Unknown option $OPTARG"
        exit 0;
        ;;
      ":")
        echo "No argument value for option $OPTARG"
        exit 0;
        ;;
      *)
        echo "Unknown error while processing options"
        exit 0;
        ;;
    esac
  done

shift $(($OPTIND - 1))


param1=$1
param2=$2


# -----------------------------------------------------------------

LOCK_FILE=/tmp/${SUBJECT}.lock

if [ -f "$LOCK_FILE" ]; then
echo "Script is already running"
exit
fi

# -----------------------------------------------------------------
trap "rm -f $LOCK_FILE" EXIT
touch $LOCK_FILE 

# -----------------------------------------------------------------
#  RELEASE LOGIC
# -----------------------------------------------------------------

token=$(<.github-auth)
origin=$(git remote -v | cut -d':' -f 2 | cut -d' ' -f 1 | tail -n 1)
repo=${origin%.*}
milestone_url="https://api.github.com/repos/${repo}/milestones"

milestone_number=$(curl -H "Authorization: token ${token}" ${milestone_url} | jq --arg title $param1 'map(select(.title == $title)) | .[0].number')

mvn -B release:prepare -DreleaseVersion=${param1} -Dtag=${param1} -DdevelopmentVersion=${param2} && git push origin && git push --tags origin && curl -X PATCH --data '{ "state": "closed" }' -H "Authorization: token ${token}" ${milestone_url}/${milestone_number}
