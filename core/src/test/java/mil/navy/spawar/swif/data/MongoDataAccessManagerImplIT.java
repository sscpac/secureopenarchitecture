package mil.navy.spawar.swif.data;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder.mongoDb;
import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static mil.navy.spawar.swif.testutils.MongoTestUtils.dbList;
import static org.junit.Assert.*;

import java.util.Map;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.testutils.EmbeddedMongoServer;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:MongoDatabaseFactoryTests-beans.xml"})
public class MongoDataAccessManagerImplIT {

	@Autowired(required=true)
	private CasAuthenticationToken authToken;
	
    @Autowired(required = true)
    private MongoDataAccessManagerImpl dataAccessManager;
    
    private static final String collection = "secureDocs";

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().configure( mongoDb()
    		.port(EmbeddedMongoServer.port)
    		.databaseName(EmbeddedMongoServer.database)
    		.build()
    ).build();

    
	@Before
	public void setUp() {
		
		// verify we have an auth token w/ userDetails 
		assertNotNull(authToken);
		assertNotNull(authToken.getUserDetails());
		assertTrue(authToken.getUserDetails() instanceof ISwifUserDetails);
		
		// update the security context w/ the auth token
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertNotNull(SecurityContextHolder.getContext());
		
		// verify we have dataAccessManager ref
		assertNotNull(dataAccessManager);
	}
	
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/readRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/readRecord/data-expected.json")
	public void testReadRecord() throws Exception  {
    	
    	// try to read entire collection
    	BasicDBList results = dataAccessManager.readRecord(collection);
    	
    	// verify response
    	assertNotNull(results);
    	assertEquals(2,results.size());
    	
    	// check rec returned
    	assertTrue(results.get(0) instanceof BasicDBObject);
    	BasicDBObject rec = (BasicDBObject) results.get(0);
    	assertTrue( rec.containsField("name"));
    	assertEquals("doc-1", rec.get("name"));
    	
    	assertTrue(results.get(1) instanceof BasicDBObject);
    	rec = (BasicDBObject) results.get(1);
    	assertTrue( rec.containsField("name"));
    	assertEquals("doc-7", rec.get("name"));
	}
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/readRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/readRecord/data-expected.json")
	public void testReadRecordQuery() throws Exception  {
    	
    	// try to read part of collection
    	BasicDBObject query = new BasicDBObject();
		query.put("name",  "doc-1");
		BasicDBList results = dataAccessManager.readRecord(collection, query);
    	
    	// check response
    	assertNotNull(results);
    	assertEquals(1,results.size());
    	
    	// verify rec
    	assertTrue(results.get(0) instanceof BasicDBObject);
    	BasicDBObject rec = (BasicDBObject) results.get(0);
    	assertTrue( rec.containsField("name"));
    	assertEquals("doc-1", rec.get("name"));
    	
    	// try to read record that does not exist
		query = new BasicDBObject();
		query.put("name",  "does-not-exist");
    	results = dataAccessManager.readRecord(collection, query);

    	// check response
    	assertNotNull(results);
    	assertEquals(0,results.size());
    }
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/readRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/readRecord/data-expected.json")
	public void testReadRecordQueryNoRecords() throws Exception  {
    	
    	// try to read record that does not exist
    	BasicDBObject query = new BasicDBObject();
		query.put("name",  "does-not-exist");
		BasicDBList results = dataAccessManager.readRecord(collection, query);

    	// check response
    	assertNotNull(results);
    	assertEquals(0,results.size());
    }    

    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/readRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/readRecord/data-expected.json")
	public void testReadRecordInvalidCollection() throws Exception  {
    	
    	try{
    		dataAccessManager.readRecord("foobar");  // invalid collection
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof DataAccessException);
    		assertEquals("invalid collection", ex.getMessage());
    	}
    }
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/readRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/readRecord/data-expected.json")
	public void testReadRecordNotAuthenticatedr() throws Exception  {
    	
    	try{
    		SecurityContextHolder.getContext().setAuthentication(null);  // erase user details
    		dataAccessManager.readRecord(collection);
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof AuthenticationCredentialsNotFoundException);
    	}    	
	}

    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/deleteRecord/data-expected.json")
	public void testDeleteRecord() throws Exception  {
    	
    	// try delete record
    	dataAccessManager.deleteRecord(collection, "000000000000000000000001");
	}    
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/deleteRecord/data-expected-error.json")
	public void testDeleteRecordNoAccess() throws Exception  {
    	
    	// try delete record the user can not access
    	try{
    		dataAccessManager.deleteRecord(collection, "000000000000000000000002");
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof RecordNotFoundException);
    	}
    }
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/deleteRecord/data-expected-error.json")
	public void testDeleteRecordDoesNotExist() throws Exception  {
    	
    	// try delete record the user can not access
    	try{
    		dataAccessManager.deleteRecord(collection, "000000000000000000000099");
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof RecordNotFoundException);
    	}
    }    
  
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/deleteRecord/data-expected-error.json")
	public void testDeleteRecordInvalidCollection() throws Exception  {

    	// try delete record with bad collection
    	try{
    		dataAccessManager.deleteRecord("foobar", "000000000000000000000001");
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof DataAccessException);
    		assertEquals("invalid collection", ex.getMessage());
    	}
    }
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/deleteRecord/data-expected-error.json")
	public void testDeleteRecordInvalidKey() throws Exception  {

       	// try delete record with bad key
    	try{
    		dataAccessManager.deleteRecord(collection, "foobar");
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof DataAccessException);
    		assertEquals("invalid key", ex.getMessage());
    	}    	
	}        
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/updateRecord/data-expected.json")
    public void testUpdateRecord() throws Exception  { 
    	
    	// read a record
		BasicDBObject query = new BasicDBObject();
		query.put("name",  "doc-1");
		BasicDBList results = dataAccessManager.readRecord(collection, query);
 
		// verify
	   	assertNotNull(results);
    	assertEquals(1,results.size());
    	assertTrue(results.get(0) instanceof BasicDBObject);
    	BasicDBObject rec = (BasicDBObject) results.get(0);
    	assertTrue( rec.containsField("name"));
    	assertEquals("doc-1", rec.get("name"));
 
    	// update the record
    	rec.put("name", "foobar");
    	dataAccessManager.updateRecord(collection, rec);
  	}   
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/updateRecord/data-expected-error.json")
	public void testUpdateRecordNoAccess() throws Exception  {
    	
    	// try update record the user can not access
    	try{
        	BasicDBObject rec = new BasicDBObject();
        	rec.put("_id", new ObjectId("000000000000000000000002"));
        	rec.put("name", "foobar");
    		dataAccessManager.updateRecord(collection, rec);
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof RecordNotFoundException);
    	}
    }
    
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/updateRecord/data-expected-error.json")
	public void testUpdateRecordDoesNotExist() throws Exception  {
    	
    	// try update record the user can not access
    	try{
        	BasicDBObject rec = new BasicDBObject();
        	rec.put("_id", new ObjectId("000000000000000000000099"));
        	rec.put("name", "foobar");
    		dataAccessManager.updateRecord(collection, rec);
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof RecordNotFoundException);
    	}
    }

    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/updateRecord/data-expected-error.json")
	public void testUpdateInvalidCollection() throws Exception  {
    
       	// try delete record with bad collection
    	try{
        	BasicDBObject rec = new BasicDBObject();
        	rec.put("_id", new ObjectId("000000000000000000000001"));
        	rec.put("name", "foobar");
    		dataAccessManager.updateRecord("foobar", rec);
    		fail("expected exception not thrown");
    	} catch(Exception ex) {
    		assertTrue(ex instanceof DataAccessException);
    		assertEquals("invalid collection", ex.getMessage());
    	}    	
	}           
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/createRecord/data-expected.json")
    public void testCreateRecord() throws Exception  { 
    	
    	// create new rec
    	BasicDBObject rec = new BasicDBObject();
    	rec.put("_id", new ObjectId("000000000000000000000099"));
    	rec.put("name", "doc-99");
    	BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"U"}));
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"TK"}));
        securityLabel.put("SAP", dbList(new String[]{"BP"}));
        rec.put("securityLabel", securityLabel);
        rec.put("foo-1", "bar-1");
        rec.put("foo-2", "bar-2");
        
        // save
		dataAccessManager.createRecord(collection, rec);
   	}
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/createRecord/data-expected-error.json")
    public void testCreateRecordNoAccess() throws Exception  { 
    	        
       	try{
        	// create new rec
        	BasicDBObject rec = new BasicDBObject();
        	rec.put("_id", new ObjectId("000000000000000000000099"));
        	rec.put("name", "doc-99");
        	BasicDBObject securityLabel = new BasicDBObject();
            securityLabel.put("classification", dbList(new String[]{"S"}));  // user does not have access
            securityLabel.put("RELTO", dbList(new String[]{"USA"}));
            securityLabel.put("SCI", dbList(new String[]{"TK", "COMINT"}));  // user does not have access
            securityLabel.put("SAP", dbList(new String[]{"BP", "SWAGGER"}));  // user does not have access
            rec.put("securityLabel", securityLabel);
            rec.put("foo-1", "bar-1");
            rec.put("foo-2", "bar-2");

            // save
       	 	dataAccessManager.createRecord(collection, rec);
    	} catch(Exception ex) {
    		assertTrue(ex instanceof DataAccessException);
    		assertEquals("record not created", ex.getMessage());
    	}    	
    }
    
    @Test
    @UsingDataSet(locations={"/data/mongoDataAccessManagerImplIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/mongoDataAccessManagerImplIT/createRecord/data-expected-error.json")
    public void testCreateRecordInvalidCollection() throws Exception  { 
     
      	try{
        	// create new rec
        	BasicDBObject rec = new BasicDBObject();
        	rec.put("_id", new ObjectId("000000000000000000000099"));
        	rec.put("name", "doc-99");
        	BasicDBObject securityLabel = new BasicDBObject();
            securityLabel.put("classification", dbList(new String[]{"U"}));   // user has access
            securityLabel.put("RELTO", dbList(new String[]{"USA"}));  // user has access
            securityLabel.put("SCI", dbList(new String[]{"TK"}));  // user has access
            securityLabel.put("SAP", dbList(new String[]{"BP"}));  // user has access
            rec.put("securityLabel", securityLabel);
            rec.put("foo-1", "bar-1");
            rec.put("foo-2", "bar-2");

            // save
       	 	dataAccessManager.createRecord("foobar", rec);  // invalid collection
    	} catch(Exception ex) {
    		assertTrue(ex instanceof DataAccessException);
    		assertEquals("invalid collection", ex.getMessage());
    	}    	       	
   	}       
    
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetClassificationSecurityAttributes() throws Exception  { 
     
   		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("classification");  
		assertEquals(2, securityAttributes.size());
		assertTrue("expected U entry", securityAttributes.containsKey("U"));
		assertTrue("expected C entry", securityAttributes.containsKey("C"));
		assertEquals("expected Unclassified entry", securityAttributes.get("U"), "Unclassified");
		assertEquals("expected Confidential entry", securityAttributes.get("C"), "Confidential");
   	}       
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetRelToSecurityAttributes() throws Exception  { 
     
   		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("RELTO");  
		assertEquals(4, securityAttributes.size());
		assertTrue("expected AUS entry", securityAttributes.containsKey("AUS"));
		assertTrue("expected CAN entry", securityAttributes.containsKey("CAN"));
		assertTrue("expected USA entry", securityAttributes.containsKey("USA"));
		assertTrue("expected GBR entry", securityAttributes.containsKey("GBR"));
		assertEquals("expected Australia entry", securityAttributes.get("AUS"), "Australia");
		assertEquals("expected Canada entry", securityAttributes.get("CAN"), "Canada");
		assertEquals("expected USA entry", securityAttributes.get("USA"), "USA");
		assertEquals("expected United Kingdom entry", securityAttributes.get("GBR"), "United Kingdom");
   	}       
 
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSAPSecurityAttributes() throws Exception  { 
     
   		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("SAP");  
		assertEquals(1, securityAttributes.size());
		assertTrue("expected BP entry", securityAttributes.containsKey("BP"));
		assertEquals("expected BUTTERED POPCORN entry", securityAttributes.get("BP"), "BUTTERED POPCORN");
   	}       

    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSCISecurityAttributes() throws Exception  { 
     
   		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("SCI");  
		assertEquals(1, securityAttributes.size());
		assertTrue("expected TK entry", securityAttributes.containsKey("TK"));
		assertEquals("expected TALENT KEYHOLE entry", securityAttributes.get("TK"), "TALENT KEYHOLE");
   	}       

    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSecurityAttributesInvalidCollection() throws Exception  { 
     
   		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("foo");  
		assertNull(securityAttributes);
  	}    
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetClassificationSecurityAttributeLabel() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("classification", "U");
		assertNotNull(label);
		assertEquals("Unclassified", label);		
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetClassificationSecurityAttributeLabelDoesNotExist() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("classification", "FOO");
		assertNull(label);
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetClassificationSecurityAttributeLabelNoAccess() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("classification", "S");
		assertNull(label);
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetRelToSecurityAttributeLabel() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("RELTO", "USA");
		assertNotNull(label);
		assertEquals("USA", label);		
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetRelToSecurityAttributeLabelDoesNotExist() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("RELTO", "FOO");
		assertNull(label);
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetRelToSecurityAttributeLabelNoAccess() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("RELTO", "CAN");
		assertEquals("Canada", label);
   	}         
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSAPSecurityAttributeLabel() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("SAP", "BP");
		assertNotNull(label);
		assertEquals("BUTTERED POPCORN", label);		
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSAPSecurityAttributeLabelNoAccess() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("SAP", "SWAGGER");
		assertNull(label);
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSAPSecurityAttributeLabelDoesNotExist() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("SAP", "FOO");
		assertNull(label);   	
	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSCISecurityAttributeLabel() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("SCI", "TK");
		assertNotNull(label);
		assertEquals("TALENT KEYHOLE", label);		
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSCISecurityAttributeLabelNoAccess() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("SCI", "KLONDIKE");
		assertNull(label);
   	}     
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSCISecurityAttributeLabelDoesNotExist() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("SCI", "FOO");
		assertNull(label);   	
	}         
    
    @Test
    @UsingDataSet(locations={"/data/security-label-data.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/security-label-data.json")
    public void testGetSecurityAttributeLabelInvalidCollection() throws Exception  { 
    	
		String label = dataAccessManager.getSecurityAttributeLabel("FOO", "BAR");
		assertNull(label);   	
	}         
}
