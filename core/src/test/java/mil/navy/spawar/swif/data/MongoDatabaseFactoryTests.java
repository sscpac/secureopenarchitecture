package mil.navy.spawar.swif.data;

import static org.junit.Assert.*;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.DB;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:MongoDatabaseFactoryTests-beans.xml"})
public class MongoDatabaseFactoryTests {

    @Autowired(required = true)
    private DB mongodb;

    @Autowired(required = true)
    private DB secureMongodb;

	@Test
	public void testDatabaseFactory()  {
		
		assertNotNull(mongodb);
		assertTrue(mongodb instanceof com.mongodb.DB);
		//assertEquals("127.0.0.1", mongodb.getMongo().getAddress().getHost()); - getMongo() now attempts to connecto non existing server
		//assertEquals(12345, mongodb.getMongo().getAddress().getPort());
		assertEquals("swifDB", mongodb.getName());
	}
}
