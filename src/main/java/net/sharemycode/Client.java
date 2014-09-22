package net.sharemycode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class Client {

    public static final String DOMAIN = "localhost:8080";		       // The domain of your REST service. Include the port after : if required.
    public static final String DIRECTORY = "";					       // The directory where your service webapp lives
    public static final String RESTENDPOINT = "/sharemycode/rest";     // REST endpoint directory.
    public static final String UPLOADENDPOINT = "/sharemycode/upload"; // File upload endpoint
    public static final int MAX_UPLOAD = 10485760; // 10MB

    // Client instance variables
    private String target;
    private HttpClient client;

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
    @SuppressWarnings("deprecation")
    public Client(String domain, String directory, String RESTEndpoint) {
        // Constructor: create HTTPClient
        this.target = "http://" + domain + directory + RESTEndpoint;
        this.client = new DefaultHttpClient();
    }
    /*
     * TEST CONNECTION
     * Test connection to the server is ok
     */
    public Boolean testConnection() throws ClientProtocolException, IOException {
        //test connection to the server
        String requestContent = "/system/test";
        HttpGet request = new HttpGet(target + requestContent);	// set up the request
        HttpResponse response = client.execute(request);
        request.releaseConnection();							// release the connection
        if(response.getStatusLine().getStatusCode() == 200) {	// if connection successful, return true
            return true;
        } else {
            return false;
        }
    }

    /*
     * GET REQUEST
     * Standard HTTP GET Request
     */
    public HttpResponse getRequest(String requestContent) {
        // perform a GET request. Client must decode the response
        HttpGet request = new HttpGet(target + requestContent);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (ClientProtocolException e) {
            System.err.println("Error: ClientProtocolException when GETing from " + requestContent);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: IOException when GETing from " + requestContent);
            e.printStackTrace();
        }
        request.releaseConnection();
        return response;
    }

    /*
     * POST REQUESTS
     * HTTP POST Requests: JSON, urlencodedstring
     * Scheduled for removal: multipart(HTTPEntity)
     */

    /* POST JSON */
    public HttpResponse postRequest(String postContent, JSONObject postData) {
        // submit a POST request with JSON data
        HttpPost request = new HttpPost(target + postContent);
        StringEntity input = null;
        HttpResponse response = null;
        try {
            input = new StringEntity(postData.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(input);

            response = client.execute(request);
        } catch (UnsupportedEncodingException e1) {
            System.err.println("Error: UnsupportedEncodingException when POSTing to " + postContent);
            e1.printStackTrace();
        } catch (ClientProtocolException e) {
            System.err.println("Error: ClientProtocolException when POSTing to " + postContent);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: IOException when POSTing to " + postContent);
            e.printStackTrace();
        }
        request.releaseConnection(); // release the connection
        return response;
    }
    /* POST URLENCODEDSTRING */
    public HttpResponse postRequest(String postContent, String postData) {
        // submit a POST request with urlencodedstring data
        HttpPost request = new HttpPost(target + postContent);
        StringEntity input = null;
        HttpResponse response = null;
        try {
            input = new StringEntity(postData);
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(input);
            response = client.execute(request);
        } catch (UnsupportedEncodingException e1) {
            System.err.println("Error: UnsupportedEncodingException when POSTing to " + postContent);
            e1.printStackTrace();
        } catch (ClientProtocolException e) {
            System.err.println("Error: ClientProtocolException when POSTing to " + postContent);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: IOException when POSTing to " + postContent);
            e.printStackTrace();
        }
        request.releaseConnection(); // release the connection
        return response;
    }

    /* POST MULTIPART */
    public HttpResponse postRequest(String postContent, HttpEntity postData) {
        // submit a POST request with Multipart data. Assumes HttpEntity already created with MultipartBuilder
        HttpPost request = new HttpPost(target + postContent);
        request.setEntity(postData);
        request.addHeader("content-type", postData.getContentType().getValue());
        request.addHeader("accept-encoding", "multipart/form-data");
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (ClientProtocolException e) {
            System.err.println("Error: ClientProtocolException when POSTing to " + postContent);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: IOException when POSTing to " + postContent);
            e.printStackTrace();
        }
        request.releaseConnection(); // release the connection
        return response;
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
    
    // TODO DELETE REQUEST
    
    // TODO PUT REQUEST
    
    /* 
     * *********************************************************
     * PROCEDURES
     * *********************************************************
     */
    
    /*
     * CREATE USER - POST JSON
     * Register new user
     */
    public String createUser(String username, String email, String emailc, String password, String passwordc, String firstName, String lastName) {

        JSONObject userJSON = new JSONObject();
        userJSON.put("username", username);
        userJSON.put("email", email);
        userJSON.put("emailc", emailc);
        userJSON.put("password", password);
        userJSON.put("passwordc", passwordc);
        userJSON.put("firstName", firstName);
        userJSON.put("lastName", lastName);
        HttpResponse response = postRequest("/register", userJSON);
        String message = null;
        try {
            message = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }
    
    /*
     * PUBLISH PROJECT - POST MULTIPART (OLD)
     * Share project from local files, upload as zip
     */
/*   public HttpResponse createProject(String name, String version, String description, String projectPath){
        // create a HttpEntity from project information and zip archive
        MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
        multipart.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        // add the text parts first
        multipart.addTextBody("pname", name, ContentType.APPLICATION_FORM_URLENCODED);
        multipart.addTextBody("version", version, ContentType.APPLICATION_FORM_URLENCODED);
        multipart.addTextBody("description", description, ContentType.APPLICATION_FORM_URLENCODED);
        // now prepare the file part
        try {
            File file = new File(projectPath);
            if (file.exists()) {
                if(file.isFile()){
                    ZipFile projectZip = new ZipFile(projectPath);
                    if(projectZip.isValidZipFile()) {
                        // valid zip file, upload it
                        multipart.addBinaryBody("projectFile", file);
                    } else {
                        System.err.println("Error: Attempt to upload invalid file");
                        return null;
                    }
                } else if(file.isDirectory()) {
                    // create a zip file and upload
                    String newPath = projectPath + "/" + name + "_" +
                            System.currentTimeMillis() + ".zip";	// ensure that name is unique
                    ZipFile projectZip = new ZipFile(newPath);
                    ZipParameters parameters = new ZipParameters();
                    parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                    parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
                    parameters.setEncryptFiles(false);
                    projectZip.createZipFileFromFolder(file, parameters, false, 0l);
                    File projectFile = new File(newPath);
                    // now test if valid, then upload
                    if(projectZip.isValidZipFile()) {
                        // valid zip file, upload it
                        multipart.addBinaryBody("projectFile", projectFile);
                    } else {
                        System.err.println("Error: Attempt to upload invalid file");
                        return null;
                    }
                } else {
                    System.err.println("Error: Unknown error occured");
                    return null;
                }
            }else {
                System.err.println("Error: File/path does not exist. Please check the path and try again");
                return null;
            }
        } catch (ZipException e) {
            System.err.println("Error checking project file. Ensure that project is a .zip file, or a writable directory");
            e.printStackTrace();
        }

        HttpEntity projectEntity = multipart.build();
        HttpResponse response = postRequest("/project/create", projectEntity);

        return response;

    }
*/
    /* LIST PROJECTS - GET JSON */
    // TODO test this function
    public JSONObject listProjects() {
        // return a list of projects (User's projects when Authentication is working)
        
        HttpResponse response = getRequest("/projects");
        String body = null;
        try {
            body = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(body);
    }
    
    /* FETCH PROJECT - GET JSON */
    // TODO test this function
    public JSONObject fetchProject(String projectId) {
        // return project data as JSON object
        String resource = "/projects/" + projectId;
        HttpResponse response = getRequest(resource);
        String body = null;
        try {
            body = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(body);
    }
    
    /* LIST RESOURCES - GET JSON*/
    // TODO test this function
    public JSONObject listResources(String projectId) {
        // Return a list of resources for a project as JSON
        String resource = "/projects/" + projectId + "/resources";
        HttpResponse response = getRequest(resource);
        String body = null;
        try {
            body = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new JSONObject(body);
    }
    
    /* CREATE PROJECT - POST JSON */
    // TODO test this function
    public String createProject(String name, String version, String description, List<String> attachments) {
        // create a new project, returns url to project
        JSONObject project = new JSONObject();
        project.put("name", name);
        project.put("version", version);
        project.put("description", description);
        project.put("attachments", attachments);    // attachments are Long encoded as String
        // TODO attachments
        HttpResponse response = postRequest("/projects/", project);
        // returns unique URL of project
        String message = null;
        try {
            message = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
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
    //TODO delete project       - DELETE
    //TODO fetch resource       - GET
    
    //TODO delete resource      - DELETE
    /* DELETE RESOURCE - DELETE */
    public String deleteResource(Long resourceId) {
        String resource = "/resources/" + resourceId;
        //HttpResponse response = deleteRequest(resource);
        return null;
    }
    
    
    //TODO user login   - POST
    //TODO user logout  - GET
    //TODO update authorisation - POST
    //TODO remove authorisation - GET?
    //TODO publish resource     - POST
    //TODO subscribe to resource updates - POST/WS?
    //TODO publish resource update  - POST/WS?
}