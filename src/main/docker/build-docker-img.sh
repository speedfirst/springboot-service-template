#!/bin/bash

DOCKER_REGISTRY=reg.frontnode.net
APP_NAME=${project.build.finalName}

# docker image name MUST be lower case
IMAGE_NAME=$(echo $DOCKER_REGISTRY/app/$APP_NAME | tr [:upper:] [:lower:])

echo " " >> $(dirname $0)/build-result.properties
echo "IMAGE_NAME=$IMAGE_NAME" >> $(dirname $0)/build-result.properties

echo "building docker image [$IMAGE_NAME]"
docker build -t $IMAGE_NAME ${project.build.directory}