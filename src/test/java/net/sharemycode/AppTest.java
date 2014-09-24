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
        tests.addTest(new AppTest("createUserTest"));
        tests.addTest(new AppTest("createProjectTest"));
        //tests.addTest(new AppTest("postLoginTest"));
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

    public void connectionTest() throws ClientProtocolException, IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        assertTrue(test.testConnection());
    }
    /*
    public void postLoginTest() throws ClientProtocolException, IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        HttpResponse response = test.postRequest("/user/login", "username=test@password=testpassword");
        assertTrue(response.getStatusLine().getStatusCode() == 200);
    }
     */
    public void createUserTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        String result = test.createUser("testUser", "test@test.com", "test@test.com", "test", "test", "testFirstName", "testLastName");
        assertEquals("User registration failed", "Registration successful!", result);
    }

    public void createProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
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
    public void listProjectsTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        List<Project> projects = test.listProjects();
        System.out.println(projects.get(0).getName());
        assertTrue(projects.size() > 0);	// at least one project is returned
    }

    public void fetchProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        Project project = test.fetchProject("2c90518148a504630148a53e3e7d0000");
        System.out.println(project.getName());
        assertNotNull(project);
    }

    public void listResourcesTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        List<ProjectResource> resources = test.listResources("2c90518148a504630148a55f4f050001");
        System.out.println(resources.get(0).getName());
        assertTrue(resources.size() > 0);
    }

    public void fetchResourceTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        assertTrue(test.fetchResource(7L) == 200);
    }

    public void closeClientTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.close();
        try {
            test.testConnection();
            fail("This test should have failed");
        } catch (IllegalStateException e) {
            assertTrue(true);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}