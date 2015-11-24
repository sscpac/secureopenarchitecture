package mil.navy.spawar.swif.security.dialog;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class SecurityDialogManagerImplTests {

	@Mock
	private ISecurityInputReqBuilder mockClassificationReqBuilder;

	@Mock
	private ISecurityInputReqBuilder mockSciReqBuilder;

	@Mock
	private ISecurityInputReqBuilder mockSapReqBuilder;

	private SecurityDialogManagerImpl secDialogMgr;

	private String classificationAttrName = "classification";
	private String classificationInputRequirement = "{'ValueSet':[{'Value':'U','Label':'Unclassified'},{'Value':'C','Label':'Confidential'}],'Rank':'1','Multiple':'false','DisplayName':'Classification'}".replace('\'', '"');

	private String sciAttrName = "SCI";
	private String sciInputRequirement = "{'ValueSet':[{'Value':'TK','Label':'TALENT KEYHOLE'}],'Rank':'2','Multiple':'true','DisplayName':'SCI Marking'}".replace('\'', '"');

	private String sapAttrName = "SAP";
	private String sapInputRequirement = "{'ValueSet':[{'Value':'BP','Label':'BUTTERED POPCORN'}],'Rank':'3','Multiple':'true','DisplayName':'SAP Marking'}".replace('\'', '"');

	
	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		when(mockClassificationReqBuilder.getAttributeName()).thenReturn(classificationAttrName);
		when(mockClassificationReqBuilder.getInputRequirements()).thenReturn(classificationInputRequirement);
		
		when(mockSciReqBuilder.getAttributeName()).thenReturn(sciAttrName);
		when(mockSciReqBuilder.getInputRequirements()).thenReturn(sciInputRequirement);

		when(mockSapReqBuilder.getAttributeName()).thenReturn(sapAttrName);
		when(mockSapReqBuilder.getInputRequirements()).thenReturn(sapInputRequirement);

		List<ISecurityInputReqBuilder> mockReqBuilderList = new ArrayList<ISecurityInputReqBuilder>();
		mockReqBuilderList.add(mockClassificationReqBuilder);
		mockReqBuilderList.add(mockSciReqBuilder);
		mockReqBuilderList.add(mockSapReqBuilder);
		
		secDialogMgr = new SecurityDialogManagerImpl();
		secDialogMgr.setRequirementBuilders(mockReqBuilderList);
	}
	
	@Test
	public void testBuildInputRequirements() {
		
		String result = secDialogMgr.buildInputRequirements();
		assertNotNull(result);
		DBObject dbObj = (DBObject) JSON.parse(result);
		assertTrue(dbObj.containsField("SecurityLabel"));
		Object obj = dbObj.get("SecurityLabel");
		assertNotNull(obj);
		assertTrue(obj instanceof DBObject);
		dbObj = (DBObject)obj;
		assertTrue(dbObj.containsField("SAP"));
		assertTrue(dbObj.containsField("SCI"));
		assertTrue(dbObj.containsField("classification"));
	}
}
