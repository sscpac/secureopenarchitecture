package mil.navy.spawar.swif.security.filters;

import com.mongodb.BasicDBObject;
import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import mil.navy.spawar.swif.security.SecurityLabelRequirement;
import mil.navy.spawar.swif.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;

import java.util.*;

public class ClassificationMongoNodeFilter implements IMongoNodeFilter {

    private static final Logger log = (Logger) LoggerFactory.getLogger(ClassificationMongoNodeFilter.class);
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
            if (userAttrValues.size() == 0) {
                log.error("userdetails does not contain value(s) for: " + userdetailName);
                log.error("please check user entry in LDAP");
                throw new AuthorizationServiceException("userdetails does not contains entry for " + userdetailName);
            }
            userAttrs.addAll(userAttrValues);
        } else {
            log.error("userdetails does not contain value(s) for: " + userdetailName);
            log.error("please check user entry in LDAP");
            throw new AuthorizationServiceException("userdetails does not contains entry for " + userdetailName);
        }
    }

    public boolean initNewRecord(BasicDBObject obj) {

        if (attributeConfig.getSecLabelRequirement().equals(SecurityLabelRequirement.RECORD)) {
            String databaseName = attributeConfig.getDbAttributeName();
            List<String> fieldAttrs = SecurityManager.getDocumentSecurityAttributeValues(obj,
                    databaseName, databaseLabel);

            if(fieldAttrs.isEmpty()) {
                log.warn("record missing " + databaseLabel+ "." + databaseName + " marking has been filtered");
                return false;
            }
        }

        aggregate.clear();

        return true;
    }

    @Override
    public boolean filter(BasicDBObject obj) {

        String databaseName = attributeConfig.getDbAttributeName();

        //get security labels (if any) for this object
        List<String> fieldAttrs = SecurityManager.getDocumentSecurityAttributeValues(obj, databaseName, databaseLabel);

        if (attributeConfig.getSecLabelRequirement().equals(SecurityLabelRequirement.CELL) && fieldAttrs.isEmpty()) {
            log.warn("field missing " + databaseLabel+ "." + databaseName + " marking has been filtered");
            return false;
        }

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
        HashSet<String> retList = new HashSet<String>();
        String highestClass = null;
        Map<String, Integer> classRanks = attributeConfig.getRankings();

        for (String classification : aggregate) {
            if (highestClass == null ||
                    (classRanks.get(classification) != null && classRanks.get(highestClass) != null &&
                            classRanks.get(classification) > classRanks.get(highestClass))) {
                highestClass = classification;
            }
        }

        retList.add(highestClass);

        return retList;
    }



}
