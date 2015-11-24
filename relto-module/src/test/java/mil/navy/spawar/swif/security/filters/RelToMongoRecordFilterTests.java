package mil.navy.spawar.swif.security.filters;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import mil.navy.spawar.swif.security.SwifUserDetailsImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:relto-module-test-config.xml"})
public class RelToMongoRecordFilterTests {

    private static final String EXPECTED_DB_LABEL = "securityLabel";
    private static final String EXPECTED_DB_ATTR = "RELTO";
    private static final String EXPECTED_USERDETAIL_ATTR = "country";


    @Autowired(required=true)
    private RelToMongoRecordFilter filter;

    @Autowired(required=true)
    private SwifUserDetailsImpl userDetails;

    private BasicDBList inputData;
    private Map<String, Collection<String>> userAttrs;

    @Before
    public void setUp() {

        // check filter config
        assertNotNull(filter);
        assertTrue(filter instanceof RelToMongoRecordFilter);
        assertEquals(EXPECTED_DB_LABEL, filter.getDatabaseLabel());
        assertEquals(EXPECTED_DB_ATTR, filter.getAttributeConfig().getDbAttributeName());
        assertEquals(EXPECTED_USERDETAIL_ATTR, filter.getAttributeConfig().getUserDetailsName());

        // check userDetail config
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof SwifUserDetailsImpl);
        assertNotNull(userDetails.getCustomAttributes());
        assertTrue(userDetails.getCustomAttributes().containsKey(EXPECTED_USERDETAIL_ATTR));
        userAttrs = userDetails.getCustomAttributes();

        // load input data
        inputData = loadJson("/relto-module-test-data.json");
        assertNotNull(inputData);
        assertTrue(inputData instanceof BasicDBList);
        assertEquals(5, (inputData).size());
    }


    @Test
    @DirtiesContext
    public void testUserDetailsCountryIsUSA()  {

        // set user attrs
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("USA");

        // do filter
        BasicDBList result = filter.filter(inputData, userDetails);

        // check post-conditions
        assertNotNull(result);
        
        // all recs should have been returned
        assertEquals(5,result.size());
        
        // rec(0) should have USA & CAN
        BasicDBObject rec = (BasicDBObject) result.get(0);
        assertDocRelToSize(rec, 2);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "CAN");
      
        // rec(1) should have only USA 
        rec = (BasicDBObject) result.get(1);
        assertDocRelToSize(rec, 1);
    	assertDocRelToContains(rec, "USA");

        // rec(2)  should have USA & GBR
        rec = (BasicDBObject) result.get(2);
        assertDocRelToSize(rec, 2);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "GBR");

        // rec(3)  should have USA & AUS
        rec = (BasicDBObject) result.get(3);
        assertDocRelToSize(rec, 2);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "AUS");

        // rec(4)  should have none
        rec = (BasicDBObject) result.get(4);
        assertDocRelToSize(rec, 0);

     }

    @Test
    @DirtiesContext
    public void testUserDetailsCountryIsFOO()  {

        // set user attrs
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("FOO");

        // do filter
        BasicDBList result = filter.filter(inputData, userDetails);

        // check post-conditions
        assertNotNull(result);
        
        // all recs should have been returned
        assertEquals(5,result.size());
        
        // rec(0) should have USA & CAN & FOO
        BasicDBObject rec = (BasicDBObject) result.get(0);
        assertDocRelToSize(rec, 3);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "CAN");
    	assertDocRelToContains(rec, "FOO");
      
        // rec(1) should have only USA & FOO
        rec = (BasicDBObject) result.get(1);
        assertDocRelToSize(rec, 2);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "FOO");

        // rec(2)  should have USA & GBR & FOO
        rec = (BasicDBObject) result.get(2);
        assertDocRelToSize(rec, 3);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "GBR");
    	assertDocRelToContains(rec, "FOO");

        // rec(3)  should have USA & AUS & FOO
        rec = (BasicDBObject) result.get(3);
        assertDocRelToSize(rec, 3);
    	assertDocRelToContains(rec, "USA");
    	assertDocRelToContains(rec, "AUS");
    	assertDocRelToContains(rec, "FOO");

        // rec(4)  should have none
        rec = (BasicDBObject) result.get(4);
        assertDocRelToSize(rec, 0);
        
    }
    

    @Test
    @DirtiesContext
    public void testDataHasNoRelto()  {

        // set user attrs
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).add("ZWB");

        // remove securitylabel.RELTO values from all records
        for(int i=0; i<5; i++) {
        	
            assertTrue(inputData.get(i) instanceof BasicDBObject);
            BasicDBObject rec = (BasicDBObject) inputData.get(i);
            
            // clear relto entries
            clearDocRelToEmpty(rec);
            
            // check empty
            assertDocRelToEmpty(rec);
        }

        // do filter
        BasicDBList result = filter.filter(inputData, userDetails);

        // check post-conditions
        assertNotNull(result);
        
        // should get back all 5 recs
        assertEquals(5,result.size());
        
        // check each rec.relto
        for(int i=0; i<5; i++) {
        	
        	// relto should be empty still
        	BasicDBObject rec = (BasicDBObject) result.get(i);
        	assertDocRelToEmpty(rec);
        }
    }

    @Test(expected=AuthorizationServiceException.class)
    @DirtiesContext
    public void testUserHasNoCountryAttribute()  {

        // make user have no country attrs
        userAttrs.clear();
        assertFalse(userAttrs.containsKey(EXPECTED_USERDETAIL_ATTR));

        // do filter, should generate error
        filter.filter(inputData, userDetails);
    }
    
    @Test(expected=AuthorizationServiceException.class)
    @DirtiesContext
    public void testUserHasNoCountryAttributeEntries()  {

        // make user have no country attr values
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
        assertEquals(0,userAttrs.get(EXPECTED_USERDETAIL_ATTR).size());
        
        // do filter, should generate error
        filter.filter(inputData, userDetails);
    }

    
    // helper method to check doc relto contains a value
    private void assertDocRelToContains(BasicDBObject doc,  String value) {

    	// get reltos
 	    BasicDBList reltos = getRelToListFromDoc(doc);

	    // assert securityLabel.relto contains
	    assertTrue(reltos.contains(value));
    }
    
    // helper method to check doc relto contains number of items
    private void assertDocRelToSize(BasicDBObject doc, int kount) {

    	// get reltos
 	    BasicDBList reltos = getRelToListFromDoc(doc);

	    // assert securityLabel.relto contains number of items
	    assertEquals(kount, reltos.size());
    }
    
 
    
    // helper method to check doc relto is empty
    private void assertDocRelToEmpty(BasicDBObject doc) {

    	// get reltos
 	    BasicDBList reltos = getRelToListFromDoc(doc);
	    
	    // assert securityLabel.relto empty
	    assertTrue(reltos.isEmpty());
    }
    
    // helper method to clear doc relto 
    private void clearDocRelToEmpty(BasicDBObject doc) {

    	// get reltos
 	    BasicDBList reltos = getRelToListFromDoc(doc);
	    
	    // assert securityLabel.relto empty
 	    reltos.clear();
	    assertTrue(reltos.isEmpty());
    }
     
    // helper method to get relto from doc
    private BasicDBList getRelToListFromDoc(BasicDBObject doc) {

    	// assert securityLabel
    	assertTrue(doc.containsField(EXPECTED_DB_LABEL));
	    Object securityContextObj = doc.get(EXPECTED_DB_LABEL);
	    assertTrue(securityContextObj instanceof BasicDBObject);
	    BasicDBObject securityContext = (BasicDBObject) securityContextObj;
	    
	    // assert securityLabel.relto
	    assertTrue(securityContext.containsField(EXPECTED_DB_ATTR));
	    Object obj = securityContext.get(EXPECTED_DB_ATTR);
	    assertTrue(obj instanceof BasicDBList);
	    BasicDBList reltos = (BasicDBList) obj;
	    
	    return reltos;

    }

    // helper method to parse json
    private BasicDBList loadJson(String resource)  {
        String jsonData = loadResourceAsString(resource);
        assertNotNull(jsonData);
        Object jsonObjects = JSON.parse(jsonData);
        assertNotNull(jsonObjects);
        assertTrue(jsonObjects instanceof BasicDBList);
        BasicDBList result = (BasicDBList) jsonObjects;
        return result;
    }

    // help to load resource file
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
