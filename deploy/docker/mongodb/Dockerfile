FROM java:8-jre-alpine

ADD dump /dump
ADD run.sh  /usr/local/bin/
RUN chmod +x /usr/local/bin/run.sh && touch /.firstrun && apk upgrade --update \
  && echo "http://dl-4.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories \
  && apk add --no-cache mongodb mongodb-tools netcat-openbsd dos2unix \
  && dos2unix /usr/local/bin/run.sh

VOLUME /data/db

ENTRYPOINT /usr/local/bin/run.sh
