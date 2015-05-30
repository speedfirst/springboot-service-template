#!/bin/sh

DOCKER_REGISTRY=reg.frontnode.net
APP_NAME=${project.build.finalName}

# docker image name MUST be lower case
IMAGE_NAME=`echo $DOCKER_REGISTRY/app/$APP_NAME | tr [:upper:] [:lower:]`

function build {
    echo "building docker image [$1]"
    docker build -t $1 ${project.build.directory}
}

# do the build
build $IMAGE_NAME