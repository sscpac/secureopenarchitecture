package mil.navy.spawar.soaf.security.filters;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import mil.navy.spawar.soaf.security.SoafUserDetailsImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:relto-module-test-config.xml"})
public class RelToMongoQueryFilterTests {

    private static final String EXPECTED_DB_LABEL = "securityLabel";
    private static final String EXPECTED_DB_ATTR = "RELTO";
    private static final String EXPECTED_USERDETAIL_ATTR = "country";

    private static final String OR = "$or";

    @Autowired(required=true)
    RelToMongoQueryFilter filter;

    @Autowired(required=true)
    SoafUserDetailsImpl userDetails;

    private Map<String, Collection<String>> userAttrs;

    @Before
    public void setUp() {
        assertNotNull(filter);
        assertTrue(filter instanceof RelToMongoQueryFilter);
        assertNotNull(filter.getAttributeConfig());
        assertEquals(EXPECTED_DB_LABEL, filter.getDatabaseLabel());
        assertEquals(EXPECTED_DB_ATTR, filter.getAttributeConfig().getDbAttributeName());
        assertEquals(EXPECTED_USERDETAIL_ATTR, filter.getAttributeConfig().getUserDetailsName());


        assertNotNull(userDetails);
        assertTrue(userDetails instanceof SoafUserDetailsImpl);
        assertTrue(userDetails.getCustomAttributes().containsKey(EXPECTED_USERDETAIL_ATTR));
        assertEquals(1,userDetails.getCustomAttributes().get(EXPECTED_USERDETAIL_ATTR).size());

        userAttrs = userDetails.getCustomAttributes();
    }

    @Test
    public void testUserHasCountry()  {

        int kount = userAttrs.get(EXPECTED_USERDETAIL_ATTR).size();
        String[] userAttrList = userAttrs.get(EXPECTED_USERDETAIL_ATTR).toArray(new String[kount]);

        // do filter
        BasicDBObject filterResult = filter.filter(userDetails);

        // check post-conditions
        assertNotNull(filterResult);
        assertNotNull(filterResult.get(OR));
        assertTrue(filterResult.get(OR) instanceof List);

        String actualResult = filterResult.get(OR).toString();

        StringBuilder expectedResult = new StringBuilder("[");
        expectedResult.append("{ \"" + EXPECTED_DB_LABEL + "." + EXPECTED_DB_ATTR + "\" : \"" + userAttrList[0] + "\"}, ");
        expectedResult.append("{ \"" + EXPECTED_DB_LABEL + "." + EXPECTED_DB_ATTR + "\" : [ ]}");
        expectedResult.append("]");

        assertEquals(expectedResult.toString(), actualResult);
    }

    @Test(expected=AuthorizationServiceException.class)
    @DirtiesContext
    public void testUserHasNoCountryAttribute()  {

        // make user have no access
        userAttrs.get(EXPECTED_USERDETAIL_ATTR).clear();
        assertEquals(0,userAttrs.get(EXPECTED_USERDETAIL_ATTR).size());

        // do filter
        BasicDBObject filterResult = filter.filter(userDetails);

        // check post-conditions
//        assertNotNull(filterResult);
//        assertNotNull(filterResult.get(OR));
//        assertTrue(filterResult.get(OR) instanceof List);
//
//        String actualResult = filterResult.get(OR).toString();
//
//        StringBuilder expectedResult = new StringBuilder("[");
//        expectedResult.append("{ \"" + EXPECTED_DB_LABEL + "." + EXPECTED_DB_ATTR + "\" : \"\"}, ");
//        expectedResult.append("{ \"" + EXPECTED_DB_LABEL + "." + EXPECTED_DB_ATTR + "\" : [ ]}");
//        expectedResult.append("]");
//
//        assertEquals(expectedResult.toString(), actualResult);
    }

    @Test(expected=AuthorizationServiceException.class)    @DirtiesContext
    public void testUserHasNoCountry()  {

        // make user have no attr
        userAttrs.clear();
        assertFalse(userAttrs.containsKey(EXPECTED_USERDETAIL_ATTR));

        // do filter
        BasicDBObject filterResult = filter.filter(userDetails);

        // check post-conditions
//        assertNotNull(filterResult);
//        assertNotNull(filterResult.get(OR));
//        assertTrue(filterResult.get(OR) instanceof List);
//
//        String actualResult = filterResult.get(OR).toString();
//        StringBuilder expectedResult = new StringBuilder("[");
//        expectedResult.append("{ \"" + EXPECTED_DB_LABEL + "." + EXPECTED_DB_ATTR + "\" : \"\"}, ");
//        expectedResult.append("{ \"" + EXPECTED_DB_LABEL + "." + EXPECTED_DB_ATTR + "\" : [ ]}");
//        expectedResult.append("]");
//
//        assertEquals(expectedResult.toString(), actualResult);
    }
}
