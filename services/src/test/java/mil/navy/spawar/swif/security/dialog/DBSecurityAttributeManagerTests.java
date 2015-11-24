package mil.navy.spawar.swif.security.dialog;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import mil.navy.spawar.swif.data.IMongoDataAccessManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DBSecurityAttributeManagerTests {

	@Mock
	private IMongoDataAccessManager mockAccessManager;
	
	private DBSecurityAttributeManager securityManager;
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		securityManager = new DBSecurityAttributeManager();
		securityManager.setAccessManager(mockAccessManager);
	}
	
	@Test
	public void testGetAllLabels() {
		
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("U", "Unclassified");
		labels.put("C", "Confidential");
		when(mockAccessManager.getSecurityAttributes("classification")).thenReturn(labels);

		Map<String, String> result = securityManager.getAllLabels("classification");
		assertEquals(2,result.size());
		assertTrue(result.containsKey("U"));
		assertTrue(result.containsKey("C"));

		String value = result.get("U");
		assertEquals("Unclassified",value);
		value = result.get("C");
		assertEquals("Confidential",value);
	}
	
	@Test
	public void testGetLabel() {
		
		when(mockAccessManager.getSecurityAttributeLabel("classification", "U")).thenReturn("Unclassified");
		when(mockAccessManager.getSecurityAttributeLabel("classification", "C")).thenReturn("Confidential");

		assertEquals("Unclassified",securityManager.getLabel("classification", "U"));
		assertEquals("Confidential",securityManager.getLabel("classification", "C"));
		
	}

}
