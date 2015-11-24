package mil.navy.spawar.swif.security.dialog;

//Interface for the dialog manager
public interface ISecurityDialogManager
{
    //returns json string with information needed by the client to build the security dialog
    public String buildInputRequirements();
}
