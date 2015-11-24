package mil.navy.spawar.swif.security.filters;

import com.mongodb.BasicDBObject;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

public interface IMongoQueryFilter {
	
	public BasicDBObject filter(ISwifUserDetails userDetails);
	public String getDatabaseLabel(); 
	public void setDatabaseLabel(String attrName) ;
	public SecurityAttributeConfig getAttributeConfig() ;
	public void setAttributeConfig(SecurityAttributeConfig attrName) ;
}
