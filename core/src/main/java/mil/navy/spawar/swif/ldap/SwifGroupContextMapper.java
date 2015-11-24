package mil.navy.spawar.swif.ldap;


import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

import java.util.ArrayList;

public class SwifGroupContextMapper extends AbstractContextMapper {

    private String groupNameMapping = "cn";
    private String memberMapping = "member";
    private ISwifLdapService ldapService;

    @Override
    protected Object doMapFromContext(DirContextOperations dirContextOperations) {

        SwifGroup swifGroup = new SwifGroup();
        swifGroup.setName(dirContextOperations.getStringAttribute(groupNameMapping));

        ArrayList<SwifUser> swifUsers = new ArrayList<SwifUser>();
        String[] userDns = dirContextOperations.getStringAttributes(memberMapping);
        for(String userDn : userDns) {
            SwifUser swifUser = ldapService.lookUpUser(userDn);
            if(swifUser != null) {
                swifUsers.add(swifUser);
            }
        }

        swifGroup.setMembers(swifUsers);

        return swifGroup;
    }

    public String getGroupNameMapping() {
        return groupNameMapping;
    }

    @Required
    public void setLdapService(ISwifLdapService ldapService) {
        this.ldapService = ldapService;
    }

    public void setGroupNameMapping(String groupNameMapping) {
        this.groupNameMapping = groupNameMapping;
    }

    public void setMemberMapping(String memberMapping) {
        this.memberMapping = memberMapping;
    }
}


