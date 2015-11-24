package mil.navy.spawar.swif.security.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;

import mil.navy.spawar.swif.security.ISwifUserDetails;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class RelToMongoRecordFilter implements IMongoRecordFilter {

    private static final Logger log = (Logger) LoggerFactory.getLogger(RelToMongoRecordFilter.class);

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


        long startTime = System.nanoTime();

        // init output list
        BasicDBList outList = new BasicDBList();

        String userDetailAttributeValue = "";
        // does user have any value(s) for attribute
        if (userDetails.getCustomAttributes().containsKey(userdetailName)) {
            // get all value(s) for the attribute for the user
            Collection<String> userDetailsAttributeValues = userDetails.getCustomAttributes().get(userdetailName);
            if (!userDetailsAttributeValues.isEmpty()) {
                userDetailAttributeValue = userDetailsAttributeValues.iterator().next();
            } else {
                log.debug("userdetails does not contain value(s) for: " + userdetailName);
                throw new AuthorizationServiceException("userdetails does not contain value(s) for: " + userdetailName);
            }
        } else {
            log.debug("userdetails does not contain value(s) for: " + userdetailName);
            throw new AuthorizationServiceException("userdetails does not contain value(s) for: " + userdetailName);
        }

        // iterate through each doc in the list
        for (int i = 0; i < inList.size(); i++) {

            if (inList.get(i) == null) {
                outList.add(null); //carry the null responses
            } else {

                // get doc to examine
                BasicDBObject inRec = (BasicDBObject) inList.get(i);

                // get all the relto attr values from the doc
                List<String> docAttrs = getDocumentSecurityAttributeValues(inRec, databaseLabel, databaseName);
                if (docAttrs.isEmpty()) {

                    // if the doc contains no relto attrs then let it go thru
                    outList.add(inRec);

                } else {

                    // if doc contains relto attrs make sure the doc contains relto with

                    // 1) user country code, add it if not already on doc
                    if (!docAttrs.contains(userDetailAttributeValue)) {
                        addAttrToDoc(inRec, databaseLabel, databaseName, userDetailAttributeValue);
                    }

                    // 2) the default value, add it if not already there
                    if (!docAttrs.contains(attributeConfig.getDefaultValue())) {
                        addAttrToDoc(inRec, databaseLabel, databaseName, attributeConfig.getDefaultValue());
                    }

                    // ok let doc thru
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

    // adds an attr value to document
    private void addAttrToDoc(BasicDBObject doc, String secCtx, String attrName, String attrValue) {

        // drill down to the attribute
        if (doc.containsField(secCtx)) {
            BasicDBObject securityContext = (BasicDBObject) doc.get(secCtx);
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
