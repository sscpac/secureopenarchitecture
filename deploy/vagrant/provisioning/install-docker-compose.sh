#!/bin/bash

# per https://docs.docker.com/compose/install
echo 'installing docker-compose'
curl -Ls https://github.com/docker/compose/releases/download/1.7.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose 
chmod +x /usr/local/bin/docker-compose
echo "installed: `docker-compose --version`"
