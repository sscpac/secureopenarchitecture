package mil.navy.spawar.swif.security.dialog;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import mil.navy.spawar.swif.security.SwifUserDetailsImpl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/config/UserDetailsSecurityInputReqBuilderTests/config.xml"})
public class UserDetailsSecurityInputReqBuilderTests {

	@Autowired(required=true)
	private CasAuthenticationToken authToken;
	
	@Autowired(required=true)
	private SecurityAttributeConfig relToConfig;

	@Autowired(required=true)
	private SecurityAttributeConfig sapConfig;

	@Autowired(required=true)
	private SecurityAttributeConfig sciConfig;

	@Autowired(required=true)
	private SecurityAttributeConfig classificationConfig;

	@Mock
	private DBSecurityAttributeManager mockAttrManager;
	
	private UserDetailsSecurityInputReqBuilder reqBuilder;
	
	@Before
	public void setup() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		
		assertNotNull(relToConfig);
		assertNotNull(sapConfig);
		assertNotNull(classificationConfig);
		assertNotNull(sciConfig);

		assertNotNull(authToken.getUserDetails());
		assertTrue(authToken.getUserDetails() instanceof SwifUserDetailsImpl);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		
		reqBuilder = new UserDetailsSecurityInputReqBuilder();
		reqBuilder.setAttributeManager(mockAttrManager);

	}
	
	
	@Test
	public void testGetAttributeName() {
		
		reqBuilder.setAttributeConfig(classificationConfig);
		assertEquals(classificationConfig.getDbAttributeName(),reqBuilder.getAttributeName());
		
		reqBuilder.setAttributeConfig(sciConfig);
		assertEquals(sciConfig.getDbAttributeName(),reqBuilder.getAttributeName());

		reqBuilder.setAttributeConfig(sapConfig);
		assertEquals(sapConfig.getDbAttributeName(),reqBuilder.getAttributeName());

		reqBuilder.setAttributeConfig(relToConfig);
		assertEquals(relToConfig.getDbAttributeName(),reqBuilder.getAttributeName());

	}
	
	@Test
	public void  testGetInputRequirements() {
		
		reqBuilder.setAttributeConfig(classificationConfig);
		String result = reqBuilder.getInputRequirements();
		assertNotNull(result);
		DBObject dbObj = (DBObject) JSON.parse(result);
		assertTrue(dbObj.containsField("type"));
		assertTrue(dbObj.containsField("displayName"));
		assertTrue(dbObj.containsField("rank"));
		assertTrue(dbObj.containsField("valueSet"));
		Object obj = dbObj.get("displayName");
		assertNotNull(obj);
		assertEquals("Classification",obj.toString());

	
		reqBuilder.setAttributeConfig(sciConfig);
		result = reqBuilder.getInputRequirements();
		assertNotNull(result);
		dbObj = (DBObject) JSON.parse(result);
		assertTrue(dbObj.containsField("type"));
		assertTrue(dbObj.containsField("displayName"));
		assertTrue(dbObj.containsField("rank"));
		assertTrue(dbObj.containsField("valueSet"));
		obj = dbObj.get("displayName");
		assertNotNull(obj);
		assertEquals("SCI Marking",obj.toString());


		reqBuilder.setAttributeConfig(sapConfig);
		result = reqBuilder.getInputRequirements();
		assertNotNull(result);
		dbObj = (DBObject) JSON.parse(result);
		assertTrue(dbObj.containsField("type"));
		assertTrue(dbObj.containsField("displayName"));
		assertTrue(dbObj.containsField("rank"));
		assertTrue(dbObj.containsField("valueSet"));
		obj = dbObj.get("displayName");
		assertNotNull(obj);
		assertEquals("SAP Marking",obj.toString());

		reqBuilder.setAttributeConfig(relToConfig);
		result = reqBuilder.getInputRequirements();
		assertNotNull(result);
		dbObj = (DBObject) JSON.parse(result);
		assertTrue(dbObj.containsField("type"));
		assertTrue(dbObj.containsField("displayName"));
		assertTrue(dbObj.containsField("rank"));
		assertTrue(dbObj.containsField("valueSet"));
		obj = dbObj.get("displayName");
		assertNotNull(obj);
		assertEquals("Release To",obj.toString());
	}
	
	
	@Test
	public void testBuildClassificationValueSet() throws Exception {
		
		// user has U & C
		when(mockAttrManager.getLabel("classification", "U")).thenReturn("Unclassified");
		when(mockAttrManager.getLabel("classification", "C")).thenReturn("Confidential");

		reqBuilder.setAttributeConfig(classificationConfig);
		JSONArray result = reqBuilder.buildValueSet();
		
		assertNotNull(result);
		assertEquals(2,result.size());

		assertTrue(result.get(0) instanceof JSONObject);
		JSONObject rec = (JSONObject) result.get(0);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='U'", rec.containsValue("U"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='Unclassified'", rec.containsValue("Unclassified"));
		
		assertTrue(result.get(1) instanceof JSONObject);
		rec = (JSONObject) result.get(1);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='C'", rec.containsValue("C"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='Confidential'", rec.containsValue("Confidential"));
	}
	
	@Test
	public void testBuildSCIValueSet() throws Exception {
		
		// user has TK
		when(mockAttrManager.getLabel("SCI", "TK")).thenReturn("TALENT KEYHOLE");

		reqBuilder.setAttributeConfig(sciConfig);
		JSONArray result = reqBuilder.buildValueSet();
		
		assertNotNull(result);
		assertEquals(1,result.size());

		assertTrue(result.get(0) instanceof JSONObject);
		JSONObject rec = (JSONObject) result.get(0);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='TK'", rec.containsValue("TK"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='TALENT KEYHOLE'", rec.containsValue("TALENT KEYHOLE"));
	}	
	
	@Test
	public void testBuildSAPValueSet() throws Exception {
		
		// user has BP
		when(mockAttrManager.getLabel("SAP", "BP")).thenReturn("BUTTERED POPCORN");

		reqBuilder.setAttributeConfig(sapConfig);
		JSONArray result = reqBuilder.buildValueSet();
		
		assertNotNull(result);
		assertEquals(1,result.size());

		assertTrue(result.get(0) instanceof JSONObject);
		JSONObject rec = (JSONObject) result.get(0);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='BP'", rec.containsValue("BP"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='BUTTERED POPCORN'", rec.containsValue("BUTTERED POPCORN"));
	}		
	
	@Test
	public void testBuildRELTOValueSet() throws Exception {
		
		// user has USA
		when(mockAttrManager.getLabel("RELTO", "USA")).thenReturn("USA");

		reqBuilder.setAttributeConfig(relToConfig);
		JSONArray result = reqBuilder.buildValueSet();
		
		assertNotNull(result);
		assertEquals(1,result.size());

		assertTrue(result.get(0) instanceof JSONObject);
		JSONObject rec = (JSONObject) result.get(0);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='USA'", rec.containsValue("USA"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='USA'", rec.containsValue("USA"));
	}			
}
