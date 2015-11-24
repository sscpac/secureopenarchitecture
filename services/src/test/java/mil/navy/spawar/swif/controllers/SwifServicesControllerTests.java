package mil.navy.spawar.swif.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mil.navy.spawar.swif.data.AuthorizationException;
import mil.navy.spawar.swif.data.DataAccessException;
import mil.navy.spawar.swif.data.IMongoDataAccessManager;
import mil.navy.spawar.swif.data.RecordNotFoundException;
import mil.navy.spawar.swif.security.SwifUserDetailsImpl;
import mil.navy.spawar.swif.security.dialog.ISecurityDialogManager;

import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
 
public class SwifServicesControllerTests {
	
	private MockMvc mockMvc;
	
	@InjectMocks
	private SwifServicesController controller;
	
	@Mock
	private IMongoDataAccessManager mockDataAccessManager;
	
	@Mock
	private ISecurityDialogManager mockSecurityDialogManager;

	@Mock
	private Map<String,List<String>> mockFullTextColumnMapping;


	/*
	@Before
	public void setup() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}
	
	@Test
	public void testPing() throws Exception {
		
		RequestBuilder request = MockMvcRequestBuilders.get("/ping")
				.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$.status").value("ok"))
        	.andReturn();
	}
	
	@Test
	public void testUserDetails() throws Exception {
		
		// load user details from context
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/config/SwifServicesControllerTests/userDetails-config.xml");
		assertNotNull(ctx);
		
		CasAuthenticationToken authToken = (CasAuthenticationToken) ctx.getBean("authToken");
		assertNotNull(authToken);
		assertNotNull(authToken.getUserDetails());
		assertTrue(authToken.getUserDetails() instanceof SwifUserDetailsImpl);
		
		// update the security context w/ the auth token
		SecurityContextHolder.getContext().setAuthentication(authToken);

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
	public void testFindAll() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		for(int i=0;i<10;i++) {
			DBObject rec = new BasicDBObject();
			rec.put("foo-"+i,"bar-"+i);
			recs.add(rec);
		}
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection")
				.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$",hasSize(10)))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}
	
	@Test
	public void testFindAllInvalidCollection() throws Exception {

		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenThrow(new DataAccessException("invalid collection"));
		
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
	public void testFindAllNoRecordsReturned() throws Exception {

		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request)
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}
		
	@Test
	public void testFindAllUnexpecetdError() throws Exception {
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenThrow(new RuntimeException("whoops"));
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
	}
	
	@Test
	public void testFindById() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/000000000000000000000001")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(rec.toString()))
        	.andReturn();
	}	
	
	@Test
	public void testFindByIdInvalidCollection() throws Exception {
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new DataAccessException("collection is invalid"));
		
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
	public void testFindByIdRecordNotFound() throws Exception {
		
		BasicDBList recs = new BasicDBList(); // empty result
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/000000000000000000000001")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
	}
	
	@Test
	public void testFindByIdBadIdFormat() throws Exception {
		
		BasicDBList recs = new BasicDBList(); // empty result
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/foobar")
			.accept(MediaType.APPLICATION_JSON);			
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
	}		
	
	@Test
	public void testFindByIdUnexpectedError() throws Exception {
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new RuntimeException("whoops"));
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/000000000000000000000001")
			.accept(MediaType.APPLICATION_JSON);		
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(""))
        	.andReturn();
	}	
	
	@Test
	public void testFullTextQuery() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}	

	@Test
	public void testFullTextQueryNothingFound() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);
		
		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}	

	@Test
	public void testFullTextQueryEmptyParam() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}		

	@Test
	public void testFullTextQueryEmptyParamNoRecordsFound() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);
		
		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}		
	

	@Test
	public void testFullTextQuerySplatParam() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);

		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "*")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}		
	
	@Test
	public void testFullTextQuerySplatParamNoRecordsFound() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);

		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "*")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}		
	
	@Test
	public void testFullTextQueryInvalidCollection() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);

		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new DataAccessException("invalid collection"));

		RequestBuilder request = MockMvcRequestBuilders.get("/foobar/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}		
	
	@Test
	public void testFullTextQueryCollectionNotSupported() throws Exception {
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(false);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
	    	// .andDo(print())
	    	.andExpect(status().isBadRequest())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(content().string("[]"))
	    	.andReturn();
	}	
	
	@Test
	public void testFullTextQueryNoSupportedColumns() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
	    	// .andDo(print())
	    	.andExpect(status().isBadRequest())
	    	.andExpect(content().contentType("application/json"))
	    	.andExpect(content().string("[]"))
	    	.andReturn();
	}
	
	@Test
	public void testFullTextQueryUnexpectedError() throws Exception {
		
		ArrayList<String> fullTextCols = new ArrayList<String>();
		fullTextCols.add("name");
		fullTextCols.add("desc");
		
		when(mockFullTextColumnMapping.containsKey(any(String.class)))
			.thenReturn(true);
		
		when(mockFullTextColumnMapping.get(any(String.class)))
			.thenReturn(fullTextCols);

		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new RuntimeException("whoops"));
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/query")
			.param("q", "foo+bar")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
    }			
	
	@Test
	public void testFindQuery() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/find")
			.param("q", new BasicDBObject("foo","bar").toString())
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}		

	@Test
	public void testFindQueryInvalidCollection() throws Exception {
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new DataAccessException("invalid collection"));

		RequestBuilder request = MockMvcRequestBuilders.get("/foobar/find")
			.param("q", new BasicDBObject("foo","bar").toString())
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}		
	
	@Test
	public void testFindQueryEmptyParam() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/find")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}			

	@Test
	public void testFindQuerySplatParam() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/find")
			.param("q", "*")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(recs.toString()))
        	.andReturn();
	}		
	
	@Test
	public void testFindQueryNothingFound() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/find")
			.param("q", new BasicDBObject("foo","bar").toString())
			.accept(MediaType.APPLICATION_JSON);		
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}		
	
	@Test
	public void testFindQueryInvalidQuery() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/find")
			.param("q", "foo")
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
	}	
	
	@Test
	public void testFindQueryUnexpectedError() throws Exception {
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new RuntimeException("whoops"));
		
		RequestBuilder request = MockMvcRequestBuilders.get("/collection/find")
			.param("q", new BasicDBObject("foo","bar").toString())
			.accept(MediaType.APPLICATION_JSON);	
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("[]"))
        	.andReturn();
    }			
	
	@Test
	public void testCreate() throws Exception {

		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");
		
		RequestBuilder request = MockMvcRequestBuilders.post("/collection")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isCreated())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$._id").exists())  
        	.andReturn();
    }			

	@Test
	public void testCreateUnAuthorized() throws Exception {
		
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");

		doThrow(new AuthorizationException()).when(mockDataAccessManager)
			.createRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.post("/collection")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("{}"))
        	.andReturn();
    }	
	
	@Test
	public void testCreateInvalidCollection() throws Exception {
		
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");

		doThrow(new DataAccessException("invalid coolection"))
			.when(mockDataAccessManager)
			.createRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.post("/foobar")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("{}"))
        	.andReturn();
    }			
	
	@Test
	public void testCreateInvalidData() throws Exception {
		
		RequestBuilder request = MockMvcRequestBuilders.post("/collection")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content("foobar");  // invalid data
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string("{}"))
        	.andReturn();
    }			

	@Test
	public void testCreateUnexpectedError() throws Exception {
		
		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");

		doThrow(new RuntimeException("whoops"))
			.when(mockDataAccessManager)
			.createRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.post("/collection")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }			
	
	@Test
	public void testDelete() throws Exception {
		 
		RequestBuilder request = MockMvcRequestBuilders.delete("/collection/000000000000000000000001")
				.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andReturn();
    }	
	
	@Test
	public void testDeleteUnAuthorized() throws Exception {
		 
		doThrow(new RecordNotFoundException())
			.when(mockDataAccessManager)
			.deleteRecord(any(String.class), any(String.class), any(Collection.class));

		RequestBuilder request = MockMvcRequestBuilders.delete("/collection/000000000000000000000001")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isNotFound())
        	.andReturn();
    }		
	
	@Test
	public void testDeleteInvalidCollection() throws Exception {
		 
		doThrow(new DataAccessException("invalid collection"))
			.when(mockDataAccessManager)
			.deleteRecord(any(String.class), any(String.class), any(Collection.class));

		RequestBuilder request = MockMvcRequestBuilders.delete("/foobar/000000000000000000000001")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }			

	@Test
	public void testDeleteInvalidKey() throws Exception {
		 
		doThrow(new DataAccessException("invalid key"))
			.when(mockDataAccessManager)
			.deleteRecord(any(String.class), any(String.class), any(Collection.class));

		RequestBuilder request = MockMvcRequestBuilders.delete("/collection/foobar")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }			

	@Test
	public void testDeleteUnexpectedError() throws Exception {
		 
		doThrow(new RuntimeException("whoops"))
			.when(mockDataAccessManager)
			.deleteRecord(any(String.class), any(String.class), any(Collection.class));

		RequestBuilder request = MockMvcRequestBuilders.delete("/collection/000000000000000000000001")
			.contentType(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }	
	
	@Test
	public void testUpdate() throws Exception {
		
		// gen id
		ObjectId objId = new ObjectId();
		
		// create obj
		DBObject rec = new BasicDBObject();
		rec.put("_id", objId);
		rec.put("foo","bar");

		RequestBuilder request = MockMvcRequestBuilders.put("/collection/" + objId.toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isOk())
        	.andReturn();
    }	
	
	@Test
	public void testUpdateInValidCollection() throws Exception {
		 
		// gen id
		ObjectId objId = new ObjectId();

		DBObject rec = new BasicDBObject();
		rec.put("_id", objId);
		rec.put("foo","bar");

		doThrow(new DataAccessException("invalid collection"))
			.when(mockDataAccessManager)
			.updateRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.put("/foobar/" + objId.toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }		
	
	@Test
	public void testUpdateRecordWithNoKey() throws Exception {
		 
		// gen id
		ObjectId objId = new ObjectId();

		DBObject rec = new BasicDBObject();
		rec.put("foo","bar");

		doThrow(new DataAccessException("no key"))
			.when(mockDataAccessManager)
			.updateRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.put("/collection/" + objId.toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request)
        	// .andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }
	
	@Test
	public void testUpdateUnAuthorized() throws Exception {
		 
		// gen id
		ObjectId objId = new ObjectId();

		DBObject rec = new BasicDBObject();
		rec.put("_id", objId);
		rec.put("foo","bar");
		
		doThrow(new RecordNotFoundException())
			.when(mockDataAccessManager)
			.updateRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.put("/collection/" + objId.toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isNotFound())
        	.andReturn();
    }
	
	
	@Test
	public void testUpdateUnexpectedError() throws Exception {
		 
		// gen id
		ObjectId objId = new ObjectId();

		DBObject rec = new BasicDBObject();
		rec.put("_id", objId);
		rec.put("foo","bar");

		doThrow(new RuntimeException("whoops"))
			.when(mockDataAccessManager)
			.updateRecord(any(String.class), any(DBObject.class));

		RequestBuilder request = MockMvcRequestBuilders.put("/collection/" + objId.toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(rec.toString());
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andReturn();
    }		

	@Test
	public void testDialog() throws Exception {
		
		// SAP
		DBObject SAP_BP = new BasicDBObject();
		SAP_BP.put("Value", "BP");
		SAP_BP.put("Label", "BUTTERED POPCORN");
	
		BasicDBList SAP_LIST = new BasicDBList();
		SAP_LIST.add(SAP_BP);
		
		DBObject SAP = new BasicDBObject();
		SAP.put("ValueSet", SAP_LIST);
		SAP.put("Rank", "3");
		SAP.put("Multiple", "true");
		SAP.put("DisplayName", "SAP Marking");
		
		// CLASSIFICATION
		DBObject CLASS_U = new BasicDBObject();
		CLASS_U.put("Value", "U");
		CLASS_U.put("Label", "Unclassified");

		DBObject CLASS_C = new BasicDBObject();
		CLASS_C.put("Value", "C");
		CLASS_C.put("Label", "Confidential");

		BasicDBList CLASS_LIST = new BasicDBList();
		CLASS_LIST.add(CLASS_U);
		CLASS_LIST.add(CLASS_C);
		
		DBObject CLASS = new BasicDBObject();
		CLASS.put("ValueSet", CLASS_LIST);
		CLASS.put("Rank", "1");
		CLASS.put("Multiple", "false");
		CLASS.put("DisplayName", "Classification");

		// RELTO
		DBObject RELTO_USA = new BasicDBObject();
		RELTO_USA.put("Value", "USA");
		RELTO_USA.put("Label", "USA");
	
		BasicDBList RELTO_LIST = new BasicDBList();
		RELTO_LIST.add(RELTO_USA);
		
		DBObject RELTO = new BasicDBObject();
		RELTO.put("ValueSet", RELTO_LIST);
		RELTO.put("Rank", "4");
		RELTO.put("Multiple", "true");
		RELTO.put("DisplayName", "Release To");
		
		// SCI
		DBObject SCI_TK = new BasicDBObject();
		SCI_TK.put("Value", "TK");
		SCI_TK.put("Label", "TALENT KEYHOLE");
	
		BasicDBList SCI_LIST = new BasicDBList();
		SCI_LIST.add(SCI_TK);
		
		DBObject SCI = new BasicDBObject();
		SCI.put("ValueSet", SCI_LIST);
		SCI.put("Rank", "2");
		SCI.put("Multiple", "true");
		SCI.put("DisplayName", "SCI Marking");

		// SecurityLabel
		DBObject securityLabel = new BasicDBObject();
		securityLabel.put("SAP", SAP);
		securityLabel.put("classification", CLASS);
		securityLabel.put("RELTO", RELTO);
		securityLabel.put("SCI", SCI);

		// ROOT
		DBObject root = new BasicDBObject();
		root.put("SecurityLabel", securityLabel);
		
		when(mockSecurityDialogManager.buildInputRequirements())
			.thenReturn(root.toString());
		
		RequestBuilder request = MockMvcRequestBuilders.get("/dialog")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/json"))
        	.andExpect(content().string(root.toString()))
        	.andReturn();
	}
	
	@Test
	public void testDialogUnExpectedError() throws Exception {
		
		when(mockSecurityDialogManager.buildInputRequirements())
			.thenThrow(new RuntimeException("whoops"));
		
		RequestBuilder request = MockMvcRequestBuilders.get("/dialog")
			.accept(MediaType.APPLICATION_JSON);
		
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(jsonPath("$.error").value("whoops"))  
        	.andExpect(content().contentType("application/json"))
        	.andReturn();
	}	

	@Test
	public void testFindBinaryById() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		DBObject rec = new BasicDBObject();
		rec.put("contentType","foobar/content-type");
		rec.put("fileName","foobar-filename");
		rec.put("data", "foobar-data".getBytes());
		rec.put("securityLabel", "{ classification:{\"U\"}}");
		recs.add(rec);
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/data/000000000000000000000001");
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isOk())
        	.andExpect(header().string("Content-Type", "foobar/content-type"))
        	.andExpect(header().string("Content-Disposition", "attachment;filename=foobar-filename"))
        	.andExpect(content().contentType("foobar/content-type"))
        	.andExpect(content().string("foobar-data"))
        	.andReturn();
	}	
	
	@Test
	public void testFindBinaryByIdNotFound() throws Exception {
		
		BasicDBList recs = new BasicDBList();
		
		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenReturn(recs);
		
		RequestBuilder request = MockMvcRequestBuilders.get("/data/000000000000000000000001");
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(status().reason("record not found"))
        	.andReturn();
	}		
	
	@Test
	public void testFindBinaryByIdUnexpectedError() throws Exception {

		when(mockDataAccessManager.readRecord(any(String.class), any(DBObject.class)))
			.thenThrow(new RuntimeException("whoops"));

		RequestBuilder request = MockMvcRequestBuilders.get("/data/000000000000000000000001");
        mockMvc.perform(request) 
        	//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(status().reason("whoops"))
        	.andReturn();
	}	
	
	@Test
	public void testPostBinary() throws Exception {
		
    	BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"CLASS1"})); 
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"SCI1"}));  
        securityLabel.put("SAP", dbList(new String[]{"SAP1"}));  

        String fileContent = "file-contents";

		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.param("securityLabel", securityLabel.toString());
		request.file(new MockMultipartFile("data","file-name","text/plain",fileContent.getBytes()));
		
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isOk())
        	.andReturn();
	}
	
	@Test
	public void testPostBinaryNotAuthorized() throws Exception {

		doThrow(new DataAccessException("record not inserted", new Exception("not authorized")))
			.when(mockDataAccessManager)
			.createRecord(any(String.class), any(DBObject.class));

		BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"CLASS3"}));  // no access
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"SCI1"}));  
        securityLabel.put("SAP", dbList(new String[]{"SAP1"}));  

        String fileContent = "file-contents";
 
		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.param("securityLabel", securityLabel.toString());
        request.param("uploadedByPretty", "test");
		request.file(new MockMultipartFile("data","file-name","text/plain",fileContent.getBytes()));
		
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(status().reason("record not inserted"))
        	.andReturn();
	}		

	@Test
	public void testPostBinaryUnexpectedExcption() throws Exception {
		
		doThrow(new RuntimeException("whoops"))
			.when(mockDataAccessManager)
			.createRecord(any(String.class), any(DBObject.class));

		BasicDBObject securityLabel = new BasicDBObject();
        securityLabel.put("classification", dbList(new String[]{"CLASS1"})); 
        securityLabel.put("RELTO", dbList(new String[]{"USA"}));
        securityLabel.put("SCI", dbList(new String[]{"SCI1"}));  
        securityLabel.put("SAP", dbList(new String[]{"SAP1"}));  

        String fileContent = "file-contents";
        
		MockMultipartHttpServletRequestBuilder request = fileUpload("/data");
		request.accept(MediaType.ALL).characterEncoding("UTF-8").session(new MockHttpSession());
		request.param("securityLabel",  securityLabel.toString());
		request.file(new MockMultipartFile("data","file-name","text/plain",fileContent.getBytes()));
		
		mockMvc.perform(request) 
			//.andDo(print())
        	.andExpect(status().isBadRequest())
        	.andExpect(status().reason("whoops"))
        	.andReturn();
	}		

    private BasicDBList dbList(String[] items) {
    	BasicDBList result = new BasicDBList();
    	for( String item : items) {
    		result.add(item);
    	}
    	return result;
    }
    */
}
