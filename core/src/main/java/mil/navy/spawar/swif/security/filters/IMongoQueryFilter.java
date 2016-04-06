package mil.navy.spawar.swif.security.filters;

import com.mongodb.BasicDBObject;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

public interface IMongoQueryFilter {
	
	BasicDBObject filter(ISwifUserDetails userDetails);
	String getDatabaseLabel();
	void setDatabaseLabel(String attrName) ;
	SecurityAttributeConfig getAttributeConfig() ;
	void setAttributeConfig(SecurityAttributeConfig attrName) ;
}
