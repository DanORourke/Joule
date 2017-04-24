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
    private FXGUI stageClass;
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
    private ObservableList<Tx> txData;

    public BigWindow(FXGUI stageClass, SQLiteJDBC db, String username, NodeBase nb) {

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
        createMyProfile();

        setTabPane();
        setFeedTab();
        setMyTweetsTab();
        setReportersTab();
        setSearchTab();
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
        bankGrid.add(spendTextLabel, 0, 0);

        Button refreshBtn = new Button("Refresh Tab");
        bankGrid.add(refreshBtn, 1, 0);
        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                spendTextLabel.setText(getSpendTextLabelText());
                txData.clear();
                txData.addAll(fillTxTable());
            }
        });

        Label txPerTweetLabel = new Label("Joules Rewarded Per Report:");
        bankGrid.add(txPerTweetLabel, 0, 1);

        TextField txPerTweetField = new TextField();
        txPerTweetField.setText(db.getTxPerTweet(username));
        bankGrid.add(txPerTweetField, 1, 1);

        Text txPerTarget = new Text();
        bankGrid.add(txPerTarget, 0, 2);

        Button txPerTweetBtn = new Button("Update Reward");
        bankGrid.add(txPerTweetBtn, 1, 2);
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
                txPerTweetField.setText(db.getTxPerTweet(username));
                spendTextLabel.setText(getSpendTextLabelText());
            }
        });

        Label tableLabel = new Label("Table of Spendable Reports:");
        bankGrid.add(tableLabel, 0, 3);

        TableView table = createBankTable();
        bankGrid.add(table, 0, 4, 2, 1);

        Label giveLabel = new Label("Give Joules:");
        bankGrid.add(giveLabel, 0, 5);

        Text giveTarget = new Text();
        bankGrid.add(giveTarget, 1, 5);

        TextField giveFullField = new TextField();
        giveFullField.setPromptText("Full Public Key");
        giveFullField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        bankGrid.add(giveFullField, 0, 6);

        TextField giveHashField = new TextField();
        giveHashField.setPromptText(" Public Key Hash");
        giveHashField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        bankGrid.add(giveHashField, 0, 7);

        TextField giveNumberField = new TextField();
        giveNumberField.setPromptText("Number of Joules To Give");
        giveNumberField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        bankGrid.add(giveNumberField, 0, 8);

        Button giveFullBtn = new Button("Give To Full Key:");
        bankGrid.add(giveFullBtn, 1, 6);
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
                    txData.clear();
                    txData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }else {
                    giveTarget.setFill(Color.FIREBRICK);
                    giveTarget.setText("ERROR. JOULES NOT GIVEN");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txData.clear();
                    txData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }
            }
        });

        Button giveBtn = new Button("Give To Key Hash:");
        bankGrid.add(giveBtn, 1, 7);
        giveBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String pubKeyHash = giveHashField.getText();
                String number = giveNumberField.getText();
                boolean success = nb.giveTx(username, pubKeyHash, number);
                if (success){
                    giveTarget.setFill(Color.BLACK);
                    giveTarget.setText("Tx given");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txData.clear();
                    txData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }else {
                    giveTarget.setFill(Color.FIREBRICK);
                    giveTarget.setText("ERROR. TX NOT GIVEN");
                    spendTextLabel.setText(getSpendTextLabelText());
                    txData.clear();
                    txData.addAll(fillTxTable());
                    giveFullField.clear();
                    giveHashField.clear();
                    giveNumberField.clear();
                }
            }
        });

        tab.setContent(bankGrid);
        tabPane.getTabs().add(tab);

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

        txData = fillTxTable();

        TableColumn hashCol = new TableColumn("Report Hash");
        hashCol.setCellValueFactory( new PropertyValueFactory<>( "hash" ) );

        TableColumn typeCol = new TableColumn("Report Type");
        typeCol.setCellValueFactory( new PropertyValueFactory<>( "type" ) );

        TableColumn numberCol = new TableColumn("Joules");
        numberCol.setCellValueFactory( new PropertyValueFactory<>( "number" ) );

        TableColumn headerCol = new TableColumn("Header Hash");
        headerCol.setCellValueFactory( new PropertyValueFactory<>( "headerHash") );

        table.setItems( txData );
        table.getColumns().addAll(hashCol, typeCol, numberCol, headerCol);

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        hashCol.setMaxWidth( 1f * Integer.MAX_VALUE * 35 ); // 50% width
        typeCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        numberCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        headerCol.setMaxWidth( 1f * Integer.MAX_VALUE * 35 );

        return table;
    }

    private ObservableList<Tx> fillTxTable() {
        ArrayList<ArrayList> myOpenTx = db.getMyOpenTx(username);
        System.out.println("window getMyOpenTx myOpenTx: " + myOpenTx);
        ObservableList<Tx> txList = FXCollections.observableArrayList();
        for (ArrayList<String> tx : myOpenTx){
            txList.add(new Tx(tx.get(0), tx.get(1), tx.get(2), tx.get(3)));
        }
        return txList;
    }

    private void setBankConstraints(GridPane bankGrid){
        RowConstraints rowConstraint0 = new RowConstraints();
        rowConstraint0.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint4 = new RowConstraints();
        rowConstraint4.setVgrow(Priority.ALWAYS);

        RowConstraints rowConstraint5 = new RowConstraints();
        rowConstraint5.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint6 = new RowConstraints();
        rowConstraint6.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint7 = new RowConstraints();
        rowConstraint7.setVgrow(Priority.NEVER);

        RowConstraints rowConstraint8 = new RowConstraints();
        rowConstraint8.setVgrow(Priority.NEVER);
        bankGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2, rowConstraint3,
                rowConstraint4, rowConstraint5, rowConstraint6, rowConstraint7, rowConstraint8);

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

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setHalignment(HPos.LEFT);
        columnConstraint0.setFillWidth(true);
        columnConstraint0.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setHalignment(HPos.CENTER);
        columnConstraint1.setFillWidth(true);
        columnConstraint1.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setHalignment(HPos.CENTER);
        columnConstraint2.setFillWidth(true);
        columnConstraint2.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnConstraint3 = new ColumnConstraints();
        columnConstraint3.setHalignment(HPos.RIGHT);
        columnConstraint3.setFillWidth(true);
        columnConstraint3.setHgrow(Priority.ALWAYS);
        myTweetsGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1,
                columnConstraint2, columnConstraint3);

        myTweetArea = new TextArea();
        myTweetArea.setWrapText(true);
        myTweetArea.setEditable(false);
        myTweetArea.setPromptText("Feed");
        myTweetArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        myTweetArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        myTweetsGrid.add(myTweetArea, 0, 0, 4, 1);
        myTweetArea.appendText(createMyTweets());


        TextArea myTweetTweetArea = new TextArea();
        myTweetTweetArea.setWrapText(true);
        myTweetTweetArea.setPromptText("Report Here");
        myTweetTweetArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        myTweetTweetArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        myTweetsGrid.add(myTweetTweetArea, 0, 1, 4, 1);

        Text actionTarget = new Text();
        myTweetsGrid.add(actionTarget, 2, 2);

        Button cyclePastTweetsBtn = new Button("Last 100");
        myTweetsGrid.add(cyclePastTweetsBtn, 0, 2);
        cyclePastTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastTweetNumber = myLastTweetNumber + 100;
                myTweetArea.setText(createMyTweets());
            }
        });

        Button resetTweetsBtn = new Button("Reset");
        myTweetsGrid.add(resetTweetsBtn, 1, 2);
        resetTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastTweetNumber = 0;
                myTweetArea.setText(createMyTweets());
            }
        });

        Button tweetBtn = new Button("Report");
        tweetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String tweet = myTweetTweetArea.getText();
                boolean success = nb.addTweet(tweet, username, null);
                if (success){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Report Sent");
                    String nameToUse = whatNameToUse();
                    if (myLastTweetNumber == 0){
                        myTweetArea.appendText(tweet + "\nNew\n\n");
                    }
                    if (myLastFeedNumber == 0 && db.doIFollow(myProfile.getHash(), username)){
                        feedArea.appendText(nameToUse + ":\n" + tweet + "\nNew\n\n");
                    }
                    myTweetTweetArea.clear();
                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR. REPORT NOT SENT");
                }
                System.out.println("Window succes = " + success);
            }
        });
        myTweetsGrid.add(tweetBtn, 3, 2, 1, 1);

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
            feed = myPastTweets.get(i) + "\n"  + db.getTimeOfTweet(myPastTweets.get(i+1)) + "\n\n" + feed;
            i +=2;
        }
        return nameToUse + ":\n\n" + feed;
    }

    private void setProfileTab(){
        //TODO include user info as well?
        Tab tab = new Tab();
        tab.setText("Profile");
        tab.setClosable(false);

        GridPane profileGrid = new GridPane();
        profileGrid.setAlignment(Pos.CENTER);
        profileGrid.setHgap(10);
        profileGrid.setVgap(10);
        profileGrid.setPadding(new Insets(25, 25, 25, 25));



        RowConstraints rowConstraint0 = new RowConstraints();
        rowConstraint0.setPercentHeight(10);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setPercentHeight(10);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setPercentHeight(10);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setPercentHeight(10);

        RowConstraints rowConstraint4 = new RowConstraints();
        rowConstraint4.setPercentHeight(50);
        rowConstraint4.setValignment(VPos.TOP);

        RowConstraints rowConstraint5 = new RowConstraints();
        rowConstraint5.setPercentHeight(10);
        profileGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2, rowConstraint3,
                rowConstraint4, rowConstraint5);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setPercentWidth(20);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setPercentWidth(20);

        ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setPercentWidth(48);

        ColumnConstraints columnConstraint3 = new ColumnConstraints();
        columnConstraint3.setPercentWidth(12);
        profileGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1,
                columnConstraint2, columnConstraint3);

        final Text actionTarget = new Text();
        profileGrid.add(actionTarget, 2, 3, 2, 1);

        Label pubKeyHashLabel = new Label("Public Key\nHash");
        profileGrid.add(pubKeyHashLabel, 3, 0);

        Label pubKeyLabel = new Label("Public Key");
        profileGrid.add(pubKeyLabel, 3, 1);

        Label privKeyLabel = new Label("Private Key");
        profileGrid.add(privKeyLabel, 3, 2);

        Label nameLabel = new Label("Public Name");
        profileGrid.add(nameLabel, 1, 3);

        Label aboutLabel = new Label("About");
        profileGrid.add(aboutLabel, 3, 4);

        Button signOutBtn = new Button("Sign Out");
        profileGrid.add(signOutBtn, 0, 0);

        signOutBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                nb.signOut();
                stageClass.setLoginScene();
            }
        });

        TextField usernameField = new TextField();
        usernameField.setEditable(true);
        usernameField.setText(username);
        profileGrid.add(usernameField, 0, 1);

        PasswordField pwBox = new PasswordField();
        profileGrid.add(pwBox, 1, 0);

        PasswordField pwBox2 = new PasswordField();
        profileGrid.add(pwBox2, 1, 1);

        Button nameBtn = new Button("Change\nUsername");
        profileGrid.add(nameBtn, 0, 2);

        nameBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (db.changeUsername(username, usernameField.getText())){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Username changed");
                    changeUsername(usernameField.getText());

                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("No Good.  Try Again");
                    usernameField.setText(username);
                }
            }
        });

        Button pwdBtn = new Button("Change\nPassword");
        profileGrid.add(pwdBtn, 1, 2);

        pwdBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (db.changePassword(username, pwBox.getText(), pwBox2.getText())){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Password changed");
                    pwBox.clear();
                    pwBox2.clear();

                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("Error.  Try Again");
                    pwBox.clear();
                    pwBox2.clear();                }
            }
        });

        TextArea pubKeyHashArea = new TextArea();
        pubKeyHashArea.setEditable(false);
        pubKeyHashArea.setWrapText(true);
        pubKeyHashArea.setText(myProfile.getHash());
        profileGrid.add(pubKeyHashArea, 2, 0);

        ArrayList<String> keys = db.getUserKeys(username);

        TextArea pubKeyArea = new TextArea();
        pubKeyArea.setEditable(false);
        pubKeyArea.setWrapText(false);
        pubKeyArea.setText(keys.get(0));
        profileGrid.add(pubKeyArea, 2, 1);

        TextArea privKeyArea = new TextArea();
        privKeyArea.setEditable(false);
        privKeyArea.setWrapText(false);
        privKeyArea.setText(keys.get(1));
        profileGrid.add(privKeyArea, 2, 2);

        TextField nameField = new TextField();
        nameField.setEditable(true);
        nameField.setText(myProfile.getName());
        profileGrid.add(nameField, 0, 3);

        TextArea aboutArea = new TextArea();
        aboutArea.setEditable(true);
        aboutArea.setWrapText(true);
        aboutArea.setText(createMyProfileAbout(myProfile.getAbout()));
        profileGrid.add(aboutArea, 0, 4, 3, 2);

        Button updateProfileBtn = new Button("Update\nProfile");
        profileGrid.add(updateProfileBtn, 3, 5);

        updateProfileBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String newName = nameField.getText();
                String newAbout = aboutArea.getText();
                String profileTweet = convertProfile(newName, newAbout);
                boolean success = nb.updateMyProfile(profileTweet);
                if (success){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Update Sent");
                    createMyProfile();
                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR. Update NOT SENT");
                }
                createMyProfile();
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
        rowConstraint0.setFillHeight(true);
        rowConstraint0.setPercentHeight(5);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setFillHeight(false);
        rowConstraint1.setPercentHeight(40);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setFillHeight(true);
        rowConstraint2.setPercentHeight(55);

        reporterGrid.getRowConstraints().addAll(rowConstraint0, rowConstraint1, rowConstraint2);

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setPercentWidth(100);
        reporterGrid.getColumnConstraints().addAll(columnConstraint0);


        Button resetBtn = new Button("Reset Tab");
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
        //TODO make nextTweetBtn do something

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

        TableColumn profileBtnCol = new TableColumn("Profile");
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
                                            results.setText(createTweetsResults(person.getHash(), person.getName(),
                                                    lastReporterTweetsNumber));

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
                        return cell;
                    }
                };

        followBtnCol.setCellFactory( cellFactory3 );

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        hashCol.setMaxWidth( 1f * Integer.MAX_VALUE * 49 ); // 50% width
        nameCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        followCol.setMaxWidth( 1f * Integer.MAX_VALUE * 7 );
        profileBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 8 );
        tweetsBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 8 );
        followBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 13 );

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
        rowConstraint0.setFillHeight(true);
        rowConstraint0.setPercentHeight(5);

        RowConstraints rowConstraint1 = new RowConstraints();
        rowConstraint1.setFillHeight(false);
        rowConstraint1.setPercentHeight(5);

        RowConstraints rowConstraint2 = new RowConstraints();
        rowConstraint2.setFillHeight(true);
        rowConstraint2.setPercentHeight(5);

        RowConstraints rowConstraint3 = new RowConstraints();
        rowConstraint3.setFillHeight(true);
        rowConstraint3.setPercentHeight(30);

        RowConstraints rowConstraint4 = new RowConstraints();
        rowConstraint4.setFillHeight(true);
        rowConstraint4.setPercentHeight(55);

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
        searchGrid.add(hashBtn, 3, 0);

        TextField nameText = new TextField();
        nameText.setPromptText("Enter Name");
        nameText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(nameText, 0, 1);

        Button nameBtn = new Button("Search Name");
        searchGrid.add(nameBtn, 1, 1);

        TextField tweetText = new TextField();
        tweetText.setPromptText("Enter Report");
        tweetText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(tweetText, 2, 1);

        Button tweetBtn = new Button("Search Reports");
        searchGrid.add(tweetBtn, 3, 1);

        TextField aboutText = new TextField();
        aboutText.setPromptText("Enter About");
        aboutText.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        searchGrid.add(aboutText, 0, 2, 3, 1);

        Button aboutBtn = new Button("Search About");
        searchGrid.add(aboutBtn, 3, 2);

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
        //TODO make nextTweetBtn do something

        ObservableList<Profile> data = FXCollections.observableArrayList();

        aboutBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String about = aboutText.getText();
                if (about.equals(lastSearchString) && lastSearchType.equals("about")){
                    lastSearchStartNumber = lastSearchStartNumber + 5;
                }else {
                    lastSearchString = about;
                    lastSearchStartNumber = 0;
                    lastSearchType = "about";
                }
                ArrayList<ArrayList> profiles = db.searchProfileAbout(about, lastSearchStartNumber);
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
                String tweet = tweetText.getText();
                if (tweet.equals(lastSearchString) && lastSearchType.equals("tweet")){
                    lastSearchStartNumber = lastSearchStartNumber + 100;
                }else {
                    lastSearchString = tweet;
                    lastSearchStartNumber = 0;
                    lastSearchType = "tweet";
                }
                ArrayList<ArrayList> pastTweets = db.searchTweets(tweet, lastSearchStartNumber);
                results.setText(createSearchTweetsResults(pastTweets));
            }
        });

        hashBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
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
                String name = nameText.getText();
                if (name.equals(lastSearchString) && lastSearchType.equals("name")){
                    lastSearchStartNumber = lastSearchStartNumber + 5;
                }else {
                    lastSearchString = name;
                    lastSearchStartNumber = 0;
                    lastSearchType = "name";
                }
                ArrayList<ArrayList> profiles = db.searchProfileName(name, lastSearchStartNumber);
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

        TableColumn profileBtnCol = new TableColumn("Profile");
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
                                            results.setText(createTweetsResults(person.getHash(), person.getName(),
                                                    lastProfileTweetsNumber));

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
                        return cell;
                    }
                };

        followBtnCol.setCellFactory( cellFactory3 );

        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        hashCol.setMaxWidth( 1f * Integer.MAX_VALUE * 49 ); // 50% width
        nameCol.setMaxWidth( 1f * Integer.MAX_VALUE * 15 );
        followCol.setMaxWidth( 1f * Integer.MAX_VALUE * 7 );
        profileBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 8 );
        tweetsBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 8 );
        followBtnCol.setMaxWidth( 1f * Integer.MAX_VALUE * 13 );

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

        //TODO add time element, now top equals newer
        for (ArrayList<String> tweet : pastTweets){
            results = tweet.get(2) + "\n" + tweet.get(0) + ":\n" + tweet.get(1) + "\n" +
                    db.getTimeOfTweet(tweet.get(3)) + "\n\n" + results;
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
            return "EMPTY";
        }
        String results = "";

        //TODO too many calls
        int i = 0;
        while (i < tweets.size()){
            results = tweets.get(i) + "\n"  + db.getTimeOfTweet(tweets.get(i+1)) + "\n" + results;
            i +=2;
        }
        return name + "\n" + results;
    }

    private String createProfileResults(String name, String about, String hash) {
        return "Hash: " + hash + "\nName: " + name + "\nAbout: " + createMyProfileAbout(about);
    }

    private String createMyProfileAbout(String about){
        //TODO update for fancy about
        return about;
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

        ColumnConstraints columnConstraint0 = new ColumnConstraints();
        columnConstraint0.setHalignment(HPos.LEFT);
        columnConstraint0.setFillWidth(true);
        columnConstraint0.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setHalignment(HPos.CENTER);
        columnConstraint1.setFillWidth(true);
        columnConstraint1.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setHalignment(HPos.CENTER);
        columnConstraint2.setFillWidth(true);
        columnConstraint2.setHgrow(Priority.ALWAYS);

        ColumnConstraints columnConstraint3 = new ColumnConstraints();
        columnConstraint3.setHalignment(HPos.RIGHT);
        columnConstraint3.setFillWidth(true);
        columnConstraint3.setHgrow(Priority.ALWAYS);
        feedGrid.getColumnConstraints().addAll(columnConstraint0, columnConstraint1,
                columnConstraint2, columnConstraint3);

        feedArea = new TextArea();
        feedArea.setWrapText(true);
        feedArea.setEditable(false);
        feedArea.setPromptText("Feed");
        feedArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        feedArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        feedGrid.add(feedArea, 0, 0, 4, 1);
        feedArea.appendText(createTweetsFeed());


        TextArea feedTweetArea = new TextArea();
        feedTweetArea.setWrapText(true);
        feedTweetArea.setPromptText("Report Here");
        feedTweetArea.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
        feedTweetArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        feedGrid.add(feedTweetArea, 0, 1, 4, 1);

        Text actionTarget = new Text();
        feedGrid.add(actionTarget, 2, 2);

        Button cyclePastTweetsBtn = new Button("Last 100");
        feedGrid.add(cyclePastTweetsBtn, 0, 2);
        cyclePastTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastFeedNumber = myLastFeedNumber + 100;
                feedArea.setText(createTweetsFeed());
            }
        });

        Button resetTweetsBtn = new Button("Reset");
        feedGrid.add(resetTweetsBtn, 1, 2);
        resetTweetsBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                myLastFeedNumber = 0;
                feedArea.setText(createTweetsFeed());
            }
        });

        Button tweetBtn = new Button("Report");
        tweetBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String tweet = feedTweetArea.getText();
                boolean success = nb.addTweet(tweet, username, null);
                if (success){
                    actionTarget.setFill(Color.BLACK);
                    actionTarget.setText("Report Sent");
                    String nameToUse = whatNameToUse();
                    if (myLastTweetNumber == 0){
                        myTweetArea.appendText(tweet + "\nNew\n\n");
                    }
                    if (myLastFeedNumber == 0 && db.doIFollow(myProfile.getHash(), username)){
                        feedArea.appendText(nameToUse + ":\n" +tweet + "\nNew\n\n");
                    }
                    feedTweetArea.clear();
                }else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR. REPORT NOT SENT");
                }
                System.out.println("Window succes = " + success);
                feedTweetArea.clear();
            }
        });
        feedGrid.add(tweetBtn, 3, 2);

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
                    feed = whatNameToUse() + ":\n" + pastTweets.get(i + 2) + "\n" +
                            db.getTimeOfTweet(pastTweets.get(i + 3)) + "\n\n"+ feed;
                    i += 4;
                }else {
                    feed = pastTweets.get(i + 1) + ": " + pastTweets.get(i + 2) + "\n" +
                            db.getTimeOfTweet(pastTweets.get(i + 3)) + "\n\n"+ feed;
                    i += 4;
                }
            }else {
                feed = pastTweets.get(i) + ":\n" + pastTweets.get(i + 2) + "\n" +
                        db.getTimeOfTweet(pastTweets.get(i + 3)) + "\n\n"+ feed;
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
        feedArea.appendText(name + ":\n" + tweet + "\nNew\n\n");
    }

}

