#!/usr/bin/env bash

echo $1
docker run -d -p 8080 $1