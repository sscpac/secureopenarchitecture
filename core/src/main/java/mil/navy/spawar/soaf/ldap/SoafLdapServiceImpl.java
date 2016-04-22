package mil.navy.spawar.soaf.ldap;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class SoafLdapServiceImpl implements ISoafLdapService {

    private LdapTemplate ldapTemplate;
    private SoafUserContextMapper soafUserContextMapper;
    private String userBaseDN;
    private String soafUserObjClass = "person";


    public SoafLdapServiceImpl(LdapContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    @Override
    public Collection<SoafUser> getAllUsers() {
        return ldapTemplate.search(query().base(userBaseDN).where("objectclass").is(soafUserObjClass),
                soafUserContextMapper);
    }

    @Override
    public Collection<SoafUser> getUsers(Collection<String> users) {
        ArrayList<SoafUser> soafUsers = new ArrayList<SoafUser>();

        for(String user : users ) {
            SoafUser soafUser = getUser(user);
            if(soafUser != null) {
                soafUsers.add(soafUser);
            }
        }

        return soafUsers;
    }

    @Override
    public SoafUser lookUpUser(String dn) {
        try {
            return (SoafUser) ldapTemplate.lookup(dn, soafUserContextMapper);
        } catch(NameNotFoundException e) {
            return null;
        }
    }

    @Required
    public void setSoafUserContextMapper(SoafUserContextMapper soafUserContextMapper) {
        this.soafUserContextMapper = soafUserContextMapper;
    }

    @Required
    public void setUserBaseDN(String userBaseDN) {
        this.userBaseDN = userBaseDN;
    }

    private SoafUser getUser(String user) {

        LdapName dn = LdapNameBuilder.newInstance(userBaseDN)
                .add(soafUserContextMapper.getUserNameMapping(), user)
                .build();

        return lookUpUser(dn.toString());
    }

}
