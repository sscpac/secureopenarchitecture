package mil.navy.spawar.swif.security.dialog;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Map;

//implementation of the Security Input Requirement Builder that retrieves
//the value set for each attribute from the User Details object
public class UserDetailsSecurityInputReqBuilder extends AbstractSecurityInputReqBuilder {

    private static Logger log = Logger.getLogger(UserDetailsSecurityInputReqBuilder.class.getName());

    //get possible values for this attribute from UserDetails object
    @Override
    protected JSONArray buildValueSet() {

        //get the UserDetails from the Security Context
        ISwifUserDetails userDetails = SecurityManager.getUserDetails();

        JSONArray valueSet = new JSONArray();
        Map<String, Collection<String>> customAttributes = userDetails.getCustomAttributes();
        if (customAttributes != null) {
            Collection<String> keys = customAttributes.get(attributeConfig.getUserDetailsName());
            String label;
            if (keys == null) {

            }
            //Iterate through all the values the user has for this attribute
            else for (String key : keys) {
                JSONObject valueObj = new JSONObject();
                valueObj.put("value", key);

                //get the label for this value from the attribute manager
                label = attributeManager.getLabel(attributeConfig.getDbAttributeName(), key);
                if (label != null) {
                    valueObj.put("label", label);
                } else {
                    valueObj.put("label", key);
                }

                valueSet.add(valueObj);
            }
        }

        return valueSet;
    }
}
