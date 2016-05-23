package mil.navy.spawar.soaf.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.naming.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

@Component
public class SoafUserDetailsContextMapper implements UserDetailsContextMapper {

	private static final Logger log = (Logger) LoggerFactory.getLogger(SoafUserDetailsContextMapper.class);
	private static ArrayList<SecurityAttributeConfig> attributeConfigs;

	public SoafUserDetailsContextMapper() { }
		
	public void setAttributeConfigs(ArrayList<SecurityAttributeConfig> value) {
		attributeConfigs = value;
	}
	
	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> roles) {
		
		// save user dn
		Name dn = ctx.getDn();
		if(log.isDebugEnabled()){
			log.debug("creating custom user details instance for principal: " + dn.toString());
		}
		
		// instantiate new user details object
		ISoafUserDetails userDetails = new SoafUserDetailsImpl(username, roles , dn.toString());
		
		for( SecurityAttributeConfig attributeConfig: attributeConfigs ){
			
			String ldapName = attributeConfig.getLdapAttributeName();
			String userdetailName = attributeConfig.getUserDetailsName();
			
			// query ldap
			String[] attrValues = ctx.getStringAttributes(ldapName);
			
			// init empty list
			Collection<String> attrValueList = new ArrayList<String>();
			
			// if any entries are returned  
			if( attrValues != null && attrValues.length > 0 ) {
				
				// add entries to list
				Collections.addAll(attrValueList, attrValues);
			}
			
			// add the list to the userdetails
			userDetails.getCustomAttributes().put(userdetailName, attrValueList);

		}
		
		if(log.isDebugEnabled()){
			log.debug("user details: " + userDetails.toString());
		}
		
		return userDetails;
	}

	@Override
	public void mapUserToContext(UserDetails arg0, DirContextAdapter arg1) {
		 throw new UnsupportedOperationException("the mapUserToContext() operation is not supported by this implementation");
	}
}