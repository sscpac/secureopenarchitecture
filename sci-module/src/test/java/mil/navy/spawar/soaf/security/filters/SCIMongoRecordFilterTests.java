package mil.navy.spawar.soaf.security.filters;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import mil.navy.spawar.soaf.security.SoafUserDetailsImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:sci-module-test-config.xml"})
public class SCIMongoRecordFilterTests {

	private static final String EXPECTED_DB_LABEL = "securityLabel";
	private static final String EXPECTED_DB_ATTR = "SCI";
	private static final String EXPECTED_USERDETAIL_ATTR = "sciMarking";


	@Autowired(required=true)
	private SCIMongoRecordFilter filter;
	
	@Autowired(required=true)
	private SoafUserDetailsImpl userDetails;
	
	private BasicDBList inputData;
	private Map<String, Collection<String>> userAttrs;

	@Before
	public void setUp() {
		
		// check filter config
		assertNotNull(filter);
		assertTrue(filter instanceof SCIMongoRecordFilter);
		assertEquals(EXPECTED_DB_LABEL, filter.getDatabaseLabel());
		assertEquals(EXPECTED_DB_ATTR, filter.getAttributeConfig().getDbAttributeName());
		assertEquals(EXPECTED_USERDETAIL_ATTR, filter.getAttributeConfig().getUserDetailsName());
		
		// check userDetail config
		assertNotNull(userDetails);
		assertTrue(userDetails instanceof SoafUserDetailsImpl);
		assertNotNull(userDetails.getCustomAttributes());
		assertTrue(userDetails.getCustomAttributes().containsKey(EXPECTED_USERDETAIL_ATTR));
		userAttrs = userDetails.getCustomAttributes();
		
		// load input data
		inputData = loadJson("/sci-module-test-data.json");
		assertNotNull(inputData);
		assertTrue(inputData instanceof BasicDBList);
		assertEquals(5, ((BasicDBList) inputData).size());
	}
	

	@Test
	@DirtiesContext
	public void testCanSeeEveryThing()  {
		
		// set user attrs
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-1");
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-2");
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-3");
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-4");
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-5");
		
		// do filter
		BasicDBList result = filter.filter(inputData, userDetails);
		
		// check post-conditions
		assertNotNull(result);
		assertEquals(5,result.size());
	}

	@Test
	@DirtiesContext
	public void testCanNotSeeAnyThing()  {
		
		// make user have no access
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
		assertEquals(0,userAttrs.get(EXPECTED_USERDETAIL_ATTR).size());
		
		// do filter
		BasicDBList result = filter.filter(inputData, userDetails);
		
		// check post-conditions
		assertNotNull(result);
		assertEquals(0,result.size());
	}

	@Test
	@DirtiesContext
	public void testUserCanSeeSomeRecords()  {
		
		DBObject rec = null;
		BasicDBList result = null;
		
		// set user attrs
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-1");
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-2");

		// do filter
		result = filter.filter(inputData, userDetails);
		
		// check post-conditions
		assertNotNull(result);
		assertEquals(2,result.size());
		
		// check correct rec return
		assertNotNull(result.get(0));
		assertTrue(result.get(0) instanceof DBObject);
		rec = (DBObject) result.get(0);
		assertTrue(rec.containsField("name"));
		assertEquals("doc-1",rec.get("name"));

		// check correct rec return
		assertNotNull(result.get(1));
		assertTrue(result.get(1) instanceof DBObject);
		rec = (DBObject) result.get(1);
		assertTrue(rec.containsField("name"));
		assertEquals("doc-2",rec.get("name"));
		
		// remove some user access
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-1");

		// do filter
		result = filter.filter(inputData, userDetails);
		
		// check post-conditions
		assertNotNull(result);
		assertEquals(1,result.size());
		
		// check correct rec return
		assertNotNull(result.get(0));
		assertTrue(result.get(0) instanceof DBObject);
		rec = (DBObject) result.get(0);
		assertTrue(rec.containsField("name"));
		assertEquals("doc-1",rec.get("name"));

	}

	@Test
	@DirtiesContext
	public void testDataIsNotLabeled()  {
		
		// set user attrs
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-1");
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("SCI-2");
		
		// remove markings from data
		for(int i=0; i<5; i++) {
			assertTrue(inputData.get(i) instanceof BasicDBObject);
			BasicDBObject rec = (BasicDBObject) inputData.get(i);
			assertTrue(rec.containsField(EXPECTED_DB_LABEL));
			Object securityContextObj = rec.get(EXPECTED_DB_LABEL);
			assertTrue(securityContextObj instanceof BasicDBObject);
			BasicDBObject securityContext = (BasicDBObject) securityContextObj;
			assertTrue(securityContext.containsField(EXPECTED_DB_ATTR));
			securityContext.removeField(EXPECTED_DB_ATTR);
			assertFalse(securityContext.containsField(EXPECTED_DB_ATTR));
		}
		
		// do filter
		BasicDBList result = filter.filter(inputData, userDetails);
		
		// check post-conditions
		assertNotNull(result);
		assertEquals(5,result.size());
	}

	@Test
	@DirtiesContext
	public void testUserHasNoSciMarking()  {
		
		// make user have no access
		userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
		assertEquals(0,userAttrs.get(EXPECTED_USERDETAIL_ATTR).size());

		// do filter
		BasicDBList result = filter.filter(inputData, userDetails);
		assertNotNull(result);
		assertEquals(0,result.size());
	}
	
	@Test
	@DirtiesContext
	public void testUserHasNoSciAttribute()  {
		
		// make user have no attr
		userAttrs.clear();
		assertFalse(userAttrs.containsKey(EXPECTED_USERDETAIL_ATTR));

		// do filter
		BasicDBList result = filter.filter(inputData, userDetails);
		assertNotNull(result);
		assertEquals(0,result.size());
	}
	
	private BasicDBList loadJson(String resource)  {
		String jsonData = loadResourceAsString(resource);
		assertNotNull(jsonData);
		Object jsonObjects = JSON.parse(jsonData);
		assertNotNull(jsonObjects);
		assertTrue(jsonObjects instanceof BasicDBList);
		BasicDBList result = (BasicDBList) jsonObjects;
		return result;
	}
	
    private String loadResourceAsString(String resource)  {
    	try {
			InputStream is = this.getClass().getResourceAsStream(resource);
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
