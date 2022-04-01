package pushrocks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PushRocks implements IObservablePushRocks {

    private int width;
    private int height;
    private String levelLayout;
    private String directionLayout; //Contains 

    private TraversableBlock[][] traversableBlocks;                                         //Blocks that make out the surface on which other blocks can move through/be placed ontop of
    private ObstacleBlock[][] obstacleBlocks;                                               //Blocks that are placed ontop of the traversable plane, which can hinder or redirect movement of moveable blocks
    private ArrayList<MoveableBlock> moveableBlocks = new ArrayList<MoveableBlock>();       //Blocks that are free to move around on the traversable plane, but are restricted by placements of directed blocks, which
                                                                                            //  includes both obstacle and other moveable blocks.

    private ArrayList<ObstacleBlock> teleporters = new ArrayList<ObstacleBlock>();
    private ArrayList<ObstacleBlock> portals = new ArrayList<ObstacleBlock>();

    private int score;
    private boolean isGravityInverted;

    private ArrayList<IObserverPushRocks> observers = new ArrayList<>();

    private boolean isGravityOnInterval;

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    private String getGravityDirection() {
        if (!this.isGravityInverted) {
            return "down";
        }
        else {
            return "up";
        }
    }
    private int getGravityDirectionInt() {
        if (!this.isGravityInverted) {
            return -1;
        }
        else {
            return 1;
        }
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

    //Returns any and all blocks that are placed at the given coordinates
    public List<BlockAbstract> getAllBlocks(int x, int y) {
        List<BlockAbstract> allBlocks = new ArrayList<BlockAbstract>();
        if (this.getDirectedBlock(x, y) != null) {          //There can only be a single directed block at any given coordinate
            allBlocks.add(this.getDirectedBlock(x, y));
        }
        if (this.getTraversableBlock(x, y) != null) {       //There can only be a single traversable block at any given coordinate
            allBlocks.add(getTraversableBlock(x, y));
        }
        return allBlocks;                                   //Together they will make out all blocks placed at the given coordinate
    }

    //Returns the top-most block at the given coordinate. There can be at most one directed block and one traversable block at any
    //given coordinate, of these two a directed block will be considered to be placed over the traversable block. 
    public BlockAbstract getTopBlock(int x, int y) {
        //We create a new list that will contain every block at the given coordinate
        List<BlockAbstract> allBlocks = this.getAllBlocks(x, y);
        //If this list size is less than one, then there must be no blocks at the given coordinate, thus we return null
        if (allBlocks.size() < 1) {
            return null;
        }
        //Otherwise there must be one or more blocks in the list, of these the first one will be the top-most block, thus we return that block.
        return this.getAllBlocks(x, y).get(0);
    }

    //Updates the score according to the current state of the game. The score increments by one for each movable block that is placed ontop
    //of a pressure plate.
    public void updateScore() {
        int scoreOld = this.score;
        this.score = 0;
        for (MoveableBlock block : moveableBlocks) {
            if (block.getState()) { //An active moveable block indicates that they are placed ontop of a pressure plate.
                this.score++;
            }
        }
        //Teleporters change their connection based on how many pressure plates have weight on them, thus these will need to be updated if the score changed.
        if (this.score == scoreOld) {
            this.updateTeleporters();
        }
    }

    //Returns the current score
    public int getScore() {
        this.updateScore();
        return this.score;
    }

    //Updates the teleporter connections according to the current score, and thus activates/deactivates them depending on wether they are connected or not
    private void updateTeleporters() {
        if (this.teleporters.size() < 2) {
            System.out.println("There are not enough teleporters, thus there is nothing to update."); //COULD THROW EXCEPTION MAYBE? !!!!!!!!!!!
            return;
        }
        System.out.println("Updating Teleporters. Score:" + this.score);
        //Connects two teleporters together based on the game's current score. A previous connection is removed once a new one is made.
        //Where there are n teleporters, and s is the score, the two teleporters that will connected are:
        //Teleporter one: index = s / n
        //Teleporter two: index = s % n
        this.teleporters.get(this.score / this.teleporters.size()).setConnection(this.teleporters.get(this.score % this.teleporters.size()));
    }

    //Attempts to place a portal (number 1 or 2), at the next wall in the direction the player aiming/looking. 
    //We assume that the player is able to aim the portal through other moveable blocks such as rocks. 
    //They can however aim at or past a teleporter. Aiming through other active teleporters and portals is also 
    //not supported.
    public boolean placePortal(boolean inputIsPortalOne, MoveableBlock player) {
        if (! player.isPlayer()) {
            return false; //only a player can place a portal
        }
        int x = player.getX();
        int y = player.getY();
        String direction = player.getDirection();

        //The direction the portal is facing will be oppsite to the direction the player was looking when they created it.
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
        //We now search for a suitable wall to place the portal on, searching in the direction the player was looking
        ObstacleBlock wall = this.findObstacle(direction, x, y);

        //If such a wall was found, then a portal could potentially be placed there.
        if (wall != null) {
            //Walls that serve as teleporters are not suitable for portal-placement
            if (wall.isTeleporter()) {
                return false;
            }
            //if the found wall already holds a portal, and that portal is the same portal as the one to be placed,
            //and it faces the same direction as the new one would, then everything is already as it should, no portals need to 
            //be changed
            if (wall.isPortal() && wall.isPortalOne() == inputIsPortalOne && wall.getDirection() == portalDirection) {
                updateScore();
                return true; //The portal is placed correctly
            }
            //If the wall is still a portal, then it could be the portal other than the one being created, and should in that case be overwritten
            if (wall.isPortal() && wall.isPortalOne() != inputIsPortalOne) {
                this.removePortal(wall);
                // this.removePortal(this.getPortal(inputIsPortalOne));
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

    //Searches for the obstacle block in the given direction that is closest to the given coordinates
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
        //If an obstacle block is found, then return that obstacle block
        else if (getObstacleBlock(newX, newY) != null) {
            return getObstacleBlock(newX, newY);
        }
        //Otherwise the search is repeated
        else {
            return findObstacle(direction, newX, newY);
        }
    }

    //Returns true if the game is over. The game is over once there is weight placed
    //ontop of every pressure plate, since the score equals the amount of pressure plates
    //that are pressed down, it follows that the game is over once the score equals
    //the total amount of pressure plates.
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

    // //// !!!!!!!!!!!!! unifying could be good, but that would require the traversableBlocks and obstacleBlocks to be merged
    ///////////////////// ideally I think using just one BlockAbstract[][] list would be good
    ///////////////////// maybe at least merge obstacleBlocks with moveableBlocks into a single directedBlocks list?
    // public BlockAbstract getBlock(int x, int y) {
    //     if(x < 0 || x >= this.width) {
    //         System.out.println("x:" + x + " is out of bounds when the width of the map is " + this.width);
    //         return null;
    //     }
    //     if(-y < 0 || -y >= this.height) {
    //         System.out.println("y:" + y + " is out of bounds when the height of the map is " + this.height);
    //         return null;
    //     }
    //     return block[-y][x];
    // }

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

    private List<BlockAbstract> getBlockTypeChain(BlockAbstract startBlock, int xDirection, int yDirection) {
        //A block is considered to be a part of the block chain if, in a search starting from the given start block
        //and moving in the direction made out of the sum of the given x and y directions, that block is the next found
        //block in the search and also shares the same class type as the start-block.
        int x = startBlock.getX();
        int y = startBlock.getY();
        //The chain will be represented by a list containing all blocks that are a part of the chain
        List<BlockAbstract> chain = new ArrayList<BlockAbstract>();
        //The start block makes out the first entry
        chain.add(startBlock);

        //The search for blocks will continue until the next found block does not meet the criteria, which would end the chain.
        boolean isSearchDone = false;
        while (!isSearchDone) {
            //Determine the coordinates for block that is next in the search
            x += xDirection;
            y += yDirection;
            //Obtain the block representing those coord
            BlockAbstract nextBlock = this.getTopBlock(x, y);
            //If  be of the same type as the start block, then it will be part of the block chain
            if (nextBlock != null && nextBlock.getClass() == startBlock.getClass()) {
                chain.add(nextBlock);
            }
            //Otherwise it must not be of the same type, which means the chain has ended, and thus the search for more blocks ends
            else {
                isSearchDone = true;
            }
        }
        return chain;
    }

    //Returns a list of moveable blocks that are stacked ontop of the given block
    private List<MoveableBlock> getBlockStack(MoveableBlock moveableBlock) {
        int yDirection = 0;
        //A block is considered to be stacked ontop of another if it is positioned in such a way that
        //gravity would push it down on that other block. As such the search for these blocks must start 
        //from the position of the given block then move in the direction opposite to the gravitational pull.
        if (!isGravityInverted) {
            yDirection++;
        } else {
            yDirection--;
        }
        List<MoveableBlock> chainMoveableBlocks= new ArrayList<MoveableBlock>();
        this.getBlockTypeChain(moveableBlock, 0, yDirection).stream()
            .forEach(a -> chainMoveableBlocks.add((MoveableBlock) a));
        return chainMoveableBlocks;
    }

    public List<List<BlockAbstract>> getGravityFallOrder() {
        //If the footing block of a given moveable block is different depending on wether or not potential traversal through a 
        //transporter is accounted for, then that means that the given block must be placed at an active transporter's
        //entry point which is also above (in relation to gravity) the transporter itself. All such moveable blocks
        //are collected into the list created below.
        List<BlockAbstract> blocksWithTransporterFooting = this.moveableBlocks.stream()
            .filter(a -> this.getFootingBlock(a, false) != this.getFootingBlock(a, true))           
            .collect(Collectors.toList());
        
        //Create a list containing lists where each contained list holds one of these blocks and their corresponding transporter footing
        List<List<BlockAbstract>> fallOrderConstruction = new ArrayList<List<BlockAbstract>>();
        for (BlockAbstract blockWithTransporterFooting : blocksWithTransporterFooting) {
            List<BlockAbstract> blockChain = new ArrayList<BlockAbstract>();
            //Add block with transporter as footing to the front of the list
            blockChain.add(blockWithTransporterFooting);
            //Then add the transporter that serves as the block's footing, unless it was previously added to one of lists in fallOrderConstruction as an exit porter
            ObstacleBlock entryTransporter = (ObstacleBlock) getFootingBlock((MoveableBlock) blockWithTransporterFooting, false);
            if(fallOrderConstruction.stream().filter(a -> a.get(2) == entryTransporter).count() != 0) {
                continue; //Continues to the next loop, since the given entry transporter is already represented
            }
            blockChain.add(entryTransporter);
            //Then add that transporter's connected transporter to the end of the list
            blockChain.add(((ObstacleBlock) blockChain.get(1)).getConnection());
            //then place this list into the list that is to contain the complete fall order 
            fallOrderConstruction.add(blockChain);
        }
        //There could be cases where other moveable blocks are stacked ontop of the block whose footing was a transporter, thus
        //we find these stacks and place them into the same list that contains that block
        
        //For every sub-list in the fall-order, that sub-list will be expanded into a complete list containing the full sequence/chain of
        //moveable blocks falling into and out of the two connected porters as a result of gravity. The porters will themselves be included 
        //in this list as they are what binds the entry and exit chains together.

        List<List<BlockAbstract>> fallOrderComplete = new ArrayList<List<BlockAbstract>>();
        for (List<BlockAbstract> blockChain : fallOrderConstruction) {
            ObstacleBlock entryPorter = (ObstacleBlock) blockChain.get(1);
            ObstacleBlock exitPorter = (ObstacleBlock) blockChain.get(2);
            MoveableBlock blockAtEntry = (MoveableBlock) blockChain.get(0);
            int[] blockAtExitXY = entryPorter.getExitPoint(blockAtEntry);
            BlockAbstract blockAtExit = this.getFootingBlock(blockAtEntry, true);

            int entryDirectionX = 0;
            int entryDirectionY = 0;
            int exitDirectionX = 0;
            int exitDirectionY = 0;
            //If the entry porter was a portal, then it follows that the exit porter is also a portal.
            //The blocks entering the entry portal will fall in the direction of gravity, the entry portal faces in the direction opposite to gravity .
            //The blocks leaving the exit portal will fall out in the direction that the exit portal is facing.
            if (entryPorter.isPortal()) {
                entryDirectionX = 0;
                entryDirectionY = (-1)*this.getGravityDirectionInt();
                exitDirectionX = exitPorter.getDirectionInt()[0];
                exitDirectionY = exitPorter.getDirectionInt()[1];
            }
            //Otherwise the porter must have been a teleporter, which maintains an entering block's movement direction.
            //Since this has to do with gravity and falling objects, the blocks must be falling in the direction of gravity,
            //and thus entering the entry teleporter from above, and leaving the exit teleporter from below.
            //Thus the entry teleporter is directed opposite to gravity to face the used entry point, and the exit teleporter
            //is faced in the same direction as gravity for its used exit point.
            else {
                entryDirectionX = exitDirectionX = 0;
                entryDirectionY = (-1)*this.getGravityDirectionInt();
                exitDirectionY = this.getGravityDirectionInt();
            }
            //The entry chain is thus obtained and put into its own list
            List<BlockAbstract> entryChain = new ArrayList<BlockAbstract>();
            entryChain = getBlockTypeChain(blockAtEntry, entryDirectionX, entryDirectionY);
            
            //The process is thus repeated for the exit chain as long as the block at the exit was in fact a moveable
            //block, otherwise it has no place in a moveable block chain
            List<BlockAbstract> exitChain = new ArrayList<BlockAbstract>();
            if (blockAtExit instanceof MoveableBlock) {
                exitChain = getBlockTypeChain(blockAtExit, exitDirectionX, exitDirectionY);
            }

            //The two moveable block lists should thus put be together, bound together by the entry and exit porter, which forms the complete chain
            List<BlockAbstract> completeChain = new ArrayList<BlockAbstract>();

            int entryChainWeight;

            if (exitChain.size() >= 1) {
                //If the exit porter faces the opposite direction of gravity, then there will be blocks falling into both the entry and exit portal
                //at the same time, and thus colliding. In this case the direction the chain as a whole should move in will be determined by the amount
                //of moveable blocks falling on each side, whichever side is heavier will steer the movement. Should both sides be perfectly balanced, 
                //then the chain will not move at all.
                if  (exitPorter.getDirectionInt()[1] == -(this.getGravityDirectionInt()) ) {
                    //if the entry side is heavier than the exit side
                    if (entryChain.size() > exitChain.size()) {
                        completeChain.add(entryPorter);
                        Collections.reverse(entryChain);
                        completeChain.addAll(entryChain);
                        completeChain.add(exitPorter);
                        completeChain.addAll(exitChain);
                        fallOrderComplete.add(completeChain);
                    }
                    //if the exit side is heavier than the entry side
                    else if (entryChain.size() < exitChain.size()) {
                        completeChain.add(exitPorter);
                        Collections.reverse(exitChain);
                        completeChain.addAll(exitChain);
                        completeChain.add(entryPorter);
                        completeChain.addAll(entryChain);
                        fallOrderComplete.add(completeChain);
                    }
                    //Otherwise the sides are perfectly balanced, and so gravity will have no effect, thus there is no need to include any of those blocks
                    //in the gravity fall order.
                    else {
                        continue;
                    }

                }
                //If the exit direction is non-vertical, then the blocks on that side will not move into or out of the portal, but since they
                //outnumber the other side they will hinder the movement of that other side, thus the entry side are excluded from the gravity
                //fall order, wheras the heavier side remain, as they can still fall down from where they are currently standing.
                else if (exitDirectionX != 0) {
                    if (entryChain.size() < exitChain.size()) {
                        completeChain.add(exitPorter);
                        Collections.reverse(exitChain);
                        completeChain.addAll(exitChain);
                        fallOrderComplete.add(completeChain);
                    }
                }
            }
            

            //The two moveable block lists should thus put be together, bound together by the entry and exit porter, which forms the complete chain
            List<BlockAbstract> completeChain = new ArrayList<BlockAbstract>();
            //But first the entry chain list is reversed as to represent the order in which the blocks would fall into the entry teleporter.
            Collections.reverse(entryChain);

            completeChain.addAll(entryChain);
            completeChain.add(entryPorter);
            completeChain.add(exitPorter);
            completeChain.addAll(exitChain);

            fallOrderComplete.add(completeChain);
        }
        
        return fallOrderComplete;
    }

    public void gravityStep(boolean hasPlayerMoved) {
        //The first blocks that should fall down are the ones furthest down in the gravity's direction, thus
        //these blocks should be issued to move first.
        List<MoveableBlock> transportersAsFooting = this.moveableBlocks.stream()
        .filter(a -> this.getFootingBlock(a, false) instanceof ObstacleBlock)
        .filter(a -> ((ObstacleBlock) this.getFootingBlock(a, false)).isTransporter())
        .collect(Collectors.toList());
        System.out.println(transportersAsFooting);
        List<MoveableBlock> list = this.moveableBlocks.stream()
            .sorted( (a, b) -> (b.getX() - a.getX()))
            .sorted( (a, b) -> (b.getY() - a.getY()))
            .collect(Collectors.toList());
        this.moveableBlocks = new ArrayList<MoveableBlock>(list);
        //When gravity is inverted the blocks with the highest value for their Y coordinates should fall first
        if (this.isGravityInverted) {
            for (int i = 0; i < moveableBlocks.size(); i++) {
                if (moveableBlocks.get(i).isPlayer() && hasPlayerMoved) {
                }
                else if (!isInBirdView(moveableBlocks.get(i))) {
                    moveBlock(moveableBlocks.get(i), "up", 1, "gravity");
                }
                System.out.println(this.prettyString());
            }
        }

        //When gravity is not inverted the blocks with the lowest value for their Y coordinates should fall first
        else {
            for (int i = moveableBlocks.size() - 1; i >= 0; i--) {
                if (moveableBlocks.get(i).isPlayer() && hasPlayerMoved) {
                }
                else if (!isInBirdView(moveableBlocks.get(i))) {
                        moveBlock(moveableBlocks.get(i), "down", 1, "gravity");
                }
                System.out.println(this.prettyString());
                notifyObservers();
            }
        }
        this.isGameOver();
        this.notifyObservers();
    }

    private BlockAbstract getFootingBlock(MoveableBlock block, boolean isTransportationConsidered) {
        int footingBlockY = block.getY();
        //Block is not considered to be airborne when the view-angle is set to birdview, as that view-angle does not
        //support free-fall. When bird view is off, the traversable blocks are assumed to serve as a surface to walk on.
        //Wheras in birdview these blocks are assumed to be air to walk/fall through.
        //Thus a block placed where bird view is enabled will use the traversable block they are placed upon as their
        //footing block.
        if (isInBirdView(block)) {
            return getTraversableBlock(block.getX(), block.getY());
        }
        //If gravity is not inverted, then the footing block will have a y-coordinate one less than the given block
        if (!isGravityInverted) {
            footingBlockY--;
        }
        //Otherwise gravity must be inverted, and thus the footing block will have a y-coordinate one greater than the given block
        else {
            footingBlockY++;
        }
        //The footing block for the given block must be at the coordinates matching the given block's x-coordinate
        //and the now established footing y-coordinate. We thus set the footing block to be the top block at that coordinate.
        BlockAbstract footingBlock = this.getTopBlock(block.getX(), footingBlockY);

        //If this footing block is an obstacle block, then it could serve as footing
        if (footingBlock instanceof ObstacleBlock) {
            //Should this directed block be an active transporter, then the footing block will instead be located at the exit point of that active transporter.
            //Should the parameter "isTransportationConsidered" have been set to false, then the transporter itself will represent the footing.
            if ( ( ((ObstacleBlock) footingBlock).isTransporter() ) && footingBlock.getState() && isTransportationConsidered) {
                //Since the footing block was an active transporter and as long as the given block is also placed at one of the transporter's entry points,
                //then the footingblock should instead be set to the block placed at the exit point of that transporter. Otherwise the transporter will remain
                //as the footing block

                //We first retrieve the transporter:
                ObstacleBlock entryTransporter = this.getObstacleBlock(footingBlock.getX(), footingBlock.getY());
                //If the given moveable block can enter the transporter, then that block's footing should be set
                //to the block positioned at the transporter's exit point
                if (entryTransporter.canBlockEnter(block)) {
                    footingBlock = getTopBlock(entryTransporter.getExitPoint(block)[0], entryTransporter.getExitPoint(block)[1]);
                }
            }
        }
        //The footing block will at last be returned
        return footingBlock;

    }

    //Checks if the given block is placed at a corrdinate where bird-view is enabled
    private boolean isInBirdView(BlockAbstract block) {
        if (block== null) {
            System.out.println("what");
        }
        TraversableBlock traversableBlock = getTraversableBlock(block.getX(), block.getY());
        return traversableBlock.isBirdView();
    }

    public boolean isBlockAirborne(MoveableBlock block) {
        // int footingBlockY = block.getY();

        //Block is not considered to be airborne when the view-angle is set to birdview, as that view-angle does not
        //support free-fall. When bird view is off, the traversable blocks are assumed to serve as a surface to walk on.
        //Wheras in birdview these blocks are assumed to be air to walk/fall through.
        if (isInBirdView(block)) {
            return false;
        }
        //Find the given block's footing block, with potential transportation being accounted for.
        BlockAbstract footingBlock = getFootingBlock(block, true);

        //If this footing block is null, then there must not have been any blocks of any kind at the footing-coordinate, thus it follows
        //that the footing coordinates are out of bounds. The block must thus be standing in such a way that the borders of the map are
        //at its feet, the borders will thus serve as its footing, and since it then has footing it will not be considered to be airborne.
        if (footingBlock == null) { //Case: footing is out of bounds, meaning the borders are considered to have collision -> must not be airborne
            return false;
        }
        //Otherwise a block must exist, and if that block is not of the directed type, then it will not have collision, and as such
        //there will exist no footing for the given block, and as such it must be airborne.
        else if (footingBlock instanceof TraversableBlock) { //Case: footing is not a DirectedBlock (alternatively: block is a TraversableBlock) meaning it has no collision -> must be airborne
            return true;
        }
        //If the block is neither null nor traversable, then it follows that it must be a directed block, which could have collision to serve as footing.
        else { //Could have used "else" instead here, but left it like this in case further classes extending BlockAbstract are added.

            //A moveable block can not be its own footing. This could happen in cases where the original footing block was moveable 
            //and stood ontop of an active portal with an exit point above itself (in relation to current gravity direction).
            //If the footing block is the block itself, then the block has no footing and is thus considered airborne.
            if (footingBlock == block) {
                return true;
            }
            //Otherwise the footing block must have collision and be able to serve as footing, thus the block must not be airborne.
            return false;
        }
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
            else if (moveBlock(player, direction, 1, "player")) {
                this.isGameOver();
                wasMoved = true;
            }
        }
        //If gravity does not move forward on a set interval, then let it instead move forward once every time 
        //the player block was issued to move
        if (!this.isGravityOnInterval) {
            this.gravityStep(wasMoved);
        }
        System.out.println(this.prettyString());
        this.notifyObservers();
        return wasMoved;
    }

    public boolean pushMoveable(MoveableBlock block, String direction, int strength, boolean hasTakenPortal, String movementSource) { //CHECK THIS !!!!!!!!!!!!!! may work against intent
        //A block can not push another block in the same direction as the gravity affecting it
        if( (direction == "up" || direction == "down") && (direction == this.getGravityDirection()) && !isInBirdView(block) ) {
        // if ( ((direction == "down" && !this.isGravityInverted) || (direction == "up" && this.isGravityInverted)) && !isInBirdView(block) ) {
            if (!hasTakenPortal) {
                return false;
            }
        }
        //If the push is made by a player, then the strength of the pushing motion increases
        if (block.isPlayer()) {
            strength++;
        }
        //Otherwise it must have been made by a rock, which itself will cost strength to push forward, thus the strength of the pushing motion decreases
        else { 
            strength--;
        }
        //If the
        if (moveBlock(block, direction, strength, movementSource)) {
            System.out.println(this.prettyString());
            this.isGameOver();
            return true;
        }
        //Otherwise the block could not be pushed, thus false is returned
        else {
            return false;
        }
    }
    
    private boolean moveBlock(MoveableBlock block, String direction, int strength, String movementSource) { //!!!!!!!!!!!!!!!!!!!!!! SHOULD NOT BE PUBLIC REMEMBE TO SET TO PRIVATE AFTER TEST
        System.out.println(block.getX() + "," + block.getY() + " to move: " + direction + ".");
        boolean hasTakenPortal = false;
        String directionOriginal = direction;

        MoveableBlock blockOld = new MoveableBlock(block.getX(), block.getY(), block.getType(), block.getDirection());
        MoveableBlock blockNew = new MoveableBlock(block.getX(), block.getY(), block.getType(), block.getDirection());

        //Attempts to move the copy-block in the given direction, if the movement was not successfull, then it would not be successfull for the original block either.
        if (!blockNew.moveInDirection(direction)) {
            return false;
        }
        //Otherwise the movement must have been successfull. We thus check if the new coordinates for the copy block placed it at a location
        //already occupied by another obstacle block
        ObstacleBlock obstacleBlock = getObstacleBlock(blockNew.getX(), blockNew.getY());
        if (obstacleBlock != null) {
            //Since an obstacle block was found at the copy-block's new location, it could potentially be a transporter type:
            //Block should be teleported if the given movement would place it at a connected teleporter
            if (obstacleBlock.isTransporter()) {
                //To enter a transporter the block must be standing at one of the transporter's entry points and 
                //the transporter must be active (meaning it must be connected to another transporter)

                //If the block to be moved can not enter the transporter, then the transporter will instead be treated
                //as if it was a wall, thus hindering the movement of the block.
                if (!obstacleBlock.canBlockEnter(blockOld)) {
                    System.out.println("Can not move further: can only enter portal from its entry point");
                    return false;
                }
                //Otherwise the transporter must be connected, thus the moving block should be transported out of the connected transporter in
                //direction of the movement if possible
                else {
                    int[] transportExit = obstacleBlock.getExitPoint(blockOld);
                    blockNew.setX(transportExit[0]);
                    blockNew.setY(transportExit[1]);
                    if (obstacleBlock.isPortal()) {
                        blockNew.setDirection(obstacleBlock.getConnection().getDirection());
                    }
                    else {  
                        blockNew.setDirection(direction);
                    }
                }
            }
            //If the new coordinates for the copy-block are still empty of obstacle blocks, then do nothing
            obstacleBlock = getObstacleBlock(blockNew.getX(), blockNew.getY());

            //If there exists an obstacle block at the current coordinates of the moveable block copy, and that obstacle
            //block is a wall, then it would not be possible for the actual moveable block to be moved there.
            if (obstacleBlock != null && obstacleBlock.isWall()) {
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

        // If the movement places this block at coordinates that are not already occupied by another block, then
        // move this block to the new coordinates
        if (getDirectedBlock(blockNew.getX(), blockNew.getY()) == null) {
            //Since the copy block was able to navigate to its coordinates without breaking any rules, then 
            //the coordinates should be legal for the original too
            block.setX(blockNew.getX());
            block.setY(blockNew.getY());
            //If the block's movement was not caused by gravity, then set the direction of the moved block to that of the copy.
            if (movementSource != "gravity") {
                block.setDirection(blockNew.getDirection());
            }
            //if the moved block was a moveable block, then update its state to reflect wether or not it 
            //is placed ontop of a pressure plate
            if (block instanceof MoveableBlock) {
                System.out.println("Width=" + this.width + "Height=" + this.height + "x=" + block.getX() + "y=" + block.getY());
                block.setState(getTraversableBlock(block.getX(), block.getY()).isPlate());
            }
            //When a block has successfully entered a portal, they are relocated in a flash, thus the baggage loses its carrier, meaning they are left to fall      !!!!!!!!!!!!!!!IS THIS EVEN NEEDED ANYMORE?
            if (!hasTakenPortal) {
                updateBaggage(blockOld, direction);
            }
            return true;
        }
        
        // If the movement places this block at coordinates that are already occupied by another moveable block and that block has collision,
        // then try to first move that block, and if successful move this block afterwards.
        if (getMoveableBlock(blockNew.getX(), blockNew.getY()) != null && strength > 0) {
            if (pushMoveable(getMoveableBlock(blockNew.getX(), blockNew.getY()), blockNew.getDirection(), strength, hasTakenPortal, movementSource) == true) {
                //Since the copy-block was able to navigate to its coordinates without breaking any rules, then 
                //the coordinates should be legal for the original as well
                block.setX(blockNew.getX());
                block.setY(blockNew.getY());
                System.out.println(this.prettyString());

                //Then

                block.setState(getTraversableBlock(block.getX(), block.getY()).isPlate());
                updateBaggage(blockOld, directionOriginal);
                System.out.println(this.prettyString());
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
        //
        if (!isInBirdView(movedBlock)) {
            int yDirection = 0;
            if(!isGravityInverted) {
                yDirection++;
            }
            else {
                yDirection--;
            }

            MoveableBlock baggageBlock = getMoveableBlock(movedBlock.getX(), movedBlock.getY() + yDirection);
            if (baggageBlock != null) {
                moveBlock(baggageBlock, direction, 0, "baggage");
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
                //At coordinates where the underlying traversable block has bird view enabled, the type representation to be
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
        //At the very end of the direction layout string the letter 'g' should be added, lower case indicates that gravity was not inverted when the game was saved, wheras
        // uppercase indicates that it was inverted.
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
    


    //Constructor3 with ONLY terrain input
    public PushRocks(String levelLayout, String directionLayout) {
        System.out.println("Constructor 3 was used to create this PushRocks instance.");
        this.levelLayout = levelLayout;
        this.directionLayout = directionLayout;
        this.buildWorld();
        
        this.isGravityOnInterval = false;
        if (this.isGravityOnInterval == true) { //Should include something about this in the build/hasWon/pause/menu interactions
            GravityIncrementer gravityIncrementer = new GravityIncrementer(this, 1000);
            Thread thread = new Thread(gravityIncrementer);
            thread.start();
        }

    }

    public void buildWorld() {
        this.moveableBlocks.clear();
        this.teleporters.clear();
        this.portals.clear();
        this.isGravityInverted = false;

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

                //An upper-case character or the symbol '-' indicates that bird's eye view should not be active
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
    
                TraversableBlock traversableBlock = null;
                if (tangibleType == 'd') { //d indicates that a pressure plate should be made
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
                    addObstacleBlock(obstacleBlock);
                }
            }
        }
        if (directionsRemaining > 0) {
            throw new IllegalArgumentException("The direction layout can not contain more directions than combined amount of players, rocks and portals.");
        }
    }




    // public String loadFromFile() throws IOException {
    //     return new String(Files.readAllBytes(Paths.get("src/main/resources/stephenking.txt")));
    // }
    
    @Override
    public void addObserver(IObserverPushRocks observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    @Override
    public void removeObserver(IObserverPushRocks observer) {
        if (this.observers.contains(observer)) {
            this.observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        this.observers.forEach(observer -> observer.updateMap(this));
    } 
    
    


    public static void main(String[] args) {
   
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
