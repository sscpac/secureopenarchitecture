package mil.navy.spawar.swif.security.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import mil.navy.spawar.swif.security.SecurityAttributeConfig;
import mil.navy.spawar.swif.security.SwifUserDetailsImpl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/config/AllAttributeSecurityInputReqBuilderTests/config.xml"})
public class AllAttributeSecurityInputReqBuilderTests {

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

	private AllAttributeSecurityInputReqBuilder reqBuilder;
	
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
		
		reqBuilder = new AllAttributeSecurityInputReqBuilder();
		reqBuilder.setAttributeManager(mockAttrManager);

	}
	
	@Test
	public void testInit() throws Exception {
		
		Mockito.doNothing().when(mockAttrManager).preload(any(String.class));
		
		reqBuilder.setAttributeConfig(classificationConfig);
		reqBuilder.init();
	}

	/*
	@Test
	public void testBuildClassificationValueSet() throws Exception {
		
		// user has U & C
		Map<String,String> mockResponse = new HashMap<String, String>();
		mockResponse.put("U", "Unclassified");
		mockResponse.put("C", "Confidential");
		when(mockAttrManager.getAllLabels("classification")).thenReturn(mockResponse);

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
	*/
	
	@Test
	public void testBuildSCIValueSet() throws Exception {
		
		// user has TK
		Map<String,String> mockResponse = new HashMap<String, String>();
		mockResponse.put("TK", "TALENT KEYHOLE");
		when(mockAttrManager.getAllLabels("SCI")).thenReturn(mockResponse);

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
		Map<String,String> mockResponse = new HashMap<String, String>();
		mockResponse.put("BP", "BUTTERED POPCORN");
		when(mockAttrManager.getAllLabels("SAP")).thenReturn(mockResponse);

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

	/*
	@Test
	public void testBuildRELTOValueSet() throws Exception {
		
		// user has USA
		Map<String,String> mockResponse = new HashMap<String, String>();
		mockResponse.put("AUS", "Australia");
		mockResponse.put("CAN", "Canada");
		mockResponse.put("USA", "USA");
		mockResponse.put("GBR", "United Kingdom");
		when(mockAttrManager.getAllLabels("RELTO")).thenReturn(mockResponse);

		reqBuilder.setAttributeConfig(relToConfig);
		JSONArray result = reqBuilder.buildValueSet();
		
		assertNotNull(result);
		assertEquals(4,result.size());
		
		assertTrue(result.get(0) instanceof JSONObject);
		JSONObject rec = (JSONObject) result.get(0);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='USA'", rec.containsValue("USA"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='USA'", rec.containsValue("USA"));

		assertTrue(result.get(1) instanceof JSONObject);
		rec = (JSONObject) result.get(1);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='AUS'", rec.containsValue("AUS"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='Australia'", rec.containsValue("Australia"));
		
		assertTrue(result.get(2) instanceof JSONObject);
		rec = (JSONObject) result.get(2);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='CAN'", rec.containsValue("CAN"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='Canada'", rec.containsValue("Canada"));
		
		assertTrue(result.get(3) instanceof JSONObject);
		rec = (JSONObject) result.get(3);
		assertTrue("expected Key='Value'", rec.containsKey("value"));
		assertTrue("expected Value='GBR'", rec.containsValue("GBR"));
		assertTrue("expected Key='Label'", rec.containsKey("label"));
		assertTrue("expected Value='United Kingdom'", rec.containsValue("United Kingdom"));
}		
*/

}
