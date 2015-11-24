package mil.navy.spawar.swif.security.filters;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityAttributeConfig;

import java.util.Set;

public interface IMongoNodeFilter {

    public void initUserAttributes(ISwifUserDetails userDetails);
    public boolean initNewRecord(BasicDBObject obj);
	public boolean filter(BasicDBObject obj);
    public void processWrite(BasicDBObject obj);
	public void setDatabaseLabel(String databaseLabel) ;
	public void setAttributeConfig(SecurityAttributeConfig attrName) ;
    public Set<String> getFinalAggregate();
    public SecurityAttributeConfig getAttributeConfig();
}