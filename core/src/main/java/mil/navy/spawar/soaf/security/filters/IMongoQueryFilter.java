package mil.navy.spawar.soaf.security.filters;

import com.mongodb.BasicDBObject;

import mil.navy.spawar.soaf.security.ISoafUserDetails;
import mil.navy.spawar.soaf.security.SecurityAttributeConfig;

public interface IMongoQueryFilter {
	
	BasicDBObject filter(ISoafUserDetails userDetails);
	String getDatabaseLabel();
	void setDatabaseLabel(String attrName) ;
	SecurityAttributeConfig getAttributeConfig() ;
	void setAttributeConfig(SecurityAttributeConfig attrName) ;
}
