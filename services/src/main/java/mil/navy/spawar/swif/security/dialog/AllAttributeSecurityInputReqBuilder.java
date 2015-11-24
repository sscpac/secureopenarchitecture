package mil.navy.spawar.swif.security.dialog;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

//implementation of the Security Input Requirement Builder that retrieves
//the value set for each attribute from the Attribute Manager
public class AllAttributeSecurityInputReqBuilder extends AbstractSecurityInputReqBuilder {

    private static Logger log = Logger.getLogger(AllAttributeSecurityInputReqBuilder.class.getName());

    //tell the attribute manager to preload all the key/labels for this attribute
    public void init() {
        attributeManager.preload(attributeConfig.getDbAttributeName());
    }

    //get all possible values for this attribute from AttributeManager object
    @Override
    protected JSONArray buildValueSet() {

        JSONArray valueSet = new JSONArray();

        Map<String, String> attributeLabels = attributeManager.getAllLabels(attributeConfig.getDbAttributeName());

        if (attributeLabels != null) {
            for (Map.Entry<String, String> entry : attributeLabels.entrySet()) {
                JSONObject valueObj = new JSONObject();
                valueObj.put("label", entry.getValue());
                valueObj.put("value", entry.getKey());
                valueSet.add(valueObj);
            }
        }

        return valueSet;
    }
}
