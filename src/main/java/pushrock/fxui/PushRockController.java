package pushrock.fxui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
import pushrock.model.BlockAbstract;
import pushrock.model.DirectedBlock;
import pushrock.model.IObservableIntervalNotifier;
import pushrock.model.IObservablePushRock;
import pushrock.model.IObserverIntervalNotifier;
import pushrock.model.IObserverPushRock;
import pushrock.model.PushRock;

public class PushRockController implements IObserverPushRock, IObserverIntervalNotifier {

    private PushRock pushRock;
    private int blockSize;
    private ISaveHandler saveHandler = new SaveHandler();

    //APP
    @FXML 
    private AnchorPane anchorPane;
    @FXML
    private Text appInformationText;

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
    private ToggleGroup gravityApplicationChoice;
   
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


    @FXML
    public void initialize() {
        String levelName = "Start-up level";
        String levelLayout1 = """
            twwwwwwwwwwwwwwwwwd@
                               @
                               @
            rrr  r  r  rrr r  r@
            r  r r  r r    r  r@
            rrr  r  r  rr  rrrr@
            r    r  r    r r  r@
            r    rrrr rrr  r  r@
                p              @
            wwwwwwwwwwwwwwwuwww@
            -------------------@
            RRR--RRRR--RRR-R--R@
            R--R-R--R-R----R-R-@
            RRR--R--R-R----RR--@
            R-R--R--R-R----R-R-@
            R--R-RRRR--RRR-R--R@
            -------------------@
            -------------------@
            DWWWWWWWWWWWWWWVWWT@""";
        String directionLayout1 = "ddddddddddddddddddddddddddddddddddddddddddddddddddduuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuug";
                                  
        menuPage.setVisible(true);
        mapPage.setVisible(false);

        this.saveHandler = new SaveHandler();
        menuLevelChoiceBox.setValue("Select a level");
        ObservableList<String> levelList = FXCollections.observableArrayList();
        levelList.addAll(this.saveHandler.getLevelNames());
        menuLevelChoiceBox.setItems(levelList);
        
		pushRock = new PushRock(levelName, levelLayout1, directionLayout1);
        pushRock.addObserver(this);
        pushRock.pause(true);
		createMap();
		drawMap();
        updateLevelText();
	}


	//Code inspired by Snakebird project: GameController.java method createBoard()
	private void createMap() {
		//Clears out all current children objects of the map-Pane, so 
		//that the map can be rebuilt into the updated version
		map.getChildren().clear();
        this.blockSize = 25;
        int mapWidth;
        int mapHeight; 
        mapWidth = pushRock.getWidth();
        mapHeight = pushRock.getHeight();

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

		for (int y = 0; y < pushRock.getHeight(); y++) {
            for (int x = 0; x < pushRock.getWidth(); x++) {
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
                    return "#5b82bb";
                }
                else {
                    return "#168c4d";
                }
            //Wall
            case 'w':
                return "#b5b5b5";
            //PressurePlate
            case 'd':   
                if (!isBirdView) {
                    return "#7cb1fc"; 
                }
                else {
                    return "#21cc70";
                }
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
                //A default color is set for any other type, which should only be used in the case that a new type has been introduced 
                //and the controller has yet to assign it a color of its own.
            default:
                return "#84786a"; 
        }
	}

    private String getBorderColor(char type, boolean state) {
        if (type == 'd') {
            return "#5b5b5b";
        }
        else if (type == 'v' || type == 'u') {
            return "#b5b5b5";
        }
        else if (type == 'p') {
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
            case 'd':
                return this.blockSize * 1/8;
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

    private String getBlockStyle(int x, int y) {
        BlockAbstract blockCopy = pushRock.getTopBlockCopy(x, y);
        char type = blockCopy.getType();
        boolean state = blockCopy.getState();
        boolean isBirdView = pushRock.getTraversableBlockCopy(x, y).isBirdView();
        String style = "-fx-background-color: " + getBackgroundColor(type, state, isBirdView) + ";";

        if (blockCopy instanceof DirectedBlock) {
            String direction = ((DirectedBlock) blockCopy).getDirection();
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
        }
        else if (type == 'd') {
            style += "-fx-border-color: " + getBorderColor(type, state) + ";";
            int borderWidth = getBorderWidth(type);
            style += "-fx-border-width: " + borderWidth + " " + borderWidth + " " + borderWidth + " " + borderWidth + ";";
        }
        return style;
    }

    //APP
    @FXML
    private void handleInformationClick() {
        this.appInformationText.setVisible(!this.appInformationText.isVisible());
    }

    //GAME
    @FXML
    private void handlePlayerInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            if (!menuPage.isVisible()) {
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
    private void movePlayer(String direction) {
        try {
            pushRock.movePlayer(direction);
        } catch (Exception e) {
            this.appInformationText.setText(e.getMessage());
            this.appInformationText.setVisible(true);
        }
    }

	@FXML
    private void handleUp() {
        this.movePlayer("up");
        
    }
    @FXML
    private void handleDown() {
        this.movePlayer("down"); 
    }
    @FXML
    private void handleLeft() {
        this.movePlayer("left");
    }
    @FXML
    private void handleRight() {
        this.movePlayer("right");
    }

    @FXML
    private void handlePortalOne() {
        placePortal(true);
    }
    @FXML
    private void handlePortalTwo() {
        placePortal(false);
    }

    @FXML private void placePortal(boolean isPortalOne) {
        try {
            pushRock.placePortal(isPortalOne);
        } catch (IllegalStateException e) {
            this.appInformationText.setVisible(true);
            this.appInformationText.setText(e.getMessage());
        }
    }

    @FXML
    private void handleGravityInverter() {
        pushRock.gravityInverter();
        updateGravityButton();
    }

    public void updateGravityButton() {
        if (!pushRock.isGravityInverted()) {
            gravityButton.setText("Gravity ▼");
        }
        else {
            gravityButton.setText("Gravity ▲");
        }
    }
    @FXML 
    private void handleManualGravityIncrement() {
        pushRock.gravityStep();
    }
    @FXML
    private void handleResetLevel() {
        pushRock.resetLevel();
        updateGravityButton();     
        appInformationText.setText("Level reset.");
    }

    private void pause() {
        mapPage.setVisible(false);
        mapPage.setDisable(true);
        menuPage.setVisible(false);
        statusPage.setVisible(false);
        gravityManualIncrementButton.setVisible(false);
        if (this.pushRock.isGravityApplicationInterval()) {
            this.pushRock.pause(true);
        }
    }
    private void unpause() {
        mapPage.setVisible(true);
        mapPage.setDisable(false);
        menuPage.setVisible(false);
        statusPage.setVisible(false);
        gravityManualIncrementButton.setVisible(false); 
        if (this.pushRock.isGravityApplicationInterval()) {
            this.pushRock.pause(true);
        }
    }

    @FXML
    private void handleScore() {
        System.out.println("Open status page.");
        this.pause();
        mapPage.setVisible(true);
        statusTitleRightText.setText("Pushing...");
        statusMessageText.setText("Those rocks aren't going to push themselves! \n.. \n or maybe they are?");
        statusPage.setVisible(true);
        mapPage.setOpacity(1);
    }
    @FXML
    private void handleMenu() {
        System.out.println("Open menu.");
        this.pause();
        menuPage.setVisible(true);
        appInformationText.setText("Open menu.");
    }

    //MENU PAGE
    @FXML
    private void handleContinue() {
        System.out.println("Continue!");
        if (pushRock.isGameOver()) {
            handleResetLevel();
        }
        else {
            appInformationText.setText("Continue!");
        }
        updateGravityButton();
        this.unpause();

        if (menuGravityMoveInputButton.isSelected()) {
            System.out.println("Gravity: moveInput");
            this.pushRock.setGravityApplicationMoveInput();
            System.out.println("gMoveInput:" + this.pushRock.isGravityApplicationMoveInput());
            gravityManualIncrementButton.setVisible(false);
        }
        else if (menuGravityIntervalButton.isSelected()) {
            System.out.println("Gravity: interval");
            this.pushRock.setGravityApplicationInterval();
            System.out.println("gInterval" + this.pushRock.isGravityApplicationInterval());
            gravityManualIncrementButton.setVisible(false);
            this.pushRock.pause(false);
        }
        else {
            System.out.println("Gravity: manual");
            this.pushRock.setGravityApplicationManual();
            System.out.println("gManual" + this.pushRock.isGravityApplicationManual());
            gravityManualIncrementButton.setVisible(true);
        }
    }
    @FXML
    private void handleLevelButton() {
        System.out.println("Level button");
        boolean loadSuccessful = false;
        String levelFileName = menuLevelChoiceBox.getValue();
        try {
            this.pushRock = this.saveHandler.loadGame(levelFileName, false);
            loadSuccessful = true;
        } catch (FileNotFoundException e) {
            this.appInformationText.setVisible(true);
            this.appInformationText.setText("The file: " + menuLevelChoiceBox.getValue() + " could not be found. Please try another file.");
        } catch (IOException e) {
            this.appInformationText.setVisible(true);
            this.appInformationText.setText(e.getMessage());
        } catch (IllegalArgumentException e) {
            this.appInformationText.setVisible(true);
            this.appInformationText.setText(e.getMessage());
        }
        if (loadSuccessful) {
            pushRock.addObserver(this);
            pushRock.pause(true);
            appInformationText.setText(levelFileName + " was loaded successfully!");
            createMap();
            drawMap();
        }
        updateLevelText();
        updateLevelList();
    }

    private void updateLevelList() {
        ObservableList<String> levelList = FXCollections.observableArrayList();
        levelList.addAll(this.saveHandler.getLevelNames());
        menuLevelChoiceBox.setItems(levelList);
        menuLevelChoiceBox.setValue("Select a level");
    }

    @FXML
    private void handleLoadButton() {
        System.out.println("Load button");
        Path filePath = null;
        try { 
            filePath = Paths.get(menuLoadFileLocationField.getText());
        } catch (InvalidPathException e) {
            this.appInformationText.setVisible(true);
            this.appInformationText.setText("Path contains illegal characters.");
        }
        System.out.println("Load path:" + filePath);
        if (filePath != null) {
            try {
                this.pushRock = this.saveHandler.loadGame(filePath);
                this.appInformationText.setText("Level-load successful.");
                pushRock.addObserver(this);
                pushRock.pause(true);
                createMap();
                drawMap();
                appInformationText.setText("Game-save loaded successfully!");
                updateLevelText();
            } catch (FileNotFoundException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText("Could not find the file from the provided path.");
            } catch (IOException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText(e.getMessage());
            } catch (NullPointerException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText(e.getMessage());
            } catch (NumberFormatException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText("Incorrect save format for save file. Move count must only contain integers.");
            } catch (IllegalArgumentException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText(e.getMessage());
            } 
        }

    }   
    @FXML
    private void handleSaveButton() {
        System.out.println("Save button");
        Path savePath = null;
        try { 
            savePath = Paths.get(menuSaveFileLocationField.getText());
        } catch (InvalidPathException e) {
            this.appInformationText.setVisible(true);
            this.appInformationText.setText("Path contains illegal characters.");
        }
        System.out.println("Save path:" + savePath);
        if (savePath != null) {
            try {
                this.saveHandler.saveGame(this.pushRock, savePath);
                this.appInformationText.setText("Game-save successful.");
            } catch (IOException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText("Could not save at the given file path.");
            } catch (IllegalArgumentException e) {
                this.appInformationText.setVisible(true);
                this.appInformationText.setText(e.getMessage());
            }
        }
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

    //STATUS PAGE
    @FXML
    private void handleSituationalButton() {
        handleContinue();
    }

    //GAME STATUS UPDATES
    private void updateScore() {
        int score = this.pushRock.getMoveCount();
        scoreButton.setText("Score: " + score); 
        menuScoreText.setText("Score: " + score);
        statusScoreText.setText("Score: " + score);
    }

    private void updateLevelText() {
        String levelName = pushRock.getLevelName();
        menuLevelText.setText(levelName);
        statusLevelText.setText(levelName);
    }

	private void drawMap() {
		//for every possible block coordinate, set the background color according the block
		//representing that specific coordinate
		for (int y = 0; y < pushRock.getHeight(); y++) {
            for (int x = 0; x < pushRock.getWidth(); x++) {
                String style = getBlockStyle(x, -y);
                map.getChildren().get(y * pushRock.getWidth() + x).setStyle(style);
            }
        }
        this.updateScore();
        if (this.pushRock.isGameOver()) {
            this.handleScore();
            appInformationText.setText("Level complete!");
            statusTitleRightText.setText("PUSHED");
            statusMessageText.setText("Congratulations, you managed to complete this absolutely meaningless test.");
        }
	}

    @Override
    public void update(IObservablePushRock observable) {
        if (observable == this.pushRock) {
            this.drawMap();
        }
    }

    @Override
    public void update(IObservableIntervalNotifier observable) {
        this.pushRock.gravityStep();
    }

}   

