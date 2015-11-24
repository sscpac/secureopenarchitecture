package mil.navy.spawar.swif.security;

import java.util.*;

import com.mongodb.DBObject;
import mil.navy.spawar.swif.data.UnacknowledgedStatus;
import mil.navy.spawar.swif.security.filters.IMongoNodeFilter;
import mil.navy.spawar.swif.security.filters.IMongoQueryFilter;
import mil.navy.spawar.swif.security.filters.IMongoRecordFilter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class MongoAuthDecisionManagerImpl implements IMongoAuthDecisionManager {

    List<IMongoQueryFilter> qryFilters = new ArrayList<IMongoQueryFilter>();
    List<IMongoRecordFilter> recordFilters = new ArrayList<IMongoRecordFilter>();
    List<IMongoNodeFilter> nodeFilters = new ArrayList<IMongoNodeFilter>();
    private String databaseLabel;
    private String aggregateLabel;
    private String idLabel;
    private SecurityAttributeConfig unacknowledgedConfig;

    @Override
    public void setQueryFilters(List<IMongoQueryFilter> value) {
        qryFilters = value;
    }

    @Override
    public void setRecordFilters(List<IMongoRecordFilter> value) {
        recordFilters = value;
    }

    @Override
    public void setNodeFilters(List<IMongoNodeFilter> nodeFilters) {
        this.nodeFilters = nodeFilters;
    }

    @Override
    public void setDatabaseLabel(String databaseLabel) {
        this.databaseLabel = databaseLabel;
    }

    @Override
    public void setAggregateLabel(String aggregateLabel) {
        this.aggregateLabel = aggregateLabel;
    }

    @Override
    public void setIdLabel(String idLabel) {
        this.idLabel = idLabel;
    }

    @Override
    public void setUnacknowledgedConfig(SecurityAttributeConfig unacknowledgedConfig) {
        this.unacknowledgedConfig = unacknowledgedConfig;
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
    public List<IMongoNodeFilter> getNodeFilters() {
        return nodeFilters;
    }


    @Override
    public List<BasicDBObject> execQueryFilters() {

        ISwifUserDetails userDetails = SecurityManager.getUserDetails();

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

        ISwifUserDetails userDetails = SecurityManager.getUserDetails();

        if (recordFilters.size() == 0) {
            return objs;
        }

        BasicDBList results = objs;
        for (IMongoRecordFilter resultFilter : recordFilters) {
            results = resultFilter.filter(results, userDetails);
        }
        return results;
    }

    @Override
    public BasicDBList execNodeFilters(BasicDBList inList) {

        //get userdetails
        ISwifUserDetails userDetails = SecurityManager.getUserDetails();

        // init output list
        BasicDBList outList = new BasicDBList();

        //init node filters
        for (IMongoNodeFilter nodeFilter : nodeFilters) {
            nodeFilter.initUserAttributes(userDetails);
        }

        // iterate through each obj in the list
        for (Object obj : inList) {

            if (obj == null) {
                outList.add(null); //carry the null records
            } else {

                //init filters for each record
                boolean initSuccess = true;
                for (IMongoNodeFilter nodeFilter : nodeFilters) {
                    if (!nodeFilter.initNewRecord((BasicDBObject) obj)) {
                        initSuccess = false;
                    }
                }

                //proceed if init successful
                if (initSuccess) {
                    Object filterResp = filterField(obj);

                    if (filterResp instanceof UnacknowledgedStatus) {
                        if (filterResp == UnacknowledgedStatus.FALSE) {
                            outList.add(null);
                        }
                    } else {
                        BasicDBObject filterRec = (BasicDBObject) filterResp;

                        //add aggregate label to the record
                        for (IMongoNodeFilter nodeFilter : nodeFilters) {

                            //check is aggregated field already exists, if so add to this field.
                            if (filterRec.containsField(aggregateLabel)) {
                                ((BasicDBObject) filterRec.get(aggregateLabel))
                                        .put(nodeFilter.getAttributeConfig().getDbAttributeName(), nodeFilter.getFinalAggregate());
                            } else {
                                filterRec.put(aggregateLabel,
                                        new BasicDBObject(nodeFilter.getAttributeConfig().getDbAttributeName(),
                                                nodeFilter.getFinalAggregate()));
                            }
                        }

                        outList.add(filterRec);
                    }
                }
            }
        }

        return outList;
    }

    private Object filterField(Object currentValue) {


        if (currentValue instanceof BasicDBObject) {      //check if this is an object
            BasicDBObject inField = (BasicDBObject) currentValue;

            //iterate node filter and evaluate the current node
            for (IMongoNodeFilter nodeFilter : nodeFilters) {
                if (!nodeFilter.filter(inField)) {
                    return (isUnacknowledged(inField, databaseLabel));
                }
            }

            BasicDBObject outField = new BasicDBObject();
            Set<Map.Entry<String, Object>> children = inField.entrySet();

            //traverse this object's children
            for (Map.Entry<String, Object> child : children) {
                String key = child.getKey();
                Object value = child.getValue();

                if (key.equals(databaseLabel) || key.equals(idLabel)) { //do not traverse further if child is security label field  or ID field
                    outField.put(key, value);
                } else {
                    Object filterVal = filterField(value);

                    if (filterVal instanceof UnacknowledgedStatus) {
                        if (filterVal == UnacknowledgedStatus.FALSE) {
                            outField.put(key, null);
                        }
                    } else {
                        outField.put(key, filterVal);
                    }
                }

            }

            return outField;
        } else if (currentValue instanceof BasicDBList) { //check if this is an array
            BasicDBList inList = (BasicDBList) currentValue;
            BasicDBList outList = new BasicDBList();

            //iterate over each object in the array
            for (Object arrayValue : inList) {
                Object filterVal = filterField(arrayValue);

                if (filterVal instanceof UnacknowledgedStatus) {
                    if (filterVal == UnacknowledgedStatus.FALSE) {
                        outList.add(null);
                    }
                } else {
                    outList.add(filterVal);
                }
            }

            return outList;
        }

        return currentValue; //if not a name/value pair or an array return value as is
    }

    @Override
    public void execNodeWriteProccesors(DBObject record) {

        //get userdetails
        ISwifUserDetails userDetails = SecurityManager.getUserDetails();

        //init node filters
        for (IMongoNodeFilter nodeFilter : nodeFilters) {
            nodeFilter.initUserAttributes(userDetails);
        }

        writeProcessField(record);
    }

    private void writeProcessField(Object currentValue) {

        if (currentValue instanceof BasicDBObject) {      //check if this is an object
            BasicDBObject inField = (BasicDBObject) currentValue;

            //iterate node filter and do write processing for each
            for (IMongoNodeFilter nodeFilter : nodeFilters) {
                nodeFilter.processWrite(inField);
            }

            //traverse this object's children
            for (Object val : inField.values()) {
                writeProcessField(val);
            }

        } else if (currentValue instanceof BasicDBList) { //check if this is an array
            BasicDBList inList = (BasicDBList) currentValue;

            //iterate over each object in the array
            for (Object arrayValue : inList) {
                writeProcessField(arrayValue);
            }
        }
    }

    private UnacknowledgedStatus isUnacknowledged(BasicDBObject doc, String secCtx) {
        if (doc.containsField(secCtx)) {
            BasicDBObject securityContext = (BasicDBObject) doc.get(secCtx);
            if (securityContext.containsField(unacknowledgedConfig.getDbAttributeName())) {
                Object obj = securityContext.get(unacknowledgedConfig.getDbAttributeName());
                if (obj instanceof String) {
                    return UnacknowledgedStatus.valueOf(((String) obj).toUpperCase());
                }
            }
        }
        return UnacknowledgedStatus.valueOf((unacknowledgedConfig.getDefaultValue()).toUpperCase());
    }
}