package mil.navy.spawar.swif.security.dialog;

import org.json.simple.JSONArray;

public class EmptySetSecurityInputReqBuilder extends AbstractSecurityInputReqBuilder {

    @Override
    protected JSONArray buildValueSet() {
        return null;
    }
}
