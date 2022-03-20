package pushrocks.fxui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import pushrocks.model.BlockAbstract;
import pushrocks.model.PushRocks;



public class PushRocksController {

    private PushRocks pushRocks;

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
    public void initialize() {

        // String levelLayout = """
        //     wwwwwwwwww
        //     wpfwwffffw
        //     wfffrfrffw
        //     wffwwwwwfw
        //     wfffrfffdw
        //     wfwwwwfwfw
        //     wfwffwfwdw
        //     wfwfrwfwfw
        //     wfffffdfdw
        //     wwwwwwwwww""";
        
        String levelLayout0 = """
            wwwwwwwwwwwwwwwwwww
            w  wp    w        w
            w  w r   w  r     w
            w  wwww ww        w
            w   r    w        w
            w      d w        w
            w        w        w
            w t  d d w  t     w
            w        w        w
            wwwwwwwwwwwwwwwwwww
            w        w        w
            w        w   r  d w
            w        www    www
            w        w   r  d w
            w        w        w
            w        w ww     w
            w t      w t      w
            w        w        w
            wwwwwwwwwwwwwwwwwww""";

           String directionLayout0 = "rrrrrr";

		pushRocks = new PushRocks(levelLayout0, directionLayout0);
		createMap();
		drawMap();
	}


	//Code inspired by Snakebird project: GameController.java method createBoard()
	private void createMap() {
		//Clears out all current children objects of the map-Pane, so 
		//that the map can be rebuilt into the updated version
		map.getChildren().clear();
        int blockSize = 20;
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

	// private String getBlockColor(BlockAbstract blockAbstract) {

	// 	if (blockAbstract.isFloor()) {
	// 		// return "#1db121";
    //         if (blockAbstract.getY() < this.pushRocks.getGravityZone()) {
    //             // return "#7490C7"; this color made things look ugly
    //             return "#1db121";
    //         }
    //         else {
    //             return "#1db121";
    //         }
	// 	}
	// 	if (blockAbstract.isWall()) {
	// 		return "#24d628";
	// 	}
	// 	if (blockAbstract.isPlate()) {
	// 		return "#00fff2";
	// 	}
    //     if (blockAbstract.isTeleporter()) {
	// 		if (blockAbstract.getState()) {
	// 			return "#d973ff";
	// 		}
	// 		else {
	// 			return "#bd0dff";
	// 		}
	// 	}
	// 	if (blockAbstract.isPlayer()) {
	// 		if (blockAbstract.getState()) {
	// 			return "#ff0000";
	// 		}
	// 		else {
	// 			return "#a10000";
	// 		}
	// 	}
	// 	if (blockAbstract.isRock()) {
	// 		if (blockAbstract.getState()) {
	// 			return "#6e6e6e";
	// 		}
	// 		else {
	// 			return "#454545";
	// 		}
	// 	}
    //     if (blockAbstract.isPortalOne()) {
	// 		if (blockAbstract.getState()) {
	// 			return "#00adef";
	// 		}
	// 		else {
	// 			return "#282bfc";
	// 		}
	// 	}
    //     if (blockAbstract.isPortalTwo()) {
	// 		if (blockAbstract.getState()) {
	// 			return "#ff6a00";
	// 		}
	// 		else {
	// 			return "#c95604";
	// 		}
	// 	}
	// 	return "#1db121";
		
			
	// }
	private String getBlockColor(BlockAbstract blockAbstract) {
        //floor
		if (blockAbstract.getType() == 'f') {
			// return "#1db121";
            if (blockAbstract.getY() < this.pushRocks.getGravityZone()) {
                // return "#7490C7"; this color made things look ugly
                return "#1db121";
            }
            else {
                return "#1db121";
            }
		}
        //wall
		if (blockAbstract.getType() == 'w') {
			return "#24d628";
		}
        //plate
		if (blockAbstract.getType() == 'd') {
			return "#00fff2";
		}
        //teleporter
        if (blockAbstract.getType() == 't') {
			if (blockAbstract.getState()) {
				return "#d973ff";
			}
			else {
				return "#bd0dff";
			}
		}
        //player
		if (blockAbstract.getType() == 'p') {
			if (blockAbstract.getState()) {
				return "#ff0000";
			}
			else {
				return "#a10000";
			}
		}
        //rock
		if (blockAbstract.getType() == 'r') {
			if (blockAbstract.getState()) {
				return "#6e6e6e";
			}
			else {
				return "#454545";
			}
		}
        //portal 1
        if (blockAbstract.getType() == 'v') {
			if (blockAbstract.getState()) {
				return "#00adef";
			}
			else {
				return "#282bfc";
			}
		}
        //portal 2
        if (blockAbstract.getType() == 'u') {
			if (blockAbstract.getState()) {
				return "#ff6a00";
			}
			else {
				return "#c95604";
			}
		}
		return "#1db121";
		
			
	}


	@FXML
    void handleUp() {
        pushRocks.movePlayer(1, "up");
        drawMap();
    }

    @FXML
    void handleDown() {
        pushRocks.movePlayer(1, "down"); 
        drawMap();
    }

    @FXML
    void handleLeft() {
        pushRocks.movePlayer(1, "left");
        drawMap();
    }

    @FXML
    void handleRight() {
        pushRocks.movePlayer(1, "right");
        drawMap();
    }

    @FXML
    void resetLevel() {
        pushRocks.buildWorld();
        createMap();
        drawMap();
    }

    @FXML
    void handleGravity() {
        pushRocks.gravityInverter();
        drawMap();
    }

    @FXML
    void handlePortalOne() {
        pushRocks.placePortal(true, pushRocks.getPlayer(1));
        drawMap();
    }

    @FXML
    void handlePortalTwo() {
        pushRocks.placePortal(false, pushRocks.getPlayer(1));
        drawMap();
    }

    @FXML
    void handleMenu() {
        System.out.println("Open menu.");
        System.out.println(pushRocks.toGameToSaveFormat());
    }

    private void updateScore() {
        handleScore.setText("Score: " + pushRocks.getScore());
    }

	private void drawMap() {
		//for every possible block coordinate, set the background color according the block
		//representing that specific coordinate
		for (int y = 0; y < pushRocks.getHeight(); y++) {
            for (int x = 0; x < pushRocks.getWidth(); x++) {
                String style = "-fx-background-color: " + getBlockColor(pushRocks.getTopBlock(x, -y)) + ";";
            
                char type = pushRocks.getTopBlock(x, -y).getType();
                if (type == 'u' || type == 'v') {
                    style += "-fx-border-color: #24d628;";
                    String portalDirection = pushRocks.getObstacleBlock(x, -y).getDirection();
                    switch (portalDirection) {
                        case "up":
                            style += "-fx-border-width: 0 0 10 0;";
                            break;
                        case "down":
                            style += "-fx-border-width: 10 0 0 0;";
                            break;
                        case "left":
                            style += "-fx-border-width: 0 10 0 0;";
                            break;
                        case "right":
                            style += "-fx-border-width: 0 0 0 10;";
                            break;
                    }
                }
                map.getChildren().get(y * pushRocks.getWidth() + x).setStyle(style);
            }
        }
        this.updateScore();
        
	}
}

