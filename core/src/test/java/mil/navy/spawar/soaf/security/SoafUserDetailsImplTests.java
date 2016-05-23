package mil.navy.spawar.soaf.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SoafUserDetailsImplTests {

	private SoafUserDetailsImpl userDetails;
	private String userDN = "cn=foo,ou=Users,dc=soaf,dc=sd,dc=spawar,dc=navy,dc=mil";
	private String userName = "foo";
	
	@Before
	public void setUp() {
		
		// create user
		Collection<SimpleGrantedAuthority> roles = new ArrayList<SimpleGrantedAuthority>();
		roles.add(new SimpleGrantedAuthority("ADMIN"));
		roles.add(new SimpleGrantedAuthority("DEV"));
		userDetails = new SoafUserDetailsImpl(userName, roles, userDN);
		
		// add custom attrs
		Map<String, Collection<String>> attrs = new HashMap<String, Collection<String>>();
		attrs.put("classification", new ArrayList<String>(Arrays.asList("U","C")));
		attrs.put("sciMarking", new ArrayList<String>(Arrays.asList("TK")));
		attrs.put("sapMarking", new ArrayList<String>(Arrays.asList("BP")));
		attrs.put("country", new ArrayList<String>(Arrays.asList("USA")));
		userDetails.setCustomAttributes(attrs);
	}
		
	@Test
	public void testGetAuthorities() {
		
		Object obj = userDetails.getAuthorities();
		assertNotNull(obj);
		assertTrue(obj instanceof ArrayList<?>);
		ArrayList<?> authorities = (ArrayList<?>)obj;
		assertEquals(2,  authorities.size());
		
		obj = authorities.get(0);
		assertTrue(obj instanceof SimpleGrantedAuthority);
		SimpleGrantedAuthority authority = (SimpleGrantedAuthority)obj;
		assertEquals("ADMIN", authority.getAuthority());

		obj = authorities.get(1);
		assertTrue(obj instanceof SimpleGrantedAuthority);
		authority = (SimpleGrantedAuthority)obj;
		assertEquals("DEV", authority.getAuthority());
	}
	
	@Test
	public void testGetPassword() {
		assertEquals("***********",  userDetails.getPassword());
	}

	@Test
	public void testGetUserName() {
		assertEquals(userName,  userDetails.getUsername());
	}

	@Test
	public void testIsAccountNonExpired() {
		assertEquals(true,  userDetails.isAccountNonExpired());
	}
	
	@Test
	public void testIsAccountNonLocked() {
		assertEquals(true,  userDetails.isAccountNonLocked());
	}
	
	@Test
	public void testIsCredentialsNonExpired() {
		assertEquals(true,  userDetails.isCredentialsNonExpired());
	}
	
	@Test
	public void testIsEnabled() {
		assertEquals(true,  userDetails.isEnabled());
	}
	
	@Test
	public void testGetDistinguishedName() {
		assertEquals(userDN,  userDetails.getDistinguishedName());
	}
	
	@Test
	public void testGetCustomAttributes() {
		
		Object obj = userDetails.getCustomAttributes();
		assertNotNull(obj);
		assertTrue(obj instanceof HashMap<?,?>);
		HashMap<?,?> attrs = (HashMap<?,?>)obj;
		assertEquals(4,  attrs.size());
		
		assertTrue(attrs.containsKey("classification"));
		assertTrue(attrs.containsKey("sciMarking"));
		assertTrue(attrs.containsKey("sapMarking"));
		assertTrue(attrs.containsKey("country"));
		
		obj = attrs.get("classification");
		assertTrue(obj instanceof ArrayList<?>);
		ArrayList<?> attr = (ArrayList<?>)obj;
		assertEquals(2,  attr.size());
		assertEquals("U",  attr.get(0));
		assertEquals("C",  attr.get(1));

		obj = attrs.get("sciMarking");
		assertTrue(obj instanceof ArrayList<?>);
		attr = (ArrayList<?>)obj;
		assertEquals(1,  attr.size());
		assertEquals("TK",  attr.get(0));

		obj = attrs.get("sapMarking");
		assertTrue(obj instanceof ArrayList<?>);
		attr = (ArrayList<?>)obj;
		assertEquals(1,  attr.size());
		assertEquals("BP",  attr.get(0));

		obj = attrs.get("country");
		assertTrue(obj instanceof ArrayList<?>);
		attr = (ArrayList<?>)obj;
		assertEquals(1,  attr.size());
		assertEquals("USA",  attr.get(0));
	}
	
	@Test
	public void testToJson() throws Exception {
		assertNotNull(userDetails.toJson());
	}
	
	@Test
	public void testToString() {
		assertNotNull(userDetails.toString());
	}


}
