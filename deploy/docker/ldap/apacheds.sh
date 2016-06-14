#!/bin/bash

APACHEDS_INSTANCE=/var/lib/apacheds-2.0.0_M19/default

function wait_for_ldap {
	echo "Waiting for LDAP to be available "
	c=0

    ldapsearch -h localhost -p 10389 -D 'uid=admin,ou=system' -w secret ou=system;
    
    while [ $? -ne 0 ]; do
        echo "LDAP not up yet... retrying... ($c/20)"
        sleep 4
 		
 		if [ $c -eq 20 ]; then
 			echo "TROUBLE!!! After [${c}] retries LDAP is still dead :("
 			exit 2
 		fi
 		c=$((c+1))
    	
    	ldapsearch -h localhost -p 10389 -D 'uid=admin,ou=system' -w secret ou=system;
    done 
}

if [ -f /init/config.ldif ] && [ ! -f ${APACHEDS_INSTANCE}/conf/config.ldif_migrated ]; then
	echo "Using config file from /init/config.ldif"
	rm -rf ${APACHEDS_INSTANCE}/conf/config.ldif

	cp /init/config.ldif ${APACHEDS_INSTANCE}/conf/
	chown apacheds.apacheds ${APACHEDS_INSTANCE}/conf/config.ldif
fi

rm -f ${APACHEDS_INSTANCE}/run/apacheds-default.pid 

/opt/apacheds-2.0.0_M19/bin/apacheds start default

wait_for_ldap

echo "LDAP STARTED..."
	
ldapadd -h localhost -p 10389 -D 'uid=admin,ou=system' -w secret -f /init/soaf.ldif
ldapadd -h localhost -p 10389 -D 'uid=admin,ou=system' -w secret -f /init/soaf_users_groups_roles.ldif

trap "echo 'Stoping Apache DS';/opt/apacheds-2.0.0_M19/bin/apacheds stop default;exit 0" SIGTERM SIGKILL

while true
do
  tail -f /dev/null & wait ${!}
done
