package mil.navy.spawar.swif.security.filters;

import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

import com.mongodb.BasicDBList;

public interface IMongoRecordFilter {
	
	BasicDBList filter(BasicDBList objs, ISwifUserDetails userDetails);
	String getDatabaseLabel();
	void setDatabaseLabel(String attrName) ;
	SecurityAttributeConfig getAttributeConfig() ;
	void setAttributeConfig(SecurityAttributeConfig attrName) ;

}
