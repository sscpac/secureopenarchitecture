package mil.navy.spawar.swif.security.dialog;


//Interface for security input requirement builder.
//Implementations of this class are responsible building/returning the json describing the input details required for
//the attribute they are assigned.
public interface ISecurityInputReqBuilder {

    //return the attribute assigned to this builder
    public String getAttributeName();

    //return the json describing the input details required by the attribute assigned to this builder
    public String getInputRequirements();
}
