FROM centos:7

ADD apacheds.sh /usr/local/bin/	
ADD ldif-files /init

RUN  yum -y install java-1.7.0-openjdk openldap-clients dos2unix \ 
	&& curl -s http://archive.apache.org/dist/directory/apacheds/dist/2.0.0-M19/apacheds-2.0.0-M19-x86_64.rpm -o /tmp/apacheds.rpm \
	&& yum -y localinstall /tmp/apacheds.rpm && rm -rf /tmp/apacheds.rpm && chmod +x /usr/local/bin/apacheds.sh \
 	&& chown -R apacheds.apacheds /var/lib/apacheds-2.0.0_M19/default/partitions && dos2unix /usr/local/bin/apacheds.sh


ENTRYPOINT /usr/local/bin/apacheds.sh

EXPOSE 10389
EXPOSE 389
