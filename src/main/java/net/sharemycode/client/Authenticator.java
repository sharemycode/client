package net.sharemycode.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

/**
 * Adds Authorization header to client using token returned from library
 * @author Lachlan Archibald
 */

public class Authenticator implements ClientRequestFilter {

    private String token;

    public Authenticator(String token) {
        this.token = token;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", "Token " + token);
    }

    /**
     * Performs HTTP Basic Authentication for User
     * 
     * @param username String username
     * @param password String password for the user
     * @param client JAX-RS WebTarget to submit the request
     * @return valid authctoken for user
     */
    /* HTTP BASIC AUTHENTICATION */ // Tested: 25/09/2014
    protected static String httpBasicAuth(String username, String password,
            WebTarget client) {
        try {
            String encoding = Base64.encodeBase64String(new String(username
                    .toLowerCase() + ":" + password.toLowerCase())
                    .getBytes("UTF-8"));
            Response response = client.path("/auth/login").request()
                    .header("Authorization", "Basic " + encoding)
                    .post(Entity.text(""));
            String token = response.readEntity(String.class);
            return new JSONObject(token).getString("authctoken");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
