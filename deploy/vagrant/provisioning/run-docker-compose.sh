#!/bin/bash

echo 'starting SOAF...'
/usr/local/bin/docker-compose -f /vagrant/deploy/docker/docker-compose.yml up -d
