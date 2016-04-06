package mil.navy.spawar.swif.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.MongoAuthDecisionManagerImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:MongoDataAccessManagerImplTests-beans.xml"})
public class MongoDataAccessManagerImplTests {
	
	@Autowired(required=true)
	private CasAuthenticationToken authToken;
	
    @Autowired(required = true)
    private MongoAuthDecisionManagerImpl authFilters;

    // gobal mocked objects
	private DB mockDb;
	private DBCollection secureCollection;
	
	// class being tested
	private MongoDataAccessManagerImpl dataAccessManager;
	
	// test data
	private ArrayList<DBObject> mockData; 
	
	private static final String ID_ATTR = "_id";
	
	@Before
	public void setUp() {
		
		// verify filter are configured
		assertNotNull(authFilters);
		assertEquals(4,authFilters.getQueryFilters().size());
		assertEquals(4,authFilters.getRecordFilters().size());


        // verify we have an auth token w/ userDetails
		assertNotNull(authToken);
		assertNotNull(authToken.getUserDetails());
		assertTrue(authToken.getUserDetails() instanceof ISwifUserDetails);
		
		// update the security context w/ the auth token
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertNotNull(SecurityContextHolder.getContext());
		
		// load the test data
        //test-jsa
		mockData = loadMockData("secureDocs", "/data/MongoDataAccessManagerImplTests-data.json");
//        mockData = loadMockData("secureDocs",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\MongoDataAccessManagerImplTests-data.json");


        assertEquals(6, mockData.size());
		
		// mock mongo db
		mockDb = mock(DB.class);
		
		// mock mongo collection
		secureCollection = mock(DBCollection.class);
		when(secureCollection.getName()).thenReturn("secureDocs");

		// mock mongo db methods
		String[] validCollectionNames = new String[]{"secureDocs"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockDb.getCollection("secureDocs")).thenReturn(secureCollection);

		// tell dataAccessManager to use mock mongo
	   	dataAccessManager = new MongoDataAccessManagerImpl();
	   	dataAccessManager.setDatabase(mockDb);

	   	// set filters
	   	dataAccessManager.setAuthorizationManager(authFilters);
	}


	@Test
	public void testReadRecord() throws Exception  {
		
		// mock cursor to return 3 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(0)).thenReturn(mockData.get(1)).thenReturn(mockData.get(2)).thenReturn(null);
		
		// mock collection find 
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		BasicDBList result = dataAccessManager.readRecord("secureDocs");
		
		// check correct rec(s) filtered
		assertEquals(3, result.size());
	}

	@Test
	public void testReadRecordUnAuthorized() throws Exception  {
		
		// mock cursor to return 0 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(false);
		when(mockCursor.next()).thenReturn(null);
		
		// mock collection find 
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		BasicDBList result = dataAccessManager.readRecord("secureDocs");
		
		// check correct rec(s) filtered
		assertEquals(0, result.size());
	}	

	@Test
	public void testReadRecordDatabaseError() throws Exception  {
		
		try {
			when(secureCollection.find()).thenThrow(new MongoException("whoops"));
			when(secureCollection.find(any(BasicDBObject.class))).thenThrow(new MongoException("whoops"));
			
			dataAccessManager.readRecord("secureDocs");
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("query error collection: secureDocs", ex.getMessage());
			assertNotNull(ex.getCause());
			assertTrue(ex.getCause() instanceof MongoException);
			assertEquals("whoops", ex.getCause().getMessage());
		}
	}

	@Test
	public void testReadRecordInvalidCollection() throws Exception  {
		
		// mock collectionExits
		when(mockDb.collectionExists("foobar")).thenReturn(false);

		try {
			dataAccessManager.readRecord("foobar");
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertEquals("invalid collection", ex.getMessage());
		}
	}	
	
	@Test
	public void testReadRecordWithQuery() throws Exception  {
		
		// mock cursor to return 1 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(0)).thenReturn(null);
		
		// mock collection find 
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		BasicDBObject where  = new BasicDBObject("name", "doc-1"); 
		BasicDBList result = dataAccessManager.readRecord("secureDocs" , where);
		
		// check correct rec(s) filtered
		assertEquals(1, result.size());
	}

	@Test
	public void testReadRecordWithQueryUnAuthorized() throws Exception  {
		
		//mock cursor to return 0 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(false);
		when(mockCursor.next()).thenReturn(null);
		
		// mock collection find 
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		BasicDBObject where  = new BasicDBObject("name", "doc-1"); 
		BasicDBList result = dataAccessManager.readRecord("secureDocs", where);
		
		// check correct rec(s) filtered
		assertEquals(0, result.size());
	}	

	@Test
	public void testReadRecordWithQueryDatabaseError() throws Exception  {
		try {
			
			// mock collection find 
			when(secureCollection.find()).thenThrow(new MongoException("whoops"));
			when(secureCollection.find(any(BasicDBObject.class))).thenThrow(new MongoException("whoops"));
			
			BasicDBObject where  = new BasicDBObject("name", "doc-1"); 
			dataAccessManager.readRecord("secureDocs", where);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("query error collection: secureDocs", ex.getMessage());
			assertNotNull(ex.getCause());
			assertTrue(ex.getCause() instanceof MongoException);
			assertEquals("whoops", ex.getCause().getMessage());
		}
	}

	@Test
	public void testReadRecordWithQueryInvalidCollection() throws Exception  {
		
		// mock collectionExits
		when(mockDb.collectionExists("foobar")).thenReturn(false);

		try {
			BasicDBObject where  = new BasicDBObject("name", "doc-1"); 
			dataAccessManager.readRecord("foobar", where);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertEquals("invalid collection", ex.getMessage());
		}
	}	

	@Test
	public void testupdateRecord() throws Exception  {
		
		// get rec to update
		BasicDBObject recordToUpdate = (BasicDBObject)mockData.get(0);
		
		// mock cursor to return 1 record 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(0)).thenReturn(null);
		
		// collection save is expected to return writeResult
		WriteResult mockWriteResult = mock(WriteResult.class);
		when(mockWriteResult.getN()).thenReturn(1);
		CommandResult mockCommandResult = mock(CommandResult.class);
		when(mockCommandResult.getErrorMessage()).thenReturn("");
		//when(mockWriteResult.getLastError()).thenReturn(mockCommandResult);  - getLastError() was deprecated
		
		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		when(secureCollection.save(any(BasicDBObject.class))).thenReturn(mockWriteResult);
		
		// add attr
		recordToUpdate.put("foo", "bar");
		
		dataAccessManager.updateRecord("secureDocs", recordToUpdate);
	}	

	@Test
	public void testUpdateRecordUnauthorizedPreFilter() throws Exception  {
		
		BasicDBObject recordToUpdate = (BasicDBObject)mockData.get(0);

		// mock cursor to return 0 rec
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(false);
		when(mockCursor.next()).thenReturn(null);

		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		try {
			recordToUpdate.put("foo", "bar");
			dataAccessManager.updateRecord("secureDocs", recordToUpdate);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof RecordNotFoundException);
		}
	}	

	@Test
	public void testUpdateRecordUnauthorizedPostFilter() throws Exception  {
		
		BasicDBObject recordToUpdate = (BasicDBObject)mockData.get(3);

		// mock cursor to return 1 rec
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(3)).thenReturn(null);

		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		try {
			recordToUpdate.put("foo", "bar");
			dataAccessManager.updateRecord("secureDocs", recordToUpdate);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
            //RecordNotFoundException would be a valid exception
			//assertTrue(ex instanceof AuthorizationException);
		}
	}
	
	@Test
	public void testUpdateRecordDatabaseError() throws Exception  {
		
		BasicDBObject recordToUpdate = (BasicDBObject)mockData.get(0);
		
		// collection find is expected to return 1 record 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(0)).thenReturn(null);
		
		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		when(secureCollection.save(any(BasicDBObject.class),any(WriteConcern.class))).thenThrow(new MongoException("whoops"));
		
		try {
			recordToUpdate.put("foo", "bar");
			dataAccessManager.updateRecord("secureDocs", recordToUpdate);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("mongodb error:", ex.getMessage());
			assertNotNull(ex.getCause());
			assertTrue(ex.getCause() instanceof MongoException);
			assertEquals("whoops", ex.getCause().getMessage());
		}
	}	
	
	@Test
	public void testUpdateRecordInvalidCollection() throws Exception  {
		
		// mock collectionExits
		when(mockDb.collectionExists("foobar")).thenReturn(false);

		BasicDBObject recordToUpdate = (BasicDBObject)mockData.get(0);	
		
		try {
			recordToUpdate.put("foo", "bar");
			dataAccessManager.updateRecord("foobar", recordToUpdate);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("invalid collection", ex.getMessage());
		}
	}	

	@Test
	public void testUpdateRecordInvalidRecord() throws Exception  {
		
		try {
			dataAccessManager.updateRecord("secureDocs", null);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("invalid record", ex.getMessage());
		}
	}	

	@Test
	public void testDeleteRecord() throws Exception  {
		
		String OBJECT_ID = "000000000000000000000001";
		
		// mock cursor to return 1 record 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(0)).thenReturn(null);
		
		// collection save is expected to return writeResult
		WriteResult mockWriteResult = mock(WriteResult.class);
		when(mockWriteResult.getN()).thenReturn(1);
		CommandResult mockCommandResult = mock(CommandResult.class);
		when(mockCommandResult.getErrorMessage()).thenReturn("");
		//when(mockWriteResult.getLastError()).thenReturn(mockCommandResult);  - getLastError() was deprecated
		
		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		when(secureCollection.remove(any(BasicDBObject.class))).thenReturn(mockWriteResult);
		
		dataAccessManager.deleteRecord("secureDocs", OBJECT_ID);
	}	
	
	@Test
	public void testDeleteRecordUnauthorized() throws Exception  {
		
		String OBJECT_ID = "000000000000000000000001";

		// mock cursor to return 0 record 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(false);
		when(mockCursor.next()).thenReturn(null);

		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		try {
			dataAccessManager.deleteRecord("secureDocs", OBJECT_ID);
			fail("expected Exception exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof RecordNotFoundException);
		}
	}	


	@Test
	public void testDeleteRecordDatabaseError() throws Exception  {
		
		String OBJECT_ID = "000000000000000000000001";
		
		// mock cursor to return 1 record 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(mockData.get(0)).thenReturn(null);
		
		// mock collection find & save operations
		when(secureCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		when(secureCollection.remove(any(BasicDBObject.class),any(WriteConcern.class))).thenThrow(new MongoException("whoops"));
		
		try {
			dataAccessManager.deleteRecord("secureDocs", OBJECT_ID);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("mongodb error:", ex.getMessage());
			assertNotNull(ex.getCause());
			assertTrue(ex.getCause() instanceof MongoException);
			assertEquals("whoops", ex.getCause().getMessage());
		}
	}	

	@Test
	public void testDeleteRecordInvalidCollection() throws Exception  {
		
		// mock collectionExits
		when(mockDb.collectionExists("foobar")).thenReturn(false);

		String OBJECT_ID = "000000000000000000000001";
		
		try {
			dataAccessManager.deleteRecord("foobar", OBJECT_ID);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("invalid collection", ex.getMessage());
		}
	}	

	@Test
	public void testDeleteRecordInvalidKey() throws Exception  {
		try {
			dataAccessManager.deleteRecord("secureDocs", "foobar");
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("invalid key", ex.getMessage());
		}
	}	
	
	@Test
	public void testInsertRecord() throws Exception  {
		
		BasicDBObject recordToInsertWithId = (BasicDBObject)mockData.get(0);
		BasicDBObject recordToInsert = removeIdFromRecord(recordToInsertWithId);
		dataAccessManager.createRecord("secureDocs", recordToInsert);
	}	
	
	@Test
	public void testInsertRecordWithId() throws Exception  {
		
		BasicDBObject recordToInsert = (BasicDBObject)mockData.get(0);
		dataAccessManager.createRecord("secureDocs", recordToInsert);
	}	
	
	@Test
	public void testInsertRecordUnauthorized() throws Exception  {
		
		BasicDBObject recordToInsertWithId = (BasicDBObject)mockData.get(3);
		BasicDBObject recordToInsert = removeIdFromRecord(recordToInsertWithId);
		
		try {
			dataAccessManager.createRecord("secureDocs", recordToInsert);	
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
		}
	}	

	@Test
	public void testInsertRecordDatabaseError() throws Exception  {
		
		BasicDBObject recordToInsertWithId = (BasicDBObject)mockData.get(0);
		BasicDBObject recordToInsert = removeIdFromRecord(recordToInsertWithId);

		// mock collection find & save operations
		when(secureCollection.insert(any(DBObject.class),any(WriteConcern.class))).thenThrow(new MongoException("whoops"));
		
		try {
			dataAccessManager.createRecord("secureDocs", recordToInsert);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("mongodb error:", ex.getMessage());
			assertNotNull(ex.getCause());
			assertTrue(ex.getCause() instanceof MongoException);
			assertEquals("whoops", ex.getCause().getMessage());
		}
	}		
	
	@Test
	public void testInsertRecordInvalidCollection() throws Exception  {
		
		// mock collectionExits
		when(mockDb.collectionExists("foobar")).thenReturn(false);

		BasicDBObject recordToInsertWithId = (BasicDBObject)mockData.get(0);
		BasicDBObject recordToInsert = removeIdFromRecord(recordToInsertWithId);
	
		try {
			dataAccessManager.createRecord("foobar", recordToInsert);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("invalid collection", ex.getMessage());
		}
	}	

	@Test
	public void testInsertRecordInvalidRecord() throws Exception  {
		try {
			dataAccessManager.createRecord("secureDocs", null);
			fail("expected Exception not thrown");
		} catch(Exception ex) {
			assertNotNull(ex);
			assertTrue(ex instanceof DataAccessException);
			assertEquals("invalid record", ex.getMessage());
		}
	}	
	
	@Test
	public void  testGetClassificationSecurityAttributes()  throws Exception {

        //test-jsa
//        ArrayList<DBObject> securityLabels  = loadMockData("classification",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");
		ArrayList<DBObject> securityLabels  = loadMockData("classification", "/data/security-label-data.json");
		assertEquals(4, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		when(mockCollection.getName()).thenReturn("classification");
		String[] validCollectionNames = new String[]{"classification"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockDb.getCollection("classification")).thenReturn(mockCollection);
		
		// mock cursor to return 4 records
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(0)).thenReturn(securityLabels.get(1))
                .thenReturn(securityLabels.get(2)).thenReturn(securityLabels.get(3)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("classification");
		assertEquals(4, securityAttributes.size());
		assertTrue("expected U entry", securityAttributes.containsKey("U"));
		assertTrue("expected C entry", securityAttributes.containsKey("C"));
		assertEquals("expected Unclassified entry", securityAttributes.get("U"), "Unclassified");
		assertEquals("expected Confidential entry", securityAttributes.get("C"), "Confidential");
	}
	
	@Test
	public void  testGetReltoSecurityAttributes()  throws Exception {

        //test-jsa
		ArrayList<DBObject> securityLabels  = loadMockData("RELTO", "/data/security-label-data.json");
//        ArrayList<DBObject> securityLabels  = loadMockData("RELTO",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");

		assertEquals(4, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		when(mockCollection.getName()).thenReturn("RELTO");

		// mock mongo db methods
		String[] validCollectionNames = new String[]{"RELTO"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(secureCollection.getName()).thenReturn("RELTO");
		when(mockDb.getCollection("RELTO")).thenReturn(mockCollection);
		
		// mock cursor to return 4 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(0)).thenReturn(securityLabels.get(1)).thenReturn(securityLabels.get(2)).thenReturn(securityLabels.get(3)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
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
	public void  testGetSAPSecurityAttributes()  throws Exception {

        ///test-jsa
//        ArrayList<DBObject> securityLabels  = loadMockData("SAP",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");
        ArrayList<DBObject> securityLabels  = loadMockData("SAP", "/data/security-label-data.json");
		assertEquals(3, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		String[] validCollectionNames = new String[]{"SAP"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockCollection.getName()).thenReturn("SAP");
		when(mockDb.getCollection("SAP")).thenReturn(mockCollection);
		
		// mock cursor to return 3 records
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(0))
                .thenReturn(securityLabels.get(1)).thenReturn(securityLabels.get(2)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("SAP");
		assertEquals(3, securityAttributes.size());
		assertTrue("expected BP entry", securityAttributes.containsKey("BP"));
		assertTrue("expected SWAGGER entry", securityAttributes.containsKey("SWAGGER"));
		assertEquals("expected BUTTERED POPCORN entry", securityAttributes.get("BP"), "BUTTERED POPCORN");
		assertEquals("expected SWAGGER entry", securityAttributes.get("SWAGGER"), "SWAGGER");

	}
	
	@Test
	public void  testGetSCISecurityAttributes()  throws Exception {

        // load the test data
        //test-jsa
        ArrayList<DBObject> securityLabels  = loadMockData("SCI", "/data/security-label-data.json");
//        ArrayList<DBObject> securityLabels  = loadMockData("SCI", "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data/security-label-data.json");
		assertEquals(4, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		String[] validCollectionNames = new String[]{"SCI"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockCollection.getName()).thenReturn("SCI");
		when(mockDb.getCollection("SCI")).thenReturn(mockCollection);
		
		// mock cursor to return 2 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(1)).thenReturn(securityLabels.get(2)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
				
		Map<String, String> securityAttributes = dataAccessManager.getSecurityAttributes("SCI");
		assertEquals(2, securityAttributes.size());
		assertTrue("expected KLONDIKE entry", securityAttributes.containsKey("KLONDIKE"));
		assertTrue("expected COMINT entry", securityAttributes.containsKey("COMINT"));
		assertEquals("expected KLONDIKE entry", securityAttributes.get("KLONDIKE"), "KLONDIKE");
		assertEquals("expected COMINT entry", securityAttributes.get("COMINT"), "COMINT");		

	}
	
	@Test
	public void  testGetClassificationSecurityAttributeLabels()  throws Exception {

        //test-jsa
//        ArrayList<DBObject> securityLabels  = loadMockData("classification",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");
        ArrayList<DBObject> securityLabels  = loadMockData("classification", "/data/security-label-data.json");
		assertEquals(4, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		when(mockCollection.getName()).thenReturn("classification");
		String[] validCollectionNames = new String[]{"classification"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockDb.getCollection("classification")).thenReturn(mockCollection);
		
		// mock cursor to return 1 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(3)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		String label = dataAccessManager.getSecurityAttributeLabel("classification", "U");
		assertEquals("Unclassified", label);		
	}
	
	@Test
	public void  testGetReltoSecurityAttributeLabels()  throws Exception {

        //test-jsa
//        ArrayList<DBObject> securityLabels  = loadMockData("RELTO",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");
        ArrayList<DBObject> securityLabels  = loadMockData("RELTO", "/data/security-label-data.json");
		assertEquals(4, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		when(mockCollection.getName()).thenReturn("RELTO");
		String[] validCollectionNames = new String[]{"RELTO"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockDb.getCollection("RELTO")).thenReturn(mockCollection);
		
		// mock cursor to return 1 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(1)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		String label = dataAccessManager.getSecurityAttributeLabel("RELTO", "CAN");
		assertEquals("Canada", label);
	}
		
	@Test
	public void  testGetSAPtoSecurityAttributeLabels()  throws Exception {

        //test-jsa
//        ArrayList<DBObject> securityLabels  = loadMockData("SAP",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");
        ArrayList<DBObject> securityLabels  = loadMockData("SAP", "/data/security-label-data.json");
		assertEquals(3, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		when(mockCollection.getName()).thenReturn("SAP");
		String[] validCollectionNames = new String[]{"SAP"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockDb.getCollection("SAP")).thenReturn(mockCollection);
		
		// mock cursor to return 1 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(0)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		String label = dataAccessManager.getSecurityAttributeLabel("SAP", "BP");
		assertEquals("BUTTERED POPCORN", label);		
	}
			
	@Test
	public void  testGetSCItoSecurityAttributeLabels()  throws Exception {

        //test-jsa
//        ArrayList<DBObject> securityLabels  = loadMockData("SCI",
//                "C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\security-label-data.json");
        ArrayList<DBObject> securityLabels  = loadMockData("SCI", "/data/security-label-data.json");
		assertEquals(4, securityLabels.size());
		
		// mock mongo collection
		DBCollection mockCollection = mock(DBCollection.class);
		when(mockCollection.getName()).thenReturn("SCI");
		String[] validCollectionNames = new String[]{"SCI"};
		Set<String> validCollectionNameSet = new HashSet<String>(Arrays.asList(validCollectionNames));
		when(mockDb.getCollectionNames()).thenReturn(validCollectionNameSet);
		when(mockDb.getCollection("SCI")).thenReturn(mockCollection);
		
		// mock cursor to return 1 records 
		DBCursor mockCursor = mock(DBCursor.class);
		when(mockCursor.hasNext()).thenReturn(true).thenReturn(false);
		when(mockCursor.next()).thenReturn(securityLabels.get(3)).thenReturn(null);
		
		// mock collection find 
		when(mockCollection.find(any(BasicDBObject.class))).thenReturn(mockCursor);
		
		String label = dataAccessManager.getSecurityAttributeLabel("SCI", "TK");
		assertEquals("TALENT KEYHOLE", label);		
	}
				
	
	// helper method to remove id attr from mongo rec
	private BasicDBObject removeIdFromRecord(BasicDBObject rec) {
		if( rec.containsField(ID_ATTR)) {
			rec.remove(ID_ATTR);
		}
		assertFalse(rec.containsField(ID_ATTR));
		return rec;
	}
	
	// helper method to load array of mogo records into array
	private ArrayList<DBObject> loadMockData(String collection, String resource) {
		
		assertNotNull(resource);
		String json = loadResourceAsString(resource);
		assertNotNull(json);
		DBObject dbObject = (DBObject) JSON.parse(json);
		assertNotNull(dbObject);
		assertTrue(dbObject.containsField(collection));
		assertTrue(dbObject.get(collection) instanceof BasicDBList);
		BasicDBList docs = (BasicDBList) dbObject.get(collection);
		ArrayList<DBObject> result = new ArrayList<DBObject>();
		for(int i=0; i< docs.size(); i++ ){
			assertTrue(docs.get(i) instanceof DBObject);
			result.add((DBObject)docs.get(i));
		}
		return result;
	}
	
	private String loadResourceAsString(String resource)  {
    	InputStream is = null;
    	try {

            //test-jsa
//            is = new FileInputStream(resource);
            is = this.getClass().getResourceAsStream(resource);
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
}