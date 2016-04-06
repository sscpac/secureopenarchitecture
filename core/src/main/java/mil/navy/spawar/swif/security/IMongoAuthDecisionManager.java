package mil.navy.spawar.swif.security;

import java.util.List;

import mil.navy.spawar.swif.security.filters.IMongoQueryFilter;
import mil.navy.spawar.swif.security.filters.IMongoRecordFilter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public interface IMongoAuthDecisionManager {

     void setQueryFilters(List<IMongoQueryFilter> qryFilters);
     void setRecordFilters(List<IMongoRecordFilter> recordFilters);

     List<IMongoQueryFilter> getQueryFilters();
     List<IMongoRecordFilter> getRecordFilters();

     List<BasicDBObject> execQueryFilters();
     BasicDBList execRecordFilters(BasicDBList objs);
}