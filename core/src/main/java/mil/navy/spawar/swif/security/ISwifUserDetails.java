package mil.navy.spawar.swif.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

public interface ISwifUserDetails extends UserDetails {

	public Map<String, Collection<String>> getCustomAttributes();
	public String getDistinguishedName();
}