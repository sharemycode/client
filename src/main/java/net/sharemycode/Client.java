package net.sharemycode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.soap.AddressingFeature.Responses;

import net.sharemycode.model.Project;	// JavaBean entities
import net.sharemycode.model.ProjectAccess;
import net.sharemycode.model.ProjectResource;
import net.sharemycode.model.ResourceAccess;
import net.sharemycode.model.UserProfile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {

    public static final String DOMAIN = "localhost:8080";		       // The domain of your REST service. Include the port after : if required.
    public static final String DIRECTORY = "";					       // The directory where your service webapp lives
    public static final String RESTENDPOINT = "/sharemycode/rest";     // REST endpoint directory.
    public static final String UPLOADENDPOINT = "/sharemycode/upload"; // File upload endpoint
    public static final int MAX_UPLOAD = 10485760; // 10MB

    // Client instance variables
    private String restTarget;
    private javax.ws.rs.client.Client client;
    private WebTarget RESTClient;
    

    /*
     * TEST MAIN METHOD - development only
     */
    public static void main(String[] args) throws IOException {
        // Create a client
        Client client = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        // test the connection
        if (client.testConnection()) {
            System.out.println("Connection successful!");
        } else {
            System.out.println("Conection failed. Exiting...");
        }
        // Test completed!
        System.out.println("Client test complete!");
    }
    /*
     * CLIENT CONSTRUCTOR
     * Create HTTPClient with server details
     */
    public Client(String domain, String directory, String RESTEndpoint) {
        // Constructor: create HTTPClient
        restTarget = "http://" + domain + directory + RESTEndpoint;
        client = ClientBuilder.newClient();
        RESTClient = client.target(restTarget);
    }
    
    public WebTarget getClient() {
        return RESTClient;
    }

    /* CLOSE CLIENT */
    public void close() {
        // closes REST Client connection
        client.close();
    }
    /*
     * TEST CONNECTION
     * Test connection to the server is ok
     */
    public Boolean testConnection() throws ClientProtocolException, IOException {
        //test connection to the server
        String requestContent = "/system/test";

        Response response = RESTClient.path(requestContent).request(MediaType.TEXT_PLAIN).get();
        System.out.println(response.readEntity(String.class));
        if(response.getStatus() == 200) {	// if connection successful, return true
            response.close();	// release the connection
            return true;
        } else {
            response.close();
            return false;
        }
    }

    /*
     * *********************************************************
     * PROCEDURES
     * *********************************************************
     */

    /* --- POST REQUESTS --- */

    /* CREATE USER - POST JSON */	// Tested: 23/09/2014
    public String createUser(String username, String email, String emailc, String password, String passwordc, String firstName, String lastName) {
        // register a new user
        JSONObject userJSON = new JSONObject();
        userJSON.put("username", username);
        userJSON.put("email", email);
        userJSON.put("emailc", emailc);
        userJSON.put("password", password);
        userJSON.put("passwordc", passwordc);
        userJSON.put("firstName", firstName);
        userJSON.put("lastName", lastName);
        String data = userJSON.toString();
        Response response = RESTClient.path("/register").request(MediaType.TEXT_PLAIN).post(Entity.json(data));
        String message = response.readEntity(String.class);
        response.close();
        return message;
    }

    /* CREATE PROJECT - POST JSON */    // Tested 20/09/2014
    // TODO test this function
    public String createProject(String name, String version, String description, List<String> attachments) {
        // create a new project, returns url to project
        JSONObject project = new JSONObject();
        project.put("name", name);
        project.put("version", version);
        project.put("description", description);
        project.put("attachments", attachments);    // attachments are Long encoded as String
        // TODO attachments
        String data = project.toString();
        Response response = RESTClient.path("/projects").request(MediaType.TEXT_PLAIN).post(Entity.json(data));
        String message = response.readEntity(String.class);
        URI location = response.getLocation();
        if(location != null)
        	System.out.println(location);
        response.close();
        return message;
    }

    /* CREATE PROJECT AUTHORISATION */  // Tested: 01/10/2014
    public String createProjectAuthorisation(Project p, String userId, ProjectAccess.AccessLevel accessLevel) {
    	// create ProjectAccess object
    	if(p == null || userId == null || accessLevel == null)
    		return "Error: invalid parameters";
    	ProjectAccess access = new ProjectAccess();
    	access.setProject(p);
    	access.setUserId(userId);
    	access.setOpen(false);
    	access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/projects/{projectId}/access/";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .request(MediaType.TEXT_PLAIN).post(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        URI location = response.getLocation();
        response.close();
        if(status == 201) {	// resource created
        	System.out.println(location.toString());
            return "Authorisation successful";
        } else
            return "Error: " + status +  " - " + message;
    }

    /* CREATE RESOURCE AUTHORISATION */  // Tested: 01/10/2014
    public String createResourceAuthorisation(ProjectResource r, String userId, ResourceAccess.AccessLevel accessLevel) {
        // create ResourceAccess object
        if(r == null || userId == null || accessLevel == null)
            return "Error: invalid parameters";
        ResourceAccess access = new ResourceAccess();
        access.setResource(r);
        access.setUserId(userId);
        access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/resources/{resourceId}/access/";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .request(MediaType.TEXT_PLAIN).post(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        URI location = response.getLocation();
        response.close();
        if(status == 201) {
            System.out.println(location.toString());
            return "Authorisation successful";
        } else
            return "Error: " + status +  " - " + message;
    }
    
    /* FILE UPLOAD */
    // TODO Complete this function
    public JSONObject fileUpload(String path) throws IOException {

        File file = new File(path);

        URL url = new URL("http://" + DIRECTORY + DOMAIN + UPLOADENDPOINT);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setUseCaches(false);
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod("POST");
        //httpConnection.setRequestProperty("Content-Type", "application/octet-stream");
        httpConnection.setRequestProperty("filename", file.getName());

        // open output stream of HTTP connection for writing data
        OutputStream outputStream = httpConnection.getOutputStream();
        // create input stream for reading from file
        FileInputStream inputStream = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead = -1;
        System.out.println("DEBUG: Writing data to server");
        while((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        System.out.println("DEBUG: Data written");
        outputStream.close();
        inputStream.close();
        // reads server's response
        String message = null;
        try {
            message = IOUtils.toString(httpConnection.getInputStream(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server's response: " + message);
        return new JSONObject(message);
    }

    // TODO Create attachment
    /* Upload Attachment */
    /*    public String createAttachment(String filePath) {
        // upload attachment, return Long id encoded as String
        File file = new File(filePath);

        // TODO AJAX Upload to UPLOADENDPOINT

        HttpRequest response;
        String message = IOUtils.toString(response.getEntity().getContent());
        JSONObject result = new JSONObject(message);
        if(result.getBoolean("success"))
            return result.getString("id");
        else
            return null;
    }
     */
    
    //TODO publish resource     - POST
    //TODO subscribe to resource updates - POST/WS?
    //TODO publish resource update  - POST/WS?

    /* --- GET REQUESTS --- */

    /* GET AUTH STATUS */   // Tested: 25/09/2014
    public String getAuthStatus() {
        Response response = RESTClient.path("/auth/status").request().get();
        String message = response.readEntity(String.class);
        return message;
    }
    
    /* LOOKUP USER BY USERNAME */   // Tested 02/10/2014
    // Currently returns userId for use in authorisation methods. This method may need to be removed
    public String lookupUserByUsername(String username) {
        String resource = "/users/{username}";
        Response response = RESTClient.path(resource).resolveTemplate("username", username).request(MediaType.APPLICATION_JSON).get();
        try {
            JSONObject user = new JSONObject(response.readEntity(String.class));
            return user.getString("id");
        } catch(JSONException e) {
            System.err.println("Problem getting data");
            e.printStackTrace();
        }
        return null;
    }
    
    /* LOOKUP USER BY EMAIL */  // Tested 02/10/2014
    // Currently returns userId. This may need to return username only.
    public String lookupUserByEmail(String email) {
        String resource = "/users/search/";
        Response response = RESTClient.path(resource)
                .queryParam("email", email).queryParam("username", "")
                .request(MediaType.APPLICATION_JSON).get();
        try {
            JSONObject user = new JSONObject(response.readEntity(String.class));
            return user.getString("id");
        } catch(JSONException e) {
            System.err.println("Problem getting data");
            e.printStackTrace();
        }
        return null;
    }
    
    /* GET USER PROFILE */
    // TODO Write test for this function
    public UserProfile getUserProfile(String username) {
        String resource = "/users/{username}/profile";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON).get();
        UserProfile profile = response.readEntity(UserProfile.class);
        return profile;
    }
    
    /* LIST PROJECTS - GET JSON */  // Tested 23/09/2014
    public List<Project> listProjects() {
        // return a list of projects (User's projects when Authentication is working)
        GenericType<List<Project>> projectType = new GenericType<List<Project>>() {};
        List<Project> projects =  RESTClient.path("/projects").request().get(projectType);
        return projects;
    }

    /* FETCH PROJECT - GET JSON */  // Tested 23/09/2014
    public Project fetchProject(String projectId) {
        // return project data as JSON object
        String resource = "/projects/{projectId}";
        try {
            Project project = RESTClient.path(resource)
                    .resolveTemplate("projectId", projectId).request().get(Project.class);
            return project;
        } catch (NotFoundException e) {
            System.err.println("Resource: rest/projects/" + projectId + "\n" + e);
            return null;
        }
    }

    /* LIST RESOURCES - GET JSON*/  // Tested 23/09/2014
    public List<ProjectResource> listResources(Project p) {
        // Return a list of resources for a project as JSON
        String resource = "/projects/{projectId}/resources";
        GenericType<List<ProjectResource>> resourceType = new GenericType<List<ProjectResource>>() {};
        try {
            List<ProjectResource> resources =  RESTClient.path(resource)
                    .resolveTemplate("projectId", p.getId()).request().get(resourceType);
            return resources;
        } catch (NotFoundException e) {
            System.err.println("Resource: rest/projects/" + p.getId() + "/resources\n" + e);
            return null;
        }
    }

    /* FETCH RESOURCE */ // Tested 23/09/2014
    public int fetchResource(ProjectResource r) {
        int status = 0;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String resource = "/resources/{resourceId}";
        try {
            Response response = RESTClient.path(resource)
                    .resolveTemplate("resourceId", r.getId())
                    .request(MediaType.APPLICATION_OCTET_STREAM).get();
            status = response.getStatus();
            if(status == 200) {   // success, download resource
                String content = response.getHeaderString("Content-Disposition");
                String fileName = content.substring(content.indexOf('=') + 2, content.length() - 1);
                System.out.println(fileName);
                outputStream = new FileOutputStream(fileName);
                inputStream = response.readEntity(InputStream.class);
                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                System.out.println("Done!");
                inputStream.close();
                outputStream.close();
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            System.err.println("Resource: rest/resources/" + r.getId() + "\n" + e);
            return 404;
        }
        return status;
    }
    
    /* GET PROJECT ACCESS LEVEL */
    // TODO Test this function
    public ProjectAccess.AccessLevel getProjectAccessLevel(Project p) {
        // get the project access level for the current logged in user
        String resource = "/projects/{projectId}/access";
        ProjectAccess projectAccess = RESTClient.path(resource).resolveTemplate("projectId", p.getId())
                .request().get(ProjectAccess.class);
        if(projectAccess != null)
            return projectAccess.getAccessLevel();
        else
            return null;
    }
    
    /* GET RESOURCE ACCESS LEVEL */
    // TODO Test this function
    public ResourceAccess.AccessLevel getResourceAccessLevel(ProjectResource r) {
        // get the resource access level for the current logged in user
        String resource = "/resources/{resourceId}/access";
        ResourceAccess resourceAccess = RESTClient.path(resource).resolveTemplate("resourceId", r.getId())
                .request().get(ResourceAccess.class);
        if(resourceAccess != null)
            return resourceAccess.getAccessLevel();
        else
            return null;
    }
    
    /* GET PROJECT AUTHORISATION */     // Tested: 01/10/2014
    public ProjectAccess.AccessLevel getProjectAuthorisation(Project p, String userId) {
        // get the project access level for a specific user
        String resource = "/projects/{projectId}/access/{userId}";
        ProjectAccess projectAccess = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .resolveTemplate("userId", userId)
                .request().get(ProjectAccess.class);
        if(projectAccess != null)
            return projectAccess.getAccessLevel();
        else
            return null;
    }
    
    /* GET RESOURCE AUTHORISATION */    // Tested: 01/10/2014
    public ResourceAccess.AccessLevel getResourceAuthorisation(ProjectResource r, String userId) {
        // get the resource access level for a specific user
        String resource = "/resources/{resourceId}/access/{userId}";
        ResourceAccess resourceAccess = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .resolveTemplate("userId", userId)
                .request().get(ResourceAccess.class);
        if(resourceAccess != null)
            return resourceAccess.getAccessLevel();
        else
            return null;
    }

    /* --- PUT REQUESTS --- */
    // TODO UpdateProject
    // TODO UpdateResource
    
    /* UPDATE USER ACCOUNT */   // Tested 02/10/2014
    // Currently returns JSON user data. This is probably a security issue.
    // It should probably only return a success status
    public JSONObject updateUserAccount(String username, String newUsername,
            String email, String emailc, String password, String passwordc, 
            String firstName, String lastName) {
        // create userUpdate object
        JSONObject userJSON = new JSONObject();
        userJSON.put("username", newUsername);
        if (email != null && emailc != null && email.equals(emailc)) {
            userJSON.put("email", email);
            userJSON.put("emailc", emailc);
        }
        if (password != null && passwordc != null && password.equals(passwordc)) {
            userJSON.put("password", password);
            userJSON.put("passwordc", passwordc);
        }
        userJSON.put("firstName", firstName);
        userJSON.put("lastName", lastName);
        String data = userJSON.toString();
        // define REST resource
        String resource = "/users/{username}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON).put(Entity.json(data));
        int status = response.getStatus();
        String body = response.readEntity(String.class);
        response.close();
        if(status == 200) {
            return new JSONObject(body);
        } else
            System.err.println("Error occured while updating user. Status: HTTP" + status);
            return null;
    }
    
    /* UPDATE USER PROFILE */   // Tested: 02/10/2014
    public UserProfile updateUserProfile(String username, String displayName, String about, String contact, String interests) {
        UserProfile profile = new UserProfile();
        profile.setDisplayName(displayName);
        profile.setAbout(about);
        profile.setContact(contact);
        profile.setInterests(interests);
        // define REST resource
        String resource = "/users/{username}/profile";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON).put(Entity.entity(profile, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        UserProfile result = response.readEntity(UserProfile.class);
        response.close();
        if(status == 200) {
            return result;
        } else
            return null;
    }
    
    /* UPDATE PROJECT AUTHORISATION */  // Tested: 01/10/2014
    public String updateProjectAuthorisation(Project p, String userId, ProjectAccess.AccessLevel accessLevel) {
    	// create ProjectAccess object
    	if(p == null || userId == null || accessLevel == null)
    		return "Error: invalid parameters";
    	ProjectAccess access = new ProjectAccess();
    	access.setProject(p);
    	access.setUserId(userId);
    	access.setOpen(false);
    	access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/projects/{projectId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN).put(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if(status == 200) {
            return "Update successful";
        } else
            return "Error: " + status +  " - " + message;
    }
    
    /* UPDATE RESOURCE AUTHORISATION */  // Tested: 01/10/2014
    public String updateResourceAuthorisation(ProjectResource r, String userId, ResourceAccess.AccessLevel accessLevel) {
        // create ResourceAccess object
        if(r == null || userId == null || accessLevel == null)
            return "Error: invalid parameters";
        ResourceAccess access = new ResourceAccess();
        access.setResource(r);
        access.setUserId(userId);
        access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/resources/{resourceId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN).put(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if(status == 200) {
            return "Update successful";
        } else
            return "Error: " + status +  " - " + message;
    }
    
    /* --- DELETE REQUESTS --- */

    // TODO delete project       - DELETE
    /* DELETE PROJECT - DELETE */
    public int deleteProject(String projectId) {
        String resource = "/projects/{projectId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", projectId).request().delete();
        int status = response.getStatus();
        return status;
    }

    // TODO delete resource      - DELETE
    /* DELETE RESOURCE - DELETE */
    public int deleteResource(Long resourceId) {
        String resource = "/resources/{resourceId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", resourceId).request().delete();
        int status = response.getStatus();
        return status;
    }

    /* REMOVE PROJECT AUTHORISATION */  // Tested: 01/10/2014
    public String removeProjectAuthorisation(Project p, String userId) {
        // submit DELETE request
        String resource = "/projects/{projectId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId()).resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN).delete();
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if(status == 200)
            return "Authorisation removed";
        else
            return "Error: " + status +  " - " + message;
    }
    
    
    /* REMOVE RESOURCE AUTHORISATION */  // Tested: 01/10/2014
    public String removeResourceAuthorisation(ProjectResource r, String userId) {
        // submit DELETE request
        String resource = "/resources/{resourceId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId()).resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN).delete();
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if(status == 200)
            return "Authorisation removed";
        else
            return "Error: " + status +  " - " + message;
    }    
    
    /* USER AUTHENTICATION */

    /* USER LOGIN */    // Tested: 25/06/2014
    public String login(String username, String password) {
        // submit login request
        if(username == null || password == null)
            return "Username and password cannot be empty";
        String token = Authenticator.httpBasicAuth(username, password, RESTClient);
        if (token != null) {
            RESTClient.register(new Authenticator(token));
            return "Login successful!";
        } else
            return "Login failed";
    }

    /* USER LOGOUT */
    //TODO user logout  - GET
    
    /* HTTP BASIC AUTHENTICATION */ // Tested: 25/09/2014
    public String httpBasicAuth(String username, String password) {
        try {
            String encoding = Base64.encodeBase64String(new String(username.toLowerCase() + ":" + password.toLowerCase()).getBytes("UTF-8"));
            Response response = RESTClient.path("/auth/login").request()
                    .header("Authorization", "Basic " + encoding)
                    .post(Entity.text(""));
            String token = response.readEntity(String.class);
            return token;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}