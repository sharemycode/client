package net.sharemycode;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Token Authenticator Filter for Client Library
 * @author Lachlan Archibald
 * Description: Adds Authorization header to client using token returned from library
 */

public class Authenticator implements ClientRequestFilter {
    
    private String token;

    public Authenticator(String token) {
        this.token = token;
    }
    
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", "Token " + token);
    }

    public static String httpBasicAuth(String username, String password, WebTarget client) {
        try {
            String encoding = Base64.encodeBase64String(new String(username.toLowerCase() + ":" + password.toLowerCase()).getBytes("UTF-8"));
            Response response = client.path("/auth/login").request()
                    .header("Authorization", "Basic " + encoding)
                    .post(Entity.text(""));
            String token = response.readEntity(String.class);
            return new JSONObject(token).getString("authctoken");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
