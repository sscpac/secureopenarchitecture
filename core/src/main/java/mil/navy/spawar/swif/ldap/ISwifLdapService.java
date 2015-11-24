package mil.navy.spawar.swif.ldap;

import java.util.Collection;

public interface ISwifLdapService {

    public Collection<SwifUser> getAllUsers();
    public Collection<SwifUser> getUsers(Collection<String> users);
    public Collection<SwifGroup> getGroups(Collection<String> groups);
    public SwifUser lookUpUser(String dn);

}
