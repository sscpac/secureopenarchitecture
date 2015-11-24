package mil.navy.spawar.swif.security.dialog;

import java.util.Map;


//Interface for the Security Attribute Manager
//Implementations of this class are responsible for providing the mapping between the keys and labels for
//all of the attribute values
public interface ISecurityAttributeManager {

    //get the keys/labels for all possible values of an attribute
    public Map<String, String> getAllLabels(String attributeName);

    //get the label for the given key assigned to a given attribute
    public String getLabel(String attributeName, String key);

    //Preload the Key/Labels for the given Security Attribute from the data source
    public void preload(String attributeName);

}
