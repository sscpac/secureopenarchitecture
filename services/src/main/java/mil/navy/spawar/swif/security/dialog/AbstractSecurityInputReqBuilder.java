package mil.navy.spawar.swif.security.dialog;

import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//Abstract implementation of the Security Input Requirement Builder that implements the functionality
//common among implementations of the interface
abstract class AbstractSecurityInputReqBuilder implements ISecurityInputReqBuilder {

    protected ISecurityAttributeManager attributeManager;
    protected SecurityAttributeConfig attributeConfig;

    public void setAttributeConfig(SecurityAttributeConfig attributeConfig) {
        this.attributeConfig = attributeConfig;
    }

    public String getAttributeName() {
        return attributeConfig.getDbAttributeName();
    }

    public void setAttributeManager(ISecurityAttributeManager attributeManager) {
        this.attributeManager = attributeManager;
    }

    //return the json describing the input details required by the attribute assigned to this builder
    @Override
    public String getInputRequirements() {
        JSONObject root = new JSONObject();

        root.put("type", attributeConfig.getValueType());

        if(attributeConfig.getDisplayName() != null) {
            root.put("displayName", attributeConfig.getDisplayName());
        } else {
            root.put("displayName", attributeConfig.getDbAttributeName());
        }

        root.put("rank", attributeConfig.getRank());

        if(attributeConfig.getDefaultValue() != null) {
            root.put("default", attributeConfig.getDefaultValue());
        }

        JSONArray valueSet = buildValueSet();
        if(valueSet != null) {
            root.put("valueSet", valueSet);
        }

        return root.toJSONString();
    }

    //Retrieve the set of possible values for this attribute
    abstract protected JSONArray buildValueSet();
}
