package mil.navy.spawar.swif.ldap;

public class SwifUser {

    private String userName;
    private String userRealName;
    private String email;
    private String phone;
    private String organization;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return "{" +
                "\"userName\":\"" + userName + '"' +
                ", \"userRealName\":\"" + userRealName + '"' +
                ", \"email\":\"" + email + '"' +
                ", \"phone\":\"" + phone + '"' +
                ", \"organization\":\"" + organization + '"' +
                '}';
    }
}
