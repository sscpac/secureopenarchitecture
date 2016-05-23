package mil.navy.spawar.soaf.ldap;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;


public class SoafUserContextMapper extends AbstractContextMapper {

    private String userNameMapping = "cn";
    private String userRealNameMapping;
    private String emailMapping;
    private String organizationMapping;
    private String phoneMapping;

    public Object doMapFromContext(DirContextOperations ctx) {
        SoafUser soafUser = new SoafUser();

        soafUser.setUserName(ctx.getStringAttribute(userNameMapping));
        soafUser.setUserRealName(ctx.getStringAttribute(userRealNameMapping));
        soafUser.setEmail(ctx.getStringAttribute(emailMapping));
        soafUser.setOrganization(ctx.getStringAttribute(organizationMapping));
        soafUser.setPhone(ctx.getStringAttribute(phoneMapping));

        return soafUser;
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
