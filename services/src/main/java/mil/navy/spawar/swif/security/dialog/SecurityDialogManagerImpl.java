package mil.navy.spawar.swif.security.dialog;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;

public class SecurityDialogManagerImpl implements ISecurityDialogManager {

    List<ISecurityInputReqBuilder> reqBuilders;

    public void setRequirementBuilders(List<ISecurityInputReqBuilder> reqBuilders) {
        this.reqBuilders = reqBuilders;
    }

    //creates json by getting input from each of the input requirement builder objects
    @Override
    public String buildInputRequirements() {
        JSONObject root = new JSONObject();
        JSONObject secLabel = new JSONObject();
        root.put("SecurityLabel", secLabel);

        //iterate through each builder and get input
        for(ISecurityInputReqBuilder reqBuilder : reqBuilders) {
            secLabel.put(reqBuilder.getAttributeName(),
                    JSONValue.parse(reqBuilder.getInputRequirements()));
        }

        return root.toJSONString();
    }
}
