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
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.ArrayList;

public class NetworkTab extends Tab {
    //tab for either inside network or outside network depending on network name
    private SQLiteJDBC db;
    private NodeBase nb;
    private String networkName;
    private String username;
    private ObservableList<Friend> friendData;
    private Label serverLabel;
    private Label dbLabel;


    public NetworkTab(SQLiteJDBC db, NodeBase nb, String username, String networkName){
        this.db = db;
        this.nb = nb;
        this.networkName = networkName;
        this.username = username;
        setTab();
    }

    private void setTab() {
//        set the ip and port you tell others you are listening on
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

        serverLabel = new Label();
        serverLabel.setMaxHeight(Double.MAX_VALUE);
        networkGrid.add(serverLabel, 0, 0, 2, 1);

        dbLabel = new Label();
        dbLabel.setMaxHeight(Double.MAX_VALUE);
        networkGrid.add(dbLabel, 0, 1, 2, 1);
        resetServerLabel();

        Text target = new Text();
        networkGrid.add(target, 1, 3);
        GridPane.setHalignment(target, HPos.RIGHT);
        GridPane.setValignment(target, VPos.CENTER);

        TextField ipField0 = new TextField();
        ipField0.setPromptText("IP");
        ipField0.setMaxWidth(Double.MAX_VALUE);

        TextField ipField1 = new TextField();
        ipField1.setPromptText("IP");
        ipField1.setMaxWidth(Double.MAX_VALUE);

        TextField ipField2 = new TextField();
        ipField2.setPromptText("IP");
        ipField2.setMaxWidth(Double.MAX_VALUE);

        TextField ipField3 = new TextField();
        ipField3.setPromptText("IP");
        ipField3.setMaxWidth(Double.MAX_VALUE);

        HBox ipBox = new HBox();
        ipBox.setSpacing(10);
        ipBox.getChildren().addAll(ipField0, ipField1, ipField2, ipField3);
        networkGrid.add(ipBox, 0, 2, 2, 1);

        Button changeIp = new Button("Change IP");
        changeIp.setMaxWidth(Double.MAX_VALUE);
        networkGrid.add(changeIp, 2, 2);
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
        portField.setMaxWidth(Double.MAX_VALUE);

        Button changePortBtn = new Button("Change Port");
        changePortBtn.setMaxWidth(Double.MAX_VALUE);
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
        netNameField.setMaxWidth(Double.MAX_VALUE);

        VBox portBox = new VBox();
        portBox.setSpacing(10);
        portBox.getChildren().addAll(portField, netNameField);
        networkGrid.add(portBox, 0, 3);

        Button changeNetNameBtn = new Button("Change NetName");
        changeNetNameBtn.setMaxWidth(Double.MAX_VALUE);
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

        Button updateTabBtn = new Button("Refresh Tab");
        updateTabBtn.setMaxWidth(Double.MAX_VALUE);
        networkGrid.add(updateTabBtn, 2, 1);

        TextField newIpField0 = new TextField();
        newIpField0.setPromptText("IP");
        newIpField0.setMaxWidth(Double.MAX_VALUE);

        TextField newIpField1 = new TextField();
        newIpField1.setPromptText("IP");
        newIpField0.setMaxWidth(Double.MAX_VALUE);

        TextField newIpField2 = new TextField();
        newIpField2.setPromptText("IP");
        newIpField0.setMaxWidth(Double.MAX_VALUE);

        TextField newIpField3 = new TextField();
        newIpField3.setPromptText("IP");
        newIpField0.setMaxWidth(Double.MAX_VALUE);

        HBox newIpBox = new HBox();
        newIpBox.setSpacing(10);
        newIpBox.getChildren().addAll(newIpField0, newIpField1, newIpField2, newIpField3);
        networkGrid.add(newIpBox, 0, 5, 2, 1);

        TextField newPortField = new TextField();
        newPortField.setPromptText("Port");
        newPortField.setMaxWidth(Double.MAX_VALUE);
        networkGrid.add(newPortField, 0, 6);

        Text myCallAction = new Text();
        networkGrid.add(myCallAction, 1, 6);
        GridPane.setHalignment(myCallAction, HPos.RIGHT);

        TableView table = createNetworkTable(updateTabBtn, myCallAction, target);
        networkGrid.add(table, 0, 4, 3, 1);

        Button callBtn = new Button("Call Address");
        callBtn.setMaxWidth(Double.MAX_VALUE);
        networkGrid.add(callBtn, 2, 6);
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

        VBox buttonBox = new VBox();
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(changeIp, changePortBtn, changeNetNameBtn);
        networkGrid.add(buttonBox, 2, 2, 1, 2);


        setContent(networkGrid);
    }

    private void resetServerLabel(){
        ArrayList<String> ipPort = db.getIpPort(username, networkName);
        String dbText;
        if (ipPort.size() == 3){
            dbText = "DB IP: " + ipPort.get(0) + " Port: " + ipPort.get(1) +
            " NetName: " + ipPort.get(2);
        }else{
            dbText = "DB IP: NA Port: 0 NetName: NA";
        }
        dbLabel.setText(dbText);

        ArrayList<String> serverInfo = nb.getServerInfo(networkName);
        System.out.println("Window resetServerLabel ipPort:  " + ipPort + " serverInfo: " + serverInfo);
        String serverText;
        if (serverInfo.isEmpty()){
            serverText = "Server Not Listening";
        }else{
            serverText = "Server IP: " + serverInfo.get(0) + " Port: " + serverInfo.get(1) +
                    " NetName: " + serverInfo.get(2);
        }
        serverLabel.setText(serverText);
    }

    private TableView createNetworkTable(Button updateTableBtn, Text myCallAction, Text target){
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
                myCallAction.setText("");
                target.setText("");

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

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        ipCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 ); // 25% width
        portCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
        nameCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
        disconnectBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );

        return table;
    }

    private ObservableList<Friend> fillNetworkTable() {
        ArrayList<ArrayList<String>> friends = nb.getNetFriends(networkName);
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
        rowConstraint0.setVgrow(Priority.SOMETIMES);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setVgrow(Priority.SOMETIMES);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setVgrow(Priority.SOMETIMES);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setVgrow(Priority.SOMETIMES);

        RowConstraints rowConstraint4 = new RowConstraints();
        rowConstraint4.setVgrow(Priority.ALWAYS);

        RowConstraints rowConstraint5 = new RowConstraints();
        rowConstraint5.setVgrow(Priority.SOMETIMES);

        RowConstraints rowConstraint6 = new RowConstraints();
        rowConstraint6.setVgrow(Priority.SOMETIMES);

        networkGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2, rowConstraint3,
                rowConstraint4, rowConstraint5, rowConstraint6);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setPercentWidth(15);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setPercentWidth(55);

        ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setPercentWidth(30);
        networkGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1,
                columnConstraint2);
    }
}
