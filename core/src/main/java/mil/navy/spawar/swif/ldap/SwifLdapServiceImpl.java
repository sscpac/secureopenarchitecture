package mil.navy.spawar.swif.ldap;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class SwifLdapServiceImpl implements ISwifLdapService {

    private LdapTemplate ldapTemplate;
    private SwifUserContextMapper swifUserContextMapper;
    private String userBaseDN;
    private String swifUserObjClass = "person";


    public SwifLdapServiceImpl(LdapContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    @Override
    public Collection<SwifUser> getAllUsers() {
        return ldapTemplate.search(query().base(userBaseDN).where("objectclass").is(swifUserObjClass),
                swifUserContextMapper);
    }

    @Override
    public Collection<SwifUser> getUsers(Collection<String> users) {
        ArrayList<SwifUser> swifUsers = new ArrayList<SwifUser>();

        for(String user : users ) {
            SwifUser swifUser = getUser(user);
            if(swifUser != null) {
                swifUsers.add(swifUser);
            }
        }

        return swifUsers;
    }

    @Override
    public SwifUser lookUpUser(String dn) {
        try {
            return (SwifUser) ldapTemplate.lookup(dn, swifUserContextMapper);
        } catch(NameNotFoundException e) {
            return null;
        }
    }

    @Required
    public void setSwifUserContextMapper(SwifUserContextMapper swifUserContextMapper) {
        this.swifUserContextMapper = swifUserContextMapper;
    }

    @Required
    public void setUserBaseDN(String userBaseDN) {
        this.userBaseDN = userBaseDN;
    }

    private SwifUser getUser(String user) {

        LdapName dn = LdapNameBuilder.newInstance(userBaseDN)
                .add(swifUserContextMapper.getUserNameMapping(), user)
                .build();

        return lookUpUser(dn.toString());
    }

}
