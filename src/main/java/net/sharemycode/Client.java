package net.sharemycode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;

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
	
	
	public static void main(String[] args) throws IOException {
		// Create a client
		Client client = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		// test the connection
		if (client.testConnection()) {
			System.out.println("Connection successful!");
		} else {
			System.out.println("Conection failed. Exiting...");
		}
		// test a get request
		HttpResponse response = client.getRequest("/project/randomURL");
		BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        
        // now test a post request application/x-www-form-urlencoded
        response = client.postRequest("/user/create", "username=clienttest&email=test%40test.com&password=testpass&passwordc=testpass&emailc=test%40test.com&gname=client&sname=test");
        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }
        // uploading multipart is currently giving issues.
        // test uploading project zip file
        HttpEntity project = createProject("testProject", "testVersion", "description", "/home/larchibald/testProject.zip");
        response = client.postRequest("/project/create", project);
        if(response.getStatusLine().getStatusCode() == 200) {
			rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
	        line = "";
	        while ((line = rd.readLine()) != null) {
	          System.out.println(line);
	        }
		} else if(response.getStatusLine().getStatusCode() == 400) {
			System.out.println("Error: 400 Bad Request");
		}
        
        // Test completed!
        System.out.println("Client test complete!");
	}

	public Client(String domain, String directory, String RESTEndpoint) {
		// Constructor: create HTTPClient
		this.target = "http://" + domain + directory + RESTEndpoint;
		this.client = new DefaultHttpClient();
	}
	
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

	public HttpResponse getRequest(String requestContent) throws ClientProtocolException, IOException {
		// perform a GET request. Client must decode the response
        HttpGet request = new HttpGet(target + requestContent);
        HttpResponse response = client.execute(request);
        request.releaseConnection();
        return response;
    }
	
	public HttpResponse postRequest(String postContent, JSONObject postData) throws ClientProtocolException, IOException {
		// submit a POST request with JSON data
		HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(target + postContent);
        StringEntity input = new StringEntity(postData.toString());
        post.addHeader("content-type", "application/json");
        post.setEntity(input);
        HttpResponse response = client.execute(post);
        post.releaseConnection(); // release the connection
        return response;
    }
	
	public HttpResponse postRequest(String postContent, String postData) throws ClientProtocolException, IOException {
		// submit a POST request with urlencodedstring data
		HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(target + postContent);
        StringEntity input = new StringEntity(postData);
        request.addHeader("content-type", "application/x-www-form-urlencoded");
        request.setEntity(input);
        HttpResponse response = client.execute(request);
        request.releaseConnection(); // release the connection
        return response;
    }
	
	public HttpResponse postRequest(String postContent, HttpEntity postData) throws ClientProtocolException, IOException {
		// submit a POST request with Multipart data. Assumes HttpEntity already created with MultipartBuilder
		HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(target + postContent);
        request.setEntity(postData);
        request.addHeader("content-type", postData.getContentType().getValue());
        request.addHeader("accept-encoding", "multipart/form-data");
        HttpResponse response = client.execute(request);
        request.releaseConnection(); // release the connection
        return response;
    }
	
	public static HttpEntity createProject(String name, String version, String description, String projectPath){
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
		
		HttpEntity entity = multipart.build();
		return entity;
		
	}
	/* Now no longer needed. MultipartEntityBuilder generates the boundary automatically
	private static String generateBoundary() {
        return (Long.toString(System.currentTimeMillis(), 16));
    }
	*/
}
