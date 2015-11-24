package mil.navy.spawar.swif.ldap;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;


public class SwifUserContextMapper extends AbstractContextMapper {

    private String userNameMapping = "cn";
    private String userRealNameMapping;
    private String emailMapping;
    private String organizationMapping;
    private String phoneMapping;

    public Object doMapFromContext(DirContextOperations ctx) {
        SwifUser swifUser = new SwifUser();

        swifUser.setUserName(ctx.getStringAttribute(userNameMapping));
        swifUser.setUserRealName(ctx.getStringAttribute(userRealNameMapping));
        swifUser.setEmail(ctx.getStringAttribute(emailMapping));
        swifUser.setOrganization(ctx.getStringAttribute(organizationMapping));
        swifUser.setPhone(ctx.getStringAttribute(phoneMapping));

        return swifUser;
    }

    public void setUserNameMapping(String userNameMapping) {
        this.userNameMapping = userNameMapping;
    }

    public String getUserNameMapping() {
        return userNameMapping;
    }

    @Required
    public void setUserRealNameMapping(String userRealNameMapping) {
        this.userRealNameMapping = userRealNameMapping;
    }

    @Required
    public void setEmailMapping(String emailMapping) {
        this.emailMapping = emailMapping;
    }

    @Required
    public void setOrganizationMapping(String organizationMapping) {
        this.organizationMapping = organizationMapping;
    }

    @Required
    public void setPhoneMapping(String phoneMapping) {
        this.phoneMapping = phoneMapping;
    }
}
