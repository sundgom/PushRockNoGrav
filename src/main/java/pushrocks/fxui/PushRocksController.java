package pushrocks.fxui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import pushrocks.model.IObservableIntervalNotifier;
import pushrocks.model.IObservablePushRocks;
import pushrocks.model.IObserverIntervalNotifier;
import pushrocks.model.IObserverPushRocks;
import pushrocks.model.IntervalNotifier;
import pushrocks.model.PushRocks;




public class PushRocksController implements IObserverPushRocks, IObserverIntervalNotifier {

    private PushRocks pushRocks;
    private int blockSize;
    private IntervalNotifier intervalNotifier;
    private boolean incrementGravityOnInterval;
    private SaveHandler saveHandler = new SaveHandler();

    @FXML 
    private AnchorPane anchorPane;

    //GAME PAGE
    @FXML
    private GridPane mapPage;
    @FXML
    private Pane map;

    @FXML
    private RowConstraints controlBoxRowConstraints;

    @FXML
    private HBox inputBox;
    @FXML 
    private Button scoreButton;
    @FXML
    private Button gravityButton;
    @FXML
    private Button gravityManualIncrementButton;

    //MENU PAGE
    @FXML
    private Pane menuPage;

    @FXML 
    private Text menuLevelText;
    @FXML 
    private Text menuScoreText;
    @FXML
    private Button menuContinueButton;

    @FXML
    private Button menuLevelButton;
    @FXML
    private Button menuLoadButton;
    @FXML
    private Button menuSaveButton;

    @FXML
    private ChoiceBox<String> menuLevelChoiceBox;
    @FXML
    private TextField menuLoadFileLocationField;
    @FXML
    private TextField menuSaveFileLocationField;

    @FXML 
    private RadioButton menuGravityManualButton;
    @FXML 
    private RadioButton menuGravityMoveInputButton;
    @FXML 
    private RadioButton menuGravityIntervalButton;

    @FXML
    private Button menuApplyChangesButton;
    @FXML
    private Button menuRevertChangesButton;
   
    //STATUS PAGE
    @FXML
    private Pane statusPage;
    @FXML
    private Text statusTitleRightText;
    @FXML 
    private Text statusMessageText;

    @FXML
    private Text statusLevelText;
    @FXML
    private Text statusScoreText;

    @FXML
    private Button statusSituationalButton;
    @FXML
    private Button statusMenuButton;

    //Controller attributes
    private int gravityChoice;
    
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
            wwwwwwwwwwwwwwwwwww@
            w  w     w        w@
            w  w r   w  r     w@
            w  wwww ww        w@
            w   r    w        w@
            w        www      w@
            w        w        w@
            w        w        w@
            w        w        w@
            wwwwwwwwwwwwwwwwwww@
            W--------W---T----W@
            W--------W------ -W@
            W--------WWW----WWW@
            W--------W-   --D-W@
            W--------W--------W@
            W--------W-WVR----W@
            W- ------W-T------W@
            W--------W---R--P-W@
            WWWWWWWWWWWWWUWWWWW@""";
        String directionLayout1 = "rrrrrrruG";
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
        this.saveHandler = new SaveHandler();
        menuLevelChoiceBox.setValue("Select a level");
        // ObservableList<String> levelList = FXCollections.observableArrayList("Level 1", "Level 2", "Level 3", "Level 4", "Level 5");
        ObservableList<String> levelList = FXCollections.observableArrayList();
        levelList.addAll(this.saveHandler.getLevelNames());
        menuLevelChoiceBox.setItems(levelList);
        handleGravityChoiceManual(); //Gravity application is set to manual until other is chosen.
        


        
        
        


		pushRocks = new PushRocks(levelLayout1, directionLayout1);
        pushRocks.addObserver(this);
        pushRocks.pause(true);
		createMap();
		drawMap();
        this.incrementGravityOnInterval = true;
        this.updateLevelText();
        // if (incrementGravityOnInterval) {
        //     IntervalNotifier intervalNotifier = new IntervalNotifier(this, 1000, true);
        //     Thread thread = new Thread(intervalNotifier);
        //     thread.start();
        //     this.intervalNotifier = intervalNotifier;
        //     this.intervalNotifier.stop();
        // }
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

    private String getBackgroundColor(char type, boolean state, boolean isBirdView) {
        switch(type) {
            //Floor
            case ' ':
                if (!isBirdView) {
                    return "#5b82bb"; //this color made things look ugly
                    // return "#1db121";
                }
                else {
                    // return "#84786a";
                    return "#168c4d";
                    // return "#32804e";
                }
            //Wall
            case 'w':
                // return "#a0a0a1";
                return "#b5b5b5";
            //PressurePlate
            case 'd':   
                return "#a72702";
                // // return "#f1b469";
            //Teleporter
            case 't':   
                if (state) {
                    return "#d973ff";
                }
                else {
                    return "#bd0dff";
                }
            //Player
            case 'p':   
                if (state) {
                    return "#356bc6";
                }
                else {
                    return "#2d5aa6";
                }
            //Rock
            case 'r':   
                if (state) {
                    return "#5b5b5b";
                }
                else {
                    return "#454545";
                }
            //Portal1
            case 'v':   
                if (state) {
                    return "#00adef";
                }
                else {
                    return "#282bfc";
                }
            //Portal2
            case 'u':   
                if (state) {
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

    private String getBorderColor(char type, boolean state) {
        if (type == 'v' || type == 'u') {
            // return "#a0a0a1";
            return "#b5b5b5";
        }
        if (type == 'p') {
			if (state) {
                return "#245097";
				
			}
			else {
                return "#204683";
			}
		}
        return "#24d628";
    }

    private int getBorderWidth(char type) {
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

    // private String getBlockStyle(BlockAbstract blockAbstract) {
    private String getBlockStyle(int x, int y) {
        // String style = "-fx-background-color: " + getBackgroundColor(blockAbstract) + ";";
        char type = pushRocks.getTopBlockType(x, y);
        boolean state = pushRocks.getTopBlockState(x, y);
        boolean isBirdView = pushRocks.getTopBlockBirdView(x, y);
        String style = "-fx-background-color: " + getBackgroundColor(type, state, isBirdView) + ";";

        String direction = pushRocks.getTopBlockDirection(x, y);
        if (direction != null) {
            style += "-fx-border-color: " + getBorderColor(type, state) + ";";
            int borderWidth = getBorderWidth(type);

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
        return style;
    }

    @FXML
    private void handlePlayerInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            if (!menuPage.isVisible()) { //If score page is added, then ESC should make sure to close that before entering menu
                handleMenu();
            }
            else {
                handleContinue();
            }
        }
        // These key-presses should only work when the actual game is visible.
        else if (mapPage.isVisible()) {
            if (keyEvent.getCode() == KeyCode.TAB) {
                handleScore();
            }
            else if (keyEvent.getCode() == KeyCode.W) {
                handleUp();
            }
            else if (keyEvent.getCode() == KeyCode.S) {
                handleDown();
            }
            else if (keyEvent.getCode() == KeyCode.D) {
                handleRight();
            }
            else if (keyEvent.getCode() == KeyCode.A) {
                handleLeft();
            }
            else if (keyEvent.getCode() == KeyCode.Q) {
                handlePortalOne();
            }
            else if (keyEvent.getCode() == KeyCode.E) {
                handlePortalTwo();
            }
            else if (keyEvent.getCode() == KeyCode.R) {
                handleGravityInverter();
            }
            else if (keyEvent.getCode() == KeyCode.F) {
                handleResetLevel();
            }
            else if (keyEvent.getCode() == KeyCode.T) {
                if (gravityManualIncrementButton.isVisible()) {
                    handleManualGravityIncrement();
                }
            }
        }
    }

	@FXML
    private void handleUp() {
        pushRocks.movePlayer(1, "up");
    }
    @FXML
    private void handleDown() {
        pushRocks.movePlayer(1, "down"); 
    }
    @FXML
    private void handleLeft() {
        pushRocks.movePlayer(1, "left");
    }
    @FXML
    private void handleRight() {
        pushRocks.movePlayer(1, "right");
    }

    @FXML
    private void handlePortalOne() {
        pushRocks.placePortal(true);
    }
    @FXML
    private void handlePortalTwo() {
        pushRocks.placePortal(false);
    }

    @FXML
    private void handleGravityInverter() {
        pushRocks.gravityInverter();
        updateGravityButton();
    }

    public void updateGravityButton() {
        if (pushRocks.getGravityDirectionY() < 0) {
            gravityButton.setText("Gravity ▼");
        }
        else {
            gravityButton.setText("Gravity ▲");
        }

    }
    @FXML 
    private void handleManualGravityIncrement() {
        pushRocks.gravityStep(false);
    }
    @FXML
    private void handleResetLevel() {
        pushRocks.resetLevel();
        updateGravityButton();     

        // if (this.incrementGravityOnInterval) {
        //     if (this.intervalNotifier != null) {
        //         this.intervalNotifier.stop();
        //         try {
        //             TimeUnit.MILLISECONDS.sleep(3000);
        //         } catch (InterruptedException e) {
        //             System.out.println("So impatient");
        //             e.printStackTrace();
        //         }
        //         this.intervalNotifier.start();
        //         this.intervalNotifier.run();
        //     }
        // }
    }

    private void pause() {
        mapPage.setVisible(false);
        mapPage.setDisable(true);
        menuPage.setVisible(false);
        statusPage.setVisible(false);
        gravityManualIncrementButton.setVisible(false);
        if (this.incrementGravityOnInterval) {
            // if (this.intervalNotifier != null) {
            //     this.intervalNotifier.stop();
            // }
            this.pushRocks.pause(true);
        }
    }
    private void unpause() {
        mapPage.setVisible(true);
        mapPage.setDisable(false);
        menuPage.setVisible(false);
        statusPage.setVisible(false);
        gravityManualIncrementButton.setVisible(false); //!!!!
        if (this.incrementGravityOnInterval) {
            // if (this.intervalNotifier != null) {
            //     this.intervalNotifier.stop();
            // }
            this.pushRocks.pause(true);
        }
    }

    @FXML
    private void handleScore() {
        System.out.println("Open status page.");
        this.pause();
        mapPage.setVisible(true);
        statusTitleRightText.setText("Pushing...");
        statusMessageText.setText("Those rocks aren't going to push themselves!");
        statusPage.setVisible(true);
        mapPage.setOpacity(1);
        
    }
    @FXML
    private void handleMenu() {
        System.out.println("Open menu.");
        this.pause();
        menuPage.setVisible(true);
    }

    //MENU PAGE
    @FXML
    private void handleContinue() {
        System.out.println("Continue!");
        if (pushRocks.isGameOver()) {
            handleResetLevel();
        }
        updateGravityButton();
        this.unpause();
        

        if (menuGravityMoveInputButton.isSelected()) {
            System.out.println("Gravity: moveInput");
            this.pushRocks.setGravityApplicationMoveInput();
            System.out.println("gMoveInput:" + this.pushRocks.isGravityApplicationMoveInput());
            gravityManualIncrementButton.setVisible(false);
        }
        else if (menuGravityIntervalButton.isSelected()) {
            System.out.println("Gravity: interval");
            this.pushRocks.setGravityApplicationInterval();
            System.out.println("gInterval" + this.pushRocks.isGravityApplicationInterval());
            gravityManualIncrementButton.setVisible(false);

            if (this.intervalNotifier != null) {
                this.intervalNotifier.start();
                this.intervalNotifier.run();
            }
            this.pushRocks.pause(false);
        }
        else {
            System.out.println("Gravity: manual");
            this.pushRocks.setGravityApplicationManual();
            System.out.println("gManual" + this.pushRocks.isGravityApplicationManual());
            gravityManualIncrementButton.setVisible(true);
        }
    }
    @FXML
    private void handleLevelButton() {
        System.out.println("Level button");
        try {
            this.pushRocks = this.saveHandler.loadGame(menuLevelChoiceBox.getValue(), false);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pushRocks.addObserver(this);
        pushRocks.pause(true);
        createMap();
		drawMap();
        this.incrementGravityOnInterval = true;
        this.updateLevelText();
        
        
    }
    @FXML
    private void handleLoadButton() {
        System.out.println("Load button");
        Path filePath = Paths.get(menuLoadFileLocationField.getText());
        System.out.println("Load path:" + filePath);
        try {
            this.pushRocks = this.saveHandler.loadGame(filePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pushRocks.addObserver(this);
        pushRocks.pause(true);
        createMap();
		drawMap();
        this.incrementGravityOnInterval = true;
        this.updateLevelText();
    }   
    @FXML
    private void handleSaveButton() {
        System.out.println("Save button");
        Path savePath = Paths.get(menuSaveFileLocationField.getText());
        System.out.println("Save path:" + savePath);
        try {
            this.saveHandler.saveGame(this.pushRocks, savePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        menuLoadFileLocationField.setText(null);
    }

    private Path getFilePathBrowse(boolean isSave) {
        FileChooser fileChooser = new FileChooser();
        Path path = saveHandler.getResourceFoldersPath("saves");
        fileChooser.setInitialDirectory(path.toFile());
        File file = null;
        if (isSave) {
            final Window window = menuSaveFileLocationField.getScene().getWindow();
            file = fileChooser.showSaveDialog(window);
        }
        else {
            final Window window = menuLoadFileLocationField.getScene().getWindow();
            file = fileChooser.showOpenDialog(window);
        }
        if (file != null) {
            return file.toPath();
        }
        return null;
    }

    @FXML 
    private void handleLoadBrowse() {
        System.out.println("Load browse");
        Path loadPath = getFilePathBrowse(false);
        if (loadPath != null) {
            menuLoadFileLocationField.setText(loadPath.toString());
        }
    }
    @FXML 
    private void handleSaveBrowse() {
        System.out.println("Save browse");
        Path savePath = getFilePathBrowse(true);
        if (savePath != null) {
            menuSaveFileLocationField.setText(savePath.toString());
        }
    }

    @FXML
    private void handleGravityChoiceManual() {
        System.out.println("Gravity application: manual");
        menuGravityChoice(1);
    }
    @FXML
    private void handleGravityChoiceMoveInput() {
        System.out.println("Gravity application: move input");
        menuGravityChoice(0);
    }
    @FXML
    private void handleGravityChoiceInterval() {
        System.out.println("Gravity application: interval");
        menuGravityChoice(-1);
    }

    @FXML
    private void handleApplyChanges() {
        System.out.println("Apply changes");
    }
    @FXML
    private void handleRevertChanges() {
        System.out.println("Revert changes");
    }

    private void menuGravityChoice(int c) {
        this.gravityChoice = c;
    }

    //STATUS PAGE
    @FXML
    private void handleSituationalButton() {
        handleContinue();
    }

    //GAME STATUS UPDATES
    private void updateScore() {
        int score = this.pushRocks.getMoveCount();
        scoreButton.setText("Score: " + score); 
        menuScoreText.setText("Score: " + score);
        statusScoreText.setText("Score: " + score);
    }

    private void updateLevelText() {
        String levelName = pushRocks.getLevelName();
        menuLevelText.setText(levelName);
        statusLevelText.setText(levelName);
    }

	private void drawMap() {
		//for every possible block coordinate, set the background color according the block
		//representing that specific coordinate
		for (int y = 0; y < pushRocks.getHeight(); y++) {
            for (int x = 0; x < pushRocks.getWidth(); x++) {
                String style = getBlockStyle(x, -y);
                map.getChildren().get(y * pushRocks.getWidth() + x).setStyle(style);
            }
        }
        this.updateScore();
        if (this.pushRocks.isGameOver()) {
            this.handleScore();
            statusTitleRightText.setText("PUSHED");
            statusMessageText.setText("Congratulations, you managed to complete this absolutely meaningless test.");
            // statusSituationalButton.setText("Reset"); //causes thread exception
        }
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

}   

