package mil.navy.spawar.soaf.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

public interface ISoafUserDetails extends UserDetails {

	Map<String, Collection<String>> getCustomAttributes();
	String getDistinguishedName();
}