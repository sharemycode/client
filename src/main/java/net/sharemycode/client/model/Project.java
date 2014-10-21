package net.sharemycode.client.model;

import java.io.Serializable;

public class Project implements Serializable {

    private String id; // unique Project ID
    private String url; // unique project URL (generated using generateURL)
    private String name; // project name
    private String description; // project description
    private String owner_id; // user ID of project owner (referential integrity
                             // not enforced)
    private String version; // version of the project

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner_id;
    }

    public void setOwner(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}