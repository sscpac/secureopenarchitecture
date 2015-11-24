package mil.navy.spawar.swif.ldap;

import java.util.Collection;

public class SwifGroup {

    private String name;
    private Collection<SwifUser> members;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<SwifUser> getMembers() {
        return members;
    }

    public void setMembers(Collection<SwifUser> members) {
        this.members = members;
    }

    @Override
    public String toString() {


        String ret = "{" +
                "\"name\":\"" + name + '"' +
                ", \"members\": [ ";

        for(SwifUser user : members )  {
            ret = ret.concat(user.toString());
            ret = ret.concat(", ");
        }

        ret = ret.substring(0, ret.length()-2); //remove last comma
        ret = ret.concat(" ] }");

        return ret;
    }
}
