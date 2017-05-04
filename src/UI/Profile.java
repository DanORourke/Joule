package UI;

import javafx.beans.property.SimpleStringProperty;

public class Profile {
    //used as entries in tables in reporters and search tabs
    private SimpleStringProperty hash;
    private SimpleStringProperty name;
    private SimpleStringProperty follow;
    private SimpleStringProperty about;

    public Profile(String hash, String name, String follow, String about){
        this.name = new SimpleStringProperty(name);
        this.hash = new SimpleStringProperty(hash);
        this.follow = new SimpleStringProperty(follow);
        this.about = new SimpleStringProperty(about);
    }

    public String getName() {
        return name.get();
    }
    public void setName(String fName) {
        name.set(fName);
    }

    public String getHash() {
        return hash.get();
    }
    public void setHash(String fName) {
        hash.set(fName);
    }

    public String getFollow() {
        return follow.get();
    }
    public void setFollow(String fName) {
        follow.set(fName);
    }

    public String getAbout() {
        return about.get();
    }

    public void setAbout(String about) {
        this.about.set(about);
    }
}
