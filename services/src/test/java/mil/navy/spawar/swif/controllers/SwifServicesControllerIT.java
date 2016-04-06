package mil.navy.spawar.swif.controllers;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder.mongoDb;
import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import mil.navy.spawar.swif.data.IMongoDataAccessManager;
import mil.navy.spawar.swif.security.ISwifUserDetails;
import mil.navy.spawar.swif.security.SwifUserDetailsImpl;
import mil.navy.spawar.swif.testutils.EmbeddedMongoServer;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/config/SwifServicesControllerIT/config.xml"})
public class SwifServicesControllerIT {
	
	private MockMvc mockMvc;
    private static final String collection = "secureDocs";

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().configure( mongoDb()
    		.port(EmbeddedMongoServer.port)
    		.databaseName(EmbeddedMongoServer.database)
    		.build()
    ).build();
    
	@InjectMocks
	private SwifServicesController controller;
	
	@Autowired(required=true)
	private CasAuthenticationToken authToken;
	
	@Autowired(required=true)
	private IMongoDataAccessManager dataAccessManager;

	@Resource(name="fullTextQueryColumnMapping")
	private Map<String,List<String>> fullTextColumnMapping;

	@Before
	public void setup() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		// manually inject required beans into the controller being tested
		controller.dataAccessMgr = dataAccessManager;
		controller.fullTextColumnMapping = fullTextColumnMapping;
		
		// verify we have an auth token w/ userDetails 
		assertNotNull(authToken);
		assertNotNull(authToken.getUserDetails());
		assertTrue(authToken.getUserDetails() instanceof ISwifUserDetails);
		
		// update the security context w/ the auth token
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertNotNull(SecurityContextHolder.getContext());
	}
	
	@Test
	public void testPing() throws Exception {
		
    	DBObject dbo = new BasicDBObject();
		dbo.put("status", "ok");
        String expectectedResponse = dbo.toString();

		RequestBuilder request = MockMvcRequestBuilders.get("/ping")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().string(expectectedResponse))
        	.andReturn();
	}
		
	@Test
	public void testUserDetails() throws Exception {
		
		assertTrue(authToken.getUserDetails() instanceof SwifUserDetailsImpl);
		String expectedResponse =  ((SwifUserDetailsImpl)authToken.getUserDetails()).toJson();
		
		RequestBuilder request = MockMvcRequestBuilders.get("/userDetails")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(expectedResponse))
        	.andReturn();
	}
	
	@Test
	public void testUserDetailsNotAuthenticated() throws Exception {
		
		// update the security context w/ the auth token
		SecurityContextHolder.getContext().setAuthentication(null);

    	DBObject dbo = new BasicDBObject();
		dbo.put("error", "unable to find/retieve userDetails from securityContext");
		String  expectedResponse = dbo.toString();

		RequestBuilder request = MockMvcRequestBuilders.get("/userDetails")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(expectedResponse))
        	.andReturn();
	}

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollection() throws Exception  {
    	
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection)
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(3)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andExpect(jsonPath("$[2].name").value("doc-8"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollectionInvalidCollection() throws Exception  {
    	
		RequestBuilder request = MockMvcRequestBuilders.get("/foobar")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollectionUnAuthenticated() throws Exception  {
    	
		SecurityContextHolder.getContext().setAuthentication(null);

		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection)
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollectionId() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + " /000000000000000000000001")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$.name").value("doc-1"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollectionIdInvalidCollection() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/foobar/000000000000000000000001")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollectionIdUnAuthenticated() throws Exception  {
    			
		SecurityContextHolder.getContext().setAuthentication(null);

		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + " /000000000000000000000001")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/readCollection/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/readCollection/data-expected.json")
	public void testReadCollectionIdNoRecordFound() throws Exception  {

		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + " /000000000000000000000002")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/fullTextQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/fullTextQuery/data-expected.json")
	public void testFullTextQuery() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/query")
			.param("q", "efg+uvw")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(2)))
        	.andExpect(jsonPath("$[0].name").value("abc efg hij"))
        	.andExpect(jsonPath("$[1].name").value("doc-8"))
         	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/fullTextQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/fullTextQuery/data-expected.json")
	public void testFullTextQueryCaseSensitivity() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/query")
			.param("q", "EFG+UVW")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(2)))
        	.andExpect(jsonPath("$[0].name").value("abc efg hij"))
        	.andExpect(jsonPath("$[1].name").value("doc-8"))
         	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/fullTextQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/fullTextQuery/data-expected.json")
	public void testFullTextQueryNoRecordsFound() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
       		.andExpect(status().isOk())
       		.andExpect(content().contentType("application/json"))
       		.andExpect(content().string("[]"))
       		.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/fullTextQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/fullTextQuery/data-expected.json")
	public void testFullTextQueryNoParam() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/query")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(3)))
        	.andExpect(jsonPath("$[0].name").value("abc efg hij"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andExpect(jsonPath("$[2].name").value("doc-8"))
         	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/fullTextQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/fullTextQuery/data-expected.json")
	public void testFullTextQueryEmptyParam() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/query")
			.param("q", "")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(3)))
        	.andExpect(jsonPath("$[0].name").value("abc efg hij"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andExpect(jsonPath("$[2].name").value("doc-8"))
         	.andReturn();
    }    
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/fullTextQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/fullTextQuery/data-expected.json")
	public void testFullTextQueryInvalidCollection() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/foo/query")
			.param("q", "efg+uvw")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
     		.andExpect(status().isBadRequest())
     		.andExpect(content().contentType("application/json"))
     		.andExpect(content().string("[]"))
     		.andReturn();
    }
        
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testFullTextQueryUnAuthenticated() throws Exception  {
    			
		SecurityContextHolder.getContext().setAuthentication(null);
    	
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/query")
			.param("q", "efg+uvw")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
         	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
    }        
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQuery() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{'name':'doc-1'}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(1)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryNoRecordsFound() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{'name':'doc-foo'}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
    }    
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryNameOr() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{$or:[{'name':'doc-1'},{'name':'doc-7'}]}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(2)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryNameStartsWith() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{'name':{$regex:'doc.*'}}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(3)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andExpect(jsonPath("$[2].name").value("doc-8"))
        	.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryNameContains() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{ 'name':{$regex:'c'}}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(3)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andExpect(jsonPath("$[2].name").value("doc-8"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryOrNoAccessSomeRecord() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{$or:[{'name':'doc-1'},{'name':'doc-2'}]}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(1)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryNoParam() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(3)))
        	.andExpect(jsonPath("$[0].name").value("doc-1"))
        	.andExpect(jsonPath("$[1].name").value("doc-7"))
        	.andExpect(jsonPath("$[2].name").value("doc-8"))
        	.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryBadParam() throws Exception  {
    			
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "foobar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/mongoQuery/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/mongoQuery/data-expected.json")
	public void testMongoQueryUnAuthenticated() throws Exception  {
    			
		SecurityContextHolder.getContext().setAuthentication(null);
    	
		RequestBuilder request = MockMvcRequestBuilders.get("/" + collection + "/find")
			.param("q", "{'name':'doc-1'}")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
         	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
    }    

    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected.json")
	public void testCreate() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['C'],'RELTO':[],'SCI':[],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
	    	//.andDo(print())
	    	.andExpect(status().isCreated())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(jsonPath("$._id").exists())  
	    	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateDuplicatekey() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'_id':{'$oid': '000000000000000000000001'},'name':'doc-z','securityLabel':{'classification':['CLASS1'],'RELTO':[],'SCI':[],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
	    	//.andDo(print())
	    	.andExpect(status().isBadRequest())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(content().string("{}"))
	    	.andReturn();
    }    

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateRecordWithClassificationUserDoesNotHave() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['CLASS3'],'RELTO':[],'SCI':[],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
    		//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andExpect(content().contentType("application/json"))
    		.andExpect(content().string("{}"))
    		.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateRecordWithRelToUserDoesNotHave() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['CLASS1'],'RELTO':['NON'],'SCI':[],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
    		//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andExpect(content().contentType("application/json"))
    		.andExpect(content().string("{}"))
    		.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateRecordWithSAPUserDoesNotHave() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['CLASS1'],'RELTO':[],'SCI':[],'SAP':['NON']},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andExpect(content().contentType("application/json"))
    		.andExpect(content().string("{}"))
    		.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateRecordWithSCIUserDoesNotHave() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['CLASS1'],'RELTO':[],'SCI':['NON'],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
	    	//.andDo(print())
	    	.andExpect(status().isBadRequest())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(content().string("{}"))
	    	.andReturn();
    }

    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateInvalidCollection() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.post("/foobar")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['CLASS1'],'RELTO':[],'SCI':[],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
	    	//.andDo(print())
	    	.andExpect(status().isBadRequest())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(content().string("{}"))
	    	.andReturn();
    }
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/createRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/createRecord/data-expected-error.json")
	public void testCreateNotAuthenticated() throws Exception  {
    		
		SecurityContextHolder.getContext().setAuthentication(null);
		 
		RequestBuilder request = MockMvcRequestBuilders.post("/" + collection)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("{'name':'doc-z','securityLabel':{'classification':['CLASS1'],'RELTO':[],'SCI':[],'SAP':[]},'foo-1':'bar-1','foo-2':'bar-2'}");
		
        mockMvc.perform(request) 
	        //.andDo(print())
	    	.andExpect(status().isBadRequest())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(content().string("{}"))
	    	.andReturn();
    }    
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/deleteRecord/data-expected.json")
	public void testDelete() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.delete("/" + collection + "/000000000000000000000001")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andReturn();
    }    
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/deleteRecord/data-expected-error.json")
	public void testDeleteRecordUserCanNotSee() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.delete("/" + collection + "/000000000000000000000002")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isNotFound())
        	.andReturn();
    }    
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/deleteRecord/data-expected-error.json")
	public void testDeleteInvalidCollection() throws Exception  {
    					
		RequestBuilder request = MockMvcRequestBuilders.delete("/foobar/000000000000000000000001")
				.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andReturn();
    }        
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/deleteRecord/data-expected-error.json")
	public void testDeleteNotAuthenticated() throws Exception  {
    			
		SecurityContextHolder.getContext().setAuthentication(null);
		 
		RequestBuilder request = MockMvcRequestBuilders.delete("/" + collection + "/000000000000000000000001")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andReturn();
    }    
    
    @Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/deleteRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/deleteRecord/data-expected-error.json")
	public void testDeleteInvalidKey() throws Exception  {
    			
		SecurityContextHolder.getContext().setAuthentication(null);
		 
		RequestBuilder request = MockMvcRequestBuilders.delete("/" + collection + "/foobar")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andReturn();
    }      
    
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected.json")
	public void testUpdate() throws Exception {
		 
		// retieve an existing obj from the db
		// change an attribute then update via rest call
		String id = "000000000000000000000001";
		DBObject rec = getExistingRecord(id);
		rec.put("foo-3","bar-3");

		RequestBuilder request = MockMvcRequestBuilders.put("/" + collection + "/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andReturn();
    }	    
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected-error.json")
	public void testUpdateInValidCollection() throws Exception {

		String id = "000000000000000000000001";
		DBObject rec = getExistingRecord(id);
		rec.put("foo-3","bar-3");

		RequestBuilder request = MockMvcRequestBuilders.put("/foobar/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
        	.andReturn();
    }	    	
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected-error.json")
	public void testUpdateRecordWithNoKey() throws Exception {
		 
		// get exisiting objec from db
		String id = "000000000000000000000001";
		DBObject rec = getExistingRecord(id);
		assertTrue("expecting record id", rec.containsField("_id"));
		
		// remove id attr & try to update via rest call
		rec.removeField("_id");
		assertFalse("not expecting record id", rec.containsField("_id"));
		rec.put("foo-3","bar-3");

		RequestBuilder request = MockMvcRequestBuilders.put("/" + collection + "/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
        	.andReturn();
    }	    	
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected-error.json")
	public void testUpdateNoSecurityLabel() throws Exception {
		 
		// try to update a doc without security label
		String id = "000000000000000000000002";
    	BasicDBObject rec = new BasicDBObject();
    	rec.put("_id", new ObjectId(id));
		rec.put("foo-3","bar-3");

		RequestBuilder request = MockMvcRequestBuilders.put("/" + collection + "/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isNotFound())
        	.andReturn();
    }	    	
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected-error.json")
	public void testUpdateAccessNotGranted() throws Exception {
		 
		String id = "000000000000000000000001";
       	BasicDBObject rec = new BasicDBObject();
       	rec.put("_id", new ObjectId(id));
       	rec.put("foo-3","bar-3");
       	
    	BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"CLASS3"}));  // user does not have access
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"SCI1", "SCI2"}));  // user does not have access
        securityLabel.put("SAP", dbList(new String[]{"SAP1", "SAP2"}));  // user does not have access
        rec.put("securityLabel", securityLabel);
        
		RequestBuilder request = MockMvcRequestBuilders.put("/" + collection + "/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andReturn();
    }	    

	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected-error.json")
	public void testUpdateNotAuthenticated() throws Exception {
		 
		String id = "000000000000000000000001";
		DBObject rec = getExistingRecord(id);
		rec.put("foo-3","bar-3");

		SecurityContextHolder.getContext().setAuthentication(null);
       
		RequestBuilder request = MockMvcRequestBuilders.put("/" + collection + "/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andReturn();
    }	  
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/updateRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/updateRecord/data-expected-error.json")
	public void testUpdateInvalidRecord() throws Exception {
		 
		String id = "000000000000000000000001";
		
		RequestBuilder request = MockMvcRequestBuilders.put("/" + collection + "/" + id)
			.contentType(MediaType.APPLICATION_JSON)
			.content("foo");
		
        mockMvc.perform(request) 
        	//.andDo(print())
    		.andExpect(status().isBadRequest())
    		.andReturn();
    }	    
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-expected.json")
	public void testBinaryCreate() throws Exception {
		
    	BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"C"})); 
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"TK"}));  
        securityLabel.put("SAP", dbList(new String[]{"BP"}));  

        String fileContent = "file-contents";
 		
		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.param("securityLabel", securityLabel.toString());
		request.file(new MockMultipartFile("data","file.txt","text/plain",fileContent.getBytes()));
		
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isOk())
        	.andReturn();
	}	

	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-expected-error.json")
	public void testBinaryCreateNotAuthorized() throws Exception {
		
    	BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"S"}));  // no access
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"TK"}));  
        securityLabel.put("SAP", dbList(new String[]{"BP"}));  

        String fileContent = "file-contents";
 		
		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.param("securityLabel", securityLabel.toString());
		request.file(new MockMultipartFile("data","file.txt","text/plain",fileContent.getBytes()));
		
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
	}	
	
	@Test
	@UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-expected-error.json")
	public void testBinaryCreateNoSecurityLabel() throws Exception {
		
        String fileContent = "file-contents";
 		
		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.file(new MockMultipartFile("data","file.txt","text/plain",fileContent.getBytes()));
	 	
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
	}	
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-expected-error.json")
	public void testBinaryCreateNotAuthenticated() throws Exception {
		
		SecurityContextHolder.getContext().setAuthentication(null);
		  
    	BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"U"})); 
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"TK"}));  
        securityLabel.put("SAP", dbList(new String[]{"BP"}));  

        String fileContent = "file-contents";
 		
		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.param("securityLabel", securityLabel.toString());
		request.file(new MockMultipartFile("data","file.txt","text/plain",fileContent.getBytes()));
		
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
	}		
    
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-load.json")
	public void testBinaryRead() throws Exception {
		
		RequestBuilder request = MockMvcRequestBuilders.get("/data/000000000000000000000001");
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(header().string("Content-Type", "text/plain"))
        	.andExpect(header().string("Content-Disposition", "attachment;filename=test1.txt"))
        	.andExpect(content().contentType("text/plain"))
        	.andExpect(content().string("file-contents"))
        	.andReturn();
	}	
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-load.json")
	public void testBinaryReadNoAccess() throws Exception {
		
		RequestBuilder request = MockMvcRequestBuilders.get("/data/000000000000000000000002");
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
	}	
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-load.json")
	public void testBinaryReadNotAuthenticated() throws Exception {
		
		SecurityContextHolder.getContext().setAuthentication(null);
		  
		RequestBuilder request = MockMvcRequestBuilders.get("/data/000000000000000000000001");
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
	}
	
	@Test
    @UsingDataSet(locations={"/data/SwifServicesControllerIT/binaryRecord/data-load.json"},loadStrategy=LoadStrategyEnum.CLEAN_INSERT)
    @ShouldMatchDataSet(location="/data/SwifServicesControllerIT/binaryRecord/data-load.json")
	public void testBinaryReadInvalidKey() throws Exception {
		
		RequestBuilder request = MockMvcRequestBuilders.get("/data/foobar");
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
	}
	
	private DBObject getExistingRecord(String key) {
		try{
	   		DBObject query = new BasicDBObject();
			query.put("_id", new ObjectId(key));
			BasicDBList recs = dataAccessManager.readRecord(collection, query);
			assertNotNull("expecting 1 record", recs);
			assertEquals( "expecting 1 record", 1, recs.size());
			assertTrue("expecting 1 record", recs.get(0) instanceof DBObject);
			return (DBObject)recs.get(0);
		} catch(Exception ex) {
			fail("expecting 1 record but caught expection:" + ex.getMessage());
			return null;
		}
	}
	
    private BasicDBList dbList(String[] items) {
    	BasicDBList result = new BasicDBList();
    	for( String item : items) {
    		result.add(item);
    	}
    	return result;
    }		

}
