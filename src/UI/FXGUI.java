package UI;

import DB.SQLiteJDBC;
import Node.NodeBase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FXGUI extends Application{
    private Stage primaryStage;
    private static SQLiteJDBC db;
    private static NodeBase nb;
    private Label myBlockHeightLabel;
    private Label blockHeightLabel;
    private Label talkerSizeLabel;
    private Label secondsLabel;
    private int loadingReps;


    public static void begin(SQLiteJDBC db1, NodeBase nb1) {
        db = db1;
        nb = nb1;
        launch();
    }

    @Override
    public void start(Stage primaryStage2) {
        primaryStage = primaryStage2;
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                nb.shutdown();
                Platform.exit();
                System.exit(0);
            }
        });
        setCatchUpScene();
        startScheduledExecutorService();
    }

    private void setCatchUpScene(){
        primaryStage.setTitle("Joule");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Loading BlockChain");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        myBlockHeightLabel = new Label("My Height:");
        grid.add(myBlockHeightLabel, 0, 1);

        blockHeightLabel = new Label("Public Height:");
        grid.add(blockHeightLabel, 0, 2);

        talkerSizeLabel = new Label("Friends:");
        grid.add(talkerSizeLabel, 0, 3);

        secondsLabel = new Label("Seconds Waited:");
        grid.add(secondsLabel, 0, 4);

        Scene catchUp = new Scene(grid, 300, 275);
        primaryStage.setScene(catchUp);
        primaryStage.centerOnScreen();
        primaryStage.show();
        loadingReps = 0;
    }

    private void startScheduledExecutorService(){

        final ScheduledExecutorService scheduler
                = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(
                new Runnable(){
                    @Override
                    public void run() {
                        loadingReps++;
                        int[]heights = nb.getHeights();
                        int myBlockHeight = heights[0];
                        int blockHeight = heights[1];
                        int talkerSize = heights[2];
                        System.out.println("heights my: " + myBlockHeight + " them: " + blockHeight + " talkers: " +
                                talkerSize + " secondswaited: " + loadingReps);
                        if (talkerSize == 0  && loadingReps > 10){
                            scheduler.shutdown();
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    setLoginScene();
                                }
                            });
                        } else if(myBlockHeight != blockHeight || myBlockHeight == 0){
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    myBlockHeightLabel.setText("My Height: " + myBlockHeight);
                                    blockHeightLabel.setText("Public Height: " + blockHeight);
                                    talkerSizeLabel.setText("Friends: " + talkerSize);
                                    secondsLabel.setText("Seconds Waited: " + loadingReps);
                                }
                            });


                        }else{
                            scheduler.shutdown();
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    setLoginScene();
                                }
                            });
                        }

                    }
                },
                1,
                1,
                TimeUnit.SECONDS);
    }

    public void setLoginScene(){
        Scene login = new Login(this, db, nb).getScene();
        primaryStage.setScene(login);
    }

    public void changeScene(String username){
        Scene bigWindow = new BigWindow(this, db, username, nb).getScene();
        primaryStage.setScene(bigWindow);

    }

}
