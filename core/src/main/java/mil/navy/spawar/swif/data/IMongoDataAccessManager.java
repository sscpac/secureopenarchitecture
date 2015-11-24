package mil.navy.spawar.swif.data;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;

import mil.navy.spawar.swif.security.IMongoAuthDecisionManager;

import java.util.Collection;
import java.util.Map;

public interface IMongoDataAccessManager {
	
	public void setDatabase(DB db);

	public void setAuthorizationManager(IMongoAuthDecisionManager mgr) ;
	 
	// retrieve collection
	public BasicDBList readRecord(String collection) throws DataAccessException;
	
	// retrieve collection using query
	public BasicDBList readRecord(String collection, DBObject query) throws DataAccessException;
    
    // delete record from collection
    public void deleteRecord(String collection, String key) throws Exception;

    // delete record and child records
    public void deleteRecord(String collection, String key, Collection<String> childCollections) throws Exception;


    // insert record into collection
    public void createRecord(String collection, DBObject record) throws Exception;

    // update record in collection
    public void updateRecord(String collection, DBObject record) throws Exception;

    //get the keys/labels for all possible values of an attribute
    public Map<String, String> getSecurityAttributes(String attributeName);

    //get the label for the given key assigned to a given attribute
    public String getSecurityAttributeLabel(String attributeName, String key);
    

}