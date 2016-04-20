package mil.navy.spawar.swif.ldap;

import java.util.Collection;

public interface ISwifLdapService {

    Collection<SwifUser> getAllUsers();
    Collection<SwifUser> getUsers(Collection<String> users);
    SwifUser lookUpUser(String dn);

}
