package mil.navy.spawar.soaf.security;

import java.util.*;

import mil.navy.spawar.soaf.security.filters.IMongoQueryFilter;
import mil.navy.spawar.soaf.security.filters.IMongoRecordFilter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class MongoAuthDecisionManagerImpl implements IMongoAuthDecisionManager {

    List<IMongoQueryFilter> qryFilters = new ArrayList<IMongoQueryFilter>();
    List<IMongoRecordFilter> recordFilters = new ArrayList<IMongoRecordFilter>();

    @Override
    public void setQueryFilters(List<IMongoQueryFilter> value) {
        qryFilters = value;
    }

    @Override
    public void setRecordFilters(List<IMongoRecordFilter> value) {
        recordFilters = value;
    }

    @Override
    public List<IMongoQueryFilter> getQueryFilters() {
        return qryFilters;
    }

    @Override
    public List<IMongoRecordFilter> getRecordFilters() {
        return recordFilters;
    }

    @Override
    public List<BasicDBObject> execQueryFilters() {

        ISoafUserDetails userDetails = SecurityManager.getUserDetails();

        List<BasicDBObject> filters = new ArrayList<BasicDBObject>();
        for (IMongoQueryFilter qrtFilter : qryFilters) {
            BasicDBObject filter = qrtFilter.filter(userDetails);
            if (filter != null) {
                filters.add(filter);
            }
        }
        return filters;
    }

    @Override
    public BasicDBList execRecordFilters(BasicDBList objs) {

        ISoafUserDetails userDetails = SecurityManager.getUserDetails();

        if (recordFilters.size() == 0) {
            return objs;
        }

        BasicDBList results = objs;
        for (IMongoRecordFilter resultFilter : recordFilters) {
            results = resultFilter.filter(results, userDetails);
        }
        return results;
    }

}