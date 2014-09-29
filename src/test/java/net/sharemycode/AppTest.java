package net.sharemycode;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sharemycode.model.Project;
import net.sharemycode.model.ProjectResource;
import net.sharemycode.model.ProjectResource.ResourceType;

import org.apache.http.client.ClientProtocolException;

/**
 * Unit test for simple App.
 */
public class AppTest
extends TestCase {
    
    private static String validProjectId;  // projectId that works
    private static Long validResourceId;   // resourceId of a file
    
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
        tests.addTest(new AppTest("getProjectAccessTest"));
        tests.addTest(new AppTest("createProjectAuthorisationTest"));
        //tests.addTest(new AppTest("getProjectAuthorisationTest"));
        //tests.addTest(new AppTest("updateProjectAuthorisationTest"));
        //tests.addTest(new AppTest("removeProjectAuthorisationTest"));
        tests.addTest(new AppTest("listResourcesTest"));
        tests.addTest(new AppTest("fetchResourceTest"));
        tests.addTest(new AppTest("getResourceAccessTest"));
        tests.addTest(new AppTest("createResourceAuthorisationTest"));
        //tests.addTest(new AppTest("getResourceAuthorisationTest"));
        //tests.addTest(new AppTest("updateResourceAuthorisationTest"));
        //tests.addTest(new AppTest("removeResourceAuthorisationTest"));;
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
            System.out.println("Project ID: " + p.getId() +
                    ", Project Name: " + p.getName() +
                    ", Owner: " + p.getOwner() + 
                    ", Version: " + p.getVersion() +
                    ", URL:" + p.getUrl() + 
                    ", Description: " + p.getDescription());
        }
        if(projects.get(1) != null)
            validProjectId = projects.get(1).getId();  // the second project probably has a resource
        assertTrue(projects.size() > 0);	// at least one project is returned
    }

    /* FETCH PROJECT TEST */    // requires a valid projectId
    public void fetchProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project project = test.fetchProject(validProjectId);
        assertNotNull(project);
        System.out.println(project.getName());
    }

    /* LIST RESOURCES TEST */    // requires a valid projectId
    public void listResourcesTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        List<ProjectResource> resources = test.listResources(validProjectId);
        if(resources == null)
            fail("Resources could not be retrieved. No resources exist.");
        for (ProjectResource r : resources) {
            System.out.println("Resource ID: " + r.getId() +
                    ", Resource Name: " + r.getName() + 
                    ", Type: " + r.getResourceType());
            if(r.getResourceType() == ResourceType.FILE)
                validResourceId = r.getId();
        }
        assertTrue(resources.size() > 0);
        System.out.println(resources.get(0).getName());
    }

    /* FETCH RESOURCE TEST */    // requires a valid file resourceId
    public void fetchResourceTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        assertTrue(test.fetchResource(validResourceId) == 200);
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

    /* GET PROJECTACCESS TEST */
    public void getProjectAccessTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        assertNotNull(test.getProjectAccessLevel(validProjectId));
    }
    
    /* CREATE PROJECT AUTHORISATION TEST */
    public void createProjectAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User2", "user2@test.com", "user2@test.com", "user2", "user2", "user", "two");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User2");
        Project p = test.fetchProject(validProjectId);
        String result = test.createProjectAuthorisation(p, userId, "READ_WRITE");
        if(result == null) fail("Expected not null");
        assertTrue(result.equals("Authorisation created successfully"));
    }
    
    /* CREATE RESOUCE AUTHORISATION TEST */
    public void createResourceAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User2", "user2@test.com", "user2@test.com", "user2", "user2", "user", "two");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User2");
        String result = test.createResourceAuthorisation(validResourceId, userId, "READ_WRITE");
        if(result == null) fail("Expected not null");
        assertTrue(result.equals("Authorisation created successfully"));
    }
    
    /* GET RESOURCEACCESS TEST */
    public void getResourceAccessTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        assertNotNull(test.getResourceAccessLevel(validResourceId));
    }
}