package mil.navy.spawar.swif.security.filters;

import java.util.ArrayList;
import java.util.Collection;

import mil.navy.spawar.swif.security.ISwifUserDetails;

import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import org.springframework.security.access.AuthorizationServiceException;

public class RelToMongoQueryFilter implements IMongoQueryFilter {

   private static final Logger log = (Logger) LoggerFactory.getLogger(RelToMongoQueryFilter.class);

    private static final String or = "$or" ;

    private String databaseLabel ;
    private SecurityAttributeConfig attributeConfig;

    public String getDatabaseLabel() {
        return databaseLabel;
    }

    public void setDatabaseLabel(String value) {
        databaseLabel = value;
    }

    public SecurityAttributeConfig getAttributeConfig() {
        return attributeConfig;
    }

    public void setAttributeConfig(SecurityAttributeConfig value) {
        attributeConfig = value;
    }

    @Override
    public BasicDBObject filter(ISwifUserDetails userDetails) {

        String userdetailName = attributeConfig.getUserDetailsName();
        String databaseName = attributeConfig.getDbAttributeName();

        String userDetailAttributeValue = "";

        // does user have any value(s) for attribute
        if( userDetails.getCustomAttributes().containsKey(userdetailName)) {

            // get all value(s) for the attribute for the user
            Collection<String> userDetailsAttributeValues = userDetails.getCustomAttributes().get(userdetailName);
            if(!userDetailsAttributeValues.isEmpty()) {
                userDetailAttributeValue = userDetailsAttributeValues.iterator().next();
            } else {
                log.debug("userdetails does not contain value(s) for: " + userdetailName);
                throw new AuthorizationServiceException("userdetails does not contain value(s) for: " + userdetailName);

            }
        } else {
            log.debug("userdetails does not contain value(s) for: " + userdetailName);
            throw new AuthorizationServiceException("userdetails does not contain value(s) for: " + userdetailName);
        }

        ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
        list.add(new BasicDBObject(databaseLabel + "." + databaseName, userDetailAttributeValue ));
        list.add(new BasicDBObject(databaseLabel + "." + databaseName, new ArrayList()));

        // create query object
        BasicDBObject filter = new BasicDBObject();
        filter.put(or, list);

        log.debug("filter: " +  filter);
        return filter;
    }
}
