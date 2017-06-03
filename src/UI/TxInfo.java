package UI;

import javafx.beans.property.SimpleStringProperty;

public class TxInfo {
    //used as entry in table in bank tab
    private SimpleStringProperty hash;
    private SimpleStringProperty type;
    private SimpleStringProperty number;
    private SimpleStringProperty headerHash;
    private SimpleStringProperty height;

    public TxInfo(String hash, String type, String number, String headerHash, String height){
        this.hash = new SimpleStringProperty(hash);
        this.type = new SimpleStringProperty(type);
        this.number = new SimpleStringProperty(number);
        this.headerHash = new SimpleStringProperty(headerHash);
        this.height = new SimpleStringProperty(height);
    }

    public String getHash() {
        return hash.get();
    }

    public SimpleStringProperty hashProperty() {
        return hash;
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public String getNumber() {
        return number.get();
    }

    public SimpleStringProperty numberProperty() {
        return number;
    }

    public String getHeaderHash() {
        return headerHash.get();
    }

    public SimpleStringProperty headerHashProperty() {
        return headerHash;
    }

    public String getHeight() {
        return height.get();
    }

    public SimpleStringProperty heightProperty() {
        return height;
    }

    public void setHeight(String height) {
        this.height.set(height);
    }
}
