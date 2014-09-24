package net.sharemycode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sharemycode.model.Project;	// JavaBean entities
import net.sharemycode.model.ProjectResource;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

public class Client {

    public static final String DOMAIN = "localhost:8080";		       // The domain of your REST service. Include the port after : if required.
    public static final String DIRECTORY = "";					       // The directory where your service webapp lives
    public static final String RESTENDPOINT = "/sharemycode/rest";     // REST endpoint directory.
    public static final String UPLOADENDPOINT = "/sharemycode/upload"; // File upload endpoint
    public static final int MAX_UPLOAD = 10485760; // 10MB

    // Client instance variables
    private String target;
    private javax.ws.rs.client.Client client;


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
        this.target = "http://" + domain + directory + RESTEndpoint;
        this.client = ClientBuilder.newClient();
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

        Response response = client.target(target).path(requestContent).request(MediaType.TEXT_PLAIN).get();
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
        Response response = client.target(target).path("/register").request(MediaType.TEXT_PLAIN).post(Entity.json(data));
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
        Response response = client.target(target).path("/projects").request(MediaType.TEXT_PLAIN).post(Entity.json(data));
        String message = response.readEntity(String.class);
        response.close();
        return message;
    }

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

    /* --- GET REQUESTS --- */

    /* LIST PROJECTS - GET JSON */  // Tested 23/09/2014
    public List<Project> listProjects() {
        // return a list of projects (User's projects when Authentication is working)
        GenericType<List<Project>> projectType = new GenericType<List<Project>>() {};
        List<Project> projects =  client.target(target).path("/projects").request().get(projectType);
        return projects;
    }

    /* FETCH PROJECT - GET JSON */  // Tested 23/09/2014
    public Project fetchProject(String projectId) {
        // return project data as JSON object
        String resource = "/projects/{projectId}";
        Project project = client.target(target).path(resource)
                .resolveTemplate("projectId", projectId).request().get(Project.class);
        return project;
    }

    /* LIST RESOURCES - GET JSON*/  // Tested 23/09/2014
    public List<ProjectResource> listResources(String projectId) {
        // Return a list of resources for a project as JSON
        String resource = "/projects/{projectId}/resources";
        GenericType<List<ProjectResource>> resourceType = new GenericType<List<ProjectResource>>() {};
        List<ProjectResource> resources =  client.target(target).path(resource)
                .resolveTemplate("projectId", projectId).request().get(resourceType);
        return resources;
    }

    /* FETCH RESOURCE */ // Tested 23/09/2014
    public int fetchResource(Long resourceId) {
        int status = 0;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String resource = "/resources/{resourceId}";
        try {
            Response response = client.target(target).path(resource)
                    .resolveTemplate("resourceId", resourceId)
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
        }
        return status;
    }

    /* --- PUT REQUESTS --- */
    // TODO UpdateProject
    // TODO UpdateResource
    // TODO UpdateUser

    /* --- DELETE REQUESTS --- */

    //TODO delete project       - DELETE
    /* DELETE PROJECT - DELETE */
    public int deleteProject(String projectId) {
        String resource = "/projects/{projectId}";
        Response response = client.target(target).path(resource)
                .resolveTemplate("projectId", projectId).request().delete();
        int status = response.getStatus();
        return status;
    }

    //TODO delete resource      - DELETE
    /* DELETE RESOURCE - DELETE */
    public int deleteResource(Long resourceId) {
        String resource = "/resources/{resourceId}";
        Response response = client.target(target).path(resource)
                .resolveTemplate("resourceId", resourceId).request().delete();
        int status = response.getStatus();
        return status;
    }


    //TODO user login   - POST
    //TODO user logout  - GET
    //TODO update authorisation - POST
    //TODO remove authorisation - GET?
    //TODO publish resource     - POST
    //TODO subscribe to resource updates - POST/WS?
    //TODO publish resource update  - POST/WS?
}