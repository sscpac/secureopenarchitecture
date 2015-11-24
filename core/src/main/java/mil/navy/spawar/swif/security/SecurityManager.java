package mil.navy.spawar.swif.security;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class SecurityManager {
	
	public static ISwifUserDetails getUserDetails() throws AuthenticationException {

		SecurityContext ctx = SecurityContextHolder.getContext();
		Authentication auth = ctx.getAuthentication();
		if(auth != null &&  auth instanceof CasAuthenticationToken ) {
			CasAuthenticationToken casAuthToken = (CasAuthenticationToken)auth;
			ISwifUserDetails userDetails = (ISwifUserDetails)casAuthToken.getUserDetails();
			return userDetails;
		} else {
			throw new AuthenticationCredentialsNotFoundException("unable to find/retieve userDetails from securityContext");
		}
	}

    public static List<String> getDocumentSecurityAttributeValues(BasicDBObject doc,
                                                                   String databaseName,
                                                                   String databaseLabel) {


        // grab all the values for the attribute from mongo obj
        List<String> results = new ArrayList<String>();
        if (doc.containsField(databaseLabel) &&  doc.get(databaseLabel) instanceof BasicDBObject) {
            BasicDBObject securityContext = (BasicDBObject) doc.get(databaseLabel);
            if (securityContext.containsField(databaseName)) {
                Object obj = securityContext.get(databaseName);
                if (obj instanceof BasicDBList) {
                    BasicDBList attrList = (BasicDBList) obj;
                    for (Object attrElement : attrList) {
                        if (attrElement instanceof String) {
                            results.add((String) attrElement);
                        }
                    }
                }
            }
        }
        return results;
    }

}
