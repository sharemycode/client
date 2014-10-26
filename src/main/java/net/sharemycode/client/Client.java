package net.sharemycode.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sharemycode.client.model.Project;
import net.sharemycode.client.model.ProjectAccess;
import net.sharemycode.client.model.ProjectResource;
import net.sharemycode.client.model.ResourceAccess;
import net.sharemycode.client.model.UserProfile;
import net.sharemycode.client.model.ProjectResource.ResourceType;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Defines abstracted methods for client applications
 * to use the sharemycode.net service
 * 
 * @author Lachlan Archibald
 */
public class Client {

    /** The domain of the REST service */
    public static final String DOMAIN = "localhost:8080";
    /** The directory where the service webapp lives */
    public static final String DIRECTORY = "";
    /** The base REST endpoint */
    public static final String RESTENDPOINT = "/sharemycode/rest";
    /** The File Upload endpoint */
    public static final String UPLOADENDPOINT = "/sharemycode/upload";
    /** Maximum allowed size for a file - 10MB */
    public static final int MAX_UPLOAD = 10485760;

    /** Defines the base server URI */
    private String target;
    /** Defines the JAX-RS ClientBuilder */
    private javax.ws.rs.client.Client client;
    /** JAX-RS WebTarget */
    private WebTarget RESTClient;

    /**
     * Tests basic client connection
     * @throws IOException if exception occurs
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

    /**
     * Creates RESTClient with server details
     * @param domain Domain of the server
     * @param directory subdirectory where the service is
     * @param RESTEndpoint base endpoint for REST 
     */
    public Client(String domain, String directory, String RESTEndpoint) {
        // Constructor: create HTTPClient
        target = "http://" + domain + directory;
        client = ClientBuilder.newClient();
        RESTClient = client.target(target + RESTENDPOINT);
    }

    /** 
     * Returns WebTarget instance used by the client 
     * @return WebTarget
     */
    public WebTarget getClient() {
        return RESTClient;
    }

    /** Closes the JAX-RS Client */
    public void close() {
        // closes REST Client connection
        client.close();
    }

    /**
     * Tests that connection to the service is ok
     * 
     * @return Boolean true if successful
     */
    public Boolean testConnection() {
        // test connection to the server
        String requestContent = "/system/test";

        Response response = RESTClient.path(requestContent)
                .request(MediaType.TEXT_PLAIN).get();
        if (response.getStatus() == 200) {
            response.close(); // release the connection
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

    /**
     * Creates new User account on the service
     * @param username  username
     * @param email     email
     * @param emailc    email confirmation
     * @param password  password
     * @param passwordc password confirmation
     * @param firstName User's first name
     * @param lastName  User's last name
     * @return "Registration successful" if success, else error message
     */
    /* CREATE USER - POST JSON */   // Tested: 23/09/2014
    public String createUser(String username, String email, String emailc,
            String password, String passwordc, String firstName, String lastName) {
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
        Response response = RESTClient.path("/register")
                .request(MediaType.TEXT_PLAIN).post(Entity.json(data));
        String message = response.readEntity(String.class);
        response.close();
        return message;
    }

    /**
     * Creates new Project with given information
     * @param name          Project name
     * @param version       Project version
     * @param description   Project description (200 characters)
     * @param attachments   List of attachment ids
     * @return Project
     */
    /* CREATE PROJECT - POST JSON */    // Tested 20/09/2014
    public Project createProject(String name, String version,
            String description, List<String> attachments) {
        // create a new project, returns url to project
        JSONObject project = new JSONObject();
        project.put("name", name);
        project.put("version", version);
        project.put("description", description);
        project.put("attachments", attachments); // attachments are Long encoded as String
        String data = project.toString();
        Response response = RESTClient.path("/projects").request()
                .post(Entity.json(data));
        int status = response.getStatus();
        Project result = null;
        URI location = null;
        if (status == 201) {
            location = response.getLocation();
            result = response.readEntity(Project.class);
        } else {
            String message = response.readEntity(String.class);
            System.out.println(status + ": " + message);
        }
        response.close();
        return result;
    }

    /**
     * Creates Project authorisation for given User
     * @param p Project to authorise user to access
     * @param userId User id
     * @param accessLevel ProjectAccess.AccessLevel to assign
     * @return "Authorisation successful" if success
     */
    /* CREATE PROJECT AUTHORISATION */  // Tested: 01/10/2014
    public String createProjectAuthorisation(Project p, String userId,
            ProjectAccess.AccessLevel accessLevel) {
        // create ProjectAccess object
        if (p == null || userId == null || accessLevel == null)
            return "Error: invalid parameters";
        ProjectAccess access = new ProjectAccess();
        access.setProject(p);
        access.setUserId(userId);
        access.setOpen(false);
        access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/projects/{projectId}/access/";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        URI location = response.getLocation();
        response.close();
        if (status == 201) { // resource created
            System.out.println(location.toString());
            return "Authorisation successful";
        } else
            return "Error: " + status + " - " + message;
    }

    /**
     * Create Resource Authorisation for User
     * 
     * @param r ProjectResource to give access to
     * @param userId User id
     * @param accessLevel ResourceAccess.AccessLevel to assign
     * @return "Authorisation successful" if success
     */
    /* CREATE RESOURCE AUTHORISATION */ // Tested: 01/10/2014
    public String createResourceAuthorisation(ProjectResource r, String userId,
            ResourceAccess.AccessLevel accessLevel) {
        // create ResourceAccess object
        if (r == null || userId == null || accessLevel == null)
            return "Error: invalid parameters";
        ResourceAccess access = new ResourceAccess();
        access.setResource(r);
        access.setUserId(userId);
        access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/resources/{resourceId}/access/";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        URI location = response.getLocation();
        response.close();
        if (status == 201) {
            System.out.println(location.toString());
            return "Authorisation successful";
        } else
            return "Error: " + status + " - " + message;
    }

    /**
     * Creates attachment using REST endpoint
     * 
     * @param filePath String path to file to upload
     * @return String attachment id
     */
    /* CREATE ATTACHMENT */ // Tested 09/10/2014
    public String createAttachment(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                byte[] byteData = Files.readAllBytes(Paths.get(filePath));

                String data = Base64.encodeBase64String(byteData);

                String path = "/projects/attachments/" + file.getName();
                Response response = RESTClient.path(path).request()
                        .post(Entity.text(data));
                int status = response.getStatus();
                String message = response.readEntity(String.class);
                if (status == 200)
                    return message;
                else {
                    System.err.println(status + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "Error: check file path is a valid zip or file.";
    }

    /**
     * Publishes a new ProjectResource from File
     * 
     * @param project Project to create new resource for
     * @param parent Parent directory to create resource under
     * @param filePath Path to the file
     * @return "Resource created" if successful
     * @throws IOException if error with Base64Encoding
     */
    /* PUBLISH RESOURCE */  // Tested 08/10/2014
    public String publishResource(Project project, ProjectResource parent,
            String filePath) throws IOException {
        if (project == null)
            return "Error: No project provided";
        if (parent != null
                && !parent.getResourceType().equals(ResourceType.DIRECTORY))
            return "Error: Parent Resource must be a directory";
        File file = new File(filePath);
        if (file.exists()) {
            byte[] byteData = Files.readAllBytes(Paths.get(filePath));
            String data = Base64.encodeBase64String(byteData);

            // create ProjectResource
            ProjectResource r = new ProjectResource();
            r.setName(file.getName());
            r.setParent(parent);
            r.setProject(project);
            r.setResourceType(ProjectResource.ResourceType.FILE);

            // prepare to post resource meta data
            String path = "/resources/";
            Response response = RESTClient.path(path).request()
                    .post(Entity.entity(r, MediaType.APPLICATION_JSON));
            if (response.getStatus() != 201)
                return "Error submitting ProjectResource";
            URI location = response.getLocation();
            // prepare to PUT resourceContent
            response.close();
            path = location.toString().substring(
                    RESTClient.getUri().toString().length())  + "/content";
            response = RESTClient.path(path).request().put(Entity.text(data));
            int status = response.getStatus();
            response.close();
            if (status == 200)
                return "Resource created";
            else
                return "Resource not created - " + status;
        } else
            return "Invalid file entered";
    }

    /**
     * Creates a new directory in the project
     * 
     * @param project Project
     * @param parent Parent ProjectResource to create directory under
     * @param name Name of directory
     * @return created ProjectResource
     */
    /* CREATE DIRECTORY */  // Tested: 14/10/2014
    public ProjectResource createDirectory(Project project,
            ProjectResource parent, String name) {
        // create a new directory under the parent resource
        if (project == null || name.equals("")) {
            System.err.println("Error: invalid parameters");
            return null;
        }
        if (parent != null
                && !parent.getResourceType().equals(ResourceType.DIRECTORY)) {
            System.err.println("Error: invalid parameters");
            return null;
        } // create ProjectResource
        ProjectResource r = new ProjectResource();
        r.setName(name);
        r.setProject(project);
        r.setParent(parent);
        r.setResourceType(ResourceType.DIRECTORY);

        Response response = RESTClient.path("/resources").request()
                .post(Entity.entity(r, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        URI location = response.getLocation();
        ProjectResource directory = response.readEntity(ProjectResource.class);
        response.close();
        if (status == 201)
            return directory;
        else
            System.err.println("Could not create directory - " + status);
        return null;
    }

    // TODO subscribe to resource updates - POST/WS?
    // TODO publish resource update - POST/WS?

    /* --- GET REQUESTS --- */

    /**
     * Gets the authentication status for logged in user
     * 
     * @return String message "true"
     */
    /* GET AUTH STATUS */   // Tested: 25/09/2014
    public String getAuthStatus() {
        Response response = RESTClient.path("/auth/status").request().get();
        String message = response.readEntity(String.class);
        response.close();
        return message;
    }

    /**
     * Returns userid of the User with given username
     * 
     * @param username String username to lookup
     * @return String userId
     */
    /* LOOKUP USER BY USERNAME */   // Tested 02/10/2014
    // Currently returns userId for use in authorisation methods. 
    // This method may need to be removed
    public String lookupUserByUsername(String username) {
        String resource = "/users/{username}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON).get();
        try {
            JSONObject user = new JSONObject(response.readEntity(String.class));
            response.close();
            return user.getString("id");
        } catch (JSONException e) {
            System.err.println("Problem getting data");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns userid of the User with given email
     * 
     * @param email user's email address
     * @return String userId
     */
    /* LOOKUP USER BY EMAIL */  // Tested 02/10/2014
    // Currently returns userId. This may need to return username only.
    public String lookupUserByEmail(String email) {
        String resource = "/users/search/";
        Response response = RESTClient.path(resource)
                .queryParam("email", email).queryParam("username", "")
                .request(MediaType.APPLICATION_JSON).get();
        try {
            JSONObject user = new JSONObject(response.readEntity(String.class));
            response.close();
            return user.getString("id");
        } catch (JSONException e) {
            System.err.println("Problem getting data");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets UserProfile for given username
     * 
     * @param username String username to get profile for
     * @return UserProfile
     */
    /* GET USER PROFILE */  // Tested 02/10/2014
    public UserProfile getUserProfile(String username) {
        String resource = "/users/{username}/profile";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON).get();
        UserProfile profile = response.readEntity(UserProfile.class);
        response.close();
        return profile;
    }

    /**
     * Lists Projects that the user owns
     * 
     * @return List of Projects
     */
    /* LIST PROJECTS */ // Tested 23/09/2014
    public List<Project> listProjects() {
        // return a list of user's projects
        GenericType<List<Project>> projectType = new GenericType<List<Project>>() {
        };
        List<Project> projects = RESTClient.path("/projects").request()
                .get(projectType);
        return projects;
    }

    /**
     * Lists Projects that the user has READ, READ_WRITE or RESTRICTED permissions
     * 
     * @return List of Projects
     */
    /* LIST SHARED PROJECTS */  // Tested 02/10/2014
    public List<Project> listSharedProjects() {
        GenericType<List<Project>> projectType = new GenericType<List<Project>>() {
        };
        List<Project> projects = RESTClient.path("/projects/shared").request()
                .get(projectType);
        return projects;
    }

    /**
     * Download the entire Project as a .zip
     * -Note: Downloads to Client execution directory currently
     * 
     * @param p Project to download
     * @return int status, 200 if successful
     */
    /* FETCH PROJECT */ // Tested 10/10/2014
    public int fetchProject(Project p) {
        int status = 0;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String path = "/projects/{projectId}/download";
        try {
            Response response = RESTClient.path(path)
                    .resolveTemplate("projectId", p.getId())
                    .request("application/zip").get();
            status = response.getStatus();
            if (status == 200) { // success, download project
                String content = response
                        .getHeaderString("Content-Disposition");
                String fileName = content.substring(content.indexOf('=') + 2,
                        content.length() - 1);
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
        } catch (NotFoundException e) {
            System.err.println("Resource: projects/" + p.getId()
                    + "/download\n" + e);
            return 404;
        }
        return status;
    }

    /**
     * Lists Project Resources
     * 
     * @param p Project to list Resources of
     * @return List of ProjectResources
     */
    /* LIST RESOURCES */    // Tested 23/09/2014
    public List<ProjectResource> listResources(Project p) {
        // Return a list of resources for a project as JSON
        String resource = "/projects/{projectId}/resources";
        GenericType<List<ProjectResource>> resourceType = new GenericType<List<ProjectResource>>() {
        };
        try {
            List<ProjectResource> resources = RESTClient.path(resource)
                    .resolveTemplate("projectId", p.getId())
                    .request().get(resourceType);
            return resources;
        } catch (NotFoundException e) {
            System.err.println("Resource: rest/projects/" + p.getId()
                    + "/resources\n" + e);
            return null;
        }
    }
    
    /**
     * Lists the top-level resources of the Project
     * 
     * @param p Project to list resources
     * @return List of ProjectResources
     */
    /* LIST ROOT RESOURCES */    // Tested 20/10/2014
    public List<ProjectResource> listRootResources(Project p) {
        // Return a list of resources for a project as JSON
        String resource = "/projects/{projectId}/resources";
        GenericType<List<ProjectResource>> resourceType = new GenericType<List<ProjectResource>>() {
        };
        try {
            List<ProjectResource> resources = RESTClient.path(resource)
                    .resolveTemplate("projectId", p.getId())
                    .queryParam("root", 1).request()    // list only root resources
                    .get(resourceType);
            return resources;
        } catch (NotFoundException e) {
            System.err.println("Resource: rest/projects/" + p.getId()
                    + "/resources\n" + e);
            return null;
        }
    }
    
    /**
     * Lists the child resources of the given ProjectResource
     * 
     * @param r ProjectResource parent
     * @return List of ProjectResources
     */
    /* LIST CHILD RESOUCES */   // Tested: 15/10/2014
    public List<ProjectResource> listChildResources(ProjectResource r) {
        // Return a list of child resources for resource
        String resource = "/resources/{id}/children";
        GenericType<List<ProjectResource>> resourceType = new GenericType<List<ProjectResource>>() {
        };
        try {
            List<ProjectResource> resources = RESTClient.path(resource)
                    .resolveTemplate("id", r.getId()).request()
                    .get(resourceType);
            return resources;
        } catch (NotFoundException e) {
            System.err.println("Exception: " + e);
            return null;
        }
    }
    
    /**
     * Download the given ProjectResource
     * -Note: Currently downloads to Client's execution location
     * 
     * @param r ProjectResource to download
     * @return int status, 200 if successful
     */
    /* FETCH RESOURCE */    // Tested 23/09/2014
    public int fetchResource(ProjectResource r) {
        int status = 0;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String resource = "/resources/{resourceId}";
        try {
            Response response = RESTClient.path(resource)
                    .resolveTemplate("resourceId", r.getId())
                    .request(MediaType.APPLICATION_OCTET_STREAM).get();
            status = response.getStatus();
            if (status == 200) { // success, download resource
                String content = response
                        .getHeaderString("Content-Disposition");
                String fileName = content.substring(content.indexOf('=') + 2,
                        content.length() - 1);
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
        } catch (NotFoundException e) {
            System.err.println("Resource: rest/resources/" + r.getId() + "\n"
                    + e);
            return 404;
        }
        return status;
    }

    /**
     * Gets the current user's ProjectAccess level for the given Project
     * 
     * @param p Project to get accessLevel for
     * @return ProjectAccess.AccessLevel
     */
    /* GET PROJECT ACCESS LEVEL */
    public ProjectAccess.AccessLevel getProjectAccessLevel(Project p) {
        // get the project access level for the current logged in user
        String resource = "/projects/{projectId}/access";
        ProjectAccess projectAccess = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId()).request()
                .get(ProjectAccess.class);
        if (projectAccess != null)
            return projectAccess.getAccessLevel();
        else
            return null;
    }

    /**
     * Gets the current user's ResourceAccess level for the given ProjectResource
     * 
     * @param r ProjectResource to get accessLevel for
     * @return ResourceAccess.AccessLevel
     */
    /* GET RESOURCE ACCESS LEVEL */
    public ResourceAccess.AccessLevel getResourceAccessLevel(ProjectResource r) {
        // get the resource access level for the current logged in user
        String resource = "/resources/{resourceId}/access";
        ResourceAccess resourceAccess = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId()).request()
                .get(ResourceAccess.class);
        if (resourceAccess != null)
            return resourceAccess.getAccessLevel();
        else
            return null;
    }

    /**
     * Gets Project authorisation for given User
     * 
     * @param p Project to get authorisation for
     * @param userId User to get authorisation for
     * @return ProjectAccess.AccessLevel
     */
    /* GET PROJECT AUTHORISATION */ // Tested: 01/10/2014
    public ProjectAccess.AccessLevel getProjectAuthorisation(Project p,
            String userId) {
        // get the project access level for a specific user
        String resource = "/projects/{projectId}/access/{userId}";
        ProjectAccess projectAccess = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .resolveTemplate("userId", userId).request()
                .get(ProjectAccess.class);
        if (projectAccess != null)
            return projectAccess.getAccessLevel();
        else
            return null;
    }

    /**
     * Gets ProjectResource authorisation for given User
     * 
     * @param r ProjectResource to get authorisation for
     * @param userId User to get authorisation for
     * @return ResourceAccess.AccessLevel
     */
    /* GET RESOURCE AUTHORISATION */    // Tested: 01/10/2014
    public ResourceAccess.AccessLevel getResourceAuthorisation(
            ProjectResource r, String userId) {
        // get the resource access level for a specific user
        String resource = "/resources/{resourceId}/access/{userId}";
        ResourceAccess resourceAccess = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .resolveTemplate("userId", userId).request()
                .get(ResourceAccess.class);
        if (resourceAccess != null)
            return resourceAccess.getAccessLevel();
        else
            return null;
    }

    /* --- PUT REQUESTS --- */

    /**
     * Updates the Project information
     * 
     * @param p Project to update
     * @param name New Project name
     * @param version New Project version
     * @param description new Project Description
     * @return "Project updated" if successful
     */
    /* UPDATE PROJECT */    // Tested 09/10/2014
    public String updateProject(Project p, String name, String version,
            String description) {
        // update project information - cannot update owner from this request
        if (p == null)
            return "Error: Project must not be null";
        p.setName(name);
        p.setVersion(version);
        p.setDescription(description);
        String path = "/projects/{id}";
        Response response = RESTClient.path(path)
                .resolveTemplate("id", p.getId()).request()
                .put(Entity.entity(p, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        Project u = response.readEntity(Project.class);
        response.close();
        if (status == 200) {
            System.out.println("Project ID: " + u.getId() + ", Project Name: "
                    + u.getName() + ", Owner: " + u.getOwner() + ", Version: "
                    + u.getVersion() + ", URL:" + u.getUrl()
                    + ", Description: " + u.getDescription());
            return "Project updated";
        } else
            return "Project not modified - " + status;
    }

    /**
     * Adds attachments to existing Project
     * 
     * @param p Project to update
     * @param attachments List of attachmentIds to give to project
     * @return "Success" if successful
     */
    /* ADD ATTACHMENTS TO PROJECT */    // Tested 14/10/2014
    public String addAttachmentsToProject(Project p, List<String> attachments) {
        // add attachments to existing project
        if (p == null || attachments == null)
            return "Error: Project and attachments cannot be null";

        String path = "/projects/{id}/attachments";
        Response response = RESTClient.path(path)
                .resolveTemplate("id", p.getId()).request()
                .put(Entity.entity(attachments, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        response.close();
        if (status == 200)
            return "Success";
        else
            return "Failure: " + status;
    }

    /**
     * Changes the displayed owner of a project.
     * Required to be current displayed owner of project
     * 
     * @param p Project to update
     * @param username Username to make new owner
     * @return "Project owner updated" if successful"
     */
    /* CHANGE PROJECT OWNER */  // Tested 09/10/2014
    public String changeProjectOwner(Project p, String username) {
        // change the official project owner
        if (p == null)
            return "Error: Project must not be null";
        String path = "/projects/{id}/owner";
        Response response = RESTClient.path(path)
                .resolveTemplate("id", p.getId()).request()
                .put(Entity.text(username));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if (status == 200)
            return "Project owner updated";
        else
            return status + ": " + message;
    }

    /**
     * Updates the ProjectResource with new data
     * 
     * @param r ProjectResource to update
     * @param filePath Path to File
     * @return "Resource updated" if successful
     */
    /* UPDATE RESOURCE */   // Tested 09/10/2014
    public String updateResource(ProjectResource r, String filePath) {
        // update existing resource with new ResourceContent
        File file = new File(filePath);
        if (file.exists()) {
            byte[] byteData;
            try {
                byteData = Files.readAllBytes(Paths.get(filePath));
                String data = Base64.encodeBase64String(byteData);
                // prepare to PUT resourceContent
                String path = "/resources/{id}/content";
                Response response = RESTClient.path(path)
                        .resolveTemplate("id", r.getId()).request()
                        .put(Entity.text(data));
                int status = response.getStatus();
                response.close();
                if (status == 200)
                    return "Resource updated";
                else
                    return "Resource not modified - " + status;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error reading file";
            }
        } else
            return "Invalid file entered";
    }

    /**
     * Moves the given ProjectResource under a new Parent
     * 
     * @param r ProjectResource to move
     * @param parent Parent ProjectResource to move to
     * @return updated ProjectResource
     */
    /* MOVE RESOURCE */ // Tested: 14/10/2014
    public ProjectResource moveResource(ProjectResource r,
            ProjectResource parent) {
        // move resource in project hierarchy
        if (r == null)
            return null;
        if (parent == null
                || parent.getResourceType().equals(ResourceType.FILE))
            return null;
        r.setParent(parent);
        // set up PUT request
        String path = "resources/{id}";
        Response response = RESTClient.path(path)
                .resolveTemplate("id", r.getId()).request()
                .put(Entity.entity(r, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        ProjectResource update = null;
        if (status == 200)
            update = response.readEntity(ProjectResource.class);
        else
            update = null;
        return update;
    }

    /**
     * Updates the name of the given ProjectResource
     * 
     * @param r ProjectResource to rename
     * @param name New name of the resource
     * @return Updated ProjectResource
     */
    /* RENAME RESOURCE */   // Tested: 14/10/2014
    public ProjectResource renameResource(ProjectResource r, String name) {
        // update the filename of a resource
        if (r == null || name == null || name.equals(""))
            return null;
        r.setName(name);
        // set up PUT request
        String path = "resources/{id}";
        Response response = RESTClient.path(path)
                .resolveTemplate("id", r.getId()).request()
                .put(Entity.entity(r, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        ProjectResource update = null;
        if (status == 200)
            update = response.readEntity(ProjectResource.class);
        else
            update = null;
        return update;
    }

    /**
     * Updates the given user's account with new information
     * 
     * @param username Username to update the account of
     * @param newUsername New username
     * @param email New email address
     * @param emailc Confirm new email address
     * @param password New password
     * @param passwordc Confirm new password
     * @param firstName New firstName
     * @param lastName New lastName
     * @return JSONObject (may need to change)
     */
    /* UPDATE USER ACCOUNT */   // Tested 02/10/2014
    // Currently returns JSON user data. This is probably a security issue.
    // It should probably only return a success status
    public JSONObject updateUserAccount(String username, String newUsername,
            String email, String emailc, String password, String passwordc,
            String firstName, String lastName) {
        // create userUpdate object
        JSONObject userJSON = new JSONObject();
        userJSON.put("username", newUsername);
        if (email != null && emailc != null && email.equals(emailc)) {
            userJSON.put("email", email);
            userJSON.put("emailc", emailc);
        }
        if (password != null && passwordc != null && password.equals(passwordc)) {
            userJSON.put("password", password);
            userJSON.put("passwordc", passwordc);
        }
        userJSON.put("firstName", firstName);
        userJSON.put("lastName", lastName);
        String data = userJSON.toString();
        // define REST resource
        String resource = "/users/{username}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON).put(Entity.json(data));
        int status = response.getStatus();
        String body = response.readEntity(String.class);
        response.close();
        if (status == 200) {
            return new JSONObject(body);
        } else
            System.err
                    .println("Error occured while updating user. Status: HTTP"
                            + status);
        return null;
    }

    /**
     * Updates the given user's UserProfile
     * 
     * @param username Username to update
     * @param displayName New displayName
     * @param about New about section
     * @param contact New contact section
     * @param interests New interests section
     * @return updated UserProfile
     */
    /* UPDATE USER PROFILE */   // Tested: 02/10/2014
    public UserProfile updateUserProfile(String username, String displayName,
            String about, String contact, String interests) {
        UserProfile profile = new UserProfile();
        profile.setDisplayName(displayName);
        profile.setAbout(about);
        profile.setContact(contact);
        profile.setInterests(interests);
        // define REST resource
        String resource = "/users/{username}/profile";
        Response response = RESTClient.path(resource)
                .resolveTemplate("username", username)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(profile, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        UserProfile result = response.readEntity(UserProfile.class);
        response.close();
        if (status == 200) {
            return result;
        } else
            return null;
    }

    /**
     * Updates Project authorisation for the given User
     * 
     * @param p Project
     * @param userId User to update
     * @param accessLevel ProjectAccess.AccessLevel to assign
     * @return "Update successful" if successful
     */
    /* UPDATE PROJECT AUTHORISATION */  // Tested: 01/10/2014
    public String updateProjectAuthorisation(Project p, String userId,
            ProjectAccess.AccessLevel accessLevel) {
        // create ProjectAccess object
        if (p == null || userId == null || accessLevel == null)
            return "Error: invalid parameters";
        ProjectAccess access = new ProjectAccess();
        access.setProject(p);
        access.setUserId(userId);
        access.setOpen(false);
        access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/projects/{projectId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN)
                .put(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if (status == 200) {
            return "Update successful";
        } else
            return "Error: " + status + " - " + message;
    }

    /**
     * Updates Resource authorisation for the given User
     * 
     * @param r ProjectResource
     * @param userId User to update
     * @param accessLevel ResourceAccess.AccessLevel to assign
     * @return "Update successful" if successful
     */
    /* UPDATE RESOURCE AUTHORISATION */ // Tested: 01/10/2014
    public String updateResourceAuthorisation(ProjectResource r, String userId,
            ResourceAccess.AccessLevel accessLevel) {
        // create ResourceAccess object
        if (r == null || userId == null || accessLevel == null)
            return "Error: invalid parameters";
        ResourceAccess access = new ResourceAccess();
        access.setResource(r);
        access.setUserId(userId);
        access.setAccessLevel(accessLevel);
        // submit POST request
        String resource = "/resources/{resourceId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN)
                .put(Entity.entity(access, MediaType.APPLICATION_JSON));
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if (status == 200) {
            return "Update successful";
        } else
            return "Error: " + status + " - " + message;
    }

    /* --- DELETE REQUESTS --- */

    /**
     * Deletes the given Project and all associated Resources and Access
     * 
     * @param p Project to delete
     * @return int 200 if successful
     */
    /* DELETE PROJECT - DELETE */   // Tested: 14/10/2014
    public int deleteProject(Project p) {
        String resource = "/projects/{projectId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId()).request().delete();
        int status = response.getStatus();
        response.close();
        return status;
    }

    /**
     * Deletes the given ProjectResource and all associated Access and Resources
     * 
     * @param r ProjectResource to delete
     * @return int 200 if successful
     */
    /* DELETE RESOURCE - DELETE */  // Tested: 14/10/2014
    public int deleteResource(ProjectResource r) {
        String path = "/resources/{id}";
        Response response = RESTClient.path(path)
                .resolveTemplate("id", r.getId()).request().delete();
        int status = response.getStatus();
        response.close();
        return status;
    }

    /**
     * Removes Project authorisation for the given User
     * 
     * @param p Project to remove access for
     * @param userId UserId to remove access for
     * @return "Authorisation removed" if successful
     */
    /* REMOVE PROJECT AUTHORISATION */  // Tested: 01/10/2014
    public String removeProjectAuthorisation(Project p, String userId) {
        // submit DELETE request
        String resource = "/projects/{projectId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("projectId", p.getId())
                .resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN).delete();
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if (status == 200)
            return "Authorisation removed";
        else
            return "Error: " + status + " - " + message;
    }

    /**
     * Removes ProjectResource authorisation for the given user
     * 
     * @param r ProjectResource to remove access for
     * @param userId UserId to remove access for
     * @return "Authorisation removed" if successful
     */
    /* REMOVE RESOURCE AUTHORISATION */ // Tested: 01/10/2014
    public String removeResourceAuthorisation(ProjectResource r, String userId) {
        // submit DELETE request
        String resource = "/resources/{resourceId}/access/{userId}";
        Response response = RESTClient.path(resource)
                .resolveTemplate("resourceId", r.getId())
                .resolveTemplate("userId", userId)
                .request(MediaType.TEXT_PLAIN).delete();
        int status = response.getStatus();
        String message = response.readEntity(String.class);
        response.close();
        if (status == 200)
            return "Authorisation removed";
        else
            return "Error: " + status + " - " + message;
    }

    /* USER AUTHENTICATION */

    /**
     * Logs the given user in to the sharemycode.net service
     * 
     * @param username Username to login
     * @param password Password for the user
     * @return "Login successful!" if successful
     */
    /* USER LOGIN */    // Tested: 25/06/2014
    public String login(String username, String password) {
        // submit login request
        if (username == null || password == null)
            return "Username and password cannot be empty";
        String token = Authenticator.httpBasicAuth(username, password,
                RESTClient);
        if (token != null) {
            RESTClient.register(new Authenticator(token));
            return "Login successful!";
        } else
            return "Login failed";
    }

    /** 
     * Logs the current User out of the sharemycode.net service
     * 
     * @return 200 if successful
     */
    /* USER LOGOUT */
    // TODO user logout - waiting on PicketLink logout functionality.
    public int logout() {
        String path = "/users/logout"; // temporary workaround
        // String path = "/auth/logout";
        Response response = RESTClient.path(path).request().get();
        int status = response.getStatus();
        response.close();
        return status;
    }
}