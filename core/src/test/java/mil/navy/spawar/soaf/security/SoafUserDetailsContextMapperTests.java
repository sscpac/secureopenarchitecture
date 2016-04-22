package mil.navy.spawar.soaf.security;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:soafuserdetailscontextmapper-test-beans.xml"})
public class SoafUserDetailsContextMapperTests {
	
	@Autowired(required=true)
	private SoafUserDetailsContextMapper mapper;
	
	@Resource(name="authorities")						 // @autowire does not seem to work
	private List<GrantedAuthority> authorities;          // with util:list beans
	
	@Test
	public void testFullMapper()  {
				
		// init in memory ldap values
		DirContextAdapter ctx = new DirContextAdapter();
		ctx.setDn(new DistinguishedName("cn=foo,ou=users,dc=soaf"));
		ctx.setAttributeValue("uid", "fooUid");
		ctx.setAttributeValue("mail", "fooMail");
		ctx.setAttributeValues("soafUserClassification", new String[] {"CLASSIFICATION-1","CLASSIFICATION-2","CLASSIFICATION-3"});
		ctx.setAttributeValue("soafUserCountry", "USA");
		ctx.setAttributeValues("soafUserSciMarking", new String[] {"SCI-1","SCI-2","SCI-3"});
		ctx.setAttributeValues("soafUserSapMarking", new String[] {"SAP-1","SAP-2","SAP-3"});
		
		// ask mapper to create a user details instance
		Object userDetailsCandidate = mapper.mapUserFromContext(ctx, "fooUser", authorities);
		
		// verify result
		assertTrue(userDetailsCandidate instanceof ISoafUserDetails);
		assertTrue(userDetailsCandidate instanceof SoafUserDetailsImpl);
		ISoafUserDetails userDetails = (SoafUserDetailsImpl) userDetailsCandidate;
		
		// verify user details
		assertEquals("cn=foo,ou=users,dc=soaf",  userDetails.getDistinguishedName());
		assertEquals("fooUser",  userDetails.getUsername());
		
		// custom attributes
		assertEquals(4,userDetails.getCustomAttributes().size());
		
		// classification
		assertTrue(userDetails.getCustomAttributes().containsKey("classification"));
		assertEquals(3,userDetails.getCustomAttributes().get("classification").size());
		assertTrue(userDetails.getCustomAttributes().get("classification").contains("CLASSIFICATION-1"));
		assertTrue(userDetails.getCustomAttributes().get("classification").contains("CLASSIFICATION-2"));
		assertTrue(userDetails.getCustomAttributes().get("classification").contains("CLASSIFICATION-3"));

		// country code
		assertTrue(userDetails.getCustomAttributes().containsKey("country"));
		assertEquals(1,userDetails.getCustomAttributes().get("country").size());
		assertTrue(userDetails.getCustomAttributes().get("country").contains("USA"));
		
		// scimarking
		assertTrue(userDetails.getCustomAttributes().containsKey("sciMarking"));
		assertEquals(3,userDetails.getCustomAttributes().get("sciMarking").size());
		assertTrue(userDetails.getCustomAttributes().get("sciMarking").contains("SCI-1"));
		assertTrue(userDetails.getCustomAttributes().get("sciMarking").contains("SCI-2"));
		assertTrue(userDetails.getCustomAttributes().get("sciMarking").contains("SCI-3"));
		
		// sapmarking
		assertTrue(userDetails.getCustomAttributes().containsKey("sapMarking"));
		assertEquals(3,userDetails.getCustomAttributes().get("sapMarking").size());
		assertTrue(userDetails.getCustomAttributes().get("sapMarking").contains("SAP-1"));
		assertTrue(userDetails.getCustomAttributes().get("sapMarking").contains("SAP-2"));
		assertTrue(userDetails.getCustomAttributes().get("sapMarking").contains("SAP-3"));

		// roles
		assertNotNull(userDetails.getAuthorities());
		assertEquals(2,userDetails.getAuthorities().size());
		SimpleGrantedAuthority[] auths = userDetails.getAuthorities().toArray(new SimpleGrantedAuthority[2]);
		assertEquals("ADMIN", auths[0].getAuthority());
		assertEquals("DEVELOPER", auths[1].getAuthority());
	}

	@Test
	public void testPartialMapper()  {
		
		// init in memory ldap values
		DirContextAdapter ctx = new DirContextAdapter();
		ctx.setDn(new DistinguishedName("cn=foo,ou=users,dc=soaf"));
		ctx.setAttributeValue("uid", "fooUid");
		ctx.setAttributeValue("mail", "fooMail");
		ctx.setAttributeValues("soafUserClassification", new String[] {"CLASSIFICATION-1"});
		ctx.setAttributeValue("soafUserCountry", "USA");
		
		// ask mapper to create a user details instance
		Object userDetailsCandidate = mapper.mapUserFromContext(ctx, "fooUser", authorities);
		assertTrue(userDetailsCandidate instanceof ISoafUserDetails);
		assertTrue(userDetailsCandidate instanceof SoafUserDetailsImpl);
		ISoafUserDetails userDetails = (SoafUserDetailsImpl) userDetailsCandidate;
		
		// verify user details
		assertEquals("cn=foo,ou=users,dc=soaf",  userDetails.getDistinguishedName());
		assertEquals("fooUser",  userDetails.getUsername());
		
		// custom attributes
		assertEquals(4,userDetails.getCustomAttributes().size());
		
		// classification
		assertTrue(userDetails.getCustomAttributes().containsKey("classification"));
		assertEquals(1,userDetails.getCustomAttributes().get("classification").size());
		assertTrue(userDetails.getCustomAttributes().get("classification").contains("CLASSIFICATION-1"));

		// country code
		assertTrue(userDetails.getCustomAttributes().containsKey("country"));
		assertEquals(1,userDetails.getCustomAttributes().get("country").size());
		assertTrue(userDetails.getCustomAttributes().get("country").contains("USA"));
		
		// scimarking
		assertTrue(userDetails.getCustomAttributes().containsKey("sciMarking"));
		assertEquals(0,userDetails.getCustomAttributes().get("sciMarking").size());
		
		// sapmarking
		assertTrue(userDetails.getCustomAttributes().containsKey("sapMarking"));
		assertEquals(0,userDetails.getCustomAttributes().get("sciMarking").size());

		// roles
		assertNotNull(userDetails.getAuthorities());
		assertEquals(2,userDetails.getAuthorities().size());
		SimpleGrantedAuthority[] auths = userDetails.getAuthorities().toArray(new SimpleGrantedAuthority[2]);
		assertEquals("ADMIN", auths[0].getAuthority());
		assertEquals("DEVELOPER", auths[1].getAuthority());
		
	}
}