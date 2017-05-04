package UI;


import javafx.beans.property.SimpleStringProperty;

public class Friend {
    //used as entry in network tab tables
    private SimpleStringProperty ip;
    private SimpleStringProperty port;
    private SimpleStringProperty name;

    public Friend(String ip, String port, String name){
        this.name = new SimpleStringProperty(name);
        this.port = new SimpleStringProperty(port);
        this.ip = new SimpleStringProperty(ip);
    }

    public String getPort() {
        return port.get();
    }

    public String getIp() {
        return ip.get();
    }

    public String getName() {
        return name.get();
    }
}
