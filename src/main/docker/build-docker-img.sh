#!/bin/sh

DOCKER_REGISTRY=reg.frontnode.net
APP_NAME=${project.build.finalName}

# docker image name MUST be lower case
IMAGE_NAME=`echo $DOCKER_REGISTRY/app/$APP_NAME | tr [:upper:] [:lower:]`

function build() {
    echo 'building docker image [$IMAGE_NAME]'
    docker build -t $IMAGE_NAME ${project.build.directory}
}

function push() {
    echo 'pushing docker image [$IMAGE_NAME] to [$DOCKER_REGISTRY]'
    docker push $IMAGE_NAME
}

# do the build
build

if [ x$1 != 'x--no-push' ]; then
    push
fi