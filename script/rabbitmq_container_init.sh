#!/bin/bash

DEFAULT_RABBITMQ_VERSION=3.7.8          #版本号参考：https://hub.docker.com/_/rabbitmq
DEFAULT_HOST_NAME=rabbitmq-server-d
DEFAULT_CONTAINER_NAME=rabbitmq-3.7.8

#下载镜像并创建容器
#if[ $1 == "" ]

docker run -d --hostname my-rabbit --name some-rabbit rabbitmq:3.7.8

docker logs some-rabbit