package net.sharemycode.model;

import java.io.Serializable;

public class UserProfile implements Serializable {

    private String id; // relates to the userId (identity.getAccount().getId();

    private String displayName; // user's custom display name (Real name,
                                // nickname, psuedonym, whatever)

    private String aboutContent;

    private String contactContent;

    private String interestsContent;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setAbout(String content) {
        this.aboutContent = content;
    }

    public String getAbout() {
        return aboutContent;
    }

    public void setContact(String content) {
        this.contactContent = content;
    }

    public String getContact() {
        return contactContent;
    }

    public void setInterests(String content) {
        this.interestsContent = content;
    }

    public String getInterests() {
        return interestsContent;
    }
}
