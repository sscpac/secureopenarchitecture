package mil.navy.spawar.swif.security.filters;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

import com.mongodb.BasicDBList;

public interface IMongoRecordFilter {
	
	public BasicDBList filter(BasicDBList objs, ISwifUserDetails userDetails);
	public String getDatabaseLabel(); 
	public void setDatabaseLabel(String attrName) ;
	public SecurityAttributeConfig getAttributeConfig() ;
	public void setAttributeConfig(SecurityAttributeConfig attrName) ;

}
