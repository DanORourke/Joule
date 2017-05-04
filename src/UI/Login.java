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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class Login{
    private final GridPane grid;
    private final Main stageClass;
    private final SQLiteJDBC db;
    private final NodeBase nb;

    public  Login(Main stageClass, SQLiteJDBC db, NodeBase nb){
        this.stageClass = stageClass;
        this.db = db;
        this.grid = new GridPane();
        this.nb = nb;

        setGrid();
        setSceneTitle();
        setLabels();
        setFieldsAndButtons();
    }


    private void setGrid(){
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

    }

    private void setSceneTitle(){
        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);
    }

    private void setLabels(){
        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        Label pw2 = new Label("Re-Enter\nPassword:");
        pw2.setAlignment(Pos.TOP_CENTER);
        grid.add(pw2, 0, 4, 1, 2);
        grid.setValignment(pw2, VPos.TOP);

    }

    private void setFieldsAndButtons(){
        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        PasswordField pwBox2 = new PasswordField();
        grid.add(pwBox2, 1, 4);

        Button sign = new Button("Sign in");
        HBox hbsign = new HBox(10);
        hbsign.setAlignment(Pos.BOTTOM_RIGHT);
        hbsign.getChildren().add(sign);
        grid.add(hbsign, 1, 3);

        final Text actionTarget = new Text();
        grid.add(actionTarget, 0, 6, 2, 1);
        grid.setHalignment(actionTarget, HPos.CENTER);

        sign.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {

                logAttempt(userTextField.getText(), pwBox.getText());
                actionTarget.setFill(Color.FIREBRICK);
                actionTarget.setText("No Good.  Try Again");
                userTextField.clear();
                pwBox.clear();

            }
        });

        Button nuser = new Button("New User");
        HBox hbnuser = new HBox(10);
        hbnuser.setAlignment(Pos.BOTTOM_RIGHT);
        hbnuser.getChildren().add(nuser);
        grid.add(hbnuser, 1, 5);

        nuser.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (pwBox.getText().equals(pwBox2.getText())){
                    newUser(userTextField.getText(), pwBox.getText());
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("Taken.  Try Again");
                    userTextField.clear();
                    pwBox.clear();
                    pwBox2.clear();
                }
            }
        });
    }

    public Scene getScene(){
        return new Scene(grid, 300, 275);
    }

    private void logAttempt(String username, String password){
        //ask db if valid
        boolean enter = db.login(username, password);
        if (enter){
            startUser(username);
            stageClass.changeScene(username);
        }
    }

    private void newUser(String username, String password){
        //ask db if valid
        boolean enter = db.newUser(username, password);
        if (enter){
            startUser(username);
            stageClass.changeScene(username);
        }
    }

    private void startUser(String username){
        //tell nb who user is and to start miner, update connections with user info
        nb.startUser(username);
    }

}
