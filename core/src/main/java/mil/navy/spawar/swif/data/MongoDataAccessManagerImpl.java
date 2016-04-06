package mil.navy.spawar.swif.data;

import com.mongodb.*;

import mil.navy.spawar.swif.security.IMongoAuthDecisionManager;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MongoDataAccessManagerImpl implements IMongoDataAccessManager {

    private DB database;
    private IMongoAuthDecisionManager authManager;
    private static final Logger log = (Logger) LoggerFactory.getLogger(MongoDataAccessManagerImpl.class);
    private static final String KEY = "Key";
    private static final String LABEL = "Label";
    private static final String COLLECTION_ID = "_id";
    private static final String PARENT_ID = "parentDocumentId";

    @Override
    public void setDatabase(DB db) {
        database = db;
    }

    @Override
    public void setAuthorizationManager(IMongoAuthDecisionManager mgr) {
        authManager = mgr;
    }

    @Override
    public BasicDBList readRecord(String collection) throws DataAccessException {
        return readRecord(collection, null);
    }

    @Override
    public BasicDBList readRecord(String collection, DBObject query) throws DataAccessException {

        // check collection
        if (!isValidCollection(collection)) {
            throw new DataAccessException("invalid collection");
        }

        // apply authorization filters to query
        if (authManager.getQueryFilters().size() > 0) {

            List<BasicDBObject> qryParts = new ArrayList<BasicDBObject>();
            qryParts.addAll(authManager.execQueryFilters());

            // if no existing query then initize a new one
            if (query == null) {
                query = new BasicDBObject();
            }

            // if existing qry has $and then add filters to the $and list
            // else just append new $and clause
            if (query.containsField("$and")) {
                Object andObj = query.get("$and");
                if (andObj instanceof BasicDBList) {
                    BasicDBList andList = (BasicDBList) andObj;
                    andList.addAll(qryParts);
                }
            } else {
                query.put("$and", qryParts);
            }
        }

        // get collection
        DBCollection dbCollection = database.getCollection(collection);

        // execute the query
        BasicDBList results = doQuery(dbCollection, query);

        //apply results filters
        return authManager.execRecordFilters(results);
    }

    @Override
    public void deleteRecord(String collection, String key) throws Exception {
        deleteRecord(collection, key, Collections.<String>emptyList());
    }

    @Override
    public void deleteRecord(String collection, String key, Collection<String> childCollections) throws Exception {

        // check collection
        if (!isValidCollection(collection)) {
            throw new DataAccessException("invalid collection");
        }

        for(String childCollection : childCollections) {
            if (!isValidCollection(childCollection)) {
                throw new DataAccessException("invalid child collection");
            }
        }

        // check key
        if (!isValidDocumentKey(key)) {
            throw new DataAccessException("invalid key");
        }

        // create query using key
        BasicDBObject query = new BasicDBObject();
        query.put(COLLECTION_ID, new ObjectId(key));

        // try to retrieve the record from mongo
        BasicDBList records = readRecord(collection, query);
        if (records.isEmpty()) {
            throw new RecordNotFoundException();
        }

        // if we can retrieve the record then the user has access and we should allow the delete to proceed
        //BasicDBObject record = (BasicDBObject) records.get(0);

        // remove the record
        try {
            DBCollection dbCollection = database.getCollection(collection);
            dbCollection.remove(query, WriteConcern.ACKNOWLEDGED);

            for(String childCollection : childCollections) {
                query = new BasicDBObject();
                query.put(PARENT_ID, key);
                dbCollection = database.getCollection(childCollection);
                dbCollection.remove(query, WriteConcern.ACKNOWLEDGED);
            }

        } catch (Exception ex) {
            log.info("deleteRecord database error", ex);
            throw new DataAccessException("mongodb error:", ex);
        }

    }

    @Override
    public void createRecord(String collection, DBObject record) throws Exception {

        // check collection
        if (!isValidCollection(collection)) {
            throw new DataAccessException("invalid collection");
        }

        // check rec
        if (record == null) {
            throw new DataAccessException("invalid record");
        }

        // apply any auth filters to see if the object changes
        BasicDBList recordsToFilter = new BasicDBList();
        recordsToFilter.add(record);
        BasicDBList filteredRecords = authManager.execRecordFilters(recordsToFilter);

        //check record was returned after filtering
        if (filteredRecords.isEmpty()) {
            throw new DataAccessException("record not created, invalid security labeling");
        }
        DBObject filteredRecord = (DBObject) filteredRecords.get(0);

        //check record wasn't returned as null
        if (filteredRecord == null) {
            throw new DataAccessException("record not created, invalid security labeling");
        }

        //check record was not modified during filtering
        if (!record.equals(filteredRecord)) {
            throw new DataAccessException("record not created, invalid security labeling");
        }

        // save the record
        try {
            DBCollection dbCollection = database.getCollection(collection);
            dbCollection.insert(record, WriteConcern.ACKNOWLEDGED);
        } catch (Exception ex) {
            log.info("createRecord database error", ex);
            throw new DataAccessException("mongodb error:", ex);
        }
    }

    @Override
    public void updateRecord(String collection, DBObject record) throws Exception {

        // check if collection is valid
        if (!isValidCollection(collection)) {
            throw new DataAccessException("invalid collection");
        }

        // check rec
        if (record == null) {
            throw new DataAccessException("invalid record");
        }

        // check object has key
        Object obj = record.get(COLLECTION_ID);
        if (obj == null || !(obj instanceof ObjectId)) {
            throw new DataAccessException("invalid/missing id");
        }

        // create query using object key
        BasicDBObject query = new BasicDBObject();
        query.put(COLLECTION_ID, obj);

        // try to retrieve the record using query
        BasicDBList records = readRecord(collection, query);

        // check record is found
        if (records.isEmpty()) {
            throw new RecordNotFoundException();
        }

        // just in case authorize incommming rec
        BasicDBList recordsToFilter = new BasicDBList();
        recordsToFilter.add(record);

        BasicDBList filteredRecords = authManager.execRecordFilters(recordsToFilter);

        // check record was not filtered out

        //check record was returned after filtering
        if (filteredRecords.isEmpty()) {
            throw new DataAccessException("record not updated, invalid security labeling");
        }
        DBObject filteredRecord = (DBObject) filteredRecords.get(0);

        //check record wasn't returned as null
        if (filteredRecord == null) {
            throw new DataAccessException("record not updated, invalid security labeling");
        }

        //check record was not modified during filtering
        if (!record.equals(filteredRecord)) {
            throw new DataAccessException("record not updated, invalid security labeling");
        }

        // update the record in the collection
        try {
            DBCollection dbCollection = database.getCollection(collection);
            dbCollection.save(record, WriteConcern.ACKNOWLEDGED);
        } catch (Exception ex) {
            log.info("updateRecord database error", ex);
            throw new DataAccessException("mongodb error:", ex);

        }
    }

    //Is this method appropriate for this class or should there be another
    //layer of abstraction that does this sort of work?

    //get the keys/labels for all possible values of an attribute
    @Override
    public Map<String, String> getSecurityAttributes(String attributeName) {

        Map<String, String> securityAttributes = null;

        try {
            BasicDBList dbList = readRecord(attributeName);
            if (!dbList.isEmpty()) {

                securityAttributes = new HashMap<String, String>();
                for (Object aDbList : dbList) {
                    BasicDBObject obj = (BasicDBObject) aDbList;
                    securityAttributes.put((String) obj.get(KEY), (String) obj.get(LABEL));
                }
            }
        } catch (Exception e) {
            log.error("Unable to retrieve key/labels for " + attributeName);
        }

        return securityAttributes;
    }

    //Is this method appropriate for this class or should there be another
    //layer of abstraction that does this sort of work?

    //get the label for the given key assigned to a given attribute
    @Override
    public String getSecurityAttributeLabel(String attributeName, String key) {

        BasicDBObject query = new BasicDBObject(KEY, key);

        try {
            BasicDBList dbList = readRecord(attributeName, query);
            if (!dbList.isEmpty()) {
                BasicDBObject obj = (BasicDBObject) dbList.get(0);
                Object label = obj.get(LABEL);
                if (label != null) {
                    return (String) label;
                }
            }
        } catch (Exception e) {
            log.error("Unable to retrieve label assigned to " + key + " of " + attributeName);
        }

        return null;
    }

    private BasicDBList doQuery(DBCollection collection, DBObject query) throws DataAccessException {

        try {
            // execute the query
            DBCursor cursor;
            if (query != null) {
                cursor = collection.find(query);
            } else {
                cursor = collection.find();
            }

            // convert result cursor into list so it can be filtered later
            BasicDBList results = new BasicDBList();
            while (cursor.hasNext()) {
                results.add(cursor.next());
            }

            // not sure if this is needed but it can not hurt
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception ignore) {
                log.info("caught and ignored following exception while closing cursor", ignore);
            }

            // done
            return results;


        } catch (Exception ex) {

            log.error("query error collection: " + collection.getName(), ex);
            throw new DataAccessException("query error collection: " + collection.getName(), ex);
        }
    }

    private boolean isValidCollection(String collection) {

        if (collection == null || collection.length() == 0) {
            return false;
        }

        Set<String> collectionNames = database.getCollectionNames();
        return collectionNames.contains(collection);

    }

    private boolean isValidDocumentKey(String key) {

        if (key == null || key.length() == 0) {
            return false;
        }

        try {
            new ObjectId(key);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}