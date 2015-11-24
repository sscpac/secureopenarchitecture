package mil.navy.spawar.swif.security.filters;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import mil.navy.spawar.swif.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RelToMongoNodeFilter implements IMongoNodeFilter {

    private static final Logger log = (Logger) LoggerFactory.getLogger(RelToMongoNodeFilter.class);
    private SecurityAttributeConfig attributeConfig;
    private String databaseLabel;
    private String userDetailAttributeValue;
    private Set<String> aggregate = new HashSet<String>();

    @Override
    public void setDatabaseLabel(String databaseLabel) {
        this.databaseLabel = databaseLabel;
    }

    @Override
    public void setAttributeConfig(SecurityAttributeConfig attributeConfig) {
        this.attributeConfig = attributeConfig;
    }

    @Override
    public SecurityAttributeConfig getAttributeConfig() {
        return attributeConfig;
    }

    @Override
    public void initUserAttributes(ISwifUserDetails userDetails) {
        String userdetailName = attributeConfig.getUserDetailsName();

        if (userDetails.getCustomAttributes().containsKey(userdetailName)) {
            // get all value(s) for the attribute for the user
            Collection<String> userDetailsAttributeValues = userDetails.getCustomAttributes().get(userdetailName);
            if (!userDetailsAttributeValues.isEmpty()) {
                userDetailAttributeValue = userDetailsAttributeValues.iterator().next();
            } else {
                log.debug("userdetails does not contain value(s) for: " + userdetailName);
                log.error("please check user entry in LDAP");
                throw new AuthorizationServiceException("userdetails does not contain value(s) for: " + userdetailName);
            }
        } else {
            log.debug("userdetails does not contain value(s) for: " + userdetailName);
            log.error("please check user entry in LDAP");
            throw new AuthorizationServiceException("userdetails does not contain value(s) for: " + userdetailName);
        }
    }

    public boolean initNewRecord(BasicDBObject obj) {
        aggregate.clear();

        return true;
    }

    @Override
    public boolean filter(BasicDBObject obj) {

        //get security labels (if any) for this object
        List<String> fieldAttrs = SecurityManager.getDocumentSecurityAttributeValues(obj,
                attributeConfig.getDbAttributeName(), databaseLabel);

        //check user has appropriate credentials to see this object
        if(fieldAttrs.isEmpty() || fieldAttrs.contains(userDetailAttributeValue)) {
            aggregate.addAll(fieldAttrs);
            return true;
        }

        return false;
    }

    @Override
    public void processWrite(BasicDBObject obj) {
        //get security labels (if any) for this object
        List<String> fieldAttrs = SecurityManager.getDocumentSecurityAttributeValues(obj,
                attributeConfig.getDbAttributeName(), databaseLabel);

        if(!fieldAttrs.isEmpty()) {
            // 1) user country code, add it if not already on doc
            if (!fieldAttrs.contains(userDetailAttributeValue)) {
                addAttrToDoc(obj, databaseLabel, attributeConfig.getDbAttributeName(), userDetailAttributeValue);
            }

            // 2) the default value, add it if not already there
            if (!fieldAttrs.contains(attributeConfig.getDefaultValue())) {
                addAttrToDoc(obj, databaseLabel, attributeConfig.getDbAttributeName(), attributeConfig.getDefaultValue());
            }
        }
    }

    @Override
    public Set<String> getFinalAggregate() {
        return new HashSet<String>(aggregate);
    }

    // adds an attr value to field
    private static void addAttrToDoc(BasicDBObject field, String secCtx, String attrName, String attrValue) {

        // drill down to the attribute
        if (field.containsField(secCtx)) {
            BasicDBObject securityContext = (BasicDBObject) field.get(secCtx);
            if (securityContext.containsField(attrName)) {
                Object obj = securityContext.get(attrName);
                if (obj instanceof BasicDBList) {
                    BasicDBList attrList = (BasicDBList) obj;
                    // add the value to the list if it does not already exist
                    if (!attrList.contains(attrValue)) {
                        attrList.add(attrValue);
                    }
                }
            }
        } else {
            throw new AuthorizationServiceException("document has no " + secCtx + " marking");
        }
    }
}
