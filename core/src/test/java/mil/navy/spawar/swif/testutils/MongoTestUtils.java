package mil.navy.spawar.swif.testutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import static org.junit.Assert.*;

public class MongoTestUtils {
	
    public static BasicDBList executeQuery(DBCollection collection, BasicDBObject query) {
		BasicDBList results = new BasicDBList();
		DBCursor cursor = collection.find(query);
		while(cursor.hasNext()) {
			results.add(cursor.next());
		}
		cursor.close();
		return results;
    }
    
    public static BasicDBList executeQuery(DBCollection collection) {
		BasicDBList results = new BasicDBList();
		DBCursor cursor = collection.find();
		while(cursor.hasNext()) {
			results.add(cursor.next());
		}
		cursor.close();
		return results;
    }    
     
    public static void dropCollection(DB database, String collection) {
     	if(database.collectionExists(collection)) {
    		DBCollection existCol = database.getCollection(collection);
    		existCol.drop();
    	} 
     	assertFalse("collection not dropped", database.collectionExists(collection));
    }
    
    public static String loadResourceAsString(String resource)  {
    	InputStream is = null;
    	try {
    		is = MongoTestUtils.class.getResourceAsStream(resource);
    	   	InputStreamReader isr = new InputStreamReader(is);
        	BufferedReader br = new BufferedReader(isr);
        	StringBuilder result = new StringBuilder();
        	String txtLine;
        	while((txtLine = br.readLine()) != null) {
        		result.append(txtLine);
        	}
        	return result.toString();
    	} catch(Exception ex) {
    		return null;
    	}
    }
    
    public static void assertCollectionCount(DBCollection collection, int kount) {
    	assertCollectionCount(collection, (long) kount);
    }
    
    public static void assertCollectionCount(DBCollection collection, long kount) {
    	assertNotNull("DBCollection is null", collection);
    	assertEquals("record count error", kount, collection.count());
    }
    
    public static void assertDocumentHasKeyValue(DBObject obj, String key, String expectedValue) {
    	assertNotNull("DBObject is null", obj);
    	assertTrue("DBObject does not containField [" + key + "]", obj.containsField(key));
    	assertTrue("DBObject containsField [" + key + "] but the value is not an instanceOf String", obj.get(key) instanceof String );
    	String actualValue = (String) obj.get(key);
    	assertEquals(expectedValue, actualValue);
    }
    
    public static void assertDocumentHasKeyValue(DBObject obj, String key, int expectedValue) {
    	assertNotNull("DBObject is null", obj);
    	assertTrue("DBObject does not containField [" + key + "]", obj.containsField(key));
    	assertTrue("DBObject containsField [" + key + "] but the value is not an instanceOf Integer", obj.get(key) instanceof Integer );
    	int actualValue = ((Integer)obj.get(key)).intValue();
    	assertEquals(expectedValue, actualValue);
    }

    public static BasicDBList dbList(String[] items) {
    	BasicDBList result = new BasicDBList();
    	for( String item : items) {
    		result.add(item);
    	}
    	return result;
    }
    

}