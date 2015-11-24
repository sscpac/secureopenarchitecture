package mil.navy.spawar.swif.security;

import java.util.Map;

public class SecurityAttributeConfig {

    private String ldapAttributeName;
    private String dbAttributeName;
    private String userDetailsName;
    private String displayName;
    private String valueType;
    private int rank;
    private String defaultValue;
    private Map<String, Integer> rankings;
    private SecurityLabelRequirement secLabelRequirement = SecurityLabelRequirement.NONE;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, Integer> getRankings() {
        return rankings;
    }

    public void setRankings(Map<String, Integer> rankings) {
        this.rankings = rankings;
    }

    public SecurityLabelRequirement getSecLabelRequirement() {
        return secLabelRequirement;
    }


    public void setSecLabelRequirement(SecurityLabelRequirement secLabelRequirement) {
        this.secLabelRequirement = secLabelRequirement;
    }
}
