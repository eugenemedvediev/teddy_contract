#!/usr/bin/env bash

docker run -d -p 8080:8080 --expose=8090-8093 -p 8090-8093:8090-8093 imedvediev/api-contract:$1