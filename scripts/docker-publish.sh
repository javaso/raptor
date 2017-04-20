#!/bin/sh

./scripts/docker-build.sh

cd raptor-auth-service
docker build . -t raptorbox/auth
docker push raptorbox/auth

cd ..
cd raptor-profile
docker build . -t raptorbox/profile
docker push raptorbox/profile

cd ..
cd raptor-http-api
docker build . -t raptorbox/api
docker push raptorbox/api

cd ..
cd raptor-broker
docker build . -t raptorbox/broker
docker push raptorbox/broker

cd ..
cd docker/proxy
docker build . -t raptorbox/proxy
docker push raptorbox/proxy
