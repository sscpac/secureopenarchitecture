package mil.navy.spawar.swif.data;

import com.mongodb.*;
import mil.js.swif.encryption.EncryptionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import java.util.Arrays;

//
// DB Factory class derived from http://java.dzone.com/articles/plain-simple-mongodb-spring
// 
public class MongoDatabaseFactory implements FactoryBean<DB>, InitializingBean {

    private MongoClient mongo;
    private String database;
    private String host;
    private int port;
    private String user = null;
    private String password = null;

    private static final Logger log = (Logger) LoggerFactory.getLogger(MongoDatabaseFactory.class);

    @Override
    public void afterPropertiesSet() throws Exception {

        ServerAddress addr = new ServerAddress(host, port);

        if(user != null && password != null) {
            MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
            mongo = new MongoClient(addr, Arrays.asList(credential));
        } else {
            mongo = new MongoClient(addr);
        }

    }

    @Override
    public DB getObject() throws Exception {

        Assert.notNull(mongo);
        Assert.notNull(database);

        log.debug("connecting to mongo database: " + database);

        try {
            DB db = mongo.getDB(database);
            return db;

        } catch (Exception ex) {
            log.error("error connecting to mongo database", ex);
            throw new Exception("error connecting to mongo database");
        }
    }

    @Override
    public Class<?> getObjectType() {
        return DB.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Required
    public void setHost(String host) {
        this.host = host;
    }

    @Required
    public void setPort(int port) {
        this.port = port;
    }

    @Required
    public void setDatabase(String value) {
        database = value;
    }

    public void setUser(String value) {
        user = value;
    }

    public void setPassword(String value) {
        password = value;
    }

   public void setEncryptedPassword(String value) {
	   password = decrypt(value);
   }

   private String decrypt(String cipherTxt) {
	   EncryptionUtility util = new EncryptionUtility();
	   String result = util.decrypt(cipherTxt);
	   return result;
   }

}