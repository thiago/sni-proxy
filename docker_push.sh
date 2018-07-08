#!/usr/bin/env bash

set -e
export DOCKER_IMAGE_VERSION=${TRAVIS_TAG/v/}
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker-compose push app
docker logout