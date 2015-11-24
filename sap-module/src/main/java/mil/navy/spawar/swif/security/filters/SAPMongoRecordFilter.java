package mil.navy.spawar.swif.security.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class SAPMongoRecordFilter implements IMongoRecordFilter {

    private static final Logger log = (Logger) LoggerFactory.getLogger(SAPMongoRecordFilter.class);

    private String databaseLabel;
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
    public BasicDBList filter(BasicDBList inList, ISwifUserDetails userDetails) {

        String userdetailName = attributeConfig.getUserDetailsName();
        String databaseName = attributeConfig.getDbAttributeName();

        // this filter will match values for and attribute found on object and user.
        // if the object has a value that the user does not possess then the object should not be returned to the user
        long startTime = System.nanoTime();

        // init output list
        BasicDBList outList = new BasicDBList();

        // get all the user attribute value(s) into hash for quick lookup
        Set<String> userAttrs = new HashSet<String>();
        if (userDetails.getCustomAttributes().containsKey(userdetailName)) {
            Collection<String> userAttrValues = userDetails.getCustomAttributes().get(userdetailName);
            userAttrs.addAll(userAttrValues);
        }

        // iterate through each obj in the list
        for (int i = 0; i < inList.size(); i++) {

            if (inList.get(i) == null) {
                outList.add(null); //carry the null responses
            } else {
                // obj to examine
                BasicDBObject inRec = (BasicDBObject) inList.get(i);

                // all classification attributes for the obj
                List<String> docAttrs = getDocumentSecurityAttributeValues(inRec, databaseLabel, databaseName);

                // lookup each object value in the user's list
                // if the obj value is not in user's list mark the obj for removal
                boolean goodRec = true;
                for (String docAttr : docAttrs) {
                    if (!userAttrs.contains(docAttr)) {
                        goodRec = false;
                        break;
                    }
                }

                // if the obj is not marked for removal add it to the result list
                if (goodRec) {
                    outList.add(inRec);
                }
            }
        }

        long endTime = System.nanoTime();

        if (log.isDebugEnabled()) {
            long elapsedTime = endTime - startTime;
            double seconds = (double) elapsedTime / 1000000000.0;
            log.debug(String.format("filter in-recs: %d  out-recs: %d   secs: %f", inList.size(), outList.size(), seconds));
        }

        return outList;
    }

    private List<String> getDocumentSecurityAttributeValues(BasicDBObject doc, String secCtx, String attr) {

        // grab all the values for the attribute from mongo obj
        List<String> results = new ArrayList<String>();
        if (doc.containsField(secCtx)) {
            BasicDBObject securityContext = (BasicDBObject) doc.get(secCtx);
            if (securityContext.containsField(attr)) {
                Object obj = securityContext.get(attr);
                if (obj instanceof BasicDBList) {
                    BasicDBList attrList = (BasicDBList) obj;
                    for (Object attrElement : attrList) {
                        if (attrElement instanceof String) {
                            results.add((String) attrElement);
                        }
                    }
                }
            }
        } else {
            throw new AuthorizationServiceException("document has no " + secCtx + " marking");
        }
        return results;
    }

}
