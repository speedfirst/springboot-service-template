#!/bin/sh

DOCKER_REGISTRY=reg.frontnode.net
APP_NAME=${project.build.finalName}

# docker image name MUST be lower case
IMAGE_NAME=`echo $DOCKER_REGISTRY/app/$APP_NAME | tr [:upper:] [:lower:]`

function build {
    echo "building docker image [$1]"
    docker build -t $1 ${project.build.directory}
}

function push {
    echo "pushing docker image [$1] to [$2]"
    docker push $1
}

# do the build
build $IMAGE_NAME

if [ $? != 0 ]; then
    echo 'build docker image failed, exit'
    exit -1
fi

# push the image
if [ x$1 != 'x--no-push' ]; then
    push $IMAGE_NAME $DOCKER_REGISTRY
else
    echo --no-push is specified, docker push ignored
fi