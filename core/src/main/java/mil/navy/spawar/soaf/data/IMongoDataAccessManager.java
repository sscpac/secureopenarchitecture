package mil.navy.spawar.soaf.data;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;

import mil.navy.spawar.soaf.security.IMongoAuthDecisionManager;

import java.util.Collection;
import java.util.Map;

public interface IMongoDataAccessManager {
	
	void setDatabase(DB db);

	void setAuthorizationManager(IMongoAuthDecisionManager mgr) ;
	 
	// retrieve collection
	BasicDBList readRecord(String collection) throws DataAccessException;
	
	// retrieve collection using query
	BasicDBList readRecord(String collection, DBObject query) throws DataAccessException;
    
    // delete record from collection
    void deleteRecord(String collection, String key) throws Exception;

    // delete record and child records
    void deleteRecord(String collection, String key, Collection<String> childCollections) throws Exception;


    // insert record into collection
    void createRecord(String collection, DBObject record) throws Exception;

    // update record in collection
    void updateRecord(String collection, DBObject record) throws Exception;

    //get the keys/labels for all possible values of an attribute
    Map<String, String> getSecurityAttributes(String attributeName);

    //get the label for the given key assigned to a given attribute
    String getSecurityAttributeLabel(String attributeName, String key);
}