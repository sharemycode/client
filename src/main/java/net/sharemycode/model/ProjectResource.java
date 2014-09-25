package net.sharemycode.model;

import java.io.Serializable;

/**
 * Represents a single project resource, such as a source file
 *
 * @author Shane Bryzak
 */
public class ProjectResource implements Serializable {
   private static final long serialVersionUID = -4146308990787564792L;

   public static final String PATH_SEPARATOR = "/";

   public enum ResourceType {DIRECTORY, FILE};

   private Long id;

   private Project project;

   private ProjectResource parent;

   private String name;

   private ResourceType resourceType;

   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   public Project getProject()
   {
      return project;
   }

   public void setProject(Project project)
   {
      this.project = project;
   }

   public ProjectResource getParent()
   {
      return parent;
   }

   public void setParent(ProjectResource parent)
   {
      this.parent = parent;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public ResourceType getResourceType()
   {
      return resourceType;
   }

   public void setResourceType(ResourceType resourceType)
   {
      this.resourceType = resourceType;
   }

}