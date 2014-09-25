package net.sharemycode;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sharemycode.model.Project;
import net.sharemycode.model.ProjectResource;

import org.apache.http.client.ClientProtocolException;

/**
 * Unit test for simple App.
 */
public class AppTest
extends TestCase {
    
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
        tests.addTest(new AppTest("createUserTest"));
        tests.addTest(new AppTest("basicAuthTest"));
        tests.addTest(new AppTest("loginTest"));
        tests.addTest(new AppTest("getAuthStatusTest"));
        tests.addTest(new AppTest("createProjectTest"));
        //tests.addTest(new AppTest("fileUploadTest"));
        tests.addTest(new AppTest("listProjectsTest"));
        tests.addTest(new AppTest("fetchProjectTest"));
        tests.addTest(new AppTest("listResourcesTest"));
        tests.addTest(new AppTest("fetchResourceTest"));
        tests.addTest(new AppTest("closeClientTest"));
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

    /* CONNECTION TEST */
    public void connectionTest() throws ClientProtocolException, IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        assertTrue(test.testConnection());
    }

    /* CREATE USER TEST */
    public void createUserTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        String result = test.createUser("testUser", "test@test.com", "test@test.com", "test", "test", "testFirstName", "testLastName");
        assertEquals("User registration failed", "Registration successful!", result);
    }

    /* BASIC AUTHENTICATION TEST */
    public void basicAuthTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        String token = Authenticator.httpBasicAuth("testUser", "test", test.getClient());
        if(token == null)
            fail("Authentication must have failed");
        assertTrue(token.length() > 0);
    }

    /* LOGIN TEST */ // Requires createUser() to be completed
    public void loginTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        String result = test.login("testUser", "test");
        assertTrue(result.equals("Login successful!"));
    }
    
    public void getAuthStatusTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String status = test.getAuthStatus();
        assertEquals("Expected true", "true", status);
    }
    
    /* CREATE PROJECT TEST */
    public void createProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String url = test.createProject("testProject", "0.0.Test", "This is a test Project", null);
        assertTrue(url.length() == 6);  // returns a 6 character URL
    }
    /*
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
     */
    /* LIST PROJECTS TEST */
    public void listProjectsTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        List<Project> projects = test.listProjects();
        for (Project p : projects) {
            System.out.println("Project Name: " + p.getName() +
                    ", Owner: " + p.getOwner() + 
                    ", Version: " + p.getVersion() +
                    ", URL:" + p.getUrl() + 
                    ", Description: " + p.getDescription());
        }
        assertTrue(projects.size() > 0);	// at least one project is returned
    }

    /* FETCH PROJECT TEST */    // requires a valid projectId
    public void fetchProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project project = test.fetchProject("2c90518148a504630148a53e3e7d0000");
        assertNotNull(project);
        System.out.println(project.getName());
    }

    /* LIST RESOURCES TEST */    // requires a valid projectId
    public void listResourcesTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        List<ProjectResource> resources = test.listResources("2c90518148a504630148a55f4f050001");
        if(resources == null)
            fail("Resources could not be retrieved. No resources exist.");
        assertTrue(resources.size() > 0);
        System.out.println(resources.get(0).getName());
    }

    /* FETCH RESOURCE TEST */    // requires a valid file resourceId
    public void fetchResourceTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        assertTrue(test.fetchResource(7L) == 200);
    }

    /* CLOSE CLIENT TEST */
    public void closeClientTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.close();
        try {
            test.testConnection();
            fail("This test should have failed");
        } catch (IllegalStateException e) {
            assertTrue(true);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}