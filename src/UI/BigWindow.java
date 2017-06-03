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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import java.util.ArrayList;

public class BigWindow {

    private NodeBase nb;
    private String username;
    private Main stageClass;
    private SQLiteJDBC db;
    private TextArea feedArea;
    private TextArea myTweetArea;
    private StackPane root;
    private TabPane tabPane;
    private String lastSearchString;
    private int lastSearchStartNumber;
    private String lastSearchType;
    private Profile myProfile;
    private String lastProfileTweetsName;
    private int lastProfileTweetsNumber;
    private String lastReporterTweetsName;
    private int lastReporterTweetsNumber;
    private int myLastTweetNumber;
    private int myLastFeedNumber;
    private int fillTxTableStart;

    private ObservableList<TxInfo> txInfoData;

    public BigWindow(Main stageClass, SQLiteJDBC db, String username, NodeBase nb) {

        this.nb = nb;
        nb.setWindow(this);
        this.username = username;
        this.stageClass = stageClass;
        this.db = db;
        this.root = new StackPane();
        this.lastSearchString = null;
        this.lastSearchStartNumber = 0;
        this.lastSearchType = "hash";
        this.lastProfileTweetsName = null;
        this.lastProfileTweetsNumber = 0;
        this.lastReporterTweetsName = null;
        this.lastReporterTweetsNumber = 0;
        this.myLastTweetNumber = 0;
        this.myLastFeedNumber = 0;
        this.fillTxTableStart = 0;
        createMyProfile();

        setTabPane();
        setFeedTab();
        setMyTweetsTab();
        setReportersTab();
        setSearchTab();
        setInfoTab();
        setProfileTab();
        setNetworkTab();
        setBankTab();
    }

    private void setBankTab() {
//        see how many tweets you have
//        transfer tweets to someone else
        Tab tab = new Tab();
        tab.setText("Bank");
        tab.setClosable(false);

        GridPane bankGrid = new GridPane();
        bankGrid.setAlignment(Pos.CENTER);
        bankGrid.setHgap(10);
        bankGrid.setVgap(10);
        bankGrid.setPadding(new Insets(25, 25, 25, 25));
        setBankConstraints(bankGrid);

        Label spendTextLabel = new Label(getSpendTextLabelText());
        spendTextLabel.setMaxHeight(Double.MAX_VALUE);

        Pane spacer1 = new Pane();
        spacer1.setMaxHeight(Double.MAX_VALUE);

        Label txPerTweetLabel = new Label(getPerTxLabelText());
        txPerTweetLabel.setMaxHeight(Double.MAX_VALUE);

        Pane spacer2 = new Pane();
        spacer2.setMaxHeight(Double.MAX_VALUE);

        Text txPerTarget = new Text();

        VBox vbox1 = new VBox();
        vbox1.setSpacing(10);
        vbox1.getChildren().addAll(spendTextLabel, spacer1, txPerTweetLabel, spacer2, txPerTarget);
        bankGrid.add(vbox1, 0, 0);

        Label tableLabel = new Label("Table of Spendable Reports:");
        bankGrid.add(tableLabel, 0, 1);

        Button moreBtn = new Button("Next 100");
        moreBtn.setMaxWidth(Double.MAX_VALUE);
        bankGrid.add(moreBtn, 1, 1);
        moreBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                fillTxTableStart += 100;
                txInfoData.clear();
                txInfoData.addAll(fillTxTable());
            }
        });

        TableView table = createBankTable();
        bankGrid.add(table, 0, 2, 2, 1);

        Label giveLabel = new Label("Give Joules:");

        TextField giveFullField = new TextField();
        giveFullField.setPromptText("Full Public Key");
        giveFullField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        giveFullField.setMaxWidth(Double.MAX_VALUE);

        TextField giveHashField = new TextField();
        giveHashField.setPromptText(" Public Key Hash");
        giveHashField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        giveHashField.setMaxWidth(Double.MAX_VALUE);

        TextField giveNumberField = new TextField();
        giveNumberField.setPromptText("Number of Joules To Give");
        giveNumberField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        giveNumberField.setMaxWidth(Double.MAX_VALUE);

        VBox vbox3 = new VBox();
        vbox3.setSpacing(10);
        vbox3.getChildren().addAll(giveLabel, giveFullField, giveHashField, giveNumberField);
        bankGrid.add(vbox3, 0, 3);

        Text giveTarget = new Text();

        Button giveFullBtn = new Button("Give To Full Key");
        giveFullBtn.setMaxWidth(Double.MAX_VALUE);
        giveFullBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String pubKey = giveFullField.getText();
                String number = giveNumberField.getText();
                boolean success = nb.giveTx(username, pubKey, number);
                if (success){
                    giveTarget.setFill(Color.BLACK);
                    giveTarget.setText("Joules given");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txInfoData.clear();
                    txInfoData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }else {
                    giveTarget.setFill(Color.FIREBRICK);
                    giveTarget.setText("ERROR");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txInfoData.clear();
                    txInfoData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }
            }
        });

        Button giveBtn = new Button("Give To Hash");
        giveBtn.setMaxWidth(Double.MAX_VALUE);
        giveBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String pubKeyHash = giveHashField.getText();
                String number = giveNumberField.getText();
                boolean success = nb.giveTx(username, pubKeyHash, number);
                if (success){
                    giveTarget.setFill(Color.BLACK);
                    giveTarget.setText("TxInfo given");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txInfoData.clear();
                    txInfoData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }else {
                    giveTarget.setFill(Color.FIREBRICK);
                    giveTarget.setText("ERROR");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txInfoData.clear();
                    txInfoData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }
            }
        });

        VBox vbox4 = new VBox();
        vbox4.setSpacing(10);
        vbox4.getChildren().addAll(giveTarget, giveFullBtn, giveBtn);
        bankGrid.add(vbox4, 1, 3);

        Button refreshBtn = new Button("Refresh Tab");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                fillTxTableStart = 0;
                spendTextLabel.setText(getSpendTextLabelText());
                txPerTweetLabel.setText(getPerTxLabelText());
                txInfoData.clear();
                txInfoData.addAll(fillTxTable());
                txPerTarget.setText("");
                giveTarget.setText("");
                giveFullField.clear();
                giveHashField.clear();
                giveNumberField.clear();
            }
        });

        TextField txPerTweetField = new TextField();
        txPerTweetField.setPromptText("Joule Rewarded");
        txPerTweetField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        txPerTweetField.setMaxWidth(Double.MAX_VALUE);

        Button txPerTweetBtn = new Button("Update Reward");
        txPerTweetBtn.setMaxWidth(Double.MAX_VALUE);
        txPerTweetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String newReward = txPerTweetField.getText();
                if (new MathStuff().isNumber(newReward) && Integer.valueOf(newReward) > 0){
                    db.updateTxPerTweet(username, newReward);
                    txPerTarget.setFill(Color.BLACK);
                    txPerTarget.setText("Miner Reward Changed");
                }else {
                    txPerTarget.setFill(Color.FIREBRICK);
                    txPerTarget.setText("ERROR");
                }
                txPerTweetField.clear();
                spendTextLabel.setText(getSpendTextLabelText());
            }
        });

        VBox vbox2 = new VBox();
        vbox2.setSpacing(10);
        vbox2.getChildren().addAll(refreshBtn, txPerTweetField, txPerTweetBtn);
        bankGrid.add(vbox2, 1, 0);

        tab.setContent(bankGrid);
        tabPane.getTabs().add(tab);

    }

    private String getPerTxLabelText(){
        return "Joules Rewarded Per Report: " + db.getTxPerTweet(username);
    }

    private String getSpendTextLabelText(){
        return "Joules to Spend: " + db.getTxToSpend(username) +
                "     Block Height: " + nb.getBlockChainHeight();
    }

    private TableView createBankTable(){
        TableView table = new TableView();
        table.setEditable(false);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        txInfoData = fillTxTable();

        TableColumn hashCol = new TableColumn("Report Hash");
        hashCol.setCellValueFactory( new PropertyValueFactory<>( "hash" ) );

        TableColumn typeCol = new TableColumn("Report Type");
        typeCol.setCellValueFactory( new PropertyValueFactory<>( "type" ) );

        TableColumn numberCol = new TableColumn("Joules");
        numberCol.setCellValueFactory( new PropertyValueFactory<>( "number" ) );

        TableColumn headerCol = new TableColumn("Block Hash");
        headerCol.setCellValueFactory( new PropertyValueFactory<>( "headerHash") );

        TableColumn heightCol = new TableColumn("Block Height");
        heightCol.setCellValueFactory( new PropertyValueFactory<>( "height") );

        table.setItems(txInfoData);
        table.getColumns().addAll(hashCol, typeCol, numberCol, heightCol, headerCol);

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        hashCol.setMaxWidth( 1f * Integer.MAX_VALUE * 35 ); // 35% of Width
        typeCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        numberCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        heightCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        headerCol.setMaxWidth( 1f * Integer.MAX_VALUE * 20 );

        return table;
    }

    private ObservableList<TxInfo> fillTxTable() {

        //String currentHeaderHash = db.getCurrentHeaderHash();
        ArrayList<ArrayList> myOpenTx = db.getMyOpenTx(username, fillTxTableStart);
        System.out.println("window getMyOpenTx myOpenTx: " + myOpenTx);
        ObservableList<TxInfo> txInfoList = FXCollections.observableArrayList();
        for (ArrayList<String> tx : myOpenTx){
            txInfoList.add(new TxInfo(tx.get(0), tx.get(1), tx.get(2), tx.get(3), tx.get(4)));
        }
        return txInfoList;
    }

    private void setBankConstraints(GridPane bankGrid){
        RowConstraints rowConstraint0 = new RowConstraints();
        rowConstraint0.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setVgrow(Priority.ALWAYS);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setVgrow(Priority.NEVER);
        bankGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setPercentWidth(75);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setPercentWidth(25);
        columnConstraint1.setHalignment(HPos.RIGHT);
        bankGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1);
    }

    private void setNetworkTab() {
//        set your own ip and port
//        reset your name or password for the client

        String networkType = nb.getNetworkType();
        if (networkType.equals("outside")){
            NetworkTab netTab = new NetworkTab(db, nb, username, networkType);
            tabPane.getTabs().add(netTab);
        }else if (networkType.equals("inside")){
            String networkName = nb.getNameOfInsideNetwork();
            NetworkTab netTab = new NetworkTab(db, nb, username,networkName);
            tabPane.getTabs().add(netTab);
        }else {
            String networkName = nb.getNameOfInsideNetwork();
            NetworkTab netTabO = new NetworkTab(db, nb, username, "outside");
            tabPane.getTabs().add(netTabO);
            NetworkTab netTabI = new NetworkTab(db, nb, username, networkName);
            tabPane.getTabs().add(netTabI);
        }

    }

    private void createMyProfile(){
        ArrayList<String> profileList = db.createMyProfile(username);
        String follow = createFollow(profileList.get(0));
        myProfile = new Profile(profileList.get(0), profileList.get(1), follow, profileList.get(2));
    }

    private void setMyTweetsTab(){
        Tab tab = new Tab();
        tab.setText("My Reports");
        tab.setClosable(false);

        GridPane myTweetsGrid = new GridPane();
        myTweetsGrid.setAlignment(Pos.CENTER);
        myTweetsGrid.setHgap(10);
        myTweetsGrid.setVgap(10);
        myTweetsGrid.setPadding(new Insets(25, 25, 25, 25));

        RowConstraints topRowConstraint = new RowConstraints();
        topRowConstraint.setFillHeight(true);
        topRowConstraint.setPercentHeight(75);

        RowConstraints middleRowConstraint = new RowConstraints();
        middleRowConstraint.setFillHeight(true);
        middleRowConstraint.setPercentHeight(15);

        RowConstraints bottomRowConstraint = new RowConstraints();
        bottomRowConstraint.setFillHeight(true);
        bottomRowConstraint.setPercentHeight(10);
        myTweetsGrid.getRowConstraints().addAll(topRowConstraint, middleRowConstraint, bottomRowConstraint);

        myTweetArea = new TextArea();
        myTweetArea.setWrapText(true);
        myTweetArea.setEditable(false);
        myTweetArea.setPromptText("Feed");
        myTweetArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        myTweetArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        myTweetsGrid.add(myTweetArea, 0, 0);
        myTweetArea.setText(createMyTweets());
        myTweetArea.setScrollTop(0);


        TextArea myTweetTweetArea = new TextArea();
        myTweetTweetArea.setWrapText(true);
        myTweetTweetArea.setPromptText("Report Here");
        myTweetTweetArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        myTweetTweetArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        myTweetsGrid.add(myTweetTweetArea, 0, 1);

        Text actionTarget = new Text();

        Button cyclePastTweetsBtn = new Button("Last 100");
        cyclePastTweetsBtn.setMaxHeight(Double.MAX_VALUE);
        cyclePastTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastTweetNumber = myLastTweetNumber + 100;
                myTweetArea.setText(createMyTweets());
            }
        });

        Button resetTweetsBtn = new Button("Refresh Tab");
        resetTweetsBtn.setMaxHeight(Double.MAX_VALUE);
        resetTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastTweetNumber = 0;
                myTweetArea.setText(createMyTweets());
                actionTarget.setText("");
            }
        });

        Button tweetBtn = new Button("Report");
        tweetBtn.setMaxHeight(Double.MAX_VALUE);
        tweetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String tweet = myTweetTweetArea.getText();
                boolean success = nb.addTweet(tweet, username, null);
                if (success){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Report Sent");
                    if (myLastTweetNumber == 0){
                        myTweetArea.setText(createMyTweets());
                    }
                    if (myLastFeedNumber == 0 && db.doIFollow(myProfile.getHash(), username)){
                        feedArea.setText(createTweetsFeed());
                    }
                    myTweetTweetArea.clear();
                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                }
                System.out.println("Window succes = " + success);
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().addAll(resetTweetsBtn, cyclePastTweetsBtn, spacer, actionTarget, tweetBtn);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        myTweetsGrid.add(hbox, 0, 2);
        GridPane.setFillWidth(hbox, true);
        GridPane.setHgrow(hbox, Priority.ALWAYS);

        tab.setContent(myTweetsGrid);
        tabPane.getTabs().add(tab);
    }

    private String whatNameToUse(){
        if (myProfile.getName().equals("NA")){
            return username + "/temp/ ";
        }else {
            return myProfile.getName();
        }
    }

    private String createMyTweets() {
        ArrayList<String> myPastTweets = db.getPastTweetsResults(myProfile.getHash(), myLastTweetNumber);
        System.out.println("Window createMyTweets pastTweets: " + myPastTweets);
        String nameToUse = whatNameToUse();
        String feed = "";
        int i = 0;
        while (i < myPastTweets.size()){
            feed = feed + myPastTweets.get(i) + "\n"  + db.getTimeOfTweet(myPastTweets.get(i+1)) + "\n\n";
            i +=2;
        }
        return nameToUse + ":\n\n" + feed;
    }

    private void setInfoTab(){
        Tab tab = new Tab();
        tab.setText("Info");
        tab.setClosable(false);

        GridPane infoGrid = new GridPane();
        infoGrid.setAlignment(Pos.CENTER);
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(25, 25, 25, 25));

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setHgrow(Priority.NEVER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.NEVER);

        infoGrid.getColumnConstraints().addAll(col0, col1, col2);

        Button refreshBtn = new Button("Refresh Tab");
        refreshBtn.setMaxHeight(Double.MAX_VALUE);
        refreshBtn.setAlignment(Pos.TOP_LEFT);
        infoGrid.add(refreshBtn, 0, 0);

        Label usernameLabel = new Label();
        usernameLabel.setText("Username: " + username);
        usernameLabel.setMaxHeight(Double.MAX_VALUE);
        usernameLabel.setAlignment(Pos.CENTER_LEFT);
        infoGrid.add(usernameLabel, 0, 1);

        PasswordField pass1 = new PasswordField();
        pass1.setMaxHeight(Double.MAX_VALUE);
        pass1.setPromptText("New Password");
        pass1.setAlignment(Pos.BOTTOM_LEFT);
        infoGrid.add(pass1, 0, 2);

        Button signOutBtn = new Button("Sign Out");
        signOutBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        signOutBtn.setAlignment(Pos.TOP_CENTER);
        infoGrid.add(signOutBtn, 2, 0);

        TextField newUsernameField = new TextField();
        newUsernameField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        newUsernameField.setPromptText("New Username");
        newUsernameField.setAlignment(Pos.CENTER_LEFT);
        infoGrid.add(newUsernameField, 1, 1);

        PasswordField pass2 = new PasswordField();
        pass2.setMaxHeight(Double.MAX_VALUE);
        pass2.setPromptText("New Password");
        pass2.setAlignment(Pos.BOTTOM_LEFT);
        infoGrid.add(pass2, 1, 2);

        Text actionTarget = new Text();
        infoGrid.add(actionTarget, 1, 0);

        Button changeUsernameBtn = new Button("Change username");
        changeUsernameBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        changeUsernameBtn.setAlignment(Pos.CENTER_LEFT);
        infoGrid.add(changeUsernameBtn, 2, 1);

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        changePasswordBtn.setAlignment(Pos.BOTTOM_LEFT);
        infoGrid.add(changePasswordBtn, 2, 2);

        Label pubKeyHashLabel = new Label("Public Key Hash:");
        infoGrid.add(pubKeyHashLabel, 0, 3);

        Label pubKeyLabel = new Label("Public Key:");
        infoGrid.add(pubKeyLabel, 0, 4);

        Label privKeyLabel = new Label("Private Key:");
        infoGrid.add(privKeyLabel, 0, 5);

        TextArea pubKeyHashArea = new TextArea();
        pubKeyHashArea.setEditable(false);
        pubKeyHashArea.setWrapText(true);
        pubKeyHashArea.setText(myProfile.getHash());
        infoGrid.add(pubKeyHashArea, 1, 3, 2, 1);

        ArrayList<String> keys = db.getUserKeys(username);

        TextArea pubKeyArea = new TextArea();
        pubKeyArea.setEditable(false);
        pubKeyArea.setWrapText(true);
        pubKeyArea.setText(keys.get(0));
        infoGrid.add(pubKeyArea, 1, 4, 2, 1);

        TextArea privKeyArea = new TextArea();
        privKeyArea.setEditable(false);
        privKeyArea.setWrapText(true);
        privKeyArea.setText(keys.get(1));
        infoGrid.add(privKeyArea, 1, 5, 2, 1);

        signOutBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                nb.signOut();
                nb.shutdown();
                stageClass.setLoginScene();
            }
        });

        changeUsernameBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String newName = newUsernameField.getText();
                if (db.changeUsername(username, newName)){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Username changed");
                    newUsernameField.setText("");
                    changeUsername(newName);
                    usernameLabel.setText("Username: " + username);

                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                    newUsernameField.setText("");
                }
            }
        });

        changePasswordBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (db.changePassword(username, pass1.getText(), pass2.getText())){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Password changed");
                    pass1.clear();
                    pass2.clear();

                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("Error");
                    pass1.clear();
                    pass2.clear();                }
            }
        });

        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                actionTarget.setText("");
                newUsernameField.setText("");
                pass1.clear();
                pass2.clear();
                usernameLabel.setText("Username: " + username);
            }
        });

        tab.setContent(infoGrid);
        tabPane.getTabs().add(tab);
    }

    private void setProfileTab(){
        Tab tab = new Tab();
        tab.setText("Profile");
        tab.setClosable(false);

        GridPane profileGrid = new GridPane();
        profileGrid.setAlignment(Pos.CENTER);
        profileGrid.setHgap(10);
        profileGrid.setVgap(10);
        profileGrid.setPadding(new Insets(25, 25, 25, 25));

        RowConstraints row0 = new RowConstraints();
        row0.setVgrow(Priority.NEVER);

        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.NEVER);

        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.NEVER);

        RowConstraints row3 = new RowConstraints();
        row3.setVgrow(Priority.ALWAYS);
        profileGrid.getRowConstraints().addAll(row0, row1, row2, row3);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setHgrow(Priority.NEVER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        profileGrid.getColumnConstraints().addAll(col0, col1, col2);

        Button refreshBtn = new Button("Refresh Tab");
        refreshBtn.setMaxHeight(Double.MAX_VALUE);
        profileGrid.add(refreshBtn, 0, 0);

        Text actionTarget = new Text();
        profileGrid.add(actionTarget, 1, 0);
        GridPane.setHalignment(actionTarget, HPos.RIGHT);

        Button updateProfileBtn = new Button("Update Profile");
        updateProfileBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        profileGrid.add(updateProfileBtn, 2, 0);

        Label nameLabel = new Label("Name: " + myProfile.getName());
        profileGrid.add(nameLabel, 0, 1, 2, 1);

        TextField nameField = new TextField();
        nameField.setPromptText("New Name");
        profileGrid.add(nameField, 2, 1);

        Label aboutLabel = new Label("About: (Editable, update profile to lock in changes)");
        profileGrid.add(aboutLabel, 0, 2, 3, 1);

        TextArea aboutArea = new TextArea();
        aboutArea.setPromptText("About");
        aboutArea.setEditable(true);
        aboutArea.setWrapText(true);
        aboutArea.setText(myProfile.getAbout());
        aboutArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        profileGrid.add(aboutArea, 0, 3, 3, 1);

        updateProfileBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String newName = nameField.getText();
                String newAbout = aboutArea.getText();
                if (newName.length() != 0){
                    String profileTweet = convertProfile(newName, newAbout);
                    boolean success = nb.updateMyProfile(profileTweet);
                    if (success){
                        actionTarget.setFill(Color.BLACK);
                        actionTarget.setText("Update Sent");
                        createMyProfile();
                        nameLabel.setText("Name: " + myProfile.getName());
                    }else {
                        actionTarget.setFill(Color.FIREBRICK);
                        actionTarget.setText("ERROR");
                    }
                    createMyProfile();
                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                }
            }
        });

        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                actionTarget.setText("");
                nameField.setText("");
                nameLabel.setText("Name: " + myProfile.getName());
                aboutArea.setText(myProfile.getAbout());
            }
        });

        tab.setContent(profileGrid);
        tabPane.getTabs().add(tab);
    }

    private void changeUsername(String newUsername) {
        username = newUsername;
        nb.changeUsername(newUsername);
    }

    private String convertProfile(String newName,String newAbout){
        return newName + "/////" + newAbout;
    }

    private void setReportersTab(){
        Tab tab = new Tab();
        tab.setText("Reporters");
        tab.setClosable(false);

        GridPane reporterGrid = new GridPane();
        reporterGrid.setAlignment(Pos.CENTER);
        reporterGrid.setHgap(10);
        reporterGrid.setVgap(10);
        reporterGrid.setPadding(new Insets(25, 25, 25, 25));

        RowConstraints rowConstraint0 = new RowConstraints();
        rowConstraint0.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setPercentHeight(40);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setVgrow(Priority.ALWAYS);

        reporterGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setHgrow(Priority.ALWAYS);
        reporterGrid.getColumnConstraints().addAll(columnConstraint0);


        Button resetBtn = new Button("Refresh Tab");
        reporterGrid.add(resetBtn, 0, 0);

        TextArea results = new TextArea();
        results.setWrapText(true);
        results.setPromptText("Results");
        results.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        results.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        reporterGrid.add(results, 0, 2);

        TableView table = createReportersTable(resetBtn, results);
        reporterGrid.add(table, 0, 1);

        tab.setContent(reporterGrid);
        tabPane.getTabs().add(tab);
    }

    private TableView createReportersTable(Button resetBtn, TextArea results){
        TableView table = new TableView();
        table.setEditable(false);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ObservableList<Profile> data = FXCollections.observableArrayList();
        resetReporterTable(data);
        resetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                results.clear();
                lastReporterTweetsName = null;
                lastReporterTweetsNumber = 0;
                resetReporterTable(data);
            }
        });

        TableColumn hashCol = new TableColumn("PubKey Hash");
        hashCol.setCellValueFactory( new PropertyValueFactory<>( "hash" ) );

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory( new PropertyValueFactory<>( "name" ) );

        TableColumn followCol = new TableColumn("Follow");
        followCol.setCellValueFactory( new PropertyValueFactory<>( "follow" ) );

        TableColumn profileBtnCol = new TableColumn("ProfileReport");
        Callback<TableColumn<Profile, String>, TableCell<Profile, String>> cellFactory = //
                new Callback<TableColumn<Profile, String>, TableCell<Profile, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Profile, String> param )
                    {
                        final TableCell<Profile, String> cell = new TableCell<Profile, String>()
                        {

                            final Button btn = new Button( "View" );

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
                                        Profile person = getTableView().getItems().get( getIndex() );
                                        results.setText(createProfileResults(person.getName(),
                                                person.getAbout(), person.getHash()));
                                        System.out.println( person.getName() + "  " + person.getHash() +
                                                " " + person.getFollow());
                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };

        profileBtnCol.setCellFactory( cellFactory );

        TableColumn tweetsBtnCol = new TableColumn("Reports");
        Callback<TableColumn<Profile, String>, TableCell<Profile, String>> cellFactory2 = //
                new Callback<TableColumn<Profile, String>, TableCell<Profile, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Profile, String> param )
                    {
                        final TableCell<Profile, String> cell = new TableCell<Profile, String>()
                        {

                            final Button btn = new Button( "View" );

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
                                        Profile person = getTableView().getItems().get( getIndex() );
                                        if (lastReporterTweetsName != null &&
                                                lastReporterTweetsName.equals(person.getHash())){
                                            lastReporterTweetsNumber = lastReporterTweetsNumber + 100;
                                            String report = createTweetsResults(person.getHash(), person.getName(),
                                                    lastReporterTweetsNumber);
                                            if (report.length() == 0){
                                                lastReporterTweetsNumber = 0;
                                                report = createTweetsResults(person.getHash(), person.getName(),
                                                        lastReporterTweetsNumber);
                                            }
                                            results.setText(report);

                                        }else {
                                            lastReporterTweetsName = person.getHash();
                                            lastReporterTweetsNumber = 0;
                                            results.setText(createTweetsResults(person.getHash(), person.getName(),
                                                    0));
                                        }
                                        System.out.println( person.getName() + "  " + person.getHash() +
                                                " " + person.getFollow());
                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };

        tweetsBtnCol.setCellFactory( cellFactory2 );

        TableColumn followBtnCol = new TableColumn("Follow");
        Callback<TableColumn<Profile, String>, TableCell<Profile, String>> cellFactory3 = //
                new Callback<TableColumn<Profile, String>, TableCell<Profile, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Profile, String> param )
                    {
                        final TableCell<Profile, String> cell = new TableCell<Profile, String>()
                        {

                            final Button btn = new Button( "Alternate" );

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
                                        Profile person = getTableView().getItems().get( getIndex() );
                                        String follow = alternate(person.getFollow());
                                        db.alternateFollow(person.getHash(), follow, username);
                                        data.add(data.indexOf(person), new Profile(person.getHash(),
                                                person.getName(), follow, person.getAbout()));
                                        System.out.println( person.getName() + "  " + person.getHash() +
                                                " " + person.getFollow());
                                        data.remove(person);

                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };

        followBtnCol.setCellFactory( cellFactory3 );

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        hashCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 ); // 15% width
        nameCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        followCol.setMaxWidth( 1f * Integer.MAX_VALUE * 10 );
        profileBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        tweetsBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        followBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 20 );

        table.setItems( data );
        table.getColumns().addAll(hashCol, nameCol, followCol, profileBtnCol, tweetsBtnCol, followBtnCol);

        return table;
    }

    private void resetReporterTable(ObservableList<Profile> data){
        ArrayList<ArrayList> profiles = db.getReporters(username);
        data.clear();
        System.out.println("Window resetReporterTable profiles: " + profiles + " username: " + username);
        for (ArrayList<String> profile : profiles) {
            data.add(0, new Profile(profile.get(0), profile.get(1), "YES", profile.get(2)));
        }
    }

    private void setSearchTab(){
        Tab tab = new Tab();
        tab.setText("Search");
        tab.setClosable(false);

        GridPane searchGrid = new GridPane();
        searchGrid.setAlignment(Pos.CENTER);
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        searchGrid.setPadding(new Insets(25, 25, 25, 25));

        RowConstraints rowConstraint0 = new RowConstraints();
        rowConstraint0.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setPercentHeight(30);

        RowConstraints rowConstraint4 = new RowConstraints();
        rowConstraint4.setVgrow(Priority.ALWAYS);

        searchGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2,
                rowConstraint3, rowConstraint4);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setPercentWidth(25);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setPercentWidth(25);

        ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setPercentWidth(25);

        ColumnConstraints columnConstraint3 = new ColumnConstraints();
        columnConstraint3.setPercentWidth(25);
        columnConstraint3.setHalignment(HPos.RIGHT);
        searchGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1,
                columnConstraint2, columnConstraint3);

        TextField hashText = new TextField();
        hashText.setPromptText("Enter Public Key Hash");
        hashText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(hashText, 0, 0, 3, 1);

        Button hashBtn = new Button("Search Hash");
        hashBtn.setMaxWidth(Double.MAX_VALUE);

        TextField nameText = new TextField();
        nameText.setPromptText("Enter Name");
        nameText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(nameText, 0, 1);

        Button nameBtn = new Button("Search Name");
        nameBtn.setMaxWidth(Double.MAX_VALUE);
        searchGrid.add(nameBtn, 1, 1);

        TextField tweetText = new TextField();
        tweetText.setPromptText("Enter Report");
        tweetText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(tweetText, 2, 1);

        Button tweetBtn = new Button("Search Reports");
        tweetBtn.setMaxWidth(Double.MAX_VALUE);

        TextField aboutText = new TextField();
        aboutText.setPromptText("Enter About");
        aboutText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(aboutText, 0, 2, 3, 1);

        Button aboutBtn = new Button("Search About");
        aboutBtn.setMaxWidth(Double.MAX_VALUE);

        VBox vbox1 = new VBox();
        vbox1.setSpacing(10);
        vbox1.getChildren().addAll(hashBtn, tweetBtn, aboutBtn);
        searchGrid.add(vbox1, 3, 0, 1, 3);

        TextArea results = new TextArea();
        results.setWrapText(true);
        results.setPromptText("Results");
        results.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        results.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        searchGrid.add(results, 0, 4, 4, 1);

        TableView table = createSearchTable(hashText, nameText, tweetText, hashBtn, nameBtn,
                tweetBtn, aboutText, aboutBtn, results);
        searchGrid.add(table, 0, 3, 4, 1);

        tab.setContent(searchGrid);
        tabPane.getTabs().add(tab);
    }

    private TableView createSearchTable(TextField hashText, TextField nameText, TextField tweetText, Button hashBtn,
                                        Button nameBtn, Button tweetBtn, TextField aboutText,
                                        Button aboutBtn, TextArea results){
        TableView table = new TableView();
        table.setEditable(false);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ObservableList<Profile> data = FXCollections.observableArrayList();

        aboutBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                results.clear();
                String about = aboutText.getText();
                if (about.equals(lastSearchString) && lastSearchType.equals("about")){
                    lastSearchStartNumber = lastSearchStartNumber + 5;
                }else {
                    lastSearchString = about;
                    lastSearchStartNumber = 0;
                    lastSearchType = "about";
                }
                ArrayList<ArrayList> profiles = db.searchProfileAbout(about, lastSearchStartNumber);
                if (profiles.isEmpty()){
                    lastSearchStartNumber = 0;
                }
                profiles = db.searchProfileAbout(about, lastSearchStartNumber);

                data.clear();
                System.out.println("Window createSearchTable nameBtn profiles: " + profiles);
                for (ArrayList<String> profile : profiles) {
                    String follow = createFollow(profile.get(0));
                    data.add(0, new Profile(profile.get(0), profile.get(1), follow, profile.get(2)));
                    if (data.size() == 6) {
                        data.remove(5);
                    }
                }
            }
        });

        tweetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                data.clear();
                String tweet = tweetText.getText();
                if (tweet.equals(lastSearchString) && lastSearchType.equals("tweet")){
                    lastSearchStartNumber = lastSearchStartNumber + 100;
                }else {
                    lastSearchString = tweet;
                    lastSearchStartNumber = 0;
                    lastSearchType = "tweet";
                }
                ArrayList<ArrayList> pastTweets = db.searchTweets(tweet, lastSearchStartNumber);
                if (pastTweets.isEmpty()){
                    lastSearchStartNumber = 0;
                }
                pastTweets = db.searchTweets(tweet, lastSearchStartNumber);
                results.setText(createSearchTweetsResults(pastTweets));
            }
        });

        hashBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                results.clear();
                String hash = hashText.getText();
                lastSearchString = hash;
                lastSearchStartNumber = 0;
                lastSearchType = "hash";
                ArrayList<ArrayList> profiles = db.searchProfileHash(hash);
                data.clear();
                for (ArrayList<String> profile : profiles){
                    String follow = createFollow(profile.get(0));
                    data.add(0, new Profile(profile.get(0), profile.get(1), follow, profile.get(2)));
                    if (data.size() == 6){
                        data.remove(5);
                    }
                }
            }
        });

        nameBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                results.clear();
                String name = nameText.getText();
                if (name.equals(lastSearchString) && lastSearchType.equals("name")){
                    lastSearchStartNumber = lastSearchStartNumber + 5;
                }else {
                    lastSearchString = name;
                    lastSearchStartNumber = 0;
                    lastSearchType = "name";
                }
                ArrayList<ArrayList> profiles = db.searchProfileName(name, lastSearchStartNumber);
                if (profiles.isEmpty()){
                    lastSearchStartNumber = 0;
                }
                profiles = db.searchProfileAbout(name, lastSearchStartNumber);
                data.clear();
                System.out.println("Window createSearchTable nameBtn profiles: " + profiles);
                for (ArrayList<String> profile : profiles) {
                    String follow = createFollow(profile.get(0));
                    data.add(0, new Profile(profile.get(0), profile.get(1), follow, profile.get(2)));
                    if (data.size() == 6) {
                        data.remove(5);
                    }
                }
            }
        });

        TableColumn hashCol = new TableColumn("PubKey Hash");
        hashCol.setCellValueFactory( new PropertyValueFactory<>( "hash" ) );

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory( new PropertyValueFactory<>( "name" ) );

        TableColumn followCol = new TableColumn("Follow");
        followCol.setCellValueFactory( new PropertyValueFactory<>( "follow" ) );

        TableColumn profileBtnCol = new TableColumn("ProfileReport");
        Callback<TableColumn<Profile, String>, TableCell<Profile, String>> cellFactory = //
                new Callback<TableColumn<Profile, String>, TableCell<Profile, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Profile, String> param )
                    {
                        final TableCell<Profile, String> cell = new TableCell<Profile, String>()
                        {

                            final Button btn = new Button( "View" );

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
                                        Profile person = getTableView().getItems().get( getIndex() );
                                        results.setText(createProfileResults(person.getName(),
                                                person.getAbout(), person.getHash()));
                                        System.out.println( person.getName() + "  " + person.getHash() +
                                                " " + person.getFollow());
                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };

        profileBtnCol.setCellFactory( cellFactory );

        TableColumn tweetsBtnCol = new TableColumn("Reports");
        Callback<TableColumn<Profile, String>, TableCell<Profile, String>> cellFactory2 = //
                new Callback<TableColumn<Profile, String>, TableCell<Profile, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Profile, String> param )
                    {
                        final TableCell<Profile, String> cell = new TableCell<Profile, String>()
                        {

                            final Button btn = new Button( "View" );

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
                                        Profile person = getTableView().getItems().get( getIndex() );
                                        if (lastProfileTweetsName != null &&
                                                lastProfileTweetsName.equals(person.getHash())){
                                            lastProfileTweetsNumber = lastProfileTweetsNumber + 100;
                                            String reports = createTweetsResults(person.getHash(), person.getName(),
                                                    lastProfileTweetsNumber);
                                            if (reports.length() == 0){
                                                lastProfileTweetsNumber = 0;
                                                reports = createTweetsResults(person.getHash(), person.getName(),
                                                        lastProfileTweetsNumber);
                                            }
                                            results.setText(reports);


                                        }else {
                                            lastProfileTweetsName = person.getHash();
                                            lastProfileTweetsNumber = 0;
                                            results.setText(createTweetsResults(person.getHash(), person.getName(),
                                                    0));
                                        }
                                        System.out.println( person.getName() + "  " + person.getHash() +
                                                " " + person.getFollow());
                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };

        tweetsBtnCol.setCellFactory( cellFactory2 );

        TableColumn followBtnCol = new TableColumn("Follow");
        Callback<TableColumn<Profile, String>, TableCell<Profile, String>> cellFactory3 = //
                new Callback<TableColumn<Profile, String>, TableCell<Profile, String>>()
                {
                    @Override
                    public TableCell call( final TableColumn<Profile, String> param )
                    {
                        final TableCell<Profile, String> cell = new TableCell<Profile, String>()
                        {

                            final Button btn = new Button( "Alternate" );

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
                                        Profile person = getTableView().getItems().get( getIndex() );
                                        String follow = alternate(person.getFollow());
                                        db.alternateFollow(person.getHash(), follow, username);
                                        data.add(data.indexOf(person), new Profile(person.getHash(),
                                                person.getName(), follow, person.getAbout()));
                                        System.out.println( person.getName() + "  " + person.getHash() +
                                                " " + person.getFollow());
                                        data.remove(person);

                                    } );
                                    setGraphic( btn );
                                    setText( null );
                                }
                            }
                        };
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                };

        followBtnCol.setCellFactory( cellFactory3 );

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        hashCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 ); // 50% width
        nameCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        followCol.setMaxWidth( 1f * Integer.MAX_VALUE * 10 );
        profileBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        tweetsBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        followBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 20 );

        table.setItems( data );
        table.getColumns().addAll(hashCol, nameCol, followCol, profileBtnCol, tweetsBtnCol, followBtnCol);

        return table;
    }

    private String createFollow(String pubKeyHash ){
        boolean doI = db.doIFollow(pubKeyHash, username);
        if (doI){
            return "YES";
        }else {
            return "NO";
        }
    }

    private String createSearchTweetsResults(ArrayList<ArrayList> pastTweets) {
        if (pastTweets.isEmpty()){
            return "EMPTY";
        }
        String results = "";

        //now top equals newer
        for (ArrayList<String> tweet : pastTweets){
            results = results + tweet.get(2) + "\n" + tweet.get(0) + ":\n" + tweet.get(1) + "\n" +
                    db.getTimeOfTweet(tweet.get(3)) + "\n\n";
        }
        return results;
    }

    private String alternate(String follow){
            String newFollow;
        if (follow.equals("YES")){
            newFollow = "NO";
        }else{
            newFollow = "YES";
        }
        return newFollow;
    }

    private String createTweetsResults(String hash, String name, int startingNumber) {
        ArrayList<String> tweets = db.getPastTweetsResults(hash, startingNumber);
        if (tweets.isEmpty()){
            return "";
        }
        String results = "";

        //TODO too many calls
        int i = 0;
        while (i < tweets.size()){
            results = results + tweets.get(i) + "\n"  + db.getTimeOfTweet(tweets.get(i+1)) + "\n\n";
            i +=2;
        }
        return name + "\n" + results;
    }

    private String createProfileResults(String name, String about, String hash) {
        return "Hash: " + hash + "\nName: " + name + "\nAbout: " + about;
    }

    private void setFeedTab(){
        Tab tab = new Tab();
        tab.setText("Feed");
        tab.setClosable(false);

        GridPane feedGrid = new GridPane();
        feedGrid.setAlignment(Pos.CENTER);
        feedGrid.setHgap(10);
        feedGrid.setVgap(10);
        feedGrid.setPadding(new Insets(25, 25, 25, 25));

        RowConstraints topRowConstraint = new RowConstraints();
        topRowConstraint.setFillHeight(true);
        topRowConstraint.setPercentHeight(75);

        RowConstraints middleRowConstraint = new RowConstraints();
        middleRowConstraint.setFillHeight(true);
        middleRowConstraint.setPercentHeight(15);

        RowConstraints bottomRowConstraint = new RowConstraints();
        bottomRowConstraint.setFillHeight(true);
        bottomRowConstraint.setPercentHeight(10);
        feedGrid.getRowConstraints().addAll(topRowConstraint, middleRowConstraint, bottomRowConstraint);

        feedArea = new TextArea();
        feedArea.setWrapText(true);
        feedArea.setEditable(false);
        feedArea.setPromptText("Feed");
        feedArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        feedArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        feedArea.setText(createTweetsFeed());
        feedArea.setScrollTop(0);
        feedGrid.add(feedArea, 0, 0);

        TextArea feedTweetArea = new TextArea();
        feedTweetArea.setWrapText(true);
        feedTweetArea.setPromptText("Report Here");
        feedTweetArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        feedTweetArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        feedGrid.add(feedTweetArea, 0, 1);

        Text actionTarget = new Text();

        Button cyclePastTweetsBtn = new Button("Last 100");
        cyclePastTweetsBtn.setMaxHeight(Double.MAX_VALUE);
        cyclePastTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastFeedNumber = myLastFeedNumber + 100;
                feedArea.setText(createTweetsFeed());
            }
        });

        Button resetTweetsBtn = new Button("Refresh Tab");
        resetTweetsBtn.setMaxHeight(Double.MAX_VALUE);
        resetTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastFeedNumber = 0;
                feedArea.setText(createTweetsFeed());
                actionTarget.setText("");
            }
        });

        Button tweetBtn = new Button("Report");
        tweetBtn.setMaxHeight(Double.MAX_VALUE);
        tweetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String tweet = feedTweetArea.getText();
                boolean success = nb.addTweet(tweet, username, null);
                if (success){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Report Sent");
                    if (myLastTweetNumber == 0){
                        myTweetArea.setText(createMyTweets());
                    }
                    if (myLastFeedNumber == 0 && db.doIFollow(myProfile.getHash(), username)){
                        feedArea.setText(createTweetsFeed());
                    }
                    feedTweetArea.clear();
                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                }
                System.out.println("Window succes = " + success);
                feedTweetArea.clear();
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.getChildren().addAll(resetTweetsBtn, cyclePastTweetsBtn, spacer, actionTarget, tweetBtn);
        feedGrid.add(hbox, 0, 2);
        GridPane.setFillWidth(hbox, true);
        GridPane.setHgrow(hbox, Priority.ALWAYS);

        tab.setContent(feedGrid);
        tabPane.getTabs().add(tab);
    }

    private String createTweetsFeed(){
        ArrayList<String> pastTweets = db.getPastTweetsFeed(myLastFeedNumber, username);
        System.out.println("Window createTweetsFeed pastTweets: " + pastTweets);
        String feed = "";
        int i = 0;
        while (i < pastTweets.size()){
            if (pastTweets.get(i).equals("NA")){
                if (pastTweets.get(i + 1).equals(myProfile.getHash())) {
                    feed = feed + whatNameToUse() + ":\n" + pastTweets.get(i + 2) + "\n" +
                            db.getTimeOfTweet(pastTweets.get(i + 3)) + "\n\n";
                    i += 4;
                }else {
                    feed = feed + pastTweets.get(i + 1) + ":\n" + pastTweets.get(i + 2) + "\n" +
                            db.getTimeOfTweet(pastTweets.get(i + 3)) + "\n\n";
                    i += 4;
                }
            }else {
                feed = feed + pastTweets.get(i) + ":\n" + pastTweets.get(i + 2) + "\n" +
                        db.getTimeOfTweet(pastTweets.get(i + 3)) + "\n\n";
                i += 4;
            }
        }
        return feed;
    }

    private void setTabPane(){
        tabPane = new TabPane();
        tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root = new StackPane();
        root.getChildren().add(tabPane);
    }

    Scene getScene() {
        return new Scene(root, 600, 575);
    }

    public void addTweet(String name, String tweet){
        System.out.println("Window addTweet: " + name +  ": " + tweet);
        if (myLastFeedNumber == 0){
            feedArea.setText(createTweetsFeed());
        }
        if (myLastTweetNumber == 0){
            myTweetArea.setText(createMyTweets());
        }
    }
}

