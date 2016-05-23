package mil.navy.spawar.soaf.ldap;

import java.util.Collection;

public interface ISoafLdapService {

    Collection<SoafUser> getAllUsers();
    Collection<SoafUser> getUsers(Collection<String> users);
    SoafUser lookUpUser(String dn);

}
