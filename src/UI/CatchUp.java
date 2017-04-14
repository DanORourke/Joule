package UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CatchUp {
    private final GridPane grid;
    private int myBlockHeight;
    private int blockHeight;



    public  CatchUp(int myBlockHeight, int blockHeight){
        this.grid = new GridPane();
        this.myBlockHeight = myBlockHeight;
        this.blockHeight = blockHeight;
        createLoadingScreen();
    }

    private void createLoadingScreen(){
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Loading BlockChain");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label myBlockHeightLabel = new Label("My Height: " + myBlockHeight);
        grid.add(myBlockHeightLabel, 0, 1);

        Label blockHeightLabel = new Label("Public Height: " + blockHeight);
        grid.add(blockHeightLabel, 0, 2);
    }

    public Scene getScene(){
        return new Scene(grid, 300, 275);
    }
}
