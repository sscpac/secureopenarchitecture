package mil.navy.spawar.swif.security;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:sample-mock-auth-beans.xml"})
public class SampleMockAuthenticationTests {

	@Autowired
	private CasAuthenticationToken mockAuthToken;
	
	@Before
	public void setUp() {
		assertNotNull(mockAuthToken);
		SecurityContextHolder.getContext().setAuthentication(mockAuthToken);
	}
		
	@Test
	public void testSampleTest()  {
		
		// get auth token
		assertNotNull(SecurityContextHolder.getContext());
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		Object authenticationCandidate = SecurityContextHolder.getContext().getAuthentication();
		assertTrue(authenticationCandidate instanceof CasAuthenticationToken);

		// make sure it is swif user
		CasAuthenticationToken authToken = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(authToken.getUserDetails());
		Object userDetailsCandidate = authToken.getUserDetails();
		assertTrue(userDetailsCandidate instanceof ISwifUserDetails);
		assertTrue(userDetailsCandidate instanceof SwifUserDetailsImpl);
		ISwifUserDetails userDetails = (SwifUserDetailsImpl) userDetailsCandidate;

		// check some attributes
		assertEquals("cn=swifUser,ou=Users,dc=spawar,dc=navy,dc=mil",  userDetails.getDistinguishedName());
		assertEquals("swifUser",  userDetails.getUsername());
		
		// custom attributes
		assertEquals(5,userDetails.getCustomAttributes().size());
		
		// classification
		assertTrue(userDetails.getCustomAttributes().containsKey("classification"));
		assertEquals(2,userDetails.getCustomAttributes().get("classification").size());
		assertTrue(userDetails.getCustomAttributes().get("classification").contains("CLASSIFICATION-1"));
		assertTrue(userDetails.getCustomAttributes().get("classification").contains("CLASSIFICATION-2"));

		// country code
		assertTrue(userDetails.getCustomAttributes().containsKey("RELTO"));
		assertEquals(1,userDetails.getCustomAttributes().get("RELTO").size());
		assertTrue(userDetails.getCustomAttributes().get("RELTO").contains("USA"));
		
		// scimarking
		assertTrue(userDetails.getCustomAttributes().containsKey("SCI"));
		assertEquals(2,userDetails.getCustomAttributes().get("SCI").size());
		assertTrue(userDetails.getCustomAttributes().get("SCI").contains("SCI-1"));
		assertTrue(userDetails.getCustomAttributes().get("SCI").contains("SCI-2"));
		
		// sapmarking
		assertTrue(userDetails.getCustomAttributes().containsKey("SAP"));
		assertEquals(2,userDetails.getCustomAttributes().get("SAP").size());
		assertTrue(userDetails.getCustomAttributes().get("SAP").contains("SAP-1"));
		assertTrue(userDetails.getCustomAttributes().get("SAP").contains("SAP-2"));

		// dac roles
		assertTrue(userDetails.getCustomAttributes().containsKey("DAC"));
		assertEquals(2,userDetails.getCustomAttributes().get("DAC").size());
		assertTrue(userDetails.getCustomAttributes().get("DAC").contains("DAC-1"));
		assertTrue(userDetails.getCustomAttributes().get("DAC").contains("DAC-2"));
		
		// roles
		assertEquals(2,userDetails.getAuthorities().size());
		assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE-1")));
		assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE-2")));
		

	}
}
