package mil.navy.spawar.swif.security;

import java.util.List;

import com.mongodb.DBObject;
import mil.navy.spawar.swif.security.filters.IMongoNodeFilter;
import mil.navy.spawar.swif.security.filters.IMongoQueryFilter;
import mil.navy.spawar.swif.security.filters.IMongoRecordFilter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public interface IMongoAuthDecisionManager {

    public void setDatabaseLabel(String databaseLabel);
    public void setAggregateLabel(String aggregateLabel);
    public void setIdLabel(String idLabel);
    public void setUnacknowledgedConfig(SecurityAttributeConfig unacknowledgedConfig);

    public void setQueryFilters(List<IMongoQueryFilter> qryFilters);
	public void setRecordFilters(List<IMongoRecordFilter> recordFilters);
    public void setNodeFilters(List<IMongoNodeFilter> nodeFilters);

    public List<IMongoQueryFilter> getQueryFilters();
	public List<IMongoRecordFilter> getRecordFilters();
    public List<IMongoNodeFilter> getNodeFilters();

    public List<BasicDBObject> execQueryFilters();
	public BasicDBList execRecordFilters(BasicDBList objs);
    public BasicDBList execNodeFilters(BasicDBList objs);
    public void execNodeWriteProccesors(DBObject record);
}