FROM java:8-jre-alpine

ENV TOMCAT_VERSION=8.0.35

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

RUN  echo "http://dl-4.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories \
     && apk add --update curl dos2unix \
  && curl \
  --silent \
  --location \
  --retry 3 \
  --cacert /etc/ssl/certs/ca-certificates.crt \
  "https://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
    | gunzip \
    | tar x -C /usr/ \
    && mv /usr/apache-tomcat* $CATALINA_HOME \
    && rm -rf $CATALINA_HOME/webapps $CATALINA_HOME/conf/server.xml 

ADD release $CATALINA_HOME/webapps/
ADD conf/server.xml $CATALINA_HOME/conf/
ADD conf/keystore.jks $CATALINA_HOME/certs/
ADD conf/setenv.sh $CATALINA_HOME/bin/

RUN dos2unix $CATALINA_HOME/bin/setenv.sh 
CMD ["catalina.sh", "run"]
