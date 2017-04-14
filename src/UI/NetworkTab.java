package UI;

import DB.SQLiteJDBC;
import Node.NodeBase;
import ReadWrite.MathStuff;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.ArrayList;

public class NetworkTab extends Tab {
    private SQLiteJDBC db;
    private NodeBase nb;
    private String networkName;
    private String username;
    private ObservableList<Friend> friendData;
    private Label singleLabel;

    public NetworkTab(SQLiteJDBC db, NodeBase nb, String username, String networkName){
        this.db = db;
        this.nb = nb;
        this.networkName = networkName;
        this.username = username;
        setTab();
    }

    private void setTab() {
//        set the ip you tell others and port you are listening on
        String name;
        if (networkName.equals("outside")){
            name = "Out";
        }else{
            name = "In";
        }
        setText(name + "Network");
        setClosable(false);

        GridPane networkGrid = new GridPane();
        networkGrid.setAlignment(Pos.CENTER);
        networkGrid.setHgap(10);
        networkGrid.setVgap(10);
        networkGrid.setPadding(new Insets(25, 25, 25, 25));

        setNetworkConstraints(networkGrid);

        singleLabel = new Label();
        resetServerLabel();
        networkGrid.add(singleLabel, 0, 0, 4, 2);

        Text target = new Text();
        networkGrid.add(target, 1, 3, 3, 2);
        networkGrid.setHalignment(target, HPos.CENTER);

        TextField ipField0 = new TextField();
        ipField0.setPromptText("IP");
        networkGrid.add(ipField0, 0, 2);

        TextField ipField1 = new TextField();
        ipField1.setPromptText("IP");
        networkGrid.add(ipField1, 1, 2);

        TextField ipField2 = new TextField();
        ipField2.setPromptText("IP");
        networkGrid.add(ipField2, 2, 2);

        TextField ipField3 = new TextField();
        ipField3.setPromptText("IP");
        networkGrid.add(ipField3, 3, 2);

        Button changeIp = new Button("Change My IP");
        networkGrid.add(changeIp, 4, 2);
        changeIp.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String myIp = ipField0.getText() + "." + ipField1.getText() + "." +
                        ipField2.getText() + "." + ipField3.getText();
                if (new MathStuff().isValidIp(myIp, networkName) && db.updateMyIp(username, myIp, networkName)){
                    nb.updateMyIp(myIp, networkName);
                    resetServerLabel();
                    ipField0.clear();
                    ipField1.clear();
                    ipField2.clear();
                    ipField3.clear();
                    target.setFill(Color.BLACK);
                    target.setText("Ip Updated");
                }else {
                    resetServerLabel();
                    ipField0.clear();
                    ipField1.clear();
                    ipField2.clear();
                    ipField3.clear();
                    target.setFill(Color.FIREBRICK);
                    target.setText("ERROR");
                }
            }
        });

        TextField portField = new TextField();
        portField.setPromptText("Port");
        networkGrid.add(portField, 0, 3);

        Button changePortBtn = new Button("Change My Port");
        networkGrid.add(changePortBtn, 4, 3);
        changePortBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (new MathStuff().isValidPort(portField.getText()) &&
                        db.updateMyPort(username, Integer.valueOf(portField.getText()), networkName)){
                    nb.updateMyPort(Integer.valueOf(portField.getText()), networkName);
                    resetServerLabel();
                    portField.clear();
                    target.setFill(Color.BLACK);
                    target.setText("Port Updated");
                }else {
                    resetServerLabel();
                    portField.clear();
                    target.setFill(Color.FIREBRICK);
                    target.setText("ERROR");
                }
            }
        });

        TextField netNameField = new TextField();
        netNameField.setPromptText("NetName");
        networkGrid.add(netNameField, 0, 4);

        Button changeNetNameBtn = new Button("Change My NetName");
        networkGrid.add(changeNetNameBtn, 3, 4, 2, 1);
        networkGrid.setHalignment(changeNetNameBtn, HPos.RIGHT);
        changeNetNameBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String myNetName =  netNameField.getText();
                if (myNetName.length() >= 2){
                    db.updateNetName(username, myNetName, networkName);
                    nb.updateMyNetName(myNetName, networkName);
                    resetServerLabel();
                    netNameField.clear();
                    target.setFill(Color.BLACK);
                    target.setText("NetName Updated");
                }else{
                    resetServerLabel();
                    netNameField.clear();
                    target.setFill(Color.FIREBRICK);
                    target.setText("ERROR");
                }
            }
        });

        Button updateTableBtn = new Button("Update Tab");
        networkGrid.add(updateTableBtn, 4, 1);

        TableView table = createNetworkTable(updateTableBtn);
        networkGrid.add(table, 0, 5, 5, 1);

        TextField newIpField0 = new TextField();
        newIpField0.setPromptText("IP");
        networkGrid.add(newIpField0, 0, 6);

        TextField newIpField1 = new TextField();
        newIpField1.setPromptText("IP");
        networkGrid.add(newIpField1, 1, 6);

        TextField newIpField2 = new TextField();
        newIpField2.setPromptText("IP");
        networkGrid.add(newIpField2, 2, 6);

        TextField newIpField3 = new TextField();
        newIpField3.setPromptText("IP");
        networkGrid.add(newIpField3, 3, 6);

        Text changeTarget = new Text();
        networkGrid.add(changeTarget, 4, 6);

        TextField newPortField = new TextField();
        newPortField.setPromptText("Port");
        networkGrid.add(newPortField, 0, 7);

        Text myCallAction = new Text();
        networkGrid.add(myCallAction, 3, 7);

        Button callBtn = new Button("Call Address");
        networkGrid.add(callBtn, 4, 7);
        callBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String newIp = newIpField0.getText() + "." + newIpField1.getText() + "." +
                        newIpField2.getText() + "." + newIpField3.getText();
                if (new MathStuff().isValidIp(newIp, networkName) &&
                        new MathStuff().isValidPort(newPortField.getText()) &&
                        nb.callFriend(newIp, Integer.valueOf(newPortField.getText()))){
                    myCallAction.setFill(Color.BLACK);
                    myCallAction.setText("Called");
                    newIpField0.clear();
                    newIpField1.clear();
                    newIpField2.clear();
                    newIpField3.clear();
                    newPortField.clear();

                }else {
                    newIpField0.clear();
                    newIpField1.clear();
                    newIpField2.clear();
                    newIpField3.clear();
                    newPortField.clear();
                    myCallAction.setFill(Color.FIREBRICK);
                    myCallAction.setText("Error");
                }
                friendData.clear();
                friendData.addAll(fillNetworkTable());
            }
        });
        setContent(networkGrid);
    }

    private void resetServerLabel(){
        ArrayList<String> ipPort = db.getIpPort(username, networkName);
        String dbLabel;
        if (ipPort.size() == 3){
            dbLabel = "DB Ip: " + ipPort.get(0) + " Port: " + ipPort.get(1) +
            " NetName: " + ipPort.get(2);
        }else{
            dbLabel = "DB Ip: NA Port: 0 NetName: NA";
        }
        ArrayList<String> serverInfo = nb.getServerInfo(networkName);
        System.out.println("Window resetServerLabel ipPort:  " + ipPort + " serverInfo: " + serverInfo);
        String serverLabel;
        if (serverInfo.isEmpty()){
            serverLabel = "Server Not Listening";
        }else{
            serverLabel = "Server IP: " + serverInfo.get(0) + " Port: " + serverInfo.get(1) +
                    " NetName: " + serverInfo.get(2);
        }
        singleLabel.setText(serverLabel + "\n" + dbLabel);
    }

    private TableView createNetworkTable(Button updateTableBtn){
        TableView table = new TableView();
        table.setEditable(false);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        friendData = fillNetworkTable();

        updateTableBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                friendData.clear();
                friendData.addAll(fillNetworkTable());
            }
        });

        TableColumn ipCol = new TableColumn("IP");
        ipCol.setCellValueFactory( new PropertyValueFactory<>( "ip" ) );

        TableColumn portCol = new TableColumn("Port");
        portCol.setCellValueFactory( new PropertyValueFactory<>( "port" ) );

        TableColumn nameCol = new TableColumn("NetName");
        nameCol.setCellValueFactory( new PropertyValueFactory<>( "name" ) );

        TableColumn disconnectBtnCol = new TableColumn("Disconnect");
        Callback<TableColumn<Friend, String>, TableCell<Friend, String>> cellFactory = //
                new Callback<TableColumn<Friend, String>, TableCell<Friend, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Friend, String> param )
                    {
                        final TableCell<Friend, String> cell = new TableCell<Friend, String>()
                        {

                            final Button btn = new Button( "Disconnect" );

                            @Override
                            public void updateItem( String item, boolean empty )
                            {
                                super.updateItem( item, empty );
                                if ( empty )
                                {
                                    setGraphic( null );
                                    setText( null );
                                }
                                else
                                {
                                    btn.setOnAction( ( ActionEvent event ) ->
                                    {
                                        Friend friend = getTableView().getItems().get( getIndex() );
                                        nb.removeNetFriend(friend);
                                        friendData.clear();
                                        friendData.addAll(fillNetworkTable());
                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        return cell;
                    }
                };

        disconnectBtnCol.setCellFactory( cellFactory );

        table.setItems( friendData );
        table.getColumns().addAll(ipCol, portCol, nameCol, disconnectBtnCol);
        return table;
    }

    private ObservableList<Friend> fillNetworkTable() {
        ArrayList<ArrayList> friends = nb.getNetFriends(networkName);
        System.out.println("window fillNetTable friends: " + friends);
        ObservableList<Friend> friendList = FXCollections.observableArrayList();
        for (ArrayList<String> friend : friends){
            friendList.add(new Friend(friend.get(0), friend.get(1), friend.get(2)));
        }
        resetServerLabel();
        return friendList;
    }

    private void setNetworkConstraints(GridPane networkGrid) {
        RowConstraints rowConstraint0 = new RowConstraints();
        rowConstraint0.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint4 = new RowConstraints();
        rowConstraint4.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint5 = new RowConstraints();
        rowConstraint5.setVgrow(Priority.ALWAYS);

        RowConstraints rowConstraint6 = new RowConstraints();
        rowConstraint6.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint7 = new RowConstraints();
        rowConstraint7.setVgrow(Priority.NEVER);
        networkGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2, rowConstraint3,
                rowConstraint4, rowConstraint5, rowConstraint6, rowConstraint7);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setPercentWidth(20);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setPercentWidth(20);

        ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setPercentWidth(20);

        ColumnConstraints columnConstraint3 = new ColumnConstraints();
        columnConstraint3.setPercentWidth(20);

        ColumnConstraints columnConstraint4 = new ColumnConstraints();
        columnConstraint4.setPercentWidth(20);
        columnConstraint4.setHalignment(HPos.RIGHT);
        networkGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1,
                columnConstraint2, columnConstraint3, columnConstraint4);
    }
}
