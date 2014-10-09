package net.sharemycode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sharemycode.model.Project;
import net.sharemycode.model.ProjectAccess;
import net.sharemycode.model.ProjectResource;
import net.sharemycode.model.ResourceAccess;
import net.sharemycode.model.ProjectResource.ResourceType;
import net.sharemycode.model.UserProfile;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

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
        TestSuite basicTests = new TestSuite(AppTest.class);
        basicTests.addTest(new AppTest("createUserTest"));
        //basicTests.addTest(new AppTest("createProjectTest"));
        basicTests.addTest(new AppTest("listProjectsTest"));
        //basicTests.addTest(new AppTest("getProjectAccessTest"));
        basicTests.addTest(new AppTest("closeClientTest"));
        
        /*
        tests.addTest(new AppTest("connectionTest"));
        tests.addTest(new AppTest("createUserTest"));
        tests.addTest(new AppTest("basicAuthTest"));
        tests.addTest(new AppTest("loginTest"));
        tests.addTest(new AppTest("getAuthStatusTest"));
        tests.addTest(new AppTest("createProjectTest"));
        tests.addTest(new AppTest("createAttachmentTest"));
        tests.addTest(new AppTest("listProjectsTest"));
        tests.addTest(new AppTest("fetchProjectTest"));
        tests.addTest(new AppTest("getProjectAccessTest"));
        tests.addTest(new AppTest("getResourceAccessTest"));
        tests.addTest(new AppTest("createProjectAuthorisationTest"));
        tests.addTest(new AppTest("getProjectAuthorisationTest"));
        tests.addTest(new AppTest("updateProjectAuthorisationTest"));
        tests.addTest(new AppTest("removeProjectAuthorisationTest"));
        tests.addTest(new AppTest("listResourcesTest"));
        tests.addTest(new AppTest("fetchResourceTest"));
        tests.addTest(new AppTest("getResourceAccessTest"));
        tests.addTest(new AppTest("createResourceAuthorisationTest"));
        tests.addTest(new AppTest("getResourceAuthorisationTest"));
        tests.addTest(new AppTest("updateResourceAuthorisationTest"));
        tests.addTest(new AppTest("removeResourceAuthorisationTest"));
        tests.addTest(new AppTest("lookupUserByUsernameTest"));
        tests.addTest(new AppTest("lookupUserByEmailTest"));
        tests.addTest(new AppTest("getUserProfileTest"));
        tests.addTest(new AppTest("updateUserProfileTest"));
        tests.addTest(new AppTest("updateUserAccountTest"));
        tests.addTest(new AppTest("changeProjectOwnerTest"));
        tests.addTest(new AppTest("publishResourceTest"));
        tests.addTest(new AppTest("updateResourceTest"));
        tests.addTest(new AppTest("updateProjectTest"));
        tests.addTest(new AppTest("deleteProjectTest"));
        tests.addTest(new AppTest("deleteResourceTest"));
        tests.addTest(new AppTest("closeClientTest"));
        */
        return basicTests;
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
        // create a project without any attachments
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String url = test.createProject("testProject", "0.0.Test", "This is a test Project", null);
        assertTrue(url.length() == 6);  // returns a 6 character URL
    }
    
    /* CREATE ATTACHMENT TEST */
    public void createAttachmentTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String filePath = "/home/lachlan/test.txt";
        String result = test.createAttachment(filePath);
        System.out.println("Attachment id: " + result);
        assertTrue(Long.valueOf(result) > -1L);
    }
     
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
        assertTrue(projects.size() > 0);	// at least one project is returned
    }
    
    /* LIST SHARED PROJECTS TEST */
    public void listSharedProjectsTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("User2", "user2");
        List<Project> projects = test.listSharedProjects();
        for (Project p : projects) {
            System.out.println("Project ID: " + p.getId() +
                    ", Project Name: " + p.getName() +
                    ", Owner: " + p.getOwner() + 
                    ", Version: " + p.getVersion() +
                    ", URL:" + p.getUrl() + 
                    ", Description: " + p.getDescription());
        }
        assertTrue(projects.size() > 0);    // at least one project is returned
    }    

    /* FETCH PROJECT TEST */    // requires a valid projectId
    public void fetchProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project validProject = test.listProjects().get(0);
        Project project = test.fetchProject(validProject.getId());
        assertNotNull(project);
    }

    /* LIST RESOURCES TEST */    // requires a valid projectId
    public void listResourcesTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        List<ProjectResource> resources = null;
        for(Project p : projects) {
            resources = test.listResources(p);
            if(resources == null)
                fail("Resources could not be retrieved. No resources exist.");
            for(ProjectResource r : resources) {
                System.out.println("Resource ID: " + r.getId() +
                        ", Resource Name: " + r.getName() + 
                        ", Type: " + r.getResourceType());
            }
        }
        assertTrue(resources.size() > 0);
    }

    /* FETCH RESOURCE TEST */    // requires a valid file resourceId
    public void fetchResourceTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        for(Project p : projects) {
            List<ProjectResource> resources = test.listResources(p);
            for(ProjectResource r : resources) {
                if(r.getResourceType() == ResourceType.FILE) {
                    validResource = r;
                    break;
                }
            }
            
        }
        assertTrue(test.fetchResource(validResource) == 200);
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
        Project p = test.listProjects().get(0);
        assertNotNull(test.getProjectAccessLevel(p));
    }
    
    /* GET RESOURCEACCESS TEST */
    public void getResourceAccessTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        for(Project p : projects) {
            List<ProjectResource> resources = test.listResources(p);
            for(ProjectResource r : resources) {
                if(r.getResourceType() == ResourceType.FILE) {
                    validResource = r;
                    break;
                }
            }
        }
        assertNotNull(test.getResourceAccessLevel(validResource));
    }
    
    /* CREATE PROJECT AUTHORISATION TEST */ // Tested 30/09/2014
    public void createProjectAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User2", "user2@test.com", "user2@test.com", "user2", "user2", "user", "two");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User2");
        Project p = test.listProjects().get(0);
        String result = test.createProjectAuthorisation(p, userId, ProjectAccess.AccessLevel.READ_WRITE);
        assertEquals("Expected Authorisation successful ", "Authorisation successful", result);
    }
    
    /* GET PROJECT AUTHORISATION TEST */
    public void getProjectAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User2", "user2@test.com", "user2@test.com", "user2", "user2", "user", "two");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User2");
        Project p = test.listProjects().get(0);
        assertNotNull(test.getProjectAuthorisation(p, userId));
    }
    
    /* UPDATE PROJECT AUTHORISATION TEST */ 
    public void updateProjectAuthorisationTest() {
    	Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
    	test.createUser("User3", "user3@test.com", "user3@test.com", "user3", "user3", "user", "three");
    	test.login("testUser", "test");
    	String userId = test.lookupUserByUsername("User3");
    	Project p = test.listProjects().get(0);
    	test.createProjectAuthorisation(p, userId, ProjectAccess.AccessLevel.READ_WRITE);
    	String result = test.updateProjectAuthorisation(p, userId, ProjectAccess.AccessLevel.READ);
    	assertEquals("Expected Update successful ", "Update successful", result);	
    }
    
    /* REMOVE PROJECT AUTHORISATION TEST */
    public void removeProjectAuthorisationTest() {
    	Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User2", "user2@test.com", "user2@test.com", "user2", "user2", "user", "two");
    	test.login("testUser", "test");
    	String userId = test.lookupUserByUsername("User2");
    	Project p = test.listProjects().get(0);
    	String result = test.removeProjectAuthorisation(p, userId);
    	assertEquals("Expected Authorisation removed", "Authorisation removed", result);
    }
    
    /* CREATE RESOUCE AUTHORISATION TEST */
    public void createResourceAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User3", "user3@test.com", "user3@test.com", "user3", "user3", "user", "three");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User3");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        for(Project p : projects) {
            List<ProjectResource> resources = test.listResources(p);
            for(ProjectResource r : resources) {
                if(r.getResourceType() == ResourceType.FILE) {
                    validResource = r;
                    break;
                }
            }
            
        }
        String result = test.createResourceAuthorisation(validResource, userId, ResourceAccess.AccessLevel.READ_WRITE);
        assertEquals("Expected Authorisation successful ", "Authorisation successful", result);
    }
    
    /* GET RESOURCE AUTHORISATION TEST */
    public void getResourceAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User3", "user3@test.com", "user3@test.com", "user3", "user3", "user", "three");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User3");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        for(Project p : projects) {
            List<ProjectResource> resources = test.listResources(p);
            for(ProjectResource r : resources) {
                if(r.getResourceType() == ResourceType.FILE) {
                    validResource = r;
                    break;
                }
            }
        }
        assertNotNull(test.getResourceAuthorisation(validResource, userId));
    }
    
    /* UPDATE RESOURCE AUTHORISATION TEST */ 
    public void updateResourceAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User3", "user3@test.com", "user3@test.com", "user3", "user3", "user", "three");
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User3");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        for(Project p : projects) {
            List<ProjectResource> resources = test.listResources(p);
            for(ProjectResource r : resources) {
                if(r.getResourceType() == ResourceType.FILE) {
                    validResource = r;
                    break;
                }
            }
        }
        test.createResourceAuthorisation(validResource, userId, ResourceAccess.AccessLevel.READ_WRITE);
        String result = test.updateResourceAuthorisation(validResource, userId, ResourceAccess.AccessLevel.READ);
        assertEquals("Expected Update successful ", "Update successful", result);   
    }
    
    /* REMOVE PROJECT AUTHORISATION TEST */
    public void removeResourceAuthorisationTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("User3");
        ProjectResource validResource = null;
        List<Project> projects = test.listProjects();
        for(Project p : projects) {
            List<ProjectResource> resources = test.listResources(p);
            for(ProjectResource r : resources) {
                if(r.getResourceType() == ResourceType.FILE) {
                    validResource = r;
                    break;
                }
            }
            
        }
        String result = test.removeResourceAuthorisation(validResource, userId);
        assertEquals("Expected Authorisation removed", "Authorisation removed", result);
    }
    
    /* LOOKUP USER BY USERNAME TEST */
    public void lookupUserByUsernameTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String result = test.lookupUserByUsername("testUser");
        System.out.println(result);
        assertNotNull(result);
    }
    /* LOOKUP USER BY EMAIL TEST */
    public void lookupUserByEmailTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        String userId = test.lookupUserByUsername("testUser");
        String result = test.lookupUserByEmail("test@test.com");
        assertEquals("Expected matching user ID", userId, result);
    }
    
    /* GET USER PROFILE TEST */
    public void getUserProfileTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        UserProfile profile = test.getUserProfile("testUser");
        // Print profile details to stdout
        if(profile == null)
            fail("Expected not null");
        System.out.println("DisplayName: \"" + profile.getDisplayName() + 
                "\", About: \"" + profile.getAbout() + 
                "\", Contact: \"" + profile.getContact() + 
                "\", Interests: \"" + profile.getInterests() + "\"");
        assertEquals("Expected testuser", "testuser", profile.getDisplayName());
    }
    
    /* UPDATE USER PROFILE TEST */
    public void updateUserProfileTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        UserProfile update = test.updateUserProfile("testUser", "testuser", "I am a test user", "Email: test@test.com", "Testing");
        assertEquals("Expected I am a test user", "I am a test user", update.getAbout());
    }
    
    /* UPDATE USER ACCOUNT TEST */
    public void updateUserAccountTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        JSONObject result = test.updateUserAccount("testUser", "", "", "", "", "", "Test", "User");
        System.out.println(result);
        
        assertEquals("Expected User", "User", result.getString("lastName"));   
    }
    
    /* PUBLISH RESOURCE TEST */
    public void publishResourceTest() throws IOException {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        String result = test.publishResource(p, null, "/home/lachlan/test.txt");
        assertEquals("Expected Resource created", "Resource created", result);
    }
    
    /* CREATE DIRECTORY TEST */
    public void createDirectoryTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        ProjectResource r = null;   // create directory at root
        String result = test.createDirectory(p, r, "newDirectory");
        assertEquals("Expected Directory created", "Directory created", result);
    }
    
    /* UPDATE PROJECT TEST */
    public void updateProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        String result = test.updateProject(p, "newProjectName", "0.0.1", "This is a new description");
        assertEquals("Expected Project updated", "Project updated", result);
    }
    
    /* UPDATE RESOURCE TEST */
    public void updateResourceTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        ProjectResource validResource = null;
        List<ProjectResource> resources = test.listResources(p);
        for(ProjectResource r : resources) {
            if(r.getResourceType() == ResourceType.FILE) {
                validResource = r;
                break;
            }
        }
        String result = test.updateResource(validResource, "/home/lachlan/test2.txt");
        assertEquals("Expected Resource updated", "Resource updated", result);
    }
    
    /* CHANGE PROJECT OWNER TEST */
    public void changeProjectOwnerTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.createUser("User2", "user2@test.com", "user2@test.com", "user2", "user2", "user", "two");
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        String username = "user2";
        String result = test.changeProjectOwner(p, username);
        assertEquals("Expected Project owner updated", "Project owner updated", result);
    }
    
    /* DELETE RESOURCE TEST */
    public void deleteResourceTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        ProjectResource r = test.listResources(p).get(0);
        int result = test.deleteResource(r);
        assertEquals("Expected 200", 200, result);
    }
    
    /* DELETE PROJECT TEST */
    public void deleteProjectTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        Project p = test.listProjects().get(0);
        int result = test.deleteProject(p);
        assertEquals("Expected 200", 200, result);
    }
    
    /* CreateProject with attachments test */
    public void createProjectWithAttahmentsTest() {
        Client test = new Client(DOMAIN, DIRECTORY, RESTENDPOINT);
        test.login("testUser", "test");
        // create some attachments
        List<String> attachments = new ArrayList<String>();
        attachments.add(test.createAttachment("/home/lachlan/test.txt"));
        attachments.add(test.createAttachment("/home/lachlan/Sudoku.zip"));
        test.createProject("TestProject", "0.0.1", "test description", attachments);
        List<Project> projects = test.listProjects();
        Project p = projects.get(0);
        List<ProjectResource> resources = test.listResources(p);
        assertTrue(resources.size() > 0);
    }
}