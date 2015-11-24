package mil.navy.spawar.swif.security.filters;

import java.util.Collection;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;
import com.mongodb.BasicDBObject;

public class ClassificationMongoQueryFilter implements IMongoQueryFilter {

	private static final Logger log = (Logger) LoggerFactory.getLogger(ClassificationMongoQueryFilter.class);
	
	private static final char comma  = ',';
	private static final String where = "$where" ;
	private static final String funcName = "satisfies";

	private String databaseLabel ;
	private SecurityAttributeConfig attributeConfig;

	public String getDatabaseLabel() {
		return databaseLabel;
	}

	public void setDatabaseLabel(String value) {
		databaseLabel = value;
	}

	public SecurityAttributeConfig getAttributeConfig() {
		return attributeConfig;
	}

	public void setAttributeConfig(SecurityAttributeConfig value) {
		attributeConfig = value;
	}
	
	@Override
	public BasicDBObject filter(ISwifUserDetails userDetails) {

		String userdetailName = attributeConfig.getUserDetailsName();
		String databaseName = attributeConfig.getDbAttributeName();
		
		// does user have any value(s) for attribute
		if( userDetails.getCustomAttributes().containsKey(userdetailName)) {
		
			// get all value(s) for the attribute for the user
			Collection<String> userDetailsAttributeValues = userDetails.getCustomAttributes().get(userdetailName);
			
			// user must have classification
			if(userDetailsAttributeValues.size()==0) {
				log.error("userdetails does not contain value(s) for: " + userdetailName);
				throw new AuthorizationServiceException("userdetails does not contains entry for " + userdetailName);
			}
			
			// build a mongo query filter using all the attribute values
			StringBuilder sb = new StringBuilder(funcName);
			sb.append("([");
			for( String userDetailAttributeValue: userDetailsAttributeValues) {
				sb.append("'" + userDetailAttributeValue + "'" + comma);
			}
			
			// remove trailing comma
			if(sb.charAt(sb.length()-1) == comma)  {
				sb.deleteCharAt(sb.length()-1);
			}
			sb.append("],this." + databaseLabel + "." + databaseName + ")");
			
			// create query object
			BasicDBObject filter = new BasicDBObject();
			filter.put(where,  sb.toString() );

			log.debug("filter: " +  filter);
			return filter;
			
		} else {
			log.error("userdetails does not contain value(s) for: " + userdetailName);
			throw new AuthorizationServiceException("userdetails does not contains entry for " + userdetailName);
		}
	}
}
