package UI;

import DB.SQLiteJDBC;
import Node.NodeBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class Login{
    private TabPane tabPane;
    private final GridPane logGrid;
    private final GridPane opGrid;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private TextField netNameField;
    private final Main stageClass;
    private final SQLiteJDBC db;
    private NodeBase nb;

    public  Login(Main stageClass, SQLiteJDBC db){
        this.stageClass = stageClass;
        this.db = db;
        this.logGrid = new GridPane();
        this.opGrid = new GridPane();

        setTabPane();
        setLogTab();
        setOptionsTab();
    }


    private void setTabPane(){
        tabPane = new TabPane();
        tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private void setLogTab(){
        Tab logTab = new Tab("Log In");
        logTab.setClosable(false);
        logGrid.setAlignment(Pos.CENTER);
        logGrid.setHgap(10);
        logGrid.setVgap(10);
        logGrid.setPadding(new Insets(25, 25, 25, 25));
        setSceneTitle();
        setLabels();
        setFieldsAndButtons();
        logTab.setContent(logGrid);
        tabPane.getTabs().add(logTab);
    }

    private void setOptionsTab(){
        Tab opTab = new Tab("Options");
        opTab.setClosable(false);
        opGrid.setAlignment(Pos.CENTER);
        opGrid.setHgap(10);
        opGrid.setVgap(10);
        opGrid.setPadding(new Insets(25, 25, 25, 25));
        setOptionsTitle();
        setOptionsRadio();
        setOptionsField();
        opTab.setContent(opGrid);
        tabPane.getTabs().add(opTab);
    }

    private void setSceneTitle(){
        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        logGrid.add(sceneTitle, 0, 0);
    }

    private void setOptionsTitle(){
        Text sceneTitle = new Text("Network Options");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        opGrid.add(sceneTitle, 0, 0);
    }

    private void setLabels(){
        Label userName = new Label("User Name:");
        logGrid.add(userName, 0, 1);

        Label pw = new Label("Password:");
        logGrid.add(pw, 0, 2);

        Label pw2 = new Label("Re-Enter\nPassword:");
        pw2.setAlignment(Pos.TOP_CENTER);
        logGrid.add(pw2, 0, 4, 1, 2);
        logGrid.setValignment(pw2, VPos.TOP);

    }

    private void setOptionsRadio(){
        final ToggleGroup group = new ToggleGroup();

        rb1 = new RadioButton("Outside");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);
        opGrid.add(rb1, 0, 1);

        rb2 = new RadioButton("Inside");
        rb2.setToggleGroup(group);
        opGrid.add(rb2, 0, 2 );

        rb3 = new RadioButton("Both");
        rb3.setToggleGroup(group);
        opGrid.add(rb3, 0, 3 );

    }

    private void setOptionsField(){
        netNameField = new TextField();
        netNameField.setPromptText("Inside Network Name");
        opGrid.add(netNameField, 0, 4);
    }

    private void setFieldsAndButtons(){
        TextField userTextField = new TextField();
        logGrid.add(userTextField, 1, 1);

        PasswordField pwBox = new PasswordField();
        logGrid.add(pwBox, 1, 2);

        PasswordField pwBox2 = new PasswordField();
        logGrid.add(pwBox2, 1, 4);

        Button sign = new Button("Sign in");
        sign.setMaxWidth(Double.MAX_VALUE);
        HBox hbsign = new HBox(10);
        hbsign.setAlignment(Pos.BOTTOM_RIGHT);
        hbsign.getChildren().add(sign);
        logGrid.add(hbsign, 1, 3);

        final Text actionTarget = new Text();
        logGrid.add(actionTarget, 1, 0);
        logGrid.setHalignment(actionTarget, HPos.CENTER);

        sign.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (rb1.isSelected() || (!rb1.isSelected() &&
                        (netNameField.getText().length() > 0 && !netNameField.getText().equals("outside")))){
                    logAttempt(userTextField.getText(), pwBox.getText());
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                    userTextField.clear();
                    pwBox.clear();
                }else{
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                    userTextField.clear();
                    pwBox.clear();
                    pwBox2.clear();
                }
            }
        });

        Button nuser = new Button("New User");
        nuser.setMaxWidth(Double.MAX_VALUE);
        HBox hbnuser = new HBox(10);
        hbnuser.setAlignment(Pos.BOTTOM_RIGHT);
        hbnuser.getChildren().add(nuser);
        logGrid.add(hbnuser, 1, 5);

        nuser.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (pwBox.getText().equals(pwBox2.getText()) &&
                        (rb1.isSelected() || (!rb1.isSelected() &&
                                (netNameField.getText().length() > 0 && !netNameField.getText().equals("outside"))))){
                    newUser(userTextField.getText(), pwBox.getText());
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                    userTextField.clear();
                    pwBox.clear();
                    pwBox2.clear();
                }else{
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("ERROR");
                    userTextField.clear();
                    pwBox.clear();
                    pwBox2.clear();
                }
            }
        });
    }

    public Scene getScene(){
        return new Scene(tabPane, 300, 275);
    }

    private void logAttempt(String username, String password){
        //ask db if valid
        boolean enter = db.login(username, password);
        if (enter){
            startUser(username);
            stageClass.setCatchUpScene(username, nb);
        }
    }

    private void newUser(String username, String password){
        //ask db if valid
        boolean enter = db.newUser(username, password);
        if (enter){
            startUser(username);
            stageClass.setCatchUpScene(username, nb);
        }
    }

    private void startUser(String username){
        //tell nb who user is and to start miner, update connections with user info
        if (rb1.isSelected()){
            nb = new NodeBase(db);
            nb.startUser(username);
        }else if (rb2.isSelected()){
            nb = new NodeBase(db, "inside", netNameField.getText());
            nb.startUser(username);
        }else if (rb3.isSelected()){
            nb = new NodeBase(db, "both", netNameField.getText());
            nb.startUser(username);
        }
    }

}
