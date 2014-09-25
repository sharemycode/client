package net.sharemycode.model;

import java.io.Serializable;

/**
 * Controls access levels for projects
 *
 * @author Shane Bryzak
 *
 */
public class ProjectAccess implements Serializable {
   private static final long serialVersionUID = 6539427720605504095L;

   public enum AccessLevel {OWNER, READ, READ_WRITE, RESTRICTED};

   private Long id;

   private Project project;

   private String userId;

   // Indicates whether the user currently has this project open
   private boolean open;

   // Access level
   private AccessLevel accessLevel;

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

   public String getUserId()
   {
      return userId;
   }

   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   public boolean isOpen()
   {
      return open;
   }

   public void setOpen(boolean open)
   {
      this.open = open;
   }

   public AccessLevel getAccessLevel()
   {
      return accessLevel;
   }

   public void setAccessLevel(AccessLevel accessLevel)
   {
      this.accessLevel = accessLevel;
   }
}
