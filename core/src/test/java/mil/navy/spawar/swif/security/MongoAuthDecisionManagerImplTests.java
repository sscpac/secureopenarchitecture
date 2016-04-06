package mil.navy.spawar.swif.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import mil.navy.spawar.swif.security.filters.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mongoauthdecisionmanager-test-beans.xml"})
public class MongoAuthDecisionManagerImplTests {


    @Autowired(required = true)
    private CasAuthenticationToken authToken;

    @Autowired(required = true)
    private IMongoAuthDecisionManager authManager;

    private SwifUserDetailsImpl userDetails;
    private BasicDBList inputData;

    @Before
    public void setUp() {


        // verify we have auth token w/ userDetails
        assertNotNull(authToken.getUserDetails());
        assertTrue(authToken.getUserDetails() instanceof ISwifUserDetails);
        userDetails = (SwifUserDetailsImpl) authToken.getUserDetails();

        // load input data
        //test-jsa
//        inputData = loadJson("C:\\dev\\source\\swif\\core\\src\\test\\resources\\data\\auth-decision-manager-test-data.json");
		inputData = loadJson("/data/auth-decision-manager-test-data.json");
        assertNotNull(inputData);
        assertEquals(6, inputData.size());

        // set userDetails in auth context
        SecurityContextHolder.getContext().setAuthentication(authToken);
        assertNotNull(SecurityContextHolder.getContext());

        // check filters are good
        assertEquals(4, authManager.getQueryFilters().size());
        assertTrue(authManager.getQueryFilters().get(0) instanceof ClassificationMongoQueryFilter);
        assertTrue(authManager.getQueryFilters().get(1) instanceof SCIMongoQueryFilter);
        assertTrue(authManager.getQueryFilters().get(2) instanceof SAPMongoQueryFilter);
        assertTrue(authManager.getQueryFilters().get(3) instanceof RelToMongoQueryFilter);

        assertEquals(4, authManager.getRecordFilters().size());
        assertTrue(authManager.getRecordFilters().get(0) instanceof ClassificationMongoRecordFilter);
        assertTrue(authManager.getRecordFilters().get(1) instanceof SCIMongoRecordFilter);
        assertTrue(authManager.getRecordFilters().get(2) instanceof SAPMongoRecordFilter);
        assertTrue(authManager.getRecordFilters().get(3) instanceof RelToMongoRecordFilter);
    }

    @Test
    public void testExecQueryFilterExecutions() {

        // do filter & check results
        List<BasicDBObject> actualResult = authManager.execQueryFilters();
        assertNotNull(actualResult);
        assertEquals(4, actualResult.size());

    }

    @Test
    @DirtiesContext
    public void testEmptyQueryFilterExecution() {

        List<BasicDBObject> result = null;

        // remove  qry filter
        authManager.getQueryFilters().remove(0);

        // do filter & check results
        result = authManager.execQueryFilters();
        assertNotNull(result);
        assertEquals(3, result.size());

        // remove  qry filter
        authManager.getQueryFilters().remove(0);

        // do filter & check results
        result = authManager.execQueryFilters();
        assertNotNull(result);
        assertEquals(2, result.size());

        // remove  qry filter
        authManager.getQueryFilters().remove(0);

        // do filter & check results
        result = authManager.execQueryFilters();
        assertNotNull(result);
        assertEquals(1, result.size());

        // remove  qry filter
        authManager.getQueryFilters().remove(0);

        // do filter & check results
        result = authManager.execQueryFilters();
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    @Test
    @DirtiesContext
    public void testExecPostFilterExecutions() {

        // do filter & check results
        BasicDBList result = authManager.execRecordFilters(inputData);

        assertNotNull(result);
        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertTrue(result.get(i) instanceof DBObject);
            DBObject rec = (DBObject) result.get(i);
            assertTrue(rec.containsField("_id"));
        }

        // check correct recs returned
        assertEquals("1", ((DBObject) result.get(0)).get("_id"));
        assertEquals("2", ((DBObject) result.get(1)).get("_id"));
        assertEquals("6", ((DBObject) result.get(2)).get("_id"));

        authManager.getRecordFilters().remove(0);

        // do filter & check results
        result = authManager.execRecordFilters(inputData);

        assertNotNull(result);
        assertEquals(4, result.size());
        for (int i = 0; i < 4; i++) {
            assertTrue(result.get(i) instanceof DBObject);
            DBObject rec = (DBObject) result.get(i);
            assertTrue(rec.containsField("_id"));
        }

        // check correct recs returned
        assertEquals("1", ((DBObject) result.get(0)).get("_id"));
        assertEquals("2", ((DBObject) result.get(1)).get("_id"));
        assertEquals("3", ((DBObject) result.get(2)).get("_id"));
        assertEquals("6", ((DBObject) result.get(3)).get("_id"));

        authManager.getRecordFilters().remove(0);

        // do filter & check results
        result = authManager.execRecordFilters(inputData);

        assertNotNull(result);
        assertEquals(5, result.size());
        for (int i = 0; i < 5; i++) {
            assertTrue(result.get(i) instanceof DBObject);
            DBObject rec = (DBObject) result.get(i);
            assertTrue(rec.containsField("_id"));
        }

        // check correct recs returned
        assertEquals("1", ((DBObject) result.get(0)).get("_id"));
        assertEquals("2", ((DBObject) result.get(1)).get("_id"));
        assertEquals("3", ((DBObject) result.get(2)).get("_id"));
        assertEquals("4", ((DBObject) result.get(3)).get("_id"));
        assertEquals("6", ((DBObject) result.get(4)).get("_id"));

        authManager.getRecordFilters().remove(0);

        // do filter & check results
        result = authManager.execRecordFilters(inputData);

        assertNotNull(result);
        assertEquals(6, result.size());
    }

    @Test
    @DirtiesContext
    public void testExecEmptyPostFilters() {

        authManager.getRecordFilters().clear();
        assertEquals(0, authManager.getRecordFilters().size());

        // do filter & check results
        BasicDBList result = authManager.execRecordFilters(inputData);
        assertNotNull(result);
        assertEquals(6, result.size());
        for (int i = 0; i < 6; i++) {
            assertTrue(result.get(i) instanceof DBObject);
            assertTrue(((DBObject) result.get(i)).containsField("_id"));
            assertEquals(Integer.toString(i + 1), ((DBObject) result.get(i)).get("_id"));
        }
    }


    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    @DirtiesContext
    public void testExecPreFilterExecutionsWithNoUserDetails() {

        // clear userDetails
        SecurityContextHolder.getContext().setAuthentication(null);

        // do filter & expect error
        authManager.execQueryFilters();
    }


    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    @DirtiesContext
    public void testExecRecordFilterExecutionsWithNoUserDetails() {

        // clear userDetails
        SecurityContextHolder.getContext().setAuthentication(null);

        // do filter & expect error
        authManager.execRecordFilters(inputData);

    }

    @Test
    @DirtiesContext
    public void testExecPostFilterExecutionsUserHasAccessToEveryThing() {

        // set user so they have all access
        userDetails.getCustomAttributes().get("classification").clear();
        userDetails.getCustomAttributes().get("classification").add("CLASSIFICATION-1");
        userDetails.getCustomAttributes().get("classification").add("CLASSIFICATION-2");
        userDetails.getCustomAttributes().get("classification").add("CLASSIFICATION-3");

        userDetails.getCustomAttributes().get("sapMarking").clear();
        userDetails.getCustomAttributes().get("sapMarking").add("SAP-1");
        userDetails.getCustomAttributes().get("sapMarking").add("SAP-2");
        userDetails.getCustomAttributes().get("sapMarking").add("SAP-3");

        userDetails.getCustomAttributes().get("sciMarking").clear();
        userDetails.getCustomAttributes().get("sciMarking").add("SCI-1");
        userDetails.getCustomAttributes().get("sciMarking").add("SCI-2");
        userDetails.getCustomAttributes().get("sciMarking").add("SCI-3");

        // do filter & check results
        BasicDBList result = authManager.execRecordFilters(inputData);

        assertNotNull(result);
        assertEquals(6, result.size());
        for (int i = 0; i < 6; i++) {
            assertTrue(result.get(i) instanceof DBObject);
            assertTrue(((DBObject) result.get(i)).containsField("_id"));
            assertEquals(Integer.toString(i + 1), ((DBObject) result.get(i)).get("_id"));
        }
    }

    private BasicDBList loadJson(String resource) {
        String jsonData = loadResourceAsString(resource);
        assertNotNull(jsonData);
        Object jsonObjects = JSON.parse(jsonData);
        assertNotNull(jsonObjects);
        assertTrue(jsonObjects instanceof BasicDBList);
        BasicDBList result = (BasicDBList) jsonObjects;
        return result;
    }

    private String loadResourceAsString(String resource) {
        try {
            //test-jsa
//            InputStream is = new FileInputStream(resource);
            InputStream is = this.getClass().getResourceAsStream(resource);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder result = new StringBuilder();
            String txtLine;
            while ((txtLine = br.readLine()) != null) {
                result.append(txtLine);
            }
            return result.toString();
        } catch (Exception ex) {
            return null;
        }
    }
}
