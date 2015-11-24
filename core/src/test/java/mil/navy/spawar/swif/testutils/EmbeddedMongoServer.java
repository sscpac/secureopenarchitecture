package mil.navy.spawar.swif.testutils;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.distribution.GenericVersion;

public class EmbeddedMongoServer {
	
	public static int port = 12345;
	public static String database = "swifTestDatabase";
    public static String version = "2.4.1";
    public static  String host = "localhost";
	
	private Map<String,String> databaseJsFunctions = new HashMap<String,String>();

	private MongodExecutable mongodExecutable;
    private MongodProcess mongoDeamon;
    private MongoClient mongoClient;
    
    private static final Logger log = (Logger) LoggerFactory.getLogger(EmbeddedMongoServer.class);

     
	public Map<String, String> getDatabaseJsFunctions() {
		return databaseJsFunctions;
	}

	public void setDatabaseJsFunctions(Map<String,String> value) {
		databaseJsFunctions = value;
	}
	
	public DB getDB() {
		return mongoClient.getDB(database);
	}
 	
    public void start() throws Exception {
    	
    	log.info("starting server");
    	
	    try {
	    	
		    MongodStarter runtime = MongodStarter.getDefaultInstance();
	    	
		    IMongodConfig mongoConfig = new MongodConfigBuilder()
		    	.version(new GenericVersion(version))
		    	.net(new Net(port, false))
		    	.build();
	    	
		    mongodExecutable = runtime.prepare(mongoConfig);
		    mongoDeamon = mongodExecutable.start();
		    mongoClient = new MongoClient(host, port);
		    customStartupInitialization();
		    
		   	log.info("server startup complete");
		    
	    } catch(Exception ex) {
		   	log.error("server startup error", ex);
	    }
    }
    
    public void stop() {
    	
    	log.info("server shutdown");
    	
    	// stop deamon
        if (mongoDeamon != null) {
        	
        	try {
        		mongoDeamon.stop();
        	} catch(Exception ex) {
    		   	log.error("mongoDeamon shutdown error", ex);
        	}
        } 

    	// stop exe
        if (mongodExecutable != null) {
           	try {
           		mongodExecutable.stop();
        	} catch(Exception ex) {
    		   	log.error("mongodExecutable shutdown error", ex);
        	}
        } 
        
    	log.info("server shutdown complete");
 
    }   
    
    
    private void customStartupInitialization() throws Exception {
    	
    	log.info("server custom initialization initiated");

        // insert the js functions we expect to be loaded
        // could not get these to load via dbunit dataset injection
        if(databaseJsFunctions.size()>0 ){
        	for(String funcKey : databaseJsFunctions.keySet()) {
        		String funcResource = databaseJsFunctions.get(funcKey);
        		loadJsFunction(funcKey, funcResource);
        	}
        }
        
    	log.info("server custom initialization complete");

    }
    
    private void loadJsFunction(String key, String resource) throws Exception{
    	
    	DBCollection systemCollection = getDB().getCollection("system.js");
		DBObject function = systemCollection.findOne(new BasicDBObject("_id", key));
		if( function == null) {
			BasicDBObject newFunction = new BasicDBObject();
			newFunction.put("_id", key);
			String code = MongoTestUtils.loadResourceAsString(resource);
			newFunction.put("value", new Code( code ));
			systemCollection.save(newFunction);
			
			log.info("inserted custom function '" + key + "' into the " + systemCollection.getName() + " collection");
		}
    }
    
    
}