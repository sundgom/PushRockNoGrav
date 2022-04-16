package pushrocks.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PushRocks implements IObservablePushRocks, IObserverIntervalNotifier {

    private int width;
    private int height;
    private String levelName;
    private String levelMapLayout;
    private String levelDirectionLayout;

    private int activePressurePlatesCount;
    private int moveCount;
    private boolean isGravityInverted;
    private boolean isGameOver;

    private TraversableBlock[][] traversableBlocks;                                         //Blocks that make out the surface on which other blocks can move through/be placed ontop of
    private ObstacleBlock[][] obstacleBlocks;                                               //Blocks that are placed ontop of the traversable plane, which can hinder or redirect movement of moveable blocks
    private List<MoveableBlock> moveableBlocks = new ArrayList<MoveableBlock>();            //Blocks that are free to move around on the traversable plane, but are restricted by placements of directed blocks, which
                                                                                            // includes both obstacle and other moveable blocks.
    private List<TeleporterBlock> teleporters = new ArrayList<TeleporterBlock>();
    private List<PortalWallBlock> portals = new ArrayList<PortalWallBlock>();

    private List<IObserverPushRocks> observers = new ArrayList<>();

    private int gravityApplicationChoice;
    
    private IntervalNotifier intervalNotifier;
    private Thread intervalNotifierThread;
    private boolean ignoreIntervalNotifier;

    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public String getLevelName() {
        return this.levelName;
    }
    public String getLevelMapLayout() {
        return this.levelMapLayout;
    }
    public String getLevelDirectionLayout() {
        return this.levelDirectionLayout;
    }

    public boolean isGravityInverted() {
        return false;
    }

    public void setGravityApplicationManual() {
        this.gravityApplicationChoice = 1;
    }
    public boolean isGravityApplicationManual() {
        return gravityApplicationChoice > 0;
    }

    public boolean isGravityApplicationMoveInput() {
        return gravityApplicationChoice == 0;
    }
    public void setGravityApplicationMoveInput() {
        this.gravityApplicationChoice = 0;
    }

    //The below is an experimental feature. I do not have a good understanding of how threads work, but I enjoyed
    //the behaviour it enabled, so I have a hard time cutting it out of the code even though it is most definitely flawed.
    //It is entirely optional, and does not serve the code in any other way than to call the gravityStep() method once each interval.
    public void setGravityApplicationInterval() { 
        this.gravityApplicationChoice = -1;
        int interval = 1000; //this could have been used as a parameter instead.
        if (this.intervalNotifierThread == null) { 
            IntervalNotifier intervalNotifier = new IntervalNotifier(this, interval, true);
            Thread intervalNotifierThread = new Thread(intervalNotifier);
            //The thread is set to Daemon as to make sure to close the thread once the application window is closed.
            intervalNotifierThread.setDaemon(true);
            this.intervalNotifierThread = intervalNotifierThread;
            intervalNotifierThread.start();
        }
        this.ignoreIntervalNotifier = false;
    }
    public boolean isGravityApplicationInterval() {
        return gravityApplicationChoice < 0;
    }

    public void setGravityApplicationChoice(int n) {
        //manual: n > 0, moveInput: n == 0, interval: n < 0
        if (n < 0) {
            setGravityApplicationInterval();
        }
        this.gravityApplicationChoice = n;
    }

    private String getGravityDirection() {
        if (!this.isGravityInverted) {
            return "down";
        }
        else {
            return "up";
        }
    }
    public int getGravityDirectionY() { 
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

    public int getPressurePlateCount() {
        int plateCount = 0;
        for (int y = 0; y > -this.height; y--) {
            for (int x = 0; x < this.width; x++) {
                if (traversableBlocks[-y][x].isPressurePlate()) {
                    plateCount++;
                }
            }
        }
        return plateCount;
    }

    //Returns the given player block if it exists
    private MoveableBlock getPlayer() {
        for (MoveableBlock block : this.moveableBlocks) {
            if (block.isPlayer()) {
                return block;
            }
        }
        return null;
    }

    //Returns any and all blocks that are placed at the given coordinates
    private List<BlockAbstract> getAllBlocks(int x, int y) {
        List<BlockAbstract> allBlocks = new ArrayList<BlockAbstract>();
        if (this.getDirectedBlock(x, y) != null) {          //There can only be a single directed block at any given coordinate
            allBlocks.add(this.getDirectedBlock(x, y));
        }
        if (this.getTraversableBlock(x, y) != null) {       //There can only be a single traversable block at any given coordinate
            allBlocks.add(getTraversableBlock(x, y));
        }
        return allBlocks;                                   //Together they will make out all blocks placed at the given coordinate
    }

    //Returns only the top-most block at the given coordinate. There can be at most one directed block and one traversable block at any
    //given coordinate, of these two a directed block will be considered to be placed over the traversable block. 
    private BlockAbstract getTopBlock(int x, int y) {
        //We create a new list that will contain every block at the given coordinate
        List<BlockAbstract> allBlocks = this.getAllBlocks(x, y);
        //If this list size is less than one, then there must be no blocks at the given coordinate, thus we return null
        if (allBlocks.size() < 1) {
            return null;
        }
        //Otherwise there must be one or more blocks in the list, of these the first one will be the top-most block, thus we return that block.
        return this.getAllBlocks(x, y).get(0);
    }

    //Similar to the method given above, except a copy of the block is returned instead.
    public BlockAbstract getTopBlockCopy(int x, int y) {
        BlockAbstract block = this.getTopBlock(x, y);
        BlockAbstract blockCopy;
        if (block instanceof MoveableBlock) {
            blockCopy = new MoveableBlock(x, y, block.getType(), ((MoveableBlock) block).getDirection());
            blockCopy.setState(block.getState());
        }
        else if (block instanceof ObstacleBlock) {
            ObstacleBlock blockConnectionCopy = null; 
            if (((ObstacleBlock) block).isTransporter() && block.getState()) {
                ObstacleBlock blockConnection = ((ObstacleBlock) block).getConnection();
                if (blockConnection instanceof TeleporterBlock) {
                    blockConnectionCopy = new TeleporterBlock(blockConnection.getX(), blockConnection.getY(), null);
                    // blockConnectionCopy = new TeleporterBlock(blockConnection.getX(), blockConnection.getY(), blockConnection.getType(), ((ObstacleBlock) blockConnection).getDirection(), null);
                }
                else {
                    blockConnectionCopy = new PortalWallBlock(blockConnection.getX(), blockConnection.getY(), blockConnection.getType(), ((ObstacleBlock) blockConnection).getDirection(), null);
                }
            }
            if (block instanceof TeleporterBlock) {
                blockCopy = new TeleporterBlock(x, y, blockConnectionCopy);
                // blockCopy = new TeleporterBlock(x, y, block.getType(), ((ObstacleBlock) block).getDirection(), blockConnectionCopy);
            }
            else {
                blockCopy = new PortalWallBlock(x, y, block.getType(), ((ObstacleBlock) block).getDirection(), blockConnectionCopy);
            }
        }
        //Otherwise it must be a traversable block.
        else {
            blockCopy = new TraversableBlock(x, y, block.getType(), ((TraversableBlock) block).isBirdView());
            // blockCopy.setState(block.getState());
        }
        return blockCopy;
    }
    //Returns a copy of the traversable block at the given coordinate
    public TraversableBlock getTraversableBlockCopy(int x, int y) {
        TraversableBlock block = this.getTraversableBlock(x, y);
        return new TraversableBlock(x, y, block.getType(), block.isBirdView());
    }

    //Returns the current amount of activated pressure plates
    public int getActivePressurePlatesCount() {
        return this.activePressurePlatesCount;
    }
    public int getMoveCount() {
        return this.moveCount;
    }

    //Returns true if the game is over. The game is over once there is weight placed
    //ontop of every pressure plate, in other words: all pressure plates must be activated.
    private void checkGameOver() {
        this.updateActivePressurePlatesCount();
        if (this.getActivePressurePlatesCount() >= this.getPressurePlateCount()) {
            this.endGame();
        }
    }
    public boolean isGameOver() {
        return this.isGameOver;
    }
    private void endGame() {
        this.isGameOver = true;
        if (this.isGravityInverted) {
            System.out.println("Congratulations. You're upside down now." );
        }
        else {
            System.out.println("Congratulations, you managed to complete this absolutely meaningless test.");
        }
        System.out.println("Reset the game if you want to do it again.");
    }
    
    //Updates the activePressurePlatesCount according to the current state of the game. The activePressurePlatesCount increments by one for each movable block that is placed ontop
    //of a pressure plate.
    private void updateActivePressurePlatesCount() {
        int activePressurePlatesCountOld = this.activePressurePlatesCount;
        int activePressurePlatesCountNew = 0;
        for (MoveableBlock block : moveableBlocks) {
            if (block.getState()) { //An active moveable block indicates that they are placed ontop of a pressure plate.
                activePressurePlatesCountNew++;
            }
        }
        //Teleporters change their connection based on how many pressure plates have weight on them, thus these will need to be updated if the activePressurePlatesCount changed.
        if (activePressurePlatesCountNew != activePressurePlatesCountOld) {
            this.activePressurePlatesCount = activePressurePlatesCountNew;
            this.updateTeleporters();
        }
    }

    //Updates the teleporter connections according to the current activePressurePlatesCount, and thus activates/deactivates them depending on wether they are connected or not
    private void updateTeleporters() {
        if (this.teleporters.size() < 2) {
            System.out.println("There are not enough teleporters, thus there is nothing to update."); //COULD THROW EXCEPTION MAYBE? !!!!!!!!!!!
            return;
        }
        //Connects two teleporters together based on the game's current activePressurePlatesCount. A previous connection is removed once a new one is made.
        //Where there are n teleporters, and s is the activePressurePlatesCount, the two teleporters that will connect are:
        //Teleporter one: index = s / n
        //Teleporter two: index = s % n
        this.teleporters.get(this.activePressurePlatesCount / this.teleporters.size()).setConnection(this.teleporters.get(this.activePressurePlatesCount % this.teleporters.size()));
    }

    //Attempts to place a portal (number 1 or 2), at the next wall in the direction the player is aiming/looking. 
    //We assume that the player is able to aim the portal through other moveable blocks such as rocks. 
    //They can however aim at or past a teleporter. Aiming through other active teleporters and portals is also 
    //not supported.
    public void placePortal(boolean inputIsPortalOne) {
        //Can not place portals when the game is over.
        if (isGameOver) {
            throw new IllegalStateException("Portals can not be placed when the game is over.");
        }
        MoveableBlock player = this.getPlayer();
        if (player == null) {
            throw new IllegalStateException("Portals can not be placed when no player exists to place them."); 
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
        if (wall == null) {
            throw new IllegalStateException("Portals can not be placed out of bounds. Try aiming it at a wall instead.");
        }
        //If such a wall was found, then a portal could potentially be placed there.
        if (wall != null) {
            //Teleporters are not suitable for portal-placement
            if (wall instanceof TeleporterBlock) { 
                throw new IllegalStateException("Portals can not be placed at teleporters. Try aiming it at a wall instead.");
            }
            //if the found wall already holds a portal, and that portal is the same portal as the one to be placed,
            //and it faces the same direction as the new one would, then everything is already as it should, no portals need to 
            //be changed
            if (((PortalWallBlock) wall).isPortal() && ((PortalWallBlock) wall).isPortalOne() == inputIsPortalOne && wall.getDirection() == portalDirection) {
                System.out.println(this.prettyString());
                return; //The portal is placed correctly
            }
            //If the wall is still a portal, then it could be the portal other than the one being created, and should in that case be overwritten
            if (((PortalWallBlock) wall).isPortal() && ((PortalWallBlock) wall).isPortalOne() != inputIsPortalOne) {
                this.removePortal(((PortalWallBlock) wall));
            }
            //first remove any existing portal with a type matching the one to be placed, 
            // then place the new portal at the given wall.
            this.removePortal(this.getPortal(inputIsPortalOne));
            ((PortalWallBlock) wall).setPortal(inputIsPortalOne, portalDirection, this.getPortal(!inputIsPortalOne));
            this.addPortal((PortalWallBlock) wall);
            
            System.out.println(this.prettyString());
            this.notifyObservers(); //Successfull portal placement
            return; //Placement complete.
        }
    }

    private boolean addPortal(PortalWallBlock portal) {
        if (portal == null) {
            return false;
        }
        this.portals.add(portal);
        return true;
    }

    private boolean removePortal(PortalWallBlock oldPortal) {
        if (oldPortal == null) {
            return false;
        }
        oldPortal.clearPortal();
        portals.remove(oldPortal);
        return true;
    }

    private PortalWallBlock getPortal(boolean inputIsPortalOne) {
        if (portals.size() < 1) {
            return null;
        }
        for (PortalWallBlock portal : this.portals) {
            if (portal.isPortalOne() == inputIsPortalOne) {
                return portal;
            }
        }
        return null;
    }

    //Searches for the obstacle block in the given direction that is closest to the given coordinates
    private ObstacleBlock findObstacle(String direction, int x, int y) {
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

    private DirectedBlock getDirectedBlock(int x, int y) {
        if (this.getMoveableBlock(x, y) != null) {
            return getMoveableBlock(x, y);
        }
        if (this.getObstacleBlock(x, y) != null) {
            return getObstacleBlock(x, y);
        }
        return null;
    }
    private MoveableBlock getMoveableBlock(int x, int y) {
        for (MoveableBlock block : this.moveableBlocks) {
            if ((block.getY() == y) && (block.getX() == x)) {
                return block;
            }
        }
        return null;
    }

    private boolean isCoordinatesWithinBounds(int x, int y) {
        if(x < 0 || x >= this.width) {
            return false;
        }
        if(-y < 0 || -y >= this.height) {
            return false;
        }
        return true;
    }
    private ObstacleBlock getObstacleBlock(int x, int y) {
        if (!isCoordinatesWithinBounds(x, y)) {
            return null;
        }
        return obstacleBlocks[-y][x];
    }
    private TraversableBlock getTraversableBlock(int x, int y) {
        if (!isCoordinatesWithinBounds(x, y)) {
            return null;
        }
        return traversableBlocks[-y][x];
    }

    private void addObstacleBlock(ObstacleBlock block) {
        this.obstacleBlocks[-block.getY()][block.getX()] = block;
        if (block instanceof TeleporterBlock) {
            this.teleporters.add((TeleporterBlock) block);
        }
        if (block instanceof PortalWallBlock && ((PortalWallBlock) block).isPortal()) {
            this.portals.add((PortalWallBlock) block);
        }
    }
    private void addMoveableBlock(MoveableBlock block) {
        this.moveableBlocks.add(block);
    }

    //Returns a list of all blocks that are of the same type as the given block and are positioned such that they together
    //make out an uninterrupted chain in the given direction. The given block is placed first in the list, and the remainder
    //are placed in sequence from that position
    private List<BlockAbstract> getBlockChain(BlockAbstract startBlock, int directionX, int directionY) {
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
            x += directionX;
            y += directionY;
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
    private int[] directionStringToXY(String direction) {
        int directionX = 0;
        int directionY = 0;
        switch (direction) {
            case "up":
                directionY++;
                break;
            case "down":
                directionY--;
                break;
            case "right":
                directionX++;
                break;
            case "left":
                directionX--;
                break;
        }
        int[] directionXY = new int[2];
        directionXY[0] = directionX;
        directionXY[1] = directionY;
        return directionXY;
    }

    private List<List<BlockAbstract>> getGravityFallOrder() {
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
            //Then add the transporter that serves as the block's footing, unless it was previously added to some other list in 
            //fallOrderConstruction as an exit porter (placed at index 2)
            ObstacleBlock entryTransporter = (ObstacleBlock) getFootingBlock((MoveableBlock) blockWithTransporterFooting, false);
            if(fallOrderConstruction.stream().filter(a -> a.get(2) == entryTransporter).count() != 0) {
                continue; //Continues to the next loop, since the given entry transporter was already represented
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
            BlockAbstract blockAtExit = this.getFootingBlock(blockAtEntry, true);

            int entryDirectionX = 0;
            int entryDirectionY = 0;
            int exitDirectionX = 0;
            int exitDirectionY = 0;
            //If the entry porter was a portal, then it follows that the exit porter is also a portal.
            //The blocks entering the entry portal will fall in the direction of gravity, thus the entry portal faces in the direction opposite to gravity.
            //The blocks leaving the exit portal will fall out in the direction that the exit portal is facing, and then be subjected to gravity.
            if (entryPorter instanceof PortalWallBlock && ((PortalWallBlock) entryPorter).isPortal()) {
                entryDirectionX = 0;
                entryDirectionY = (-1)*this.getGravityDirectionY();
                exitDirectionX = exitPorter.getDirectionXY()[0];
                exitDirectionY = exitPorter.getDirectionXY()[1];
            }
            //Otherwise the porter must have been a teleporter, which maintains an entering block's movement direction.
            //Since this has to do with gravity and falling objects, the blocks must be falling in the direction of gravity,
            //and thus entering the entry teleporter from above, and leaving the exit teleporter from below.
            //Thus the entry teleporter's entrance is directed in the opposite direction to gravity, and the exit teleporter's
            //exit is directed in the same direction as gravity.
            else {
                entryDirectionX = exitDirectionX = 0;
                entryDirectionY = (-1)*this.getGravityDirectionY();
                exitDirectionY = this.getGravityDirectionY();
            }
            //The entry chain is thus obtained and put into its own list
            List<BlockAbstract> entryChain = new ArrayList<BlockAbstract>();
            entryChain = getBlockChain(blockAtEntry, entryDirectionX, entryDirectionY);
            
            //The process is then repeated for the exit chain as long as the block at the exit was in fact a moveable
            //block, otherwise it has no place in a moveable block chain
            List<BlockAbstract> exitChainPotential = new ArrayList<BlockAbstract>();
            List<BlockAbstract> exitChain = new ArrayList<BlockAbstract>();
            if (blockAtExit instanceof MoveableBlock) {
                exitChainPotential = getBlockChain(blockAtExit, exitDirectionX, exitDirectionY);
                //If the exit direction is horizontal, then some blocks in the exit chain may fall down before they can be pushed by the movement
                //of the blocks entering the portal that were pulled down by gravity. If one of these blocks was to fall, then that would break the
                //original chain at that block, and instead reform a new chain consisting of all blocks up until that break point, and then continue
                //down in the direction of gravity
                if (exitDirectionX != 0) { 
                    for (BlockAbstract chainBlock : exitChainPotential) {
                        //Since the chain has remained intact until this block, then this block must be part of the actual chain
                        exitChain.add(chainBlock); 
                        //If this block is airborne, then the horizontal chain will break at this point, and then instead connect with the block
                        //chain that falls down in the direction of gravity from that break point.
                        if (isBlockAirborne((MoveableBlock) chainBlock)) {
                            exitChain.addAll(getBlockChain(chainBlock, 0, this.getGravityDirectionY()));
                            break;
                        }
                    }
                }
                //Otherwise the exit direction must be vertical, in which case the exit chain will be equal to the potential one.
                else {
                    exitChain = exitChainPotential;
                }
            } 
            //The two moveable block lists should thus be put together, bound together by the entry and exit porter, which forms the complete chain
            List<BlockAbstract> completeChain = new ArrayList<BlockAbstract>();

            if (exitChain.size() >= 1) {
                //If there is at least one block at the exit point, and the exit porter faces the opposite direction to gravity, 
                //then there will be blocks falling into both the entry and exit portal at the same time, and thus colliding. In 
                //this case the direction the chain as a whole should move in will be determined by the amount of moveable blocks 
                //falling on each side, whichever side is heavier will steer the movement. Should both sides be perfectly balanced, 
                //then the chain will not move at all.
                if  (exitDirectionY == -(this.getGravityDirectionY()) ) {
                    //if the entry side is heavier than the exit side
                    int entryChainWeight = getBlockChainWeight(entryChain, 0, entryDirectionY);
                    int exitChainWeight = getBlockChainWeight(exitChain, 0, exitDirectionY);
                    if (entryChainWeight > exitChainWeight) { 
                        completeChain.add(exitPorter);
                        Collections.reverse(exitChain);
                        completeChain.addAll(exitChain);
                        completeChain.add(entryPorter);
                        completeChain.addAll(entryChain);
                        fallOrderComplete.add(completeChain);
                    }
                    //if the exit side is heavier than the entry side
                    else if (entryChainWeight < exitChainWeight) { 
                        completeChain.add(entryPorter);
                        //But first the entry chain list is reversed as to represent the order in which the blocks would fall into the entry transporter.
                        Collections.reverse(entryChain);
                        completeChain.addAll(entryChain);
                        completeChain.add(exitPorter);
                        completeChain.addAll(exitChain);
                        fallOrderComplete.add(completeChain);
                    }
                    // // //Otherwise the sides are perfectly balanced, and so gravity will have no effect, to signify that these blocks should not be considered
                    // // //we do not include the transporters
                    // // //in the gravity fall order.
                    else {
                        completeChain.addAll(entryChain);
                        completeChain.addAll(exitChain);
                        fallOrderComplete.add(completeChain);
                        // continue; THIS IS THE KEY. When the blocks are perfectly balanced they HAVE to be added to the moved blocks list somehow!!!
                    }
                }
                //If the exit direction is non-vertical, then the blocks on that side will not move into or out of the portal, but since they
                //are equal to or outnumber the other side they will hinder the movement of that other side, wheras the heavier side remain 
                //as they can still fall down from where they are currently standing.
                else if (exitDirectionX != 0) {
                    if (entryChain.size() > exitChain.size()) {
                        completeChain.add(exitPorter);
                        Collections.reverse(exitChain);
                        completeChain.addAll(exitChain);
                        completeChain.add(entryPorter);
                        completeChain.addAll(entryChain);
                        fallOrderComplete.add(completeChain);
                    }
                    else if (entryChain.size() <= exitChain.size()) { //This could be !!!!NEEDS TO BE CHANGED SOMEHOW
                        completeChain.add(exitPorter);
                        Collections.reverse(exitChain);
                        completeChain.addAll(exitChain);
                        fallOrderComplete.add(completeChain);
                    }
                }
                //Otherwise the exit direction must be the same as the gravity direction, thus the chain at the entry portal are at the top of the chain,
                //and the ones at the exit are at the bottom
                else { 
                    completeChain.add(exitPorter);
                    Collections.reverse(exitChain);
                    completeChain.addAll(exitChain);
                    completeChain.add(entryPorter);
                    completeChain.addAll(entryChain);
                    fallOrderComplete.add(completeChain);
                }
            }
            else {
                completeChain.add(exitPorter);
                completeChain.add(entryPorter);
                completeChain.addAll(entryChain);
                fallOrderComplete.add(completeChain);
            }
        }
        //In this list, the chains are ordered such that the first block chain will be the one whose last block has the 
        //lowest y-coordinate (in relation to gravity) and then according to the lowest x-coordinate should the y-coordinate be shared
        int g = this.getGravityDirectionY();
        fallOrderComplete = fallOrderComplete.stream()
            .sorted( (a, b) -> g*(b.get(b.size()-1).getX() - a.get(a.size()-1).getX())) //g is not really needed here, but why not
            .sorted( (a, b) -> g*(b.get(b.size()-1).getY() - a.get(a.size()-1).getY()))
            .collect(Collectors.toList());
        return fallOrderComplete;
    }

    public void gravityStep() {
        //Gravity should not be applied when the game is over
        if (this.isGameOver == true) {
            return; 
        }
        // The first blocks that should fall down are the ones furthest down in the gravity's direction, thus
        // these blocks should be issued to move first.
        // // // // // // List<MoveableBlock> transportersAsFooting = this.moveableBlocks.stream()
        // // // // // // .filter(a -> this.getFootingBlock(a, false) instanceof ObstacleBlock)
        // // // // // // .filter(a -> ((ObstacleBlock) this.getFootingBlock(a, false)).isTransporter())
        // // // // // // .collect(Collectors.toList());
        // // // // // // System.out.println(transportersAsFooting);
        // // // // // // List<MoveableBlock> list = this.moveableBlocks.stream()
        // // // // // //     .sorted( (a, b) -> (b.getX() - a.getX()))
        // // // // // //     .sorted( (a, b) -> (b.getY() - a.getY()))
        // // // // // //     .collect(Collectors.toList());
        // // // // // // this.moveableBlocks = new ArrayList<MoveableBlock>(list);
        // // // // // // //When gravity is inverted the blocks with the highest value for their Y coordinates should fall first
        // // // // // // if (this.isGravityInverted) {
        // // // // // //     for (int i = 0; i < moveableBlocks.size(); i++) {
        // // // // // //         if (moveableBlocks.get(i).isPlayer() && hasPlayerMoved) {
        // // // // // //         }
        // // // // // //         else if (!isInBirdView(moveableBlocks.get(i))) {
        // // // // // //             moveBlock(moveableBlocks.get(i), "up", 1, "gravity");
        // // // // // //         }
        // // // // // //         System.out.println(this.prettyString());
        // // // // // //         notifyObservers();
        // // // // // //     }
        // // // // // // }

        // // // // // // //When gravity is not inverted the blocks with the lowest value for their Y coordinates should fall first
        // // // // // // else {
        // // // // // //     for (int i = moveableBlocks.size() - 1; i >= 0; i--) {
        // // // // // //         if (moveableBlocks.get(i).isPlayer() && hasPlayerMoved) {
        // // // // // //         }
        // // // // // //         else if (!isInBirdView(moveableBlocks.get(i))) {
        // // // // // //                 moveBlock(moveableBlocks.get(i), "down", 1, "gravity");
        // // // // // //         }
        // // // // // //         System.out.println(this.prettyString());
        // // // // // //         notifyObservers();
        // // // // // //     }
        // // // // // // }

        //We first retrive the fall order
        List<List<BlockAbstract>> fallOrder = this.getGravityFallOrder();
        String porterDirection = "";
        List<BlockAbstract> movedBlocks = new ArrayList<BlockAbstract>();
        for (List<BlockAbstract> blockChain : fallOrder) {
            //All blocks contained in a block-chain that does not start with an obstacle block should not be moved
            //as it indicates that gravity would not move them away from their current coordinates.
            if (!(blockChain.get(0) instanceof ObstacleBlock)) {
                blockChain.forEach(a -> movedBlocks.add(a));
            }
            //A block chain should form an infinite falling loop in the case that a porter connects the end 
            //of an entry chain to the beginning of that same chain. In that case the first listed moveable block
            //would be placed both at index 1 and at the index equal to one more than half its total size. There needs to be 
            //at one entry and exit porter, and then at least one block at the used entrance and exit, making out a minimum 
            //chain size of 4.
            if (blockChain.size() >= 4 && blockChain.get(1) == blockChain.get(blockChain.size()/2+1)) {
                //We retrieve what could be the entry porter in which the block chain is falling into.
                BlockAbstract entryPorterBlock = blockChain.get(blockChain.size()/2);
                //if this entry porter block is an obstacle block then it must be a porter, otherwise it would
                //not have been included in the fallorder
                if (entryPorterBlock instanceof ObstacleBlock) {
                    //we then retrieve the direction in which this porter moves entering blocks
                    int[] exitDirectionXY = ((ObstacleBlock) entryPorterBlock).getExitDirectionXY(blockChain.get(1));
                    //Should this direction be equal to that of gravity, then an infinte falling loop should form.
                    if (exitDirectionXY[1] == this.getGravityDirectionY()) {
                        //To sort out the infinite loop we retrieve one of each moveable blocks in the block-chain.
                        List<BlockAbstract> uniqueMoveableBlocks = blockChain.subList(1, blockChain.size()/2);
                        //Then we store each of these unique moveable block's old y-coordinates in a list.
                        List<Integer> oldYCoordinates = uniqueMoveableBlocks.stream()
                        .mapToInt(a -> a.getY())
                        .boxed()
                        .collect(Collectors.toList());
                        //And then set each y-coordinate of these blocks equal to that of the previous block in the block-chain list.
                        //The previous is chosen because the block chain list is ordered such that the blocks with the lowest y coordinates 
                        //(in relation to gravity) are placed first, thus for every block the previous block had the lower y-coordinate, 
                        //which should be that block's new coordinate after it's been subjected to gravity.
                        for (BlockAbstract block : uniqueMoveableBlocks) {
                            int blockIndex = uniqueMoveableBlocks.indexOf(block);
                            int indexOfNewYCoordinate = Math.floorMod(blockIndex - 1, uniqueMoveableBlocks.size());
                            int newY = oldYCoordinates.get(indexOfNewYCoordinate);
                            ((MoveableBlock) block).setY(newY);
                            //Blocks should only be gravity once, thus these blocks will be added to the list of moved blocks.
                            movedBlocks.add(block);
                        }
                    }
                }
            }
            for (int i = 0; i < blockChain.size(); i++) {
                BlockAbstract block = blockChain.get(i);
                //No blocks should be moved twice by gravity.
                if (movedBlocks.contains(block)) {
                    continue;
                }
                //If the first block in the current block chain is not an obstacle block, then the rest of the chain should not be moved yet
                if (!(blockChain.get(0) instanceof ObstacleBlock)) {
                    break; //porterDirection null perhaps? Maybe add all these blocks to a list of non-moved blocks?
                }
                //Otherwise the first block in the list must be an obstacle block, and it must be a transporter, thus the next block in the list
                //must be placed such that it is at the entry point of that transporter. We then retrieve the direction that entrance is facing 
                //by comparing the coordinates of these two blocks.
                if (i == 0) {
                    BlockAbstract block2 = blockChain.get(i+1);
                    int directionY = block.getY() -block2.getY();
                    int directionX = block.getX() - block2.getX();
                    
                    if (directionY < 0) {
                        porterDirection = "up";
                    }
                    else if (directionY > 0) {
                        porterDirection = "down";
                    }
                    else if (directionX < 0) {
                        porterDirection = "right";
                    }
                    else {
                        porterDirection = "left";
                    }
                }
                //There will also be one more transporter in the list, the exit transporter, and it can appear at any non-zero index;
                else if (block instanceof ObstacleBlock) { //&& i < blockChain.size()-1 was included in the if check, but I suspect that it is redundant
                    porterDirection = this.getGravityDirection();
                }
                //Otherwise if the block is moveable, then attempt to move these blocks accordingly
                else if (block instanceof MoveableBlock) {
                    //In the case that the porterdirection is horizontal and the block is airborne
                    if ((porterDirection == "right" || porterDirection == "left") && isBlockAirborne((MoveableBlock) block)) {
                        //If the block could not be moved in the direction of gravity
                        if (!moveBlock((MoveableBlock) block, this.getGravityDirection(), 1, "gravity")) {
                            //Then try to move it in the direction of the transporter instead
                            moveBlock((MoveableBlock) block, porterDirection, 1, "gravity");
                        }
                    }
                    //Otherwise if the porterdirection is vertical and the block is airborne, then move that block in the direction of that porter
                    else if (isBlockAirborne((MoveableBlock) block)) {
                        moveBlock((MoveableBlock) block, porterDirection, 1, "gravity");
                    }
                    //Then add the current block to the moved block list, as to keep track of those that have been moved
                    movedBlocks.add(block);
                    System.out.println(this.prettyString());
                }
                System.out.println(this.prettyString());
            }
        }
        //Once all the blockchains placed at portals have been moved according to gravity the remainder of the moveable
        //blocks should be moved in the direction of gravity. The first to fall will be the block furthest down (in relation to gravity),
        //and then by horizontal position should the vertical one by equal.
        int g = this.getGravityDirectionY();
        this.moveableBlocks.stream()
            .filter(a -> !movedBlocks.contains(a))
            .filter(a -> isBlockAirborne(a))
            .sorted((a, b) -> g*(b.getX() - a.getX()))
            .sorted((a, b) -> g*(b.getY() - a.getY()))
            .forEach(a -> moveBlock(a, this.getGravityDirection(), 1, "gravity"));

        //Check if the game is over after gravity was applied
        this.checkGameOver();
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
                    footingBlock = getTopBlock(entryTransporter.getExitPointXY(block)[0], entryTransporter.getExitPointXY(block)[1]);
                }
            }
        }
        //The footing block will at last be returned, even if it is null, which would indicate that the moveable block
        //is standing at the bounds of the map and thus will use those borders as footing instead, even though they aren't actual blocks.
        return footingBlock;
    }

    //Checks if the given block is placed at a corrdinate where bird-view is enabled
    private boolean isInBirdView(BlockAbstract block) {
        //At any given coordinate on the map, there must exist a traversable block with that coordinate.
        TraversableBlock traversableBlock = getTraversableBlock(block.getX(), block.getY());
        if (traversableBlock == null) {
            throw new IllegalStateException("At any given coordinate on the map, there must exist a traversable block with that coordinate.");
        }
        return traversableBlock.isBirdView();
    }

    private boolean isBlockAirborne(MoveableBlock block) {
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
        //there will exist no footing for the given block, thus it must be airborne.
        else if (footingBlock instanceof TraversableBlock) { //Case: footing is not a DirectedBlock (alternatively: block is a TraversableBlock) meaning it has no collision -> must be airborne
            return true;
        }
        else if (footingBlock instanceof ObstacleBlock) {
            return false;
        }
        //If the block is neither null nor traversable, then it follows that it must be a directed block, which could have collision to serve as footing.
        else { 
            //A moveable block can not be its own footing. This could happen in cases where the original footing block was moveable 
            //and stood ontop of an active portal with an exit point above itself (in relation to current gravity direction).
            //If the footing block is the block itself, then the block has no footing and is thus considered airborne.
            if (footingBlock == block) {
                return true;
            }
            //In the case that the footing block is itself airborne, then it follows that the block using it as footing is also airborne.
            else if (footingBlock instanceof MoveableBlock) {
                //If the footing block is different depending on wether or not transportation is accounted for, then there must be a transporter
                //below the given block, and its effective footing is the block at the exit point of that transporter
                BlockAbstract potentialTransporter = getFootingBlock(block, false);
                if (footingBlock != potentialTransporter) {
                    //It is now confirmed that the potential transporter was in fact a transporter. However before we proceed we should check for potential infinite loops.
                    
                    


                    //In the case that the block serving as footing is on the exit point of the porter, then it is confirmed that the potential transporter is an actual transporter.
                    //The block at the exit point may be standing still, falling down out from the porter, or falling down into it. To find out which is the case we will need to find 
                    //which direction the exit porter pushes out entering blocks.
                    int[] exitDirectionXY = ((ObstacleBlock) potentialTransporter).getExitDirectionXY(block);
                    List<BlockAbstract> blockChain = getBlockChain(block, 0, -this.getGravityDirectionY());
                    if (blockChain.contains(footingBlock)) {
                        return true;
                    }

                    //Case1: If the exit direction matches the gravity direction, then the footing block will fall away from the block standing ontop of it unless it is itself not airborne
                    //and will thus carry that block instead. Thus this block airborne if it's footing block is airborne.
                    if (exitDirectionXY[1] == this.getGravityDirectionY()) {
                        return isBlockAirborne((MoveableBlock) footingBlock);
                    }
                    //If the exit direction is opposite to gravity, then the footing block will be falling towards the given block, thus they will both serve as the other's footing. 
                    //However should one side be heavier than the other, then gravity will pull down the heavier side to even out the difference, thus blocks on the lighter side will be airborne.
                    else if (exitDirectionXY[1] == -this.getGravityDirectionY()) {
                        List<BlockAbstract> blockSide = this.getBlockChain(block, 0, -this.getGravityDirectionY());
                        List<BlockAbstract> footingSide = this.getBlockChain(footingBlock, 0, -this.getGravityDirectionY());
                        // // // Collections.reverse(blockSide);
                        // // // Collections.reverse(footingSide);
                        int blockSideWeight = getBlockChainWeight(blockSide, 0, -this.getGravityDirectionY());
                        int footingSideWeight = getBlockChainWeight(footingSide, 0, -this.getGravityDirectionY());
                        if (blockSideWeight >= footingSideWeight) {
                            return true;
                        }
                    }
                    //If the exit direction is horizontal rather than vertical, then the footing block is either standing still due to not being airborne, or it is about to fall 
                    //down in the direction of gravity, and would thus move out of the way, and as such the block ontop of it would be airborne as well.
                    else if (exitDirectionXY[0] != 0) {
                        //If this footing block is airborne, then the given block is also airborne.
                        if (isBlockAirborne((MoveableBlock) footingBlock)) {
                            return true;
                        }
                        //Otherwise the footing block must be standing still. 
                        else {
                            List<BlockAbstract> blockSide = this.getBlockChain(block, 0, -this.getGravityDirectionY());
                            List<BlockAbstract> footingSide = this.getBlockChain(footingBlock, exitDirectionXY[0], 0);
                            // // // Collections.reverse(blockSide);
                            // // // Collections.reverse(footingSide);
                            int blockSideWeight = getBlockChainWeight(blockSide, 0, -this.getGravityDirectionY());
                            int footingSideWeight = getBlockChainWeight(footingSide, exitDirectionXY[0], 0);
                            if (blockSideWeight > footingSideWeight) {
                                return true;
                            }
                        }
                    }
                }
                return isBlockAirborne((MoveableBlock) footingBlock);
            }
            //Otherwise the footing block must have collision and be able to serve as footing, thus the block must not be airborne.
            return false;
        }
    }

    //Takes in a block chain that is ordered in the given direction, and returns the weight of that chain 
    private int getBlockChainWeight(List<BlockAbstract> blockChain, int directionX, int directionY) {
        //If the direction the chain is ordered in is opposite to gravity, then this block chain needs to be reversed,
        //as the weight of the chain should the evaluated from the highest and working itself downwards to the lowest
        if (directionY * this.getGravityDirectionY() > 0) {
            Collections.reverse(blockChain);
        } 
        //The weight is then counted from the highest point. Each block that is not in bird view adds to the weight,
        //and all other blocks that are in bird-view will substract from the weight, unless the weight is 0, as the weight can not be negative.
        int stackWeight = 0;
        for (BlockAbstract moveableBlock : blockChain) {
            if (!isInBirdView(moveableBlock)) {
                stackWeight++;
            }
            else {
                if (stackWeight > 0) {
                    stackWeight--;
                }
                else {
                    stackWeight = 0;
                }
            }
        }
        return stackWeight;
    }

    //Move count should be incremented every time the player moves successfully.
    private void incrementMoveCount() {
        this.moveCount++;
    }

    public void gravityInverter() {
        //Gravity can not be inverted when the game is over.
        if (this.isGameOver) {
            return;
        }
        this.isGravityInverted = !this.isGravityInverted;
    }

    //Issues to move the given player block and return boolean reflecting wether or not the player was moved. Every time 
    //the player tries to move gravity will move all blocks one step in its direction if possible.
    //Gravity should not take effect on the player if they were moved successfully by the method call, as to give the illusion of momentum
    public boolean movePlayer(String direction) {
        //Can no longer move once the game is over.
        if (this.isGameOver) {
            return false;
        }
        MoveableBlock player = this.getPlayer();
        // String directionOld = player.getDirection();

        player.setDirection(direction);
        boolean wasMoved = false;

        if (player != null) {
            //Can not move while airborne, unless the player's footing is another moveable block other than itself
            BlockAbstract footingBlock = getFootingBlock(player, true);
            if (isBlockAirborne(player) && !(footingBlock instanceof MoveableBlock) && footingBlock != player) {
                wasMoved = false;
            }
            //Attempt to move player, check this one move was enough to win the game, update the variable "wasMoved" 
            else if (moveBlock(player, direction, 1, "player")) {
                wasMoved = true;
            }
        }
        //If gravity does not move forward on a set interval, then let it instead move forward once every time 
        //the player block was issued to move
        System.out.println("Gravity is move input?" + this.isGravityApplicationMoveInput());
        if (this.isGravityApplicationMoveInput()) {
            this.gravityStep();
        }
        if (wasMoved == true) {
            this.incrementMoveCount();
        }
        System.out.println(this.prettyString());
        this.updateActivePressurePlatesCount();
        this.checkGameOver();
        this.notifyObservers();
        return wasMoved;
    }

    private boolean pushMoveable(MoveableBlock pushingBlock, MoveableBlock block, String direction, int strength, boolean hasTakenPortal, String movementSource) { //CHECK THIS !!!!!!!!!!!!!! may work against intent
        //A block can not push another block in the same direction as the gravity affecting it, unless it moved through a portal
        if( (direction == "up" || direction == "down") && (direction == this.getGravityDirection()) && !isInBirdView(block) ) {
            if (!hasTakenPortal) {
                return false;
            }
        }
        int oldX = block.getX();
        int oldY = block.getY();
        int[] directionXY = directionStringToXY(direction);
        List<BlockAbstract> blockChain = this.getBlockChain(pushingBlock, directionXY[0], directionXY[1]);
        if (blockChain.size() <= strength+1 && blockChain.size() >= 1) {
            BlockAbstract lastBlock = blockChain.get(blockChain.size()-1);
            BlockAbstract blockFollowingLastBlock = this.getTopBlock(lastBlock.getX() + directionXY[0], lastBlock.getY() + directionXY[1]);
            if (blockFollowingLastBlock instanceof ObstacleBlock && ((ObstacleBlock) blockFollowingLastBlock).canBlockEnter(lastBlock)) {
                int[] exitPointXY = ((ObstacleBlock) blockFollowingLastBlock).getExitPointXY(lastBlock);
                BlockAbstract exitPointBlock = this.getTopBlock(exitPointXY[0], exitPointXY[1]);
                List<BlockAbstract> blockChainExit = this.getBlockChain(exitPointBlock, directionXY[0], directionXY[1]);
                if (blockChainExit.containsAll(blockChain)) {
                    List<Integer> coordinatesX = blockChainExit.stream()
                        .mapToInt(a -> a.getX())
                        .boxed()
                        .collect(Collectors.toList());
                    List<Integer> coordinatesY = blockChainExit.stream()
                        .mapToInt(a -> a.getY())
                        .boxed()
                        .collect(Collectors.toList());
                    for (BlockAbstract chainedBlock : blockChainExit) {
                        if (chainedBlock != pushingBlock) {
                            int nextIndex = (blockChainExit.indexOf(chainedBlock) + 1) % blockChainExit.size();
                            int x = coordinatesX.get(nextIndex);
                            int y = coordinatesY.get(nextIndex);
                            chainedBlock.setX(x);
                            chainedBlock.setY(y);
                            TraversableBlock traversableBlock = getTraversableBlock(x, y);
                            if (chainedBlock instanceof MoveableBlock && traversableBlock.isPressurePlate()) {
                                ((MoveableBlock) chainedBlock).setState(true);
                                updateTeleporters();
                            }
                        }
                    }
                    if (this.getTopBlock(oldX, oldY) instanceof TraversableBlock) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
        }
        //If the push is made by a player, then the strength of the pushing motion increases
        if (block.isPlayer() || (movementSource.equals("gravity") && isBlockAirborne(block))) {
            // strength++;
        }
        //Otherwise it must have been made by a rock, which itself will cost strength to push forward, thus the strength of the pushing motion decreases
        else { 
            strength--;
        }

        //If the block to be pushed is able to be moved in the given direction, then the push has been successful, thus return true
        if (moveBlock(block, direction, strength, movementSource)) {
            System.out.println(this.prettyString());
            return true;
        }
        //Otherwise the block could not be pushed, thus false is returned
        else {
            return false;
        }
    }
    
    private boolean moveBlock(MoveableBlock block, String direction, int strength, String movementSource) {
        // System.out.println(block.getX() + "," + block.getY() + " to move: " + direction + ".");
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
                    int[] transportExit = obstacleBlock.getExitPointXY(blockOld);
                    blockNew.setX(transportExit[0]);
                    blockNew.setY(transportExit[1]);
                    if (obstacleBlock instanceof PortalWallBlock && ((PortalWallBlock) obstacleBlock).isPortal()) {
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
            if (obstacleBlock != null && obstacleBlock instanceof PortalWallBlock && ((PortalWallBlock) obstacleBlock).isWall()) {
                System.out.println("Can not move further: you have hit a wall");
                return false;
            } 
        }

        // Block should not be moved if the given movement would place it out of bounds
        if (blockNew.getX() < 0 || blockNew.getX() >= width) {
            System.out.println("Out of bounds, cant move x direction");
            if (blockNew.isPlayer()) {
                throw new IllegalArgumentException("Can not move further " + direction + " as it would be out of range for the map.");
            }
            return false;
        }
        if (blockNew.getY() > 0 || blockNew.getY() <= -height) {
            System.out.println("Out of bounds, cant move y direction");
            if (blockNew.isPlayer()) {
                throw new IllegalArgumentException("Can not move further " + direction + " as it would be out of range for the map.");
            }
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
            //Update the state to of the moveable block to reflect wether or not it is placed ontop of a pressure plate
            block.setState(getTraversableBlock(block.getX(), block.getY()).isPressurePlate());
            
            //When a block has successfully entered a portal, they are relocated in a flash, thus the baggage loses its carrier, meaning they are left to fall      !!!!!!!!!!!!!!!IS THIS EVEN NEEDED ANYMORE?
            if (!hasTakenPortal) {
                moveBaggage(blockOld, direction);
            }
            return true;
        }
        
        // If the movement places this block at coordinates that are already occupied by another moveable block and that block has collision,
        // then try to first push that block, and if successful move this block afterwards.
        MoveableBlock blockAtNewCoordinates = getMoveableBlock(blockNew.getX(), blockNew.getY());
        if (blockAtNewCoordinates != null && strength > 0) {
            //If the block at the new coordinates was the given block to be moved, then that block
            //is already at the correct position. This could happen in the case that the block entered
            //a transporter that had its exit point placed at the same place as its connection's entry
            //point. In this sense the block did move successfully through the portal, even if it looped
            //around and ended up in the same position as before, thus we return true as to indicate
            //a successfull move.
            if (blockAtNewCoordinates == block) {
                return true;
            }
            else if (pushMoveable(block, blockAtNewCoordinates, blockNew.getDirection(), strength, hasTakenPortal, movementSource)) {
                //Since the copy-block was able to navigate to its coordinates without breaking any rules, then 
                //the coordinates should be legal for the original as well
                block.setX(blockNew.getX());
                block.setY(blockNew.getY());
                if (movementSource != "gravity" && block.isPlayer()) {
                    block.setDirection(blockNew.getDirection());
                }
                System.out.println(this.prettyString());

                //Then

                block.setState(getTraversableBlock(block.getX(), block.getY()).isPressurePlate());
                moveBaggage(blockOld, directionOriginal);
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
    // Thus check if the given block has "baggage", and if they do: issue them to follow the movement of the original block, unless
    // Obstacles hinder the movement.
    //Input is the instance of the block prior to being moved, and the direction is the direction it was moved in
    private void moveBaggage(MoveableBlock movedBlock, String direction) {
        //Blocks that are stacked ontop of eachother will already fall together due to gravity
        if (direction == "up" || direction == "down") {
            return;
        }
        //
        if (!isInBirdView(movedBlock) && !isBlockAirborne(movedBlock)) { //A block can not carry baggage while they are airborne
            List<BlockAbstract> blockBaggage = getBlockChain(movedBlock, 0, -this.getGravityDirectionY());
            blockBaggage.remove(movedBlock);

            int directionX = 0;
            if (direction == "left") {
                directionX = -1;
            } else {
                directionX = 1;
            }
            List<BlockAbstract> moveableBaggage = new ArrayList<BlockAbstract>();
            Collections.reverse(blockBaggage);
            for (BlockAbstract baggage : blockBaggage) {
                BlockAbstract blockAtNewCoordinates = getTopBlock(baggage.getX() + directionX, baggage.getY());
                if (blockAtNewCoordinates instanceof TraversableBlock) {
                    moveableBaggage.add(baggage);
                }
                //Otherwise the baggage block must be a directed block
                else {
                    moveableBaggage.clear();
                    if (blockAtNewCoordinates instanceof ObstacleBlock) {
                        if ( ((ObstacleBlock) blockAtNewCoordinates).isTransporter() && ((ObstacleBlock) blockAtNewCoordinates).canBlockEnter(baggage) ) {
                            moveableBaggage.add(baggage);
                        }
                    }
                }  
            }
            for (BlockAbstract block : moveableBaggage) {
                moveBlock((MoveableBlock) block, direction, 0, "baggage");
            }
        }
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
        prettyString += "Score:" + this.getActivePressurePlatesCount() + " isGameOver?" + this.isGameOver() + "\n";
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
    


    //Constructors
    public PushRocks(String levelMapLayout, String levelDirectionLayout) {
        System.out.println("Constructor: minimal");
        this.levelName = "Custom level";
        this.levelMapLayout = levelMapLayout;
        this.levelDirectionLayout = levelDirectionLayout;
        this.buildWorld(this.levelMapLayout, this.levelDirectionLayout);
        
        // this.setGravityApplicationInterval();
    }
    public PushRocks(String levelName, String levelMapLayout, String levelDirectionLayout) {
        System.out.println("Constructor: build from level information");
        this.levelName = levelName;
        this.levelMapLayout = levelMapLayout;
        this.levelDirectionLayout = levelDirectionLayout;
        this.buildWorld(this.levelMapLayout, this.levelDirectionLayout);
        
        // this.setGravityApplicationInterval();
    }
    public PushRocks(String levelName, String levelMapLayout, String levelDirectionLayout, String saveMapLayout, String saveDirectionLayout, int saveMoveCount) {
        System.out.println("Constructor: build from save information");
        this.levelName = levelName;
        this.levelMapLayout = levelMapLayout;
        this.levelDirectionLayout = levelDirectionLayout;
        this.buildWorld(this.levelMapLayout, this.levelDirectionLayout);
        this.buildWorld(saveMapLayout, saveDirectionLayout);
        this.moveCount = saveMoveCount;
        
        // this.setGravityApplicationInterval();
    }

    private String checkLayoutCompabillityWithLevel(String mapLayout, String directionLayout) {
        mapLayout = mapLayout            
            .replaceAll("\\n", "")
            .replaceAll("\\r", "");
        String levelMapLayout = this.levelMapLayout
            .replaceAll("\\n", "")
            .replaceAll("\\r", "");

        //If the input map and direction layouts are equal to the level map and direction layouts, then they are the same, as they must be compatible with themselves.
        if (mapLayout == this.levelMapLayout && directionLayout.length() == this.levelDirectionLayout.length()) {
            return null;
        }
        //The map layouts must be of equal length
        if (mapLayout.length() != levelMapLayout.length()) {
            return "Map layout and level layout must be of equal length to be compatible. Map layout length was: " + mapLayout.length() + " and level layout length was: " + this.levelMapLayout.length();
        }
        //The direction layouts must also be of equal length
        if (directionLayout.length() != this.levelDirectionLayout.length()) {
            return "Map layout and level layout must be of equal length to be compatible. Map layout length was: " + mapLayout.length() + " and level layout length was: " + this.levelMapLayout.length();
        }
        //The map layouts must be of equal width
        if (mapLayout.indexOf("@") != levelMapLayout.indexOf("@")) {
            return "Map layout and level layout must be of equal width to be compatible. Map layout width was: " + mapLayout.indexOf("@") + " and level layout width was: " + this.levelMapLayout.indexOf("@");
        }
        //The layouts must have matching counts of players and rocks, thus we keep track of how many of each the layouts have.
        int inputPlayerCount = 0;
        int inputRockCount = 0;
        int levelPlayerCount = 0;
        int levelRockCount = 0;

        //For each index, there is a character representing a block's attributes in terms of type and bird-view-state.
        //The compatibillity between the two map layouts will depend on these attributes and how they compare between the two.
        for (int i = 0; i < mapLayout.length(); i++) {
            //Obtain the character represeting the block's type at index i in each map-layout.
            char inputTypeChar = mapLayout.charAt(i);
            char levelTypeChar = mapLayout.charAt(i);
            //Upper case characters and "-" indicates that bird view should be enabled, wheras lower case characters and " " indicates that it is disabled.
            //Bird-view can not be changed, and thus for any one coordinate's bird-view state in one layout, the other layout must have a matching bird-view state for their coordinate.
            if ( ((Character.isUpperCase(inputTypeChar) || inputTypeChar == '-') & !(Character.isUpperCase(levelTypeChar) || levelTypeChar == '-')) 
              || ((Character.isLowerCase(inputTypeChar) || inputTypeChar == ' ') & !(Character.isLowerCase(levelTypeChar) || levelTypeChar == ' '))) {
                return "Map layouts must have matching bird-view values for every coordinate to be compatible. Characters at index " + i + " did not match. Input type of input was: " + inputTypeChar + " and level was: " + levelTypeChar;
            }
            //Once bird-view has been assured to match, upper-case will no longer matter
            String inputType = Character.toString(Character.toLowerCase(inputTypeChar));
            String levelType = Character.toString(Character.toLowerCase(levelTypeChar));

            //If one map layout has a coordinate with a wall or portal, then the other one must also have a wall or portal at that coordinate.
            if ("wvu".contains(inputType) != "wvu".contains(levelType)) {
                return "Map layouts must have matching walls. Input type was: " + inputTypeChar + " and level type was: " + levelTypeChar;
            }
            //If one map layout has a coordinate with a teleporter, then the other one must also have a teleporter at that coordinate.
            if ("t".contains(inputType) != "t".contains(levelType)) {
                return "Map layouts must have matching teleporters. Input type was: " + inputTypeChar + " and level type was: " + levelTypeChar;
            }
            //If one map layout has a coordinate with a pressure plate, then the other one must also have a pressure plate at that coordinate.
            if ("d".contains(inputType) != "d".contains(levelType)) {
                return "Map layouts must have matching pressure plate. Input type was: " + inputTypeChar + " and level type was: " + levelTypeChar;
            }
            //Increment player/rock counts if needed
            if ("p".contains(inputType)) {
                inputPlayerCount++;
            }
            if ("p".contains(levelType)) {
                levelPlayerCount++;
            }
            if ("r".contains(inputType)) {
                inputRockCount++;
            }
            if ("r".contains(levelType)) {
                levelRockCount++;
            }
        }
        //The layouts must have matching counts of players.
        if (inputPlayerCount != levelPlayerCount) {
            return "Map layouts must have matching counts of players. Count for input was: " + inputPlayerCount + " count for level was: " + levelPlayerCount;
        }
        //The layouts must have matching counts of rocks.
        if (inputRockCount != levelRockCount) {
            return "Map layouts must have matching counts of players. Count for input was: " + inputRockCount + " count for level was: " + levelRockCount;
        }
        //Otherwise the map-layouts must be compatible, in which case no exception message is returned.
        return null;
    }

    private void buildWorld(String mapLayout, String directionLayout) {
        //If the world is being built from a layout other than that of the level, then it must be checked that this
        //new layout is compatible with the level. 
        String exceptionMessage = this.checkLayoutCompabillityWithLevel(mapLayout, directionLayout);
        if (exceptionMessage != null) {
            throw new IllegalArgumentException(exceptionMessage);
        }

        this.moveableBlocks.clear();
        this.teleporters.clear();
        this.portals.clear();
        this.activePressurePlatesCount = 0;
        this.isGravityInverted = false;
        this.isGameOver = false;

        // String typeSequence = this.mapLayout.replace("\n", "");
        String typeSequence =  mapLayout
            .replaceAll("\\n", "")
            .replaceAll("\\r", "")
            .replaceAll("@", "");
        this.width = mapLayout.indexOf("@");
        this.height = typeSequence.length() / this.width;

        String[] blockDirections = new String[this.levelDirectionLayout.length()-1];
        char gravityDirection = this.levelDirectionLayout.charAt(levelDirectionLayout.length()-1);
        if (Character.toLowerCase(gravityDirection) != 'g') {
            throw new IllegalArgumentException("The direction layout must end with the character 'g', which will determine gravity direction depending on being upper or lower case. Character was: " + gravityDirection);
        }
        else {
            //Lower-case 'g' indicates that gravity is not inverted.
            if (Character.isLowerCase(gravityDirection)) {
                this.isGravityInverted = false;
            }
            //Upper-case 'g' indicates that gravity is inverted.
            else {
                this.isGravityInverted = true;
            }
        }
        
        for (int i = 0; i < levelDirectionLayout.length()-1; i++) {
            String blockDirection = levelDirectionLayout.substring(i, i+1);
            if (!"udlr".contains(blockDirection)) {
                throw new IllegalArgumentException("Direction layout only supports a character representation of the following directions: up 'u', down 'd', left 'l', right 'r'.");
            }
            switch (blockDirection) {
                case "u":
                    blockDirection = "up";
                    break;
                case "d":
                    blockDirection = "down";
                    break;
                case "l":
                    blockDirection = "left";
                    break;
                case "r":
                    blockDirection = "right";
                    break;
            }
            blockDirections[i] = blockDirection;
            
        }
        int directionsRemaining = blockDirections.length;

        this.obstacleBlocks = new ObstacleBlock[height][width];
        this.traversableBlocks = new TraversableBlock[height][width];

        int playerCount = 0;
        int portalOneCount = 0;
        int portalTwoCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tangibleType = typeSequence.charAt(y*width + x);
                boolean birdView = true; 
                //An upper-case character or the symbol '-' indicates that bird's eye view should not be active
                //for the blocks at this coordinate
                if (Character.isUpperCase(tangibleType) || tangibleType == '-') {
                    birdView = false;
                }
                tangibleType = Character.toLowerCase(tangibleType);
                if(tangibleType == '-') {
                    tangibleType = ' ';
                }
                //In the case that type character is 'q' or 'o', then there must be a pressure plate placed
                //underneath a player or rock respectively. The type is then changed accordingly, and the
                //isPressurePlatePlacement is set to true as to indicate that there exists a pressure plate 
                //at the same coordinate.
                boolean isPressurePlatePlacement = false;
                if (tangibleType == 'q') {
                    tangibleType = 'p';
                    isPressurePlatePlacement = true;
                }
                else if (tangibleType == 'o') {
                    tangibleType = 'r';
                    isPressurePlatePlacement = true;
                }
                if (!"prwtuvd ".contains(tangibleType+"")) {
                    throw new IllegalArgumentException("Can only construct blocks with the following letters: 'prwtuvd '. Character was: " + tangibleType + "which has the acii value:" + (int) tangibleType);
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
                if (tangibleType == 'd' || isPressurePlatePlacement) { //d or isPressurePlatePlacment set to true indicates that a pressure plate should be made
                    traversableBlock = new TraversableBlock(x, -y, 'd', birdView);
                }
                else { 
                    traversableBlock = new TraversableBlock(x, -y, ' ', birdView);
                }
                this.traversableBlocks[y][x] = traversableBlock;

                String blockDirection = null;
                if ("pruv".contains(tangibleType+"")) {
                    if (directionsRemaining <= 0) {
                        throw new IllegalArgumentException("The direction layout can not contain less directions than the sum of players, rocks, portals and gravity. Direction count was: " + blockDirections.length);
                    }
                    blockDirection = blockDirections[blockDirections.length - directionsRemaining];
                    directionsRemaining--;
                }

                if ("pr".contains(tangibleType+"")) {
                    MoveableBlock moveableBlock = new MoveableBlock(x, -y, tangibleType, blockDirection);
                    System.out.println("x=" + x + "y=" + -y);
                    //Moveable blocks standing on pressure plates will have their state set to true.
                    if (isPressurePlatePlacement) {
                        moveableBlock.setState(true);
                    }
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
                    ObstacleBlock obstacleBlock;
                    if (tangibleType == 't') {
                        obstacleBlock = new TeleporterBlock(x, -y, connection);
                        // obstacleBlock = new TeleporterBlock(x, -y, tangibleType, blockDirection, connection);
                    }
                    else {
                        obstacleBlock = new PortalWallBlock(x, -y, tangibleType, blockDirection, connection);
                    }
                    addObstacleBlock(obstacleBlock);
                }
            }
        }
        if (directionsRemaining > 0) {
            throw new IllegalArgumentException("The direction layout can not contain more directions than the sum of players, rocks, portals and gravity. Direction count was: " + blockDirections.length + " and remaining directions count was: " + directionsRemaining);
        }
        this.updateActivePressurePlatesCount();
        System.out.println(this.prettyString());
        this.notifyObservers();
    }
    
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
        this.observers.forEach(observer -> observer.update(this));
    } 
    
    //Experimental! The IntervalNotifier class runs on its own thread, 
    @Override
    public void update(IObservableIntervalNotifier observable) {
        if (!ignoreIntervalNotifier) { //this is more so a band-aid as to not interrupt the thread. 
            this.gravityStep();
        }
    }
    public void pause(boolean pauseGame) {
        if (pauseGame) {
            if (this.isGravityApplicationInterval()) {
                if (this.intervalNotifier != null) {
                    this.intervalNotifier.stop();
                }
                if (this.intervalNotifierThread != null) {
                    this.ignoreIntervalNotifier = true;
                }  
            }
        }
        else {
            if (this.isGravityApplicationInterval()) {
                if (this.intervalNotifier != null) {
                    this.intervalNotifier.start();
                }
                if (this.intervalNotifierThread != null) {
                    this.ignoreIntervalNotifier = false;
                }  
            }
        }
    }

    public void resetLevel() {
        this.pause(false);
        this.buildWorld(this.levelMapLayout, this.levelDirectionLayout);
    }




    public static void main(String[] args) {
   
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
        // W--------W--------W
        // W--------W---R--D-W
        // W--------WWW----WWW
        // W--------W---R--D-W
        // W--------W--------W
        // W--------W-WW-----W
        // W-T------W-T------W
        // W--------W--------W
        // WWWWWWWWWWWWWWWWWWW""";
        // String string2Directions = "rrrrrr";

        // System.out.println(string2.indexOf("\n"));
        // System.out.println(string2.indexOf("2"));
        // System.out.println(string2.replace("\n", "").length() / string2.indexOf("\n"));

        // PushRocks game0 = new PushRocks(string2, string2Directions);
        // System.out.println(game0.prettyString());

        // // game0.resetStationaryBlock(3, -1);
        // System.out.println(game0);
        // System.out.println(game0.prettyString());

        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());

        // System.out.println(game0.getObstacleBlock(2, -7).isTeleporter());
        // System.out.println(game0.getObstacleBlock(2, -16).isTeleporter());

        // System.out.println(game0.getObstacleBlock(2, -7).getState());
        // System.out.println(game0.getObstacleBlock(2, -16).getState());
    

        // System.out.println(game0.getObstacleBlock(2, -7).getState());
        // System.out.println(game0.getObstacleBlock(2, -16).getState());

        // System.out.println(game0.prettyString());

        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());

        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "down");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "up");
        // System.out.println(game0.prettyString());
        // game0.movePlayer(1, "right");
        // System.out.println(game0.prettyString());
        // game0.placePortal(true, game0.getPlayer(1));
        // game0.movePlayer(1, "left");
        // System.out.println(game0.prettyString());
        // game0.placePortal(false, game0.getPlayer(1));
        // System.out.println(game0.prettyString());
        
 
        // System.out.println(game0.moveableBlocks.size());
        // System.out.println(game0.getMoveableBlock(4, 1));
        // System.out.println(game0.getPlayer(1).getX());
        // System.out.println(game0.getPlayer(1).getY());

        // String levelMapLayout1 = """
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
        // String levelDirectionLayout1 = "rrrrrrrrrrruu";

        // PushRocks game0 = new PushRocks(levelMapLayout1, levelDirectionLayout1);
        
        // game0.gravityInverter();

        // BlockAbstract block1 = new ObstacleBlock(0, 0, 'w', null, null);
        // ObstacleBlock block2 = new ObstacleBlock(0, 0, 'w', null, null);
        // // block = game0.getDirectedBlock(1, 1);
        
        // BlockAbstract mBlock = new MoveableBlock(0, 0, 'p', "right");
        // ((ObstacleBlock) block2).clearPortal();

        // String news = "strìng";  
        // CharSequence newnews = "a,b";
        // String hest = Normalizer.normalize(news, Form.NFC);
        // System.out.println(news + hest);
    }
}
