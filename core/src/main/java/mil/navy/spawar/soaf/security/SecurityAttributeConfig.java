package mil.navy.spawar.soaf.security;

public class SecurityAttributeConfig {

    private String ldapAttributeName;
    private String dbAttributeName;
    private String userDetailsName;
    private String defaultValue;

    public String getLdapAttributeName() {
        return ldapAttributeName;
    }

    public void setLdapAttributeName(String ldapAttributeName) {
        this.ldapAttributeName = ldapAttributeName;
    }

    public String getDbAttributeName() {
        return dbAttributeName;
    }

    public void setDbAttributeName(String dbAttributeName) {
        this.dbAttributeName = dbAttributeName;
    }

    public String getUserDetailsName() {
        return userDetailsName;
    }

    public void setUserDetailsName(String userDetailsName) {
        this.userDetailsName = userDetailsName;
    }

    public String getDefaultValue() { return defaultValue; }

    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

}
