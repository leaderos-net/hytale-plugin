package net.leaderos.hytale.shared.model.request.impl.credit;

import net.leaderos.hytale.shared.model.request.GetRequest;

import java.io.IOException;

public class GetCreditsRequest extends GetRequest {

    public GetCreditsRequest(String username) throws IOException {
        super("credits?username=" + username);
    }

}
