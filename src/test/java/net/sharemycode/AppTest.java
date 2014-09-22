package net.sharemycode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

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
        //tests.addTest(new AppTest("postLoginTest"));
        tests.addTest(new AppTest("fileUploadTest"));
        return tests;
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public static final String DOMAIN = "localhost:8080";              // The domain of your REST service. Include the port after : if required.
    public static final String DIRECTORY = "";                         // The directory where your service webapp lives
    public static final String RESTENDPOINT = "/sharemycode/rest";     // REST endpoint directory.
    public static final String UPLOADENDPOINT = "/sharemycode/upload"; // File upload endpoint

    public void connectionTest() throws ClientProtocolException, IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        assertTrue(test.testConnection());
    }

    public void getRequestTest() throws ClientProtocolException, IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        // test a get request
        HttpResponse response = test.getRequest("/system/test");
        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        String line = rd.readLine();

        assertEquals("Request must return correct response from server", "Hello client! Connection successful!", line);
    }
    
    public void postFormRequestTest() throws IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        HttpResponse response = test.postRequest("/system/test/form", "name=hello&value=world");
        assertEquals("Expected 200", 200, response.getStatusLine().getStatusCode());
    }

    public void postJSONTest() throws IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        JSONObject userJSON = new JSONObject();
        userJSON.put("name", "hello");
        userJSON.put("value", "world");
        HttpResponse response = test.postRequest("/system/test/json", userJSON);
        assertEquals("Expected 200", 200, response.getStatusLine().getStatusCode());
    }

    public void postLoginTest() throws ClientProtocolException, IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        HttpResponse response = test.postRequest("/user/login", "username=test@password=testpassword");
        assertTrue(response.getStatusLine().getStatusCode() == 200);
    }
    
    public void fileUploadTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        String filePath = "/home/larchibald/test.txt";
        JSONObject result = null;
        try {
            result = test.fileUpload(filePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(result.getBoolean("success"));
        
    }
}