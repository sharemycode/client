package net.sharemycode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
    	TestSuite tests = new TestSuite(AppTest.class);
    	tests.addTest(new AppTest("connectionTest"));
    	tests.addTest(new AppTest("getRequestTest"));
    	tests.addTest(new AppTest("postFormRequestTest"));
    	tests.addTest(new AppTest("postJSONTest"));
    	//tests.addtest(new AppTest("postMultipartTest"));
    	//tests.addTest(new AppTest("postLoginTest"));
        return tests;
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    public static final String DOMAIN = "localhost:8080";		// The domain of your REST service. Include the port after : if required.
	public static final String DIRECTORY = "";					// the directory where your service webapp lives
	public static final String RESTENDPOINT = "/sharemycode/rest";	// The rest endpoint directory.
    
	public void connectionTest() throws ClientProtocolException, IOException {
		Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		assertTrue(test.testConnection());
	}
	
	public void getRequestTest() throws ClientProtocolException, IOException {
		Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		// test a get request
		HttpResponse response = test.getRequest("/client/test");
		BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        String line = rd.readLine();
		
		assertEquals("Request must return correct response from server", "Hello client! Connection successful!", line);
	}
	
	public void postFormRequestTest() throws IOException {
		Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		HttpResponse response = test.postRequest("/user/create", "username=clienttest&email=test%40test.com&password=testpass&passwordc=testpass&emailc=test%40test.com&gname=client&sname=test");
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = rd.readLine();
        assertEquals("Expected {clienttest, test@test.com, client, test}", "{clienttest, test@test.com, client, test}", line);
	}
	public void postJSONTest() throws IOException {
		Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		JSONObject userJSON = new JSONObject();
			userJSON.put("username", "clienttest");
			userJSON.put("email", "test@test.com");
			userJSON.put("emailc", "test@test.com");
			userJSON.put("password", "testpassword");
			userJSON.put("passwordc", "testpassword");
			userJSON.put("fname", "client");
			userJSON.put("lname", "test");
		HttpResponse response = test.postRequest("/user/create", userJSON);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = rd.readLine();
        assertEquals("Expected {clienttest, test@test.com, client, test}", "{clienttest, test@test.com, client, test}", line);
	}
	/* incomplete
	public void postMultipartTest() throws IOException {
		Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		HttpResponse response = test.postRequest("/client/test/multipart", "postData");
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = rd.readLine();
        assertEquals("Expected {clienttest, test@test.com, client, test}", "{clienttest, test@test.com, client, test}", line);
	} */
	
	public void postLoginTest() throws ClientProtocolException, IOException {
		Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
		HttpResponse response = test.postRequest("/user/login", "username=test@password=testpassword");
		assertTrue(response.getStatusLine().getStatusCode() == 200);
	}
}