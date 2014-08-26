package net.sharemycode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Response;

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
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.core.ZipFile;

public class Client {
	
	public static final String DOMAIN = "localhost:8080";		// The domain of your REST service. Include the port after : if required.
	public static final String DIRECTORY = "";					// the directory where your service webapp lives
	public static final String RESTENDPOINT = "/service/rest";	// The rest endpoint directory.
	

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
        /*
        // test uploading project zip file
        //HttpEntity project = createProject("testProject", "testVersion", "description", "/home/larchibald/testProject.zip");
        // test uploading project folder
        
        response = client.createProject("testProject", "testVersion", "description", "/home/larchibald/test/HelloEE7/");
        if(response.getStatusLine().getStatusCode() == 200) {
			rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
	        line = "";
	        while ((line = rd.readLine()) != null) {
	          System.out.println(line);
	        }
		} else if(response.getStatusLine().getStatusCode() == 400) {
			System.out.println("Error: 400 Bad Request");
		}*/
        
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
		this.client = new DefaultHttpClient();
	}
	/* 
	  * TEST CONNECTION
	  * Test connection to the server is ok
	  */
	public Boolean testConnection() throws ClientProtocolException, IOException {
		//test connection to the server
		String requestContent = "/client/test";
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
	 * HTTP POST Requests: JSON, urlencodedstring and multipart(HTTPEntity)
	 */
	
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
	
	/*
	 * CREATE USER - POST JSON
	 * Register new user
	 */
	public HttpResponse createUser(String username, String email, String emailc, String password, String passwordc, String givenName, String surname) {
		
		JSONObject userJSON = new JSONObject();
			userJSON.put("username", username);
			userJSON.put("email", email);
			userJSON.put("emailc", emailc);
			userJSON.put("password", password);
			userJSON.put("passwordc", passwordc);
			userJSON.put("gname", givenName);
			userJSON.put("sname", surname);
		HttpResponse response = postRequest("/user/create", userJSON);
		return response;
	}
	/* 
	 * PUBLISH PROJECT - POST
	 * Share project from local files, upload as zip
	 */
	public HttpResponse createProject(String name, String version, String description, String projectPath){
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
	/* Now no longer needed. MultipartEntityBuilder generates the boundary automatically
	private static String generateBoundary() {
        return (Long.toString(System.currentTimeMillis(), 16));
    }
	*/
	
	//TODO user login	- POST
	
	//TODO user logout	- GET
	public HttpResponse userLogout() {
		// TODO authorise user?
		HttpResponse response = getRequest("/user/logout");
		
		return response;
	}
	//TODO update authorisation	- POST
	//TODO remove authorisation	- GET?
	//TODO publish resource		- POST
	//TODO list resources		- GET
	//TODO list projects
	//TODO fetch resource		- GET
	//TODO delete resource		- GET?
	//TODO subscribe to resource updates - POST/WS?
	//TODO publish resource update	- POST/WS?
}
