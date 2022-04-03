package pushrocks.fxui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import pushrocks.model.BlockAbstract;
import pushrocks.model.DirectedBlock;
import pushrocks.model.IObservableIntervalNotifier;
import pushrocks.model.IObservablePushRocks;
import pushrocks.model.IObserverIntervalNotifier;
import pushrocks.model.IObserverPushRocks;
import pushrocks.model.IntervalNotifier;
import pushrocks.model.PushRocks;
import pushrocks.model.TraversableBlock;



public class PushRocksController implements IObserverPushRocks, IObserverIntervalNotifier {

    private PushRocks pushRocks;
    private int blockSize;
    private IntervalNotifier intervalNotifier;
    private boolean incrementGravityOnInterval;

    @FXML
    GridPane mapPage;

    @FXML
    Pane map;

    @FXML
    Pane playerBox;

    @FXML
    Pane playerControls;

    @FXML 
    RowConstraints mapGridPaneH;

    @FXML
    RowConstraints controlBoxRowConstraints;

    @FXML 
    AnchorPane anchorPane;

    @FXML
    HBox inputBox;

    @FXML 
    Button handleScore;

    @FXML
    Button gravityButton;

    //MENU PAGE
    @FXML
    Pane menuPage;

    @FXML 
    Text levelMenuText;
    
    @FXML 
    Text scoreMenuText;

    @FXML
    Button continueButton;

    @FXML
    ChoiceBox<String> levelChoiceBox;




    @FXML
    public void initialize() {

        // String levelLayout1 = """
        //     wwwwwwwwwwwwwwwwwww
        //     w  w     w        w
        //     w  w r   w  r     w
        //     w  wwww ww        w
        //     w   r    w        w
        //     w      d www      w
        //     w        w        w
        //     w t  d d w  t     w
        //     w        w        w
        //     wwwwwwwwwwwwwwwwwww
        //     W--------W--P-----W
        //     W--------W---R--D-W
        //     W--------WWW----WWW
        //     W--------W---R--D-W
        //     W--------W--------W
        //     W--------W-WW-----W
        //     W-T------W-T------W
        //     W--------W--------W
        //     WWWWWWWWWWWWWWWWWWW""";
        // String directionLayout1 = "rrrrrr";

        // // String levelLayout1 = """
        // //     wwwwwwwwwwwwwwwwwww
        // //     w  w     w        w
        // //     w  w r   w  r     w
        // //     w  wwww ww        w
        // //     w   r    w        w
        // //     w      d www      w
        // //     w        w        w
        // //     w    d d w        w
        // //     w        w        w
        // //     wwwwwwwwwwwwwwwwwww
        // //     W--------W--PT----W
        // //     W--------W---R--D-W
        // //     W--------WWW----WWW
        // //     W--------W---R--D-W
        // //     W--------W--------W
        // //     W--------W-WW-----W
        // //     W- ------W-T------W
        // //     W--------W--------W
        // //     WWWWWWWWWWWWWWWWWWW""";
        // // String directionLayout1 = "rrrrrr";
        // String levelLayout1 = """
        //     wwwwwwwwwwwwwwwwwww
        //     w  w     w        w
        //     w  w r   w  r     w
        //     w  wwww ww        w
        //     w   r    w        w
        //     w      d www      w
        //     w        w        w
        //     w    d d w        w
        //     w        w        w
        //     wwwwwwwwwwwwwwwwwww
        //     W--------W--PT----W
        //     W--------W------D-W
        //     W--------WWW----WWW
        //     W--------W------D-W
        //     W--------W---R----W
        //     W--------W--WR--R-W
        //     W- ------W-T-R--R-W
        //     W--------W---R--R-W
        //     WWWWWWWWWWWWWVWWUWW""";
        // String directionLayout1 = "rrrrrrrrrrruu";

        // //GravityTest
        // String levelLayout1 = """
        //     wwwwwwwwwwwwwwwwwww
        //     w  w     w        w
        //     w  w r   w  r     w
        //     w  wwww ww        w
        //     w   r    w        w
        //     w      d www      w
        //     w        w        w
        //     w    d d w        w
        //     w        w        w
        //     wwwwwwwwwwwwwwwwwww
        //     W--------W---T----W
        //     W--------W------D-W
        //     W--------WWW----WWW
        //     W--------W------D-W
        //     W--------W--------W
        //     W--------W-WWP----W
        //     W- ------W-T-R----W
        //     W--------W---R----W
        //     WWWWWWWWWWWWWWWWWWW""";
        // String directionLayout1 = "rrrrrr";
        String levelLayout1 = """
            wwwwwwwwwwwwwwwwwww
            w  w     w        w
            w  w r   w  r     w
            w  wwww ww        w
            w   r    w        w
            w p    d www      w
            w        w        w
            w    d d w        w
            w        w        w
            wwwwwwwwwwwwwwwwwww
            W--------W---T----W
            W--------W------D-W
            W--------WWW----WWW
            W--------W------D-W
            W--------W--------W
            W--------W-WVR----W
            W- ------W-T------W
            W--------W---R----W
            WWWWWWWWWWWWWUWWWWW""";
        String directionLayout1 = "rrrrrrru";
        menuPage.setVisible(true);
        mapPage.setVisible(false);

                // creates a file object
        // File file = new File("C:\\Users\\Guest User\\Desktop\\Java File\\List Method");
        // System.out.println("FilePath:" + PushRocksController.class.getResource("levels/").getFile() + "Level001" + ".txt");
        // return SaveHandler.class.getResource("saves/").getFile() + fileName + ".txt";
        // File file = new File("C:\\Users\\magnu\\Documents\\LocalUNI\\2022V\\GIT\\TDT4100_prosjekt_magnsu\\src\\main\\resources\\pushrocks\\saves\\level001.txt");
        // String[] fileList = file.list();
        // System.out.println(fileList);
        // System.out.println();
        // for(String str : fileList) {
        //     System.out.println(str);
        // }
        // // returns an array of all files
        // String[] fileList = file.list();

        levelChoiceBox.setValue("Select a level");
        ObservableList<String> levelList = FXCollections.observableArrayList("Level 1", "Level 2", "Level 3", "Level 4", "Level 5");
        levelChoiceBox.setItems(levelList);

		pushRocks = new PushRocks(levelLayout1, directionLayout1);
        pushRocks.addObserver(this);
        pushRocks.pause(true);
		createMap();
		drawMap();
        this.incrementGravityOnInterval = false;
        if (incrementGravityOnInterval) {
            IntervalNotifier intervalNotifier = new IntervalNotifier(this, 1000, true);
            Thread thread = new Thread(intervalNotifier);
            thread.start();
        }
        
        
	}


	//Code inspired by Snakebird project: GameController.java method createBoard()
	private void createMap() {
		//Clears out all current children objects of the map-Pane, so 
		//that the map can be rebuilt into the updated version
		map.getChildren().clear();
        this.blockSize = 25;
        int mapWidth;
        int mapHeight; 
        mapWidth = pushRocks.getWidth();
        mapHeight = pushRocks.getHeight();

        map.setPrefWidth(mapWidth*blockSize);
        map.setMaxWidth(mapWidth*blockSize);
        map.setMinWidth(mapWidth*blockSize);

        map.setPrefHeight(mapHeight*blockSize);
        map.setMaxHeight(mapHeight*blockSize);
        map.setMinHeight(mapHeight*blockSize);

        anchorPane.setPrefWidth(mapHeight*blockSize);
        anchorPane.setMaxWidth(mapHeight*blockSize);
        anchorPane.setMinWidth(mapHeight*blockSize);

        anchorPane.setPrefHeight(mapHeight*blockSize+controlBoxRowConstraints.getMaxHeight());
        anchorPane.setMaxHeight(mapHeight*blockSize+controlBoxRowConstraints.getMaxHeight());
        anchorPane.setMinHeight(mapHeight*blockSize+controlBoxRowConstraints.getMaxHeight());

		for (int y = 0; y < pushRocks.getHeight(); y++) {
            for (int x = 0; x < pushRocks.getWidth(); x++) {
				//creates a new pane that will represent a given block 
                Pane block = new Pane();
				//determines the positioning of the pane 
                block.setTranslateX(x * blockSize);
                block.setTranslateY(y * blockSize);
                //determines the size of the pane 
                block.setPrefWidth(blockSize);
                block.setPrefHeight(blockSize);
				//adds the pane represenation of the given block to the map-pane
                map.getChildren().add(block);
            }
        }
	}

    private String getBackgroundColor(BlockAbstract blockAbstract) {
        char type = blockAbstract.getType();
        switch(type) {
            //Floor
            case ' ':
                System.out.println(((TraversableBlock) blockAbstract).isBirdView()); 
                if (!((TraversableBlock) blockAbstract).isBirdView() ) {
                    return "#5b82bb"; //this color made things look ugly
                    // return "#1db121";
                }
                else {
                    return "#84786a";
                }
            //Wall
            case 'w':
                return "#a0a0a1";
            //PressurePlate
            case 'd':   
                return "#a72702";
            //Teleporter
            case 't':   
                if (blockAbstract.getState()) {
                    return "#d973ff";
                }
                else {
                    return "#bd0dff";
                }
            //Player
            case 'p':   
                if (blockAbstract.getState()) {
                    return "#356bc6";
                }
                else {
                    return "#2d5aa6";
                }
            //Rock
            case 'r':   
                if (blockAbstract.getState()) {
                    return "#5b5b5b";
                }
                else {
                    return "#454545";
                }
            //Portal1
            case 'v':   
                if (blockAbstract.getState()) {
                    return "#00adef";
                }
                else {
                    return "#282bfc";
                }
            //Portal2
            case 'u':   
                if (blockAbstract.getState()) {
                    return "#ff6a00";
                }
                else {
                    return "#c95604";
                }
            //Defaults to match the floor-color, used if the input type has no other specified color
            default:
                return "#84786a";  //may be better to throw exception here
        }
	}

    private String getBorderColor(DirectedBlock directedBlock) {
        if (directedBlock.getType() == 'v' || directedBlock.getType() == 'u') {
            return "#a0a0a1";
        }
        if (directedBlock.getType() == 'p') {
			if (directedBlock.getState()) {
                return "#245097";
				
			}
			else {
                return "#204683";
			}
		}
        return "#24d628";
    }

    private int getBorderWidth(DirectedBlock directedBlock) {
        char type = directedBlock.getType();
        switch(type) {
            //Player
            case 'p':
                return this.blockSize * 3/4;
            //Portal1
            case 'v':
                return this.blockSize * 1/2;
            //Portal2
            case 'u':
                return this.blockSize * 1/2;
            //Other
            default:
                return 0;
        }
    }

    private String getBlockStyle(BlockAbstract blockAbstract) {
        String style = "-fx-background-color: " + getBackgroundColor(blockAbstract) + ";";

        if (blockAbstract instanceof DirectedBlock) {
            DirectedBlock directedBlock = (DirectedBlock) blockAbstract;
        
            String direction = directedBlock.getDirection();
            if (direction != null) {
                style += "-fx-border-color: " + getBorderColor(directedBlock) + ";";
                int borderWidth = getBorderWidth(directedBlock);

                switch (direction) {
                    case "up":
                        style += "-fx-border-width: 0 0 " + borderWidth + " 0;";
                        break;
                    case "down":
                        style += "-fx-border-width: " + borderWidth + " 0 0 0;";
                        break;
                    case "left":
                        style += "-fx-border-width: 0 " + borderWidth + " 0 0;";
                        break;
                    case "right":
                        style += "-fx-border-width: 0 0 0 " + borderWidth + ";";
                        break;
                }
            }
        }
        return style;
    }

	@FXML
    void handleUp() {
        pushRocks.movePlayer(1, "up");
    }

    @FXML
    void handleDown() {
        pushRocks.movePlayer(1, "down"); 
    }

    @FXML
    void handleLeft() {
        pushRocks.movePlayer(1, "left");
    }

    @FXML
    void handleRight() {
        pushRocks.movePlayer(1, "right");
    }

    @FXML
    void resetLevel() {
        // pushRocks.buildWorld();
        // createMap();
        
        
        if (this.incrementGravityOnInterval) {
            this.intervalNotifier.stop();
        }
        pushRocks.resetLevel();
    }

    @FXML
    void handleGravity() {
        pushRocks.gravityInverter();
        if (pushRocks.getGravityDirectionY() < 0) {
            gravityButton.setText("Gravity ▼");
        }
        else {
            gravityButton.setText("Gravity ▲");
        }
        // System.out.println("TEST");
        // this.pushRocks.gravityStep(false);
    }

    @FXML
    void handlePortalOne() {
        pushRocks.placePortal(true, pushRocks.getPlayer(1));
    }

    @FXML
    void handlePortalTwo() {
        pushRocks.placePortal(false, pushRocks.getPlayer(1));
    }

    @FXML
    void handleScore() {
        System.out.println("Bug-search.");
        // System.out.println(pushRocks.toGameToSaveFormat());
        this.pushRocks.gravityStep(false);
        // this.pushRocks.movePlayer(1, "right");
        // this.pushRocks.getGravityFallOrder();
    }



    private void updateScore() {
        int score = this.pushRocks.getMoveCount();
        handleScore.setText("Score: " + score); 
        scoreMenuText.setText("Score: " + score);
    }

	private void drawMap() {
		//for every possible block coordinate, set the background color according the block
		//representing that specific coordinate
		for (int y = 0; y < pushRocks.getHeight(); y++) {
            for (int x = 0; x < pushRocks.getWidth(); x++) {
                BlockAbstract block = pushRocks.getTopBlock(x, -y);
                String style = getBlockStyle(block);
                map.getChildren().get(y * pushRocks.getWidth() + x).setStyle(style);
            }
        }
        this.updateScore();
        
	}


    @Override
    public void update(IObservablePushRocks observable) {
        if (observable == this.pushRocks) {
            this.drawMap();
        }
    }


    @Override
    public void update(IObservableIntervalNotifier observable) {
        this.pushRocks.gravityStep(false);
        
    }


    @FXML
    void handleMenu() {
        System.out.println("Open menu.");
        mapPage.setVisible(false);
        menuPage.setVisible(true);

        if (this.incrementGravityOnInterval) {
            this.intervalNotifier.stop();
            this.pushRocks.pause(true);
        }
        this.pushRocks.pause(true);
        // System.out.println(pushRocks.toGameToSaveFormat());
        // this.pushRocks.gravityStep(false);
    }

    //MENU PAGE
    @FXML
    void handleContinue() {
        System.out.println("Continue!");
        menuPage.setVisible(false);
        mapPage.setVisible(true);

        if (this.incrementGravityOnInterval) {
            this.intervalNotifier.run();
            this.pushRocks.pause(false);
        }
        this.pushRocks.pause(false);
    }
    //MENU PAGE
    @FXML
    void handleLevelButton() {
        System.out.println("Level button");
    }
    //MENU PAGE
    @FXML
    void handleLoadButton() {
        System.out.println("Load button");
    }
    //MENU PAGE
    @FXML
    void handleSaveButton() {
        System.out.println("Save button");
    }
    //MENU PAGE
    @FXML
    void handleApplyChanges() {
        System.out.println("Apply changes");
    }
    //MENU PAGE
    @FXML
    void handleRevertChanges() {
        System.out.println("Revert changes");
    }
    // //MENU PAGE
    // @FXML
    // void handleContinue() {
    //     System.out.println("Bug-search.");
    // }

}   

