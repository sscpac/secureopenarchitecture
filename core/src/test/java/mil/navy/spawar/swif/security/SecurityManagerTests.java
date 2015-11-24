package mil.navy.spawar.swif.security;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:security-manager-test-beans.xml"})
public class SecurityManagerTests {

	@Autowired
	private CasAuthenticationToken authToken;
	
	@Before
	public void setUp() {
		
		// verify we have mock user details info
		assertNotNull(authToken);
		
	}
		
	@Test
	public void testGetUserDetails()  {
		
		// set auth context
		SecurityContextHolder.getContext().setAuthentication(authToken);
		
		// ask manager for user details
		ISwifUserDetails userDetails = SecurityManager.getUserDetails();
		assertNotNull(userDetails);
	}

	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void testGetUserDetailsExpectException()  {
		
		// clear auth context
		SecurityContextHolder.getContext().setAuthentication(null);
		
		// ask manager for user details
		SecurityManager.getUserDetails();
	}
}
