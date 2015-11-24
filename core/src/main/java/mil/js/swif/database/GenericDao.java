package mil.js.swif.database;

/**
 * Created with IntelliJ IDEA.
 * User: berkich
 * Date: 6/5/12
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */

import org.bson.types.ObjectId;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

public interface GenericDao  <T> {
    boolean create(String collection, T entity);
    boolean create(String collection, ObjectId id, T entity);

    String findById(String collection, ObjectId id);
    String findAll(String collection);
    BinaryDataObject binaryDataById(String id);
    String postBinaryData(CommonsMultipartFile file, String securityLabel);
    void update(String collection, ObjectId id, T entity);
    void delete(String collection, ObjectId id);
    ResultPage<String> findByFilter(String collection, String q, String orders, int pageNumber, int pageSize);
    ResultPage<String> query(String collection, String terms);

}