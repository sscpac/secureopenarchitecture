package mil.navy.spawar.swif.controllers;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.util.JSONParseException;
import mil.navy.spawar.swif.data.DataAccessException;
import mil.navy.spawar.swif.data.IMongoDataAccessManager;
import mil.navy.spawar.swif.data.RecordNotFoundException;
import mil.navy.spawar.swif.ldap.ISwifLdapService;
import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SecurityManager;
import mil.navy.spawar.swif.security.SwifUserDetailsImpl;
import mil.navy.spawar.swif.security.dialog.ISecurityDialogManager;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import sun.rmi.transport.ObjectTable;

@Controller
@RequestMapping( value = "/" )
public class SwifServicesController {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(SwifServicesController.class);
    private static final String COLLECTION_ID = "_id";
    private static final String BINARY_COLLECTION_ID = "binary";
    private static final boolean REGEX_CASE_INSENSITIVE = true;
    
    @Autowired(required=true)
    IMongoDataAccessManager dataAccessMgr;

    @Autowired(required=true)
    ISecurityDialogManager secDialogManager;

    @Autowired(required=true)
    ISwifLdapService ldapService;
    
    @Resource(name="fullTextColumnMapping")
    Map<String,List<String>> fullTextColumnMapping;

    public SwifServicesController(){
        super();
        log.info("start up");
    }

    // method to test if controller running
    @RequestMapping( value = "/ping", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String ping(HttpServletResponse response) throws IOException {
    	
		response.setStatus(HttpServletResponse.SC_OK);
    	DBObject dbo = new BasicDBObject();
		dbo.put("status", "ok");
        return dbo.toString();
    }
    
    // method to display user details
    @RequestMapping( value = "/userDetails", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String userDetails(HttpServletResponse response)  {
    	
		response.setStatus(HttpServletResponse.SC_OK);
		
		try {
	    	SwifUserDetailsImpl userDetails = (SwifUserDetailsImpl) SecurityManager.getUserDetails();
	    	return userDetails.toJson();
		} catch(Exception ex) {
	    	DBObject dbo = new BasicDBObject();
			dbo.put("error", ex.getMessage());
			return dbo.toString();
		}
    }

    @RequestMapping( value = "/retrieveUsers", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String retrieveUsers(@RequestParam(value="user", required=false) List<String> users, HttpServletResponse response)  {

        response.setStatus(HttpServletResponse.SC_OK);

        if(users == null) {
            return ldapService.getAllUsers().toString();
        } else {
            return ldapService.getUsers(users).toString();
        }
    }

    @RequestMapping( value = "/retrieveGroups", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String retrieveGroups(@RequestParam(value="group") List<String> groups, HttpServletResponse response)  {

        response.setStatus(HttpServletResponse.SC_OK);

        return ldapService.getGroups(groups).toString();
    }


    // read all document(s) in collection
    @RequestMapping( value = "/{collection}", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String findAll(@PathVariable( "collection" ) final String collection, HttpServletResponse response) throws Exception {
	   	try {
    		// get record from db
    		BasicDBList records = dataAccessMgr.readRecord(collection);
    		
    		// format reponse
    		if( records != null && records.size() > 0) {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return records.toString();
    		} else {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return "[]";
    		}
    	} catch(Exception ex) {
    		log.error("findAll error:", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "";
    	}		
    }
    

    // read document using id
    @RequestMapping( value = "/{collection}/{id}", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String findById(@PathVariable( "collection" ) final String collection, @PathVariable( "id" ) final String id, HttpServletResponse response) throws Exception {
    	try {
    		// create query using request key
    		DBObject query = new BasicDBObject();
    		query.put(COLLECTION_ID, new ObjectId(id));
    		
    		// get record from db
    		BasicDBList records = dataAccessMgr.readRecord(collection, query);
    		
    		// format response
    		if( records != null && records.size() == 1) {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return records.get(0).toString();
    		} else {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return "";
    		}
    	} catch(Exception ex) {
    		log.error("findById error:", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "";
    	}
    }

    // find document(s) using full text search
    @RequestMapping( value = "/{collection}/query", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String fullTextQuery(@PathVariable("collection") final String collection, @RequestParam(value = "q", defaultValue = "*") final String terms, HttpServletResponse response) throws Exception  {

    	try {
    		// if no keywords are sent then return the full collection
    		if( terms.equals("*")) {
    			BasicDBList records = dataAccessMgr.readRecord(collection);
          		if( records != null && records.size() > 0) {
        			response.setStatus(HttpServletResponse.SC_OK);
        			return records.toString();
        		} else {
        			response.setStatus(HttpServletResponse.SC_OK);
        			return "[]";
        		}
     		}

    		// check the collection has columns avail for full text search
    		if(!fullTextColumnMapping.containsKey(collection)) {
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			return "[]";
    		}
    		
    		// get a list of collection columns that are avail for full text search
    		List<String> columns = fullTextColumnMapping.get(collection);
    		if(columns.size() == 0 ) {
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			return "[]";
    		}
    		
    		// parse the terms into  individual keyworks
       		List<String> keyWords = Arrays.asList(terms.split("\\+"));
       		if( keyWords.size() < 1) {
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			return "[]";
       		}
    		    		
    		// build regex query from searchable columns & keywords
    		DBObject query = buildFullTextRegexQuery(columns, keyWords);

       		// execute query
    		BasicDBList records = dataAccessMgr.readRecord(collection, query);
    		
    		// format response
      		if( records != null && records.size() > 0) {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return records.toString();
    		} else {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return "[]";
    		}
      	} catch(Exception ex) {
      		log.error("fullTextQuery error:", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "[]";
    	}    	
 
    }

	//Find has been disabled until we fully assess the security risks of allowing users to perform mongo queries.
	// find document(s) with mongo query
	/*
    @RequestMapping( value = "/{collection}/find", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String mongoQuery(@PathVariable("collection") final String collection, @RequestParam(value = "q", defaultValue = "*") final String qry, HttpServletResponse response) throws Exception {
    	try {
    		
    		// if no query is requested return full collection
    		// else parse query parm into mongo object

			DBObject query = new BasicDBObject();
    		if( qry.equals("*")) {
    			query = null;  // return all recs
    		} else {
    			query =  parseJson(qry);
    		}

    		// execute query
    		BasicDBList records = dataAccessMgr.readRecord(collection, query);
    		
    		// format response
      		if( records != null && records.size() > 0) {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return records.toString();
    		} else {
    			response.setStatus(HttpServletResponse.SC_OK);
    			return "[]";
    		}
    	} catch(Exception ex) {
    		log.error("mongoQuery error:", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "[]";
    	}    	
    }
    */
    
    // create new document
    @RequestMapping( headers = {"content-type=application/json"}, value = "/{collection}", method = RequestMethod.POST, produces="application/json")
    public @ResponseBody String create(@PathVariable( "collection" ) final String collection, @RequestBody String json, HttpServletResponse response) throws Exception  {
 		try {
		   	// get json object from request
	    	DBObject record = parseJson(json);
	    	
	    	// create new key & add to obj
	    	ObjectId id = null;
	    	if( record.containsField("_id")) {
	    		id = ((ObjectId) record.get("_id"));
	    	} else {
	    		id = new ObjectId();
	    		record.put("_id", id);
	    	}
	    	
			// save to db
			dataAccessMgr.createRecord(collection, record);
			response.setStatus(HttpServletResponse.SC_CREATED);
			return "{\"_id\":\""+ id + "\"}";
		} catch(Exception ex) {
			log.error("create error:", ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "{}";
		}
    }
    
    // delete existing document
    @RequestMapping(value = "/{collection}/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void delete( @PathVariable( "collection" ) final String collection,
                                      @PathVariable( "id" ) final String id,
                                      @RequestParam(value="childCollections", required=false) String childCollections,
                                      HttpServletResponse response ) throws DataAccessException{
    	try{

            Collection<String> childColList = Collections.emptyList();

            if(childCollections != null) {
               childColList = parseStringArray(childCollections);
            }

    		dataAccessMgr.deleteRecord(collection, id, childColList);
    		response.setStatus(HttpServletResponse.SC_OK);
    	} catch(Exception ex) {
			if( ex instanceof RecordNotFoundException) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				log.error("delete error:", ex);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
    	}
    }

    // update existing document
	// the incoming object will have an id so we ignore the id in the path
    // the id on the path is left to provide backwards compatibility with prev controller
    @RequestMapping( headers = {"content-type=application/json"}, value = "/{collection}/{id}", method = RequestMethod.PUT)
    public @ResponseBody void update(@PathVariable( "collection" ) final String collection, @PathVariable( "id" ) final String id, @RequestBody String json, HttpServletResponse response) throws Exception{
    	try {
    		// get json object from request
    		DBObject rec = parseJson(json);
    		
    		// update db
    		dataAccessMgr.updateRecord(collection, rec);
    		
    		response.setStatus(HttpServletResponse.SC_OK);
    	} catch(Exception ex) {
			if( ex instanceof RecordNotFoundException) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				log.error("update error:", ex);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
    	}
    }
    
    // retrieve dialog data
    @RequestMapping( value = "/dialog", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public String dialog() throws Exception {
        return secDialogManager.buildInputRequirements();
    }

    // retrieve binary db document using id
    @RequestMapping( value = "/data/{id}", method = RequestMethod.GET)
    public void findBinaryById(@PathVariable( "id" ) final String id, HttpServletResponse response) throws Exception {
    	
    	try {
			// create query using key
			DBObject query = new BasicDBObject();
			query.put(COLLECTION_ID, new ObjectId(id));
			
			// get record
			BasicDBList records = dataAccessMgr.readRecord(BINARY_COLLECTION_ID, query);
			if( records != null && records.size() == 1) {
				DBObject record = (DBObject) records.get(0);
			    response.setContentType((String)record.get("contentType"));
			    response.setHeader("Content-Disposition", "attachment;filename=" + (String)record.get("fileName"));
			    response.getOutputStream().write((byte[])record.get("data"));
			    response.setStatus(HttpServletResponse.SC_OK);
			} else {
		    	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "record not found");
			}
    	} catch(Exception ex) {
    		log.error("findBinaryById error:", ex);
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
    	}
    }

    // update existing document
    // the incoming object will have an id so we ignore the id in the path
    // the id on the path is left to provide backwards compatibility with prev controller
    @RequestMapping( headers = {"content-type=application/json"}, value = "/data/update", method = RequestMethod.PUT)
    public @ResponseBody void updateBinary(@RequestBody String json, HttpServletResponse response) throws Exception{

        try {
            DBObject updatedRecord = parseJson(json);

            // check object has key
            Object obj = updatedRecord.get(COLLECTION_ID);
            if (obj == null || !(obj instanceof ObjectId)) {
                throw new DataAccessException("invalid/missing id");
            }

            // create query using object key
            BasicDBObject query = new BasicDBObject();
            query.put(COLLECTION_ID, obj);

            // get record
            BasicDBList records = dataAccessMgr.readRecord(BINARY_COLLECTION_ID, query);
            if( records != null && records.size() == 1) {
                DBObject record = (DBObject) records.get(0);
                Object data = record.get("data");
                updatedRecord.put("data", data);

                dataAccessMgr.updateRecord(BINARY_COLLECTION_ID, updatedRecord);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "record not found");
            }
        } catch(Exception ex) {
            if( ex instanceof RecordNotFoundException) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                log.error("findBinaryById error:", ex);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            }
        }
    }
    
    // create binary db document from post data
    // applies securityLabel
    @RequestMapping( headers = {"content-type=multipart/form-data"}, value = "/data", method = RequestMethod.POST, produces="text/html")
    @ResponseBody
    public String postBinary(@RequestParam("data") MultipartFile file,
                             @RequestParam("securityLabel") String securityLabel,
                             @RequestParam Map<String,String> allRequestParams,

                             HttpServletResponse response) throws Exception {

        String userName = null;
        String currDateTime = getCurrDateTime();
        try {
            ISwifUserDetails userDetails = SecurityManager.getUserDetails();
            userName = userDetails.getUsername();
        } catch(Exception e) {

        }

    	try {
            DBObject dbo = new BasicDBObject();
			dbo.put("contentType", file.getContentType());
			dbo.put("fileName", file.getOriginalFilename());
			dbo.put("data", file.getBytes());
            dbo.put("uploadedBy", userName);
            dbo.put("uploadedOn", currDateTime);

            for (Map.Entry<String, String> entry : allRequestParams.entrySet()) {
                dbo.put(entry.getKey(), parseParameter(entry.getValue()));
            }

            dataAccessMgr.createRecord(BINARY_COLLECTION_ID, dbo);

            DBObject result = new BasicDBObject();
            result.put(COLLECTION_ID, dbo.get(COLLECTION_ID));
            result.put("collection", BINARY_COLLECTION_ID);
            result.put("uploadedBy", userName);
            result.put("uploadedOn", currDateTime);

            response.setStatus(HttpServletResponse.SC_OK);

            return "{\"success\":true,\"data\":" + result.toString() + "}";
        } catch(Exception ex) {
    		log.error("postBinary error:", ex);
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());

            return "{\"success\":false,\"message\":" + ex.getMessage() + "}";
        }
    }
        
    @ExceptionHandler(Exception.class)
    public @ResponseBody String handleException(Exception ex, HttpServletResponse resp) {
    	log.info("error: " + ex.getMessage());
    	resp.setHeader("Content-Type", "application/json");
    	resp.setContentType("application/json");
    	resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	String errorResponse = convertError(ex);
    	return errorResponse;
    }
    
    // parses string into mongo object
    private DBObject parseJson(String json) throws Exception{
    	
    	try {
     		Object obj = JSON.parse(json);
     		if( obj instanceof DBObject) {
     			return (DBObject) obj;
     		} else {
     			throw new Exception("can not convert parsed object into instance of DBObject");
     		}    		
    	} catch(Exception ex) {
    		throw new Exception("error parsing json string into DBObject", ex);
    	}

    }

    //If its a valid DBObject return the DBObject, otherwise just return the parameter
    private Object parseParameter(String parameter) {
        try {
            Object obj = JSON.parse(parameter);
            if(obj instanceof DBObject) {
                return obj;
            }
        } catch(JSONParseException ex) {

        }
            return parameter;
    }

    private Collection<String> parseStringArray(String json) throws Exception {

        ArrayList<String> array = new ArrayList<String>();

        try {
            Object obj = JSON.parse(json);
            if( obj instanceof BasicDBList) {
                for(Object o: (BasicDBList)obj ) {
                    if (o instanceof String) {
                        array.add((String)o);
                    } else {
                        throw new Exception("The given string is not a properly formatted array of strings: " + json);
                    }
                }
                return array;

            } else {
                throw new Exception("The given string is not a properly formatted array: " + json);
            }
        } catch(Exception ex) {
            throw new Exception("The given string is not a properly formatted array of strings: " + json, ex);
        }
    }


    // build mongo query clause 
    // uses mongo $regex operator for each word in each column
    private BasicDBObject buildFullTextRegexQuery(List<String> columns, List<String> words) {
    	
    	BasicDBList orPart = new BasicDBList();
    	
		for( String column: columns) {
			String regexPattern = "";
			for( String word: words) {
				if( regexPattern.length() == 0 ) {
					regexPattern = word;
				} else {
					regexPattern = regexPattern + "|" + word;
				}
			}
			DBObject regexPart = new BasicDBObject();
			regexPart.put("$regex", regexPattern);
			if(REGEX_CASE_INSENSITIVE) {
				regexPart.put("$options", "i");
			}
			orPart.add( new BasicDBObject(column, regexPart));
		}

    	BasicDBObject result = new BasicDBObject();
		if(orPart.size() == 1) {
			result = (BasicDBObject) orPart.get(0);
		} else {
			result.put("$or", orPart);
		}
   		return result;
    }

    private String convertError(Throwable ex) {
    	DBObject json = new BasicDBObject();
    	json.put("error", ex.getMessage());
    	if( ex.getCause() != null ) {
    		json.put("cause", ex.getCause().getMessage());
    	}
    	String result = json.toString();
    	return result;
    }

    public String getCurrDateTime()  {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY hh:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
