package mil.navy.spawar.swif.security.filters;

import com.mongodb.BasicDBObject;
import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import mil.navy.spawar.swif.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SCIMongoNodeFilter implements IMongoNodeFilter {

    private static final Logger log = (Logger) LoggerFactory.getLogger(SCIMongoNodeFilter.class);
    private SecurityAttributeConfig attributeConfig;
    private String databaseLabel;
    private Set<String> userAttrs;
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

        // get all the user attribute value(s) into hash for quick lookup
        userAttrs = new HashSet<String>();

        if (userDetails.getCustomAttributes().containsKey(userdetailName)) {
            Collection<String> userAttrValues = userDetails.getCustomAttributes().get(userdetailName);
            userAttrs.addAll(userAttrValues);
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
        for (String fieldAttr : fieldAttrs) {
            if (!userAttrs.contains(fieldAttr)) {
                return false;
            }
        }

        aggregate.addAll(fieldAttrs);

        return true;
    }

    @Override
    public void processWrite(BasicDBObject obj) {
        //Do Nothing
    }

    @Override
    public Set<String> getFinalAggregate() {
        return new HashSet<String>(aggregate);
    }
}
