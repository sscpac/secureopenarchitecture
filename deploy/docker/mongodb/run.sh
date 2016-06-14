#!/bin/sh

# Initialize first run
if [[ -e /.firstrun ]]; then

  /usr/bin/mongod &

  sleep 3 
  echo "Waiting for MongoDB service to start"
  while ! nc -vz localhost 27017; do sleep 1; done

  echo "Seeding soafDB" 
  /usr/bin/mongorestore --db soafDB /dump

  /usr/bin/mongod --shutdown
    rm -rf /.firstrun /dump
fi

/usr/bin/mongod $@
