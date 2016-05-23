package mil.navy.spawar.soaf.security;


import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;

public class SoafUserDetailsImpl implements ISoafUserDetails {

	private static final long serialVersionUID = 1L;
	
	private String password = "***********";
	private String username = null;	
	private boolean enabled = true;
	private boolean accountNonExpired = true;
	private boolean credentialsNonExpired = true;
	private boolean accountNonLocked = true;
	private String distinguishedName = null;
	private Map<String, Collection<String>> customAttributes = new HashMap<String, Collection<String>>();
	
	/* added setter to support spring injection of custom attributes for testing */
	public void setCustomAttributes( Map<String, Collection<String>> attrs) {
		customAttributes = attrs;
	}

	private Collection<? extends GrantedAuthority> roles ;

	public SoafUserDetailsImpl(String username, Collection<? extends GrantedAuthority> roles, String dn) {
		this.roles = roles;
		this.username = username;
		this.distinguishedName = dn;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Map<String, Collection<String>> getCustomAttributes() {
		return customAttributes;
	}
	
	@Override
	public String getDistinguishedName() {
		return distinguishedName;
	}

	public String toJson() throws Exception {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(writer);
        mapper.writeValue(jsonGenerator, this);
        return writer.toString();    	
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder("{");
		sb.append("'type':").append(getClass().getSimpleName()).append(",");
		sb.append("'userName':").append(username).append(",");
		sb.append("'distinguishedName':").append(distinguishedName).append(",");
		sb.append("'password':").append("**************").append(",");
		sb.append("'enabled':").append(enabled).append(",");
	    sb.append("'accountNonExpired':").append(accountNonExpired).append(",");
        sb.append("'credentialsNonExpired':").append(credentialsNonExpired).append(",");
        sb.append("'accountNonLocked':").append(accountNonLocked).append(",");
        
        sb.append("'roles':[");
        boolean firstRole = true;
    	for (GrantedAuthority role : roles) {
    		if( !firstRole ) {
    			sb.append(",");
    		}
    		firstRole = false;
    		sb.append(role);
    	}
        sb.append("]").append(",");
 
        sb.append("'customAttrbutes':{");
        boolean firstEntry = true;
        for (String key : customAttributes.keySet()) {
			if(!firstEntry ) {
				sb.append(",");
			}
			firstEntry = false;	
			sb.append(key).append(":");
			sb.append(collectionToString(customAttributes.get(key)));
        }
        sb.append("}");
		return sb.append("}").toString();
	}

	private String collectionToString(Collection<String> list) {
		StringBuilder sb = new StringBuilder("[");
		boolean firstEntry= true;
		for (String listItem : list) {
			if(!firstEntry ) {
				sb.append(",");
			}
			firstEntry = false;
			sb.append(listItem);
		}
		sb.append("]");
		return sb.toString();
	}

}