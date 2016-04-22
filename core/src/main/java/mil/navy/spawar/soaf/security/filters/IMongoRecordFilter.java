package mil.navy.spawar.soaf.security.filters;

import mil.navy.spawar.soaf.security.ISoafUserDetails;
import mil.navy.spawar.soaf.security.SecurityAttributeConfig;

import com.mongodb.BasicDBList;

public interface IMongoRecordFilter {
	
	BasicDBList filter(BasicDBList objs, ISoafUserDetails userDetails);
	String getDatabaseLabel();
	void setDatabaseLabel(String attrName) ;
	SecurityAttributeConfig getAttributeConfig() ;
	void setAttributeConfig(SecurityAttributeConfig attrName) ;

}
