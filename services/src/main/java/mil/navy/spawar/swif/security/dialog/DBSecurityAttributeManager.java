package mil.navy.spawar.swif.security.dialog;

import mil.navy.spawar.swif.data.IMongoDataAccessManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

//Implementation of the Security Attribute Manager that gets all of the key/label values for each of the
//attributes from the database.
public class DBSecurityAttributeManager implements ISecurityAttributeManager {

    private static Logger log = Logger.getLogger(DBSecurityAttributeManager.class.getName());

    private IMongoDataAccessManager accessManager;
    private Map<String, Map<String, String>> cachedAttributes = new HashMap<String, Map<String, String>>();;

    public void setAccessManager(IMongoDataAccessManager accessManager) {
        this.accessManager = accessManager;
    }

    //Preload the Key/Labels for the given Security Attribute from the data source
    @Override
    public void preload(String attributeName) {
        cachedAttributes.put(attributeName, accessManager.getSecurityAttributes(attributeName));
    }

    //Get all the Key/Label pairs for a given Security Attribute
    @Override
    public Map<String, String> getAllLabels(String attributeName) {
        //get labels from cache
        Map<String, String> allLabels = cachedAttributes.get(attributeName);

        //if labels aren't in the cache check the database
        if(allLabels == null) {
            allLabels = accessManager.getSecurityAttributes(attributeName);

            //if labels are found in the database, add entry to cache
            if(allLabels != null) {
                cachedAttributes.put(attributeName, allLabels);
            }
        }

        return allLabels;
    }

    //Get the Label for the Key of a given Security Attribute
    @Override
    public String getLabel(String attributeName, String key) {
        String label = null;

        Map<String, String> attributeMap = cachedAttributes.get(attributeName);
        if(attributeMap != null) {
            //check for label in the preloaded map
            label = attributeMap.get(key);
        } else {
            //create new entry in map so attribute can be cached
            attributeMap = new HashMap<String, String>();
            cachedAttributes.put(attributeName, attributeMap);
        }

        //if label could not be found in the map, check the database
        if(label == null) {
            label = accessManager.getSecurityAttributeLabel(attributeName, key);

            //if label is found in the database, cache in the attribute map
            if(label != null) {
                attributeMap.put(key, label);
            }
        }

        return label;
    }
}
