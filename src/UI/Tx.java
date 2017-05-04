package UI;

import javafx.beans.property.SimpleStringProperty;

public class Tx {
    //used as entry in table in bank tab
    private SimpleStringProperty hash;
    private SimpleStringProperty type;
    private SimpleStringProperty number;
    private SimpleStringProperty headerHash;

    public Tx(String hash, String type, String number, String headerHash){
        this.hash = new SimpleStringProperty(hash);
        this.type = new SimpleStringProperty(type);
        this.number = new SimpleStringProperty(number);
        this.headerHash = new SimpleStringProperty(headerHash);
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
}
