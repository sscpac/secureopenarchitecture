package mil.navy.spawar.swif.ldap;

//Requires properly setup local SWIF ldap to run
//TODO: setup an embedded ldap for unit testing

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:ldap-tests.xml"})
public class SwifLdapServiceTests {

    @Autowired(required=true)
    private ISwifLdapService ldapService;

    @Test
    public void testGetAllUsers()  {
        System.out.println(ldapService.getAllUsers());
    }

    @Test
    public void testGetUsers()  {

    }



}