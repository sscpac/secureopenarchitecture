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
    private SwifGroupContextMapper swifGroupContextMapper;
    private String userBaseDN;
    private String groupBaseDN;
    private String swifUserObjClass = "person";
    private String groupObjClass = "groupOfNames";


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
    public Collection<SwifGroup> getGroups(Collection<String> groups) {
        ArrayList<SwifGroup> swifGroups = new ArrayList<SwifGroup>();

        for(String group : groups) {
            SwifGroup swifGroup = getGroup(group);
            if(swifGroup != null) {
                swifGroups.add(swifGroup);
            }
        }

        return swifGroups;
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
    public void setSwifGroupContextMapper(SwifGroupContextMapper swifGroupContextMapper) {
        this.swifGroupContextMapper = swifGroupContextMapper;
    }

    @Required
    public void setUserBaseDN(String userBaseDN) {
        this.userBaseDN = userBaseDN;
    }

    @Required
    public void setGroupBaseDN(String groupBaseDN) {
        this.groupBaseDN = groupBaseDN;
    }

    public void setSwifUserObjClass(String swifUserObjClass) {
        this.swifUserObjClass = swifUserObjClass;
    }

    public void setGroupObjClass(String groupObjClass) {
        this.groupObjClass = groupObjClass;
    }

    private SwifUser getUser(String user) {

        LdapName dn = LdapNameBuilder.newInstance(userBaseDN)
                .add(swifUserContextMapper.getUserNameMapping(), user)
                .build();

        return lookUpUser(dn.toString());
    }

    private SwifGroup getGroup(String group) {

        LdapName dn = LdapNameBuilder.newInstance(groupBaseDN)
                .add(swifGroupContextMapper.getGroupNameMapping(), group)
                .build();

        try {
            return (SwifGroup) ldapTemplate.lookup(dn, swifGroupContextMapper);
        } catch(NameNotFoundException e) {
            return null;
        }
    }

}
