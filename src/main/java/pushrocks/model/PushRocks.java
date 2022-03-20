package pushrocks.model;

import java.util.ArrayList;

public class PushRocks {

    private int width;
    private int height;
    private String levelLayout;
    private String directionLayout; //Contains 

    private TraversableBlock[][] traversableBlocks;                                         //Blocks which make out the surface on which other blocks can move around/be placed ontop of
    private ObstacleBlock[][] obstacleBlocks;                                               //Blocks that are placed ontop of the traversable plane, which can hinder or redirect movement of moveable blocks
    private ArrayList<MoveableBlock> moveableBlocks = new ArrayList<MoveableBlock>();       //Blocks that are free to move around on the traversable plane, but are limited by placements of directed blocks, which
                                                                                            // includes both obstacle and moveable blocks.
    
    private ArrayList<ObstacleBlock> teleporters = new ArrayList<ObstacleBlock>();
    private ArrayList<ObstacleBlock> portals = new ArrayList<ObstacleBlock>();

    private int score;
    private int gravityZone;
    private boolean isGravityInverted;

    public int getHeight() {
        return this.height;
    }

    private void setHeight(int height) { //redundant? Should I be using "setHeight()"" instead of "this.height = " when changing this attribute within this code?
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    private void setWidth(int width) { //redundant? See height comment
        this.width = width;
    }

    public int getRockCount() {
        int rockCount = 0;
        for (MoveableBlock block : moveableBlocks) {
            if (block.isRock()) {
                rockCount++;
            }
        }
        return rockCount;
    }

    public int getPlateCount() {
        int plateCount = 0;
        for (int y = 0; y > -this.height; y--) {
            for (int x = 0; x < this.width; x++) {
                if (traversableBlocks[-y][x].isPlate()) {
                    plateCount++;
                }
            }
        }
        System.out.println(plateCount);
        return plateCount;
    }

    public int getGravityZone() {
        return this.gravityZone;
    }
    public void setGravityZone(int gravityZone) {
        if (gravityZone < 0 || gravityZone >= this.height) {
            throw new IllegalArgumentException("Gravity zone out of bounds, must be between 0 and " + this.height + ".");
        }
        this.gravityZone = gravityZone;
    }

    //Returns the given player block if it exists
    public MoveableBlock getPlayer(int number) {
        int playerNumber = 1;
        for (MoveableBlock block : this.moveableBlocks) {
            if (block.isPlayer()) {
                if (playerNumber == number) {
                    return block;
                }
                else {
                    playerNumber++;
                }
            }
        }
        return null;
    }

    public BlockAbstract getTopBlock(int x, int y) {
        if (this.getMoveableBlock(x, y) != null) {
            return getMoveableBlock(x, y);
        }
        if (this.getObstacleBlock(x, y) != null) {
            return getObstacleBlock(x, y);
        }
        if (this.getTraversableBlock(x, y) != null) {
            return getTraversableBlock(x, y);
        }
        return null;
    }


    public void updateScore() {
        this.score = 0;
        for (MoveableBlock block : moveableBlocks) {
            if (block.isMoveable() && block.getState()) {
                this.score++;
            }
        }
        this.updateTeleporters();
    }

    public int getScore() {
        this.updateScore();
        return this.score;
    }

    private void updateTeleporters() {
        if (this.teleporters.size() < 1) {
            System.out.println("There are no teleporters, thus there is nothing to update.");
            return;
        }
        System.out.println("Updating Teleporters. Score:" + this.score);
        //Connects two teleporters together based on the game's current score. A previous connection is removed once a new one is made.
        //Where there are n teleporters, and s is the score, the two teleporters that will connected are:
        //Teleporter one: index = s / n
        //Teleporter two: index = s % n
        this.teleporters.get(this.score / this.teleporters.size()).setConnection(this.teleporters.get(this.score % this.teleporters.size()));
    }


    public boolean placePortal(boolean inputIsPortalOne, MoveableBlock creator) {
        if (! creator.isPlayer()) {
            return false; //only a player can place a portal
        }
        int x = creator.getX();
        int y = creator.getY();
        String direction = creator.getDirection();

        String portalDirection = null;

        switch (direction) {
            case "up":
                portalDirection = "down";
                break;
            case "down":
                portalDirection = "up";
                break;
            case "right":
                portalDirection = "left";
                break;
            case "left": 
                portalDirection = "right";
                break;
        }
        
        ObstacleBlock wall = this.findObstacle(direction, x, y);

        //If a wall was found, then a portal can potentially be placed there
        if (wall != null) {
            //Can not place a portal at a teleporter
            if (wall.isTeleporter()) {
                return false;
            }
            //if the found wall holds a portal, and that portal is the same portal as the one to be placed,
            //and it faces the same direction, then everything is already as it should, no portals need to 
            //be changed
            if (wall.isPortal() && wall.isPortalOne() == inputIsPortalOne && wall.getDirection() == portalDirection) {
                updateScore();
                return true;
            }
            //if the wall is still a portal, then it could be the portal other than the one being created, and should in that case be overwritten
            if (wall.isPortal() && wall.isPortalOne() != inputIsPortalOne) {
                this.removePortal(wall);
                this.removePortal(this.getPortal(inputIsPortalOne));
            }
            //first remove any existing portal with a type matching the one to be placed, 
            // then place the new portal at the given wall.
            this.removePortal(this.getPortal(inputIsPortalOne));
            wall.setPortal(inputIsPortalOne, portalDirection, this.getPortal(!inputIsPortalOne));
            this.addPortal(wall);
            
            updateScore();
            System.out.println(this.prettyString());
            return true; //portal placed successfully.
        }
        updateScore();
        System.out.println(this.prettyString());
        return false;
    }

    public boolean addPortal(ObstacleBlock portal) {
        if (portal == null) {
            return false;
        }
        this.portals.add(portal);
        if (portals.size() > 1) {
            this.portals.get(0).setConnection(this.portals.get(1));
        }
        else {
            this.portals.get(0).setConnection(null);
        }
        
        return true;

    }

    public boolean removePortal(ObstacleBlock oldPortal) {
        if (oldPortal == null) {
            return false;
        }
        oldPortal.clearPortal();
        portals.remove(oldPortal);
        return true;
    }

    public ObstacleBlock getPortal(boolean inputIsPortalOne) {
        if (portals.size() < 1) {
            return null;
        }
        for (ObstacleBlock portal : this.portals) {
            if (portal.isPortalOne() == inputIsPortalOne) {
                return portal;
            }
        }
        return null;
    }


   
    public ObstacleBlock findObstacle(String direction, int x, int y) {
        int newX = x;
        int newY = y;
        switch (direction) {
            case "up":
                newY++;
                break;
            case "down":
                newY--;
                break;
            case "right":
                newX++;
                break;
            case "left": 
                newX--;
                break;
        }
        //If the search continues far enough to get out of bounds, then it means that there are no
        //obstacles in the given direction.
        if ((newX < 0 || newX >= this.width) || (newY > 0 || newY <= -this.height)) {
            return null;
        }
        else if (getObstacleBlock(newX, newY) != null) {
            return getObstacleBlock(newX, newY);
            // if (getObstacleBlock(newX, newY).isWall() || getObstacleBlock(newX, newY).isPortal() || getObstacleBlock(newX, newY).isTeleporter()) {
            //     System.out.println("Wall found");
            //     return getObstacleBlock(newX, newY);
            // }
        }
        else {
            return findObstacle(direction, newX, newY);
        }
    }

    //Returns true if the game is over. The game is over once the every rock is placed
    //ontop of a plate block, since the score equals the amount of rocks 
    //that are placed ontop of plates it follows that the game must be over once
    //the score equals the amount of rocks 
    public boolean isGameOver() {
        if (this.getScore() >= this.getPlateCount()) {
            return true;
        }
        return false;
    }

    public DirectedBlock getDirectedBlock(int x, int y) {
        if (this.getMoveableBlock(x, y) != null) {
            return getMoveableBlock(x, y);
        }
        if (this.getObstacleBlock(x, y) != null) {
            return getObstacleBlock(x, y);
        }
        return null;
    }
    public MoveableBlock getMoveableBlock(int x, int y) {
        for (MoveableBlock block : this.moveableBlocks) {
            if ((block.getY() == y) && (block.getX() == x)) {
                return block;
            }
        }
        return null;
    }
    public ObstacleBlock getObstacleBlock(int x, int y) {
        if(x < 0 || x >= this.width) {
            System.out.println("x:" + x + " is out of bounds when the width of the map is " + this.width);
            return null;
        }
        if(-y < 0 || -y >= this.height) {
            System.out.println("y:" + y + " is out of bounds when the height of the map is " + this.height);
            return null;
        }
        return obstacleBlocks[-y][x];
    }
    public TraversableBlock getTraversableBlock(int x, int y) {
        if(x < 0 || x >= this.width) {
            System.out.println("x:" + x + " is out of bounds when the width of the map is " + this.width);
            return null;
        }
        if(-y < 0 || -y >= this.height) {
            System.out.println("y:" + y + " is out of bounds when the height of the map is " + this.height);
            return null;
        }
        return traversableBlocks[-y][x];
    }


    public void addObstacleBlock(ObstacleBlock block) {
        this.obstacleBlocks[-block.getY()][block.getX()] = block;
        if (block.isTeleporter()) {
            this.teleporters.add(block);
        }
        if (block.isPortal()) {
            this.portals.add(block);
        }
    }

    public void addMoveableBlock(MoveableBlock block) {
        this.moveableBlocks.add(block);
    }

    public void removeMoveableBlock(MoveableBlock block) {
        this.moveableBlocks.remove(block);
    }

    public void gravityStep(boolean hasPlayerMoved) {
        //The first blocks that should fall down are the ones furthest down in the gravity's direction, thus
        //the these blocks should be issued to move first. 

        //When gravity is inverted the blocks with the highest value for their Y coordinates should fall first
        if (this.isGravityInverted) {
            for (int i = 0; i < moveableBlocks.size(); i++) {
                if (moveableBlocks.get(i).isPlayer() && hasPlayerMoved) {
                }
                else if (moveableBlocks.get(i).getY() < this.gravityZone) {
                    moveBlockTwoStrength(moveableBlocks.get(i), "up", 1);
                }
            }
        }

        //When gravity is not inverted the blocks with the lowest value for their Y coordinates should fall first
        else {
            for (int i = moveableBlocks.size() - 1; i >= 0; i--) {
                if (moveableBlocks.get(i).isPlayer() && hasPlayerMoved) {
                }
                else if (moveableBlocks.get(i).getY() < this.gravityZone) {
                        moveBlockTwoStrength(moveableBlocks.get(i), "down", 1);
                }
            }
        }
        this.isGameOver();
    }

    private boolean isCoordinateMatch2(BlockAbstract block1, BlockAbstract block2) {
        if (block1.getX() == block2.getX() && block1.getY() == block2.getY()) {
            return true;
        }
        else {
            return false;
        }
    }




    public boolean isBlockAirborne(MoveableBlock block) {
        int footingBlockY = block.getY();

        //Block is not airborne when not inside the gravity zone
        if (block.getY() > gravityZone) {
            return false;
        }
        if (! isGravityInverted) {
            footingBlockY--;
        }
        else {
            footingBlockY++;
        }

        DirectedBlock footingBlock = this.getDirectedBlock(block.getX(), footingBlockY);
        //If there are no blocks of the directed block sub class at the given coordinates, then there is no footing, thus
        //the block must be airborne
        // if (this.getDirectedBlock(block.getX(), footingBlockY) == null) {
        //     return true;
        // }
        if (footingBlock == null) {
            return true; 
        }
        //If the footingblock is an active portal, then set the footingBlock to the footing of the exit point of that active portal.
        if ((footingBlock.getType() == 'v' || footingBlock.getType() == 'u') && footingBlock.getState()) {
            ObstacleBlock entryPortal = this.getObstacleBlock(footingBlock.getX(), footingBlock.getY());
            footingBlock = this.getDirectedBlock(entryPortal.getPortalExitPoint()[0], entryPortal.getPortalExitPoint()[1]);
            footingBlockY = entryPortal.getPortalExitPoint()[1];
        }
        else if (footingBlock.getType() == 't') { //should check if this needs something more
        }

        //A moveable block can not be its own footing. This could happen in cases where the original footing block of the moveable 
        //block was an active portal with an exit point above the moveable block (in relation to current gravity direction).
        //If the footing block is the block itself, then the block has no footing and is thus airborne
        if (footingBlock == block) {
            return true;
        }
        //If footing block has no collision and the block is at a coordinate where gravity is in effect, then the block
        //is airborne.
        if (footingBlockY < gravityZone) {
            if (footingBlock == null) {
                return true;
            }
            if ((footingBlock.getType() == 'v' || footingBlock.getType() == 'u') && footingBlock.getState()) {
                return true;
            }
            if ((footingBlock.getType() == 't') && footingBlock.getState()) {
                return true;
            }
        }
        // else {
        //     return !footingBlock.hasCollision() && footingBlockY < gravityZone;
        // }
        return false;
    }

    public void gravityInverter() {
        this.isGravityInverted = !this.isGravityInverted;
    }

    //Issues to move the given player block and return boolean reflecting wether or not the player was moved. Every time 
    //the player tries to move gravity will move all blocks one step in its direction if possible.
    //Gravity should not take effect on the player if they were moved successfully by the method call, as to give the illusion of momentum
    public boolean movePlayer(int playerNumber, String direction) {
        MoveableBlock player = this.getPlayer(playerNumber);
        System.out.println("AAAAAAAAAAAAAAAAAAH" + player.getX() + "" + player.getY());
        player.setDirection(direction);
        boolean wasMoved = false;

        if (player != null) {
            //Can not move while airborne
            if (isBlockAirborne(player)) {
                wasMoved = false;
            }
            //Attempt to move player, check this one move was enough to win the game, update the variable "wasMoved" 
            else if (moveBlockTwoStrength(player, direction, 1)) {
                this.isGameOver();
                wasMoved = true;
            }
        }

        this.gravityStep(wasMoved);
        System.out.println(this.prettyString());
        return wasMoved;
    }

    public boolean pushMoveable(MoveableBlock block, String direction, int strength, boolean hasTakenPortal) { //CHECK THIS !!!!!!!!!!!!!! may work against intent
        //A block can not push another block in the same direction as the gravity affecting it
        if (direction == "up" && this.isGravityInverted && block.getY() < this.gravityZone) {
            if (!hasTakenPortal) {
                return false;
            }
        }
        if (direction == "down" && !this.isGravityInverted && block.getY() < this.gravityZone) {
            if (!hasTakenPortal) {
                return false;
            }
        }   
        if (block.isPlayer()) {
            if (moveBlockTwoStrength(block, direction, strength+1)) {
                this.isGameOver();
                return true;
            }
            else {
                return false;
            }
        }
        if (block.isRock()) {
            if (moveBlockTwoStrength(block, direction, strength-1)) {
                this.isGameOver();
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

    public boolean moveBlockTwoStrength(MoveableBlock block, String direction, int strength) {
        System.out.println(block.getX() + "," + block.getY() + " to move: " + direction + ".");
        boolean hasFooting = true; //after taking portals it can become unclear whether or not the block still has footing, so we keep track of this
        boolean hasTakenPortal = false;

        //checks if the given block is moveable
        if (! block.isMoveable()) {
            return false;
        }
        MoveableBlock blockOld = new MoveableBlock(block.getX(), block.getY(), block.getType(), block.getDirection());
        MoveableBlock blockNew = new MoveableBlock(block.getX(), block.getY(), block.getType(), block.getDirection());

        //if the block copy can not move, then the actual block also can not move
        if (! blockNew.moveInDirection(direction)) {
            return false;
        }
        // block.setDirection(direction);

        
        if (getObstacleBlock(blockNew.getX(), blockNew.getY()) != null) {
            // Block should be teleported if the given movement would place it at a connected teleporter
            if (getObstacleBlock(blockNew.getX(), blockNew.getY()).isTeleporter()) {
                //If the teleporter is not connected, then it will hinder any the block's movement
                if (! getObstacleBlock(blockNew.getX(), blockNew.getY()).getState()) {
                    System.out.println("Can not move further: you have hit a disconnected teleporter");
                    return false;
                }
                //If the teleporter is connected, then the moving block should be transported out of the connected teleporter in
                //direction of the movment if possible
                else {
                    // int newX = obstacleBlocks[-blockNew.getY()][blockNew.getX()].getConnection().getX();
                    // int newY = obstacleBlocks[-blockNew.getY()][blockNew.getX()].getConnection().getY();
                    int newX = getObstacleBlock(blockNew.getX(), blockNew.getY()).getConnection().getX();
                    int newY = getObstacleBlock(blockNew.getX(), blockNew.getY()).getConnection().getY();
                    blockNew.setX(newX);
                    blockNew.setY(newY);
                    blockNew.moveInDirection(direction);
                }
            } 
            if (getObstacleBlock(blockNew.getX(), blockNew.getY()) == null) {
            }
            // Block should be teleported if the given movement would place it at a connected portal
            else if (getObstacleBlock(blockNew.getX(), blockNew.getY()).isPortal()) {
                //If the portal is not connected, then it will act as a wall instead of transporting the block
                if (!getObstacleBlock(blockNew.getX(), blockNew.getY()).getState()) {
                    System.out.println("Can not move further: you have hit a disconnected portal");
                    return false;
                }
                //If the block to be moved is not standing at the entry point of the portal that lies ahead, then it will act as a wall instead of transporting the block
                int entryX, entryY;
                entryX = getObstacleBlock(blockNew.getX(), blockNew.getY()).getPortalEntryPoint()[0];
                entryY = getObstacleBlock(blockNew.getX(), blockNew.getY()).getPortalEntryPoint()[1];
                if ( !(blockOld.getX() == entryX && blockOld.getY() == entryY)) {
                    System.out.println("Can not move further: can only enter portal from its entry point");
                    return false;
                }
                //If the teleporter is connected, then the moving block should be transported out of the connected teleporter in
                //direction the portal is facing
                else {
                    int newX = getObstacleBlock(blockNew.getX(), blockNew.getY()).getConnection().getX();
                    int newY = getObstacleBlock(blockNew.getX(), blockNew.getY()).getConnection().getY();
                    blockNew.setX(newX);
                    blockNew.setY(newY);

                    direction = getObstacleBlock(blockNew.getX(), blockNew.getY()).getDirection(); //taking a portal will change the direction of movement in regard to the direction the portal is facing
                    
                    blockNew.moveInDirection(direction);
                    hasTakenPortal = true;
                    //If the block has taken a portal, then it should not be able to use itself as footing for further movement
                    
                    if (blockNew.getY() < gravityZone) {
                        if (!this.isGravityInverted) {
                            if (blockNew.getX() == blockOld.getX() && blockNew.getY()-1 == blockOld.getY()) {
                                hasFooting = false;
                            }
                        }
                        else {
                            if (blockNew.getX() == blockOld.getX() && blockNew.getY()+1 == blockOld.getY()) {
                                hasFooting = false;
                            }
                        }
                    } 
                }
            }
            if (getObstacleBlock(blockNew.getX(), blockNew.getY()) == null) {

            }
            // Block should not be moved if the given movement would place it at a wall
            else if (getObstacleBlock(blockNew.getX(), blockNew.getY()).isWall()) {
                System.out.println("Can not move further: you have hit a wall");
                return false;
            } 
        }

        // Block should not be moved if the given movement would place it out of bounds
        if (blockNew.getX() < 0 || blockNew.getX() >= width) {
            System.out.println("Out of bounds, cant move x direction");
            return false;
        }
        if (blockNew.getY() > 0 || blockNew.getY() <= -height) {
            System.out.println("Out of bounds, cant move y direction");
            return false;
        }
        System.out.println("----");
        System.out.println("Width=" + this.width + "Height=" + this.height + "x=" + block.getX() + "y=" + block.getY());
        System.out.println("----");

        // If the movement places this block at coordinates that are not already occupied by another block, then
        // move this block to the new coordinates
        if (getDirectedBlock(blockNew.getX(), blockNew.getY()) == null) {
            //Since the copy block was able to navigate to its coordinates without breaking any rules, then 
            //the coordinates should be legal for the original too
            block.setX(blockNew.getX());
            block.setY(blockNew.getY());
            block.setDirection(blockNew.getDirection());

            //if the moved block was a rock or player, then update its state to reflect wether or not it 
            //is placed ontop of a plate block
            if (block.isRock() || block.isPlayer()) {
                System.out.println("Width=" + this.width + "Height=" + this.height + "x=" + block.getX() + "y=" + block.getY());
                block.setState(getTraversableBlock(block.getX(), block.getY()).isPlate());
            }

            // if (hasFooting) {
            //     updateBaggage(blockOld, direction);
            // }

            //When a block takes a portal, they disappear, thus the baggage loses its carrier, meaning they are left to fall
            if (!hasTakenPortal) {
                updateBaggage(blockOld, direction);
            }

            return true;
        }
        
        // If the movement places this block at coordinates that are already occupied by another block, and that block has collision,
        // then try to first move that block, and if successful move this block afterwards.

        if (getMoveableBlock(blockNew.getX(), blockNew.getY()) != null && strength > 0) {

            if (! getMoveableBlock(blockNew.getX(), blockNew.getY()).hasCollision()) {
                //Since the copy block was able to navigate to its coordinates without breaking any rules, then 
                //the coordinates should be legal for the original too
                block.setX(blockNew.getX());
                block.setY(blockNew.getY());

                if (block.isRock() || block.isPlayer()) {
                    block.setState(getTraversableBlock(block.getX(), block.getY()).isPlate());
                }
            }

            if (pushMoveable(getMoveableBlock(blockNew.getX(), blockNew.getY()), direction, strength, hasTakenPortal) == true) {
                //Since the copy block was able to navigate to its coordinates without breaking any rules, then 
                //the coordinates should be legal for the original too
                block.setX(blockNew.getX());
                block.setY(blockNew.getY());

                //if the moved block was a rock or player, then update its state to reflect wether or not it 
                //is placed ontop of a plate block
                if (block.isRock() || block.isPlayer()) {
                    block.setState(getTraversableBlock(block.getX(), block.getY()).isPlate());
                }

                System.out.println("Baggage update:" + block.getX());
                updateBaggage(blockOld, direction);
                System.out.println("Baggage update:" + block.getX());
                return true;
            }
            // do not move this block if the block ahead could not be moved
            return false;
        }
    
        System.out.println("Can not move, block occupied.");
        return false;
    }

    //Where gravity applies, a block stacked ontop of another block should attempt to match the movement of the block below. 
    // Thus check if the given block has "baggage", and if they do: issue them to follow the movement of the original block
    private void updateBaggage(MoveableBlock movedBlock, String direction) {
        //Blocks that are stacked ontop of eachother will already fall together due to gravity, they will also make it too heavy to jump for the carrier
        if (direction == "up" || direction == "down") {
            return;
        }

        if (movedBlock.getY() < gravityZone) {
            if (! isGravityInverted) {
                if (getMoveableBlock(movedBlock.getX(), movedBlock.getY() + 1) != null) {
                    moveBlockTwoStrength(getMoveableBlock(movedBlock.getX(), movedBlock.getY() + 1), direction, 0);
                }
                
            }
            else {
                if (getMoveableBlock(movedBlock.getX(), movedBlock.getY() - 1) != null) {
                    moveBlockTwoStrength(getMoveableBlock(movedBlock.getX(), movedBlock.getY() - 1), direction, 0);
                }
            }
        }
    }



    public String toGameToSaveFormat() {
        System.out.println("Save format start.");
        String levelLayoutSave = "";
        String directionLayoutSave = ">";

        for (int y = 0; y > height*(-1); y--) {
            for (int x = 0; x < width; x++) {
                char typeSave = '?';

                
                // if (getMoveableBlock(x, y) != null ) {
                //     typeSave = this.getMoveableBlock(x, y).getType();
                //     //All moveable blocks have a specified direction.
                //     directionLayoutSave += this.getObstacleBlock(x, y).getDirection();
                // }
                // else if ( getObstacleBlock(x, y) != null ) {
                //     typeSave = this.getObstacleBlock(x, y).getType();
                //     //Portals are the only obstacle block type with a specified direction.
                //     if (this.getObstacleBlock(x, y).isPortal()) {
                //         directionLayoutSave += this.getObstacleBlock(x, y).getDirection();
                //     }
                // }

                //At most one directed block can occupy a given coordinate in the level, and this block must have a type, 
                // thus let this block's type represent this coordinate in the level layout string.
                if (getDirectedBlock(x, y) != null) {
                    typeSave = this.getDirectedBlock(x, y).getType();
                    //If the directed block is a player, rock or portal, then it must have a specified direction. 
                    if (typeSave == 'p' || typeSave == 'r' || typeSave == 'v' || typeSave == 'u') {
                        directionLayoutSave += this.getDirectedBlock(x, y).getDirection().charAt(0);
                    }
                }
                //Exactly one traversable block will occupy every coordinate in the level, thus when there are no directed blocks
                // placed ontop of it, the traversable block must itself represent the given coordinate in the level layout string.
                else {
                    typeSave = this.getTraversableBlock(x, y).getType();
                }
                //At coordinates where the underlying traversable block's birdView is enabled, the type representation to be
                // saved should be set to upper case.
                if (this.getTraversableBlock(x, y).isBirdView()) {
                    typeSave = Character.toUpperCase(typeSave);
                    //Since the type representation for traversable blocks is ' ', which can't be cahnged to uppercase, then 
                    // change it to '-' instead.
                    if (typeSave == ' ') {
                        typeSave = '-';
                    }
                }
                if(typeSave == '?') {
                    throw new IllegalArgumentException("There must be at least one block occupying the (" + x + ", " + y + ") coordinates, and they must have a type in order to be saved.");
                }
                levelLayoutSave += typeSave;
            }
            levelLayoutSave += "\n";
        } 
        //At the very end of the direction layout string the letter 'g' should be added, lower case indicates that gravity was not inverted when the game was saved, and
        // uppercase indicates that it was.
        if (!this.isGravityInverted) {
            directionLayoutSave += 'g';
        }
        else {
            directionLayoutSave += 'G';
        }
        System.out.println("Save format end");
        return levelLayoutSave + directionLayoutSave;
    }

    public String toString() {
        System.out.println("MAP TO STRING START");
        String pushRocksString = new String();
        for (int y = 0; y > height*(-1); y--) {
            for (int x = 0; x < width; x++) {
                if (getMoveableBlock(x, y) != null) {
                    pushRocksString += this.getMoveableBlock(x, y).toString();
                }
                else if (getObstacleBlock(x, y) != null) {
                    pushRocksString += this.getObstacleBlock(x, y).toString();
                }
                else {
                    pushRocksString += this.getTraversableBlock(x, y).toString();
                }
            }
            pushRocksString += "\n";
        } 
        System.out.println("MAP TO STRING SUCCESSFUL");
        return pushRocksString;
    }

    public String prettyString() {
        String prettyString = new String();
        prettyString += "Score:" + this.getScore() + " isGameOver?" + this.isGameOver() + "\n";
        String originalString = this.toString();
        for (int i = 0; i < originalString.length(); i++) {
            if (i==0) {
                for (int j = 0; j < traversableBlocks.length + 1; j++) {
                    if (j==0) {
                        prettyString += "X";
                    }
                    else {
                        prettyString += " " + (j-1) % 10;
                    }
                }
                prettyString += "\n";
            }
            if (  i % (width+1) == 0) {
                prettyString += (i/(this.width + 1)) % 10 + "";
            }
            prettyString += " " + originalString.charAt(i);
        }
        return prettyString;
    }
    

    // //Constructor
    // public PushRocks(int width, int height) {
    //     this.world = new Block[height][width];
    //     this.width = width;
    //     this.height = height;
    //     for (int y = 0; y > height*(-1); y--) {
    //         for (int x = 0; x < width; x++) {
    //             if (x == 0 || x == width - 1 || y == 0 || y == -height + 1) {
    //                 this.world[-y][x] = new Block(x, y, 'w');
    //             }
    //             else {
    //                 this.world[-y][x] = new Block(x, y);
    //             }
    //         }
    //     }
    //     this.world[4][4] = new Block(4, -4, 'd');
    //     this.world[3][7] = new Block(7, -3, 'd');
    //     this.world[3][8] = new Block(8, -3, 'd');
    // }

    // //Constructor2 (with terrain input)
    // public PushRocks(String terrainString, int width, int height) {
    //     if (terrainString.length() != width*height) {
    //         System.out.println(terrainString.length());
    //         throw new IllegalArgumentException("Given terrain string does not match the given width and height");
    //     }
    //     this.width = width;
    //     this.height = height;
    //     this.world = new Block[height][width];
    //     for (int y = 0; y < height; y++) {
    //         for (int x = 0; x < width; x++) {
    //             char type = terrainString.charAt(y*width + x);
    //             System.out.println("x"+"y"+"i"+type);
    //             Block block = new Block(x, (y*(-1)), type);
    //             if (! block.isMoveable()) {
    //                 this.world[y][x] = block;
    //             }
    //             else {
    //                 Block terrainBlock = new Block(x, (y*(-1)));
    //                 this.world[y][x] = terrainBlock;
    //                 addMoveableBlock(block);
    //             }
    //         }
    //     }
    // }


        //Constructor3 with ONLY terrain input
        public PushRocks(String levelLayout, String directionLayout) {
            System.out.println("Constructor 3 was used to create this PushRocks instance.");
            this.levelLayout = levelLayout;
            this.directionLayout = directionLayout;
            this.buildWorld();
        }

        public void buildWorld() {
            this.moveableBlocks.clear();
            this.teleporters.clear();
            this.portals.clear();
            this.isGravityInverted = false;
            this.gravityZone= -9;

            String layoutSingleLine = this.levelLayout.replace("\n", "");
            this.width = levelLayout.indexOf("\n");
            System.out.println(this.width);
            this.height = layoutSingleLine.length() / this.width;
            System.out.println(this.height);

            String[] directions = new String[this.directionLayout.length()];
            
            for (int i = 0; i < directionLayout.length(); i++) {
                String direction = directionLayout.substring(i, i+1);
                if (!"udlr".contains(direction)) {
                    throw new IllegalArgumentException("Direction layout only supports a character representation of the following directions: up 'u', down 'd', left 'l', right 'r'.");
                }
                switch (direction) {
                    case "u":
                        direction = "up";
                        break;
                    case "d":
                        direction = "down";
                        break;
                    case "l":
                        direction = "left";
                        break;
                    case "r":
                        direction = "right";
                        break;
                }
                directions[i] = direction;
                
            }
            int directionsRemaining = directions.length;

            this.obstacleBlocks = new ObstacleBlock[height][width];
            this.traversableBlocks = new TraversableBlock[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char tangibleType = layoutSingleLine.charAt(y*width + x);
                    boolean birdView = true; //NEED TO BE ADDED IN THE LEVELLAYOUT/SAVE/LOADFILES
                    int playerCount = 0;
                    int portalOneCount = 0;
                    int portalTwoCount = 0;

                    //Upper-case character or the symbol '-' indicate that the bird's eye view should not be active
                    //for the blocks at this coordinate
                    if (Character.isUpperCase(tangibleType) || tangibleType == '-') {
                        birdView = false;
                    }
                    tangibleType = Character.toLowerCase(tangibleType);
                    if(tangibleType == '-') {
                        tangibleType = ' ';
                    }
                    if (!"prwtuvd ".contains(tangibleType+"")) {
                        throw new IllegalArgumentException("PushRocks only allows for level layout formats containing the following set of characters: 'prwtuvd '");
                    }
                    else if (tangibleType == 'p') {
                        playerCount++;
                        if (playerCount > 1) {
                            throw new IllegalArgumentException("There can not be more than a single player.");
                        }
                    }
                    else if (tangibleType == 'v') {
                        portalOneCount++;
                        if (portalOneCount > 1) {
                            throw new IllegalArgumentException("There can not be more than a single portal-one.");
                        }
                    }
                    else if (tangibleType == 'u') {
                        portalTwoCount++;
                        if (portalTwoCount > 1) {
                            throw new IllegalArgumentException("There can not be more than a single portal-two.");
                        }
                    }
     
                    // TraversableBlock traversableBlock = new TraversableBlock(x, (y*(-1)), traversableType, birdView);
                    // this.traversableBlocks[y][x] = traversableBlock;
                    TraversableBlock traversableBlock = null;
                    if (tangibleType == 'd') { //d indicates that a plate should be made
                        traversableBlock = new TraversableBlock(x, -y, tangibleType, birdView);
                    }
                    else { 
                        traversableBlock = new TraversableBlock(x, -y, ' ', birdView);
                    }
                    this.traversableBlocks[y][x] = traversableBlock;

                    String direction = null;
                    if ("pruv".contains(tangibleType+"")) {
                        if (directionsRemaining <= 0) {
                            throw new IllegalArgumentException("The direction layout can not contain less directions than the combined amount of players, rocks and portals.");
                        }
                        direction = directions[directions.length - directionsRemaining];
                        directionsRemaining--;
                    }

                    if ("pr".contains(tangibleType+"")) {
                        MoveableBlock moveableBlock = new MoveableBlock(x, -y, tangibleType, direction);
                        System.out.println("x=" + x + "y=" + -y);
                        addMoveableBlock(moveableBlock);
                    }
                    else if ("wtuv".contains(tangibleType+"")) {
                        ObstacleBlock connection = null;
                        //If the obstacle to be created is a portal, then this portal should have a connection to the portal 
                        //opposite of itself if it exists.
                        if (tangibleType == 'v') {
                            connection = this.getPortal(false);
                        }
                        if (tangibleType == 'u') {
                            connection = this.getPortal(true);
                        }
                        ObstacleBlock obstacleBlock = new ObstacleBlock(x, -y, tangibleType, direction, connection);
                        // this.obstacleBlocks[y][x] = obstacleBlock;


                        addObstacleBlock(obstacleBlock);
                    }
                }
            }
            if (directionsRemaining > 0) {
                throw new IllegalArgumentException("The direction layout can not contain more directions than combined amount of players, rocks and portals.");
            }
        }

        private char blockCategory(char type) {
            if ("pr".contains(type+"")) {
                return 'm';
            }
            if ("wtuv".contains(type+"")) {
                return 'o';
            }
            return 'x';
        }


    // public String loadFromFile() throws IOException {
    //     return new String(Files.readAllBytes(Paths.get("src/main/resources/stephenking.txt")));
    // }
    

    


    public static void main(String[] args) {
        // String string = """
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
        // System.out.println(string);
        // System.out.println(string.replace("\n", ""));

        // String string2 = """
        // wwwwwwwwww
        // wpfwwffffw
        // wfffrfrffw
        // wffwwwwwfw
        // wfffrfffdw
        // wfwwwwfwfw
        // wfwffwfwdw
        // wfwfrwfwfw
        // wfffffdfdw
        // wwwwwwwwww""";

        // String string2 = """
        // wwwwwwwwwwwwwwwwwww
        // wp w     w        w
        // w  w r   w        w
        // w  wwww ww        w
        // w   r    w        w
        // w      d w      d w
        // w        w        w
        // w t  d d w t  d d w
        // w        w        w
        // wwwwwwwwwwwwwwwwwww
        // w        w        w
        // w        w        w
        // w        w        w
        // w        w        w
        // w        w        w
        // w        w        w
        // w t      w t      w
        // w        w        w
        // wwwwwwwwwwwwwwwwwww""";

        // String string2 = """
        // wwwwwwwwwwwwwwwwwww
        // w  wp    w        w
        // w  w r   w  r     w
        // w  wwww ww        w
        // w   r    w        w
        // w      d w        w
        // w        w        w
        // w t  d d w  t     w
        // w        w        w
        // wwwwwwwwwwwwwwwwwww
        // w        w        w
        // w        w   r  d w
        // w        www    www
        // w        w   r  d w
        // w        w        w
        // w        w ww     w
        // w t      w t      w
        // w        w        w
        // wwwwwwwwwwwwwwwwwww""";
        String string2 = """
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
        W--------W--------W
        W--------W---R--D-W
        W--------WWW----WWW
        W--------W---R--D-W
        W--------W--------W
        W--------W-WW-----W
        W-T------W-T------W
        W--------W--------W
        WWWWWWWWWWWWWWWWWWW""";
        String string2Directions = "rrrrrr";

        System.out.println(string2.indexOf("\n"));
        System.out.println(string2.indexOf("2"));
        System.out.println(string2.replace("\n", "").length() / string2.indexOf("\n"));

        PushRocks game0 = new PushRocks(string2, string2Directions);
        System.out.println(game0.prettyString());

        // game0.resetStationaryBlock(3, -1);
        System.out.println(game0);
        System.out.println(game0.prettyString());

        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());

        System.out.println(game0.getObstacleBlock(2, -7).isTeleporter());
        System.out.println(game0.getObstacleBlock(2, -16).isTeleporter());

        System.out.println(game0.getObstacleBlock(2, -7).getState());
        System.out.println(game0.getObstacleBlock(2, -16).getState());
        
        game0.getObstacleBlock(2, -7).setConnection(game0.getObstacleBlock(2, -16));

        System.out.println(game0.getObstacleBlock(2, -7).getState());
        System.out.println(game0.getObstacleBlock(2, -16).getState());

        System.out.println(game0.prettyString());

        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());

        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "down");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "up");
        System.out.println(game0.prettyString());
        game0.movePlayer(1, "right");
        System.out.println(game0.prettyString());
        game0.placePortal(true, game0.getPlayer(1));
        game0.movePlayer(1, "left");
        System.out.println(game0.prettyString());
        game0.placePortal(false, game0.getPlayer(1));
        System.out.println(game0.prettyString());
        
 
        System.out.println(game0.moveableBlocks.size());
        System.out.println(game0.getMoveableBlock(4, 1));
        System.out.println(game0.getPlayer(1).getX());
        System.out.println(game0.getPlayer(1).getY());

        // BlockAbstract block1 = new ObstacleBlock(0, 0, 'w', null, null);
        // ObstacleBlock block2 = new ObstacleBlock(0, 0, 'w', null, null);
        // // block = game0.getDirectedBlock(1, 1);
        
        // BlockAbstract mBlock = new MoveableBlock(0, 0, 'p', "right");
        // ((ObstacleBlock) block2).clearPortal();
    }   
    
}
