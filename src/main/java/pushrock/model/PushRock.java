package pushrock.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PushRock extends AbstractObservablePushRock {

    private String levelName;
    private String levelMapLayout;
    private String levelDirectionLayout;
    private int width;
    private int height;

    //Traversable blocks make out the area which other blocks can move through/be placed ontop of
    private TraversableBlock[][] traversableBlocks;  
    //Transfer blocks are placed ontop of the traversable plane, which can hinder or redirect movement of moveable blocks                                       
    private TransferBlock[][] transferBlocks;    
    //Moveable blocks are free to move around in traversable area, but are restricted by placements of directed blocks, 
    //which includes both transfer and other moveable blocks.     
    private List<MoveableBlock> moveableBlocks = new ArrayList<MoveableBlock>();            
    //Teleporters, once activated, can transport an entering moveable block to their exit point. A set amount of these
    //are pre-placed based on the level.                                           
    private List<TeleporterBlock> teleporters = new ArrayList<TeleporterBlock>();
    //Portals, once activated, can transport an entering moveable block to their exit point. At most two of these
    //can exist at once, and can be placed/repositioned by the player at a wall in their line of sight.
    private List<PortalWallBlock> portals = new ArrayList<PortalWallBlock>();

    //Every time the player issues a move command that successfully changes the coordinates of the player-block the
    //move count is increased. The move count will serve as the game's score, where a lower score is better.
    private int moveCount;
    private int activePressurePlatesCount;
    private boolean isGameOver;

    //Constructors
    public PushRock(String levelName, String levelMapLayout, String levelDirectionLayout) {
        if (levelName == null || levelMapLayout == null || levelDirectionLayout == null) {
            throw new IllegalArgumentException("Null is invalid for all constructor parameters.");
        }
        System.out.println("Constructor: build from level information");
        levelMapLayout = levelMapLayout.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));
        this.buildWorld(levelMapLayout, levelDirectionLayout, false);
        this.setLevelValues(levelName, levelMapLayout, levelDirectionLayout);
    }
    public PushRock(String levelName, String levelMapLayout, String levelDirectionLayout, String saveMapLayout, String saveDirectionLayout, int saveMoveCount) {
        System.out.println("Constructor: build from save information");
        if (levelName == null || levelMapLayout == null || levelDirectionLayout == null || saveMapLayout == null || saveDirectionLayout == null) {
            throw new IllegalArgumentException("Null is invalid for all constructor parameters.");
        }
        if (saveMoveCount < 0) {
            throw new IllegalArgumentException("Negative values are invalid for saved move count.");
        }
        levelMapLayout = levelMapLayout.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));
        saveMapLayout = saveMapLayout.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));
        this.buildWorld(levelMapLayout, levelDirectionLayout, false);
        this.setLevelValues(levelName, levelMapLayout, levelDirectionLayout);
        this.checkLayoutCompabillityWithLevel(saveMapLayout, saveDirectionLayout);
        this.buildWorld(saveMapLayout, saveDirectionLayout, true);
        if (moveCount < 0) {
            throw new IllegalArgumentException("Move count must be at least zero.");
        }
        this.moveCount = saveMoveCount;
    }

    private void setLevelValues(String levelName, String levelMapLayout, String levelDirectionLayout) {
        if (levelName.length() < 1) {
            throw new IllegalArgumentException("Level name must contain at least one character.");
        }
        this.levelName = levelName;
        this.levelMapLayout = levelMapLayout;
        this.levelDirectionLayout = levelDirectionLayout;
    }

    private String checkLayoutsValidity(String mapLayout, String directionLayout, boolean isSave) {
        if (mapLayout.length() < 2) {
            return ("Map layout length must be at least 2. Length was: " + mapLayout.length());
        }
        if (mapLayout.toLowerCase().chars().filter(ch -> ch == 'p' || ch == 'q').count() != 1) {
            return ("Map layout must contain exactly one player: 'p'/'q'.");
        }
        if (!(mapLayout.toLowerCase().contains("d"))) {
            return ("Map layout must contain at least one unoccupied pressure plate 'd'.");
        }
        if (!mapLayout.toLowerCase().matches("[ -prqotwuv@]+")) {
            return ("The map layout can only contain the letters representing existing types: ' -prqotwu' and the width marker '@'.");
        }
        if (!(mapLayout.contains("@"))) {
            return ("Map layout must contain at least one '@' as to mark the map's width.");
        }
        int width = mapLayout.indexOf("@");
        String mapLayoutNoLineSeparators = mapLayout.replaceAll("\\n|\\r\\n", "");
        for (int i = width; i < mapLayoutNoLineSeparators.length(); i+=width+1) {
            if (mapLayoutNoLineSeparators.charAt(i) != '@') {
                throw new IllegalArgumentException("The map layout must have a consistent width for all its rows.");
            }
        }
        if (mapLayoutNoLineSeparators.charAt(mapLayoutNoLineSeparators.length() - 1) != '@') {
            throw new IllegalArgumentException("The map layout must end with '@' to mark the end of the map.");
        }
        if (mapLayoutNoLineSeparators.length() % (width+1) != 0) {
            throw new IllegalArgumentException("The map's last row must be of equal width as the rest of the map.");
        }
        if (directionLayout.length() < 1) {
            return ("Direction layout must contain at least 1 character, one for the direction of the player.");
        }
        if (!directionLayout.toLowerCase().endsWith("g") || directionLayout.toLowerCase().chars().filter(ch -> ch == 'g').count() > 1) {
            return ("Direction layout must contain exactly one instance of the character 'g', and it must be placed at the end.");
        }
        if (!directionLayout.toLowerCase().matches("[udrlg]+")) {
            return ("The direction layout can only contain the letters udrlg.");
        }
        if (isSave) {
            String exceptionMessage = this.checkLayoutCompabillityWithLevel(mapLayout, directionLayout);
            if (exceptionMessage != null) {
                return exceptionMessage;
            }
        }
        return null;
    }
    private String checkLayoutCompabillityWithLevel(String mapLayout, String directionLayout) {
        mapLayout = mapLayout.replaceAll("\\n|\\r\\n", "").stripTrailing();
        String levelMapLayout = this.levelMapLayout.replaceAll("\\n|\\r\\n", "").stripTrailing();

        //If the input map and direction layouts are equal to the level map and direction layouts, then they are the same, as they must be compatible with themselves.
        if (mapLayout == this.levelMapLayout && directionLayout.length() == this.levelDirectionLayout.length()) {
            return null;
        }
        //The map layouts must be of equal length
        if (mapLayout.length() != levelMapLayout.length()) {
            return "Save map layout and level map layout must be of equal length to be compatible. Save map layout length was: " + mapLayout.length() + " and level map layout length was: " + this.levelMapLayout.length();
        }
        //The map layouts must be of equal width
        if (mapLayout.indexOf("@") != levelMapLayout.indexOf("@")) {
            return "Map layout and level layout must be of equal width to be compatible. Map layout width was: " + mapLayout.indexOf("@") + " and level layout width was: " + this.levelMapLayout.indexOf("@");
        }
        //The direction layouts must contain the same amount of non-portal directions.
        if (directionLayout.length() - mapLayout.toLowerCase().chars().filter(ch -> ch == 'u' || ch == 'v').count()  != this.levelDirectionLayout.length() - this.levelMapLayout.toLowerCase().chars().filter(ch -> ch == 'u' || ch == 'v').count()) {
            return "Save direction layout must contain the same amount character representations for non-portal directions.";
        }

        //The layouts must have matching counts of players and rocks, thus we keep track of how many of each the layouts have.
        int inputRockCount = 0;
        int levelRockCount = 0;

        //For each index, there is a character representing a block's attributes in terms of type and bird-view-state.
        //The compatibillity between the two map layouts will depend on these attributes and how they compare between the two.
        for (int i = 0; i < mapLayout.length(); i++) {
            //Obtain the character represeting the block's type at index i in each map-layout.
            char inputTypeChar = mapLayout.charAt(i);
            char levelTypeChar = levelMapLayout.charAt(i);
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
            if ( ("d".contains(inputType) || "q".contains(inputType) || "o".contains(inputType) ) != ("d".contains(levelType) || "q".contains(levelType) || "o".contains(levelType))) {
                return "Map layouts must have matching pressure plate. Input type was: " + inputTypeChar + " and level type was: " + levelTypeChar;
            }
            //Increment rock count accordingly
            if ("r".contains(inputType) || "o".contains(inputType)) {
                inputRockCount++;
            }
            if ("r".contains(levelType) || "o".contains(levelType)) {
                levelRockCount++;
            }
        }
        //The layouts must have matching counts of rocks.
        if (inputRockCount != levelRockCount) {
            return "Map layouts must have matching counts of players. Count for input was: " + inputRockCount + " count for level was: " + levelRockCount;
        }
        //Otherwise the map-layouts must be compatible, in which case no exception message is returned.
        return null;
    }

    private void buildWorld(String mapLayout, String directionLayout, boolean isSave) {
        //If the world is being built from a layout other than that of the level, then it must be checked that this
        //new layout is compatible with the level. 
        mapLayout = mapLayout.replaceAll("\\n|\\r\\n", "").stripTrailing();
        String validityMessage = this.checkLayoutsValidity(mapLayout, directionLayout, isSave);
        if (validityMessage != null) {
            throw new IllegalArgumentException(validityMessage);
        }
        this.moveableBlocks.clear();
        this.teleporters.clear();
        this.portals.clear();
        this.activePressurePlatesCount = 0;
        this.isGameOver = false;
        this.moveCount = 0;
        //The map's width is determined by the index of the first '@'
        this.width = mapLayout.indexOf("@");
        //And the map's height is in turn determined by the length of the sequence of types in the string divided by the map width,
        //'@' is not considered an actual type, so it is excluded from the type sequence and thus not counted in the length.
        String typeSequence =  mapLayout
            .replaceAll("@", "");
        this.height = typeSequence.length() / this.width;
        String[] blockDirections = new String[directionLayout.length()-1];

        for (int i = 0; i < directionLayout.length()-1; i++) {
            String blockDirection = directionLayout.substring(i, i+1);
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
        this.transferBlocks = new TransferBlock[height][width];
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
                    traversableBlock = new TraversableBlock(x, -y, 'd');
                }
                else { 
                    traversableBlock = new TraversableBlock(x, -y, ' ');
                }
                this.traversableBlocks[y][x] = traversableBlock;

                String blockDirection = null;
                if ("pruv".contains(tangibleType+"")) {
                    if (directionsRemaining <= 0) {
                        throw new IllegalArgumentException("The direction layout can not contain less directions than the sum of players, rocks, and portals. Direction count was: " + blockDirections.length);
                    }
                    blockDirection = blockDirections[blockDirections.length - directionsRemaining];
                    directionsRemaining--;
                }
                if ("pr".contains(tangibleType+"")) {
                    MoveableBlock moveableBlock = new MoveableBlock(x, -y, tangibleType, blockDirection);
                    //Moveable blocks standing on pressure plates will have their state set to true.
                    if (isPressurePlatePlacement) {
                        moveableBlock.setState(true);
                    }
                    addMoveableBlock(moveableBlock);
                }
                else if ("wtuv".contains(tangibleType+"")) {
                    TransferBlock transferBlock;
                    TransferBlock connection = null;
                    //If the type is 't' then the block to be created should be a teleporter
                    if (tangibleType == 't') {
                        transferBlock = new TeleporterBlock(x, -y);
                        ((TeleporterBlock) transferBlock).setConnection(connection);
                    }
                    //Otherwise it must be a portal, thus this portal should have a connection to the portal 
                    //opposite to itself if it exists.
                    else {
                        transferBlock = new PortalWallBlock(x, -y);
                        if (tangibleType == 'v') {
                            connection = this.getPortal(false);
                            ((PortalWallBlock) transferBlock).setPortal(true, blockDirection, (PortalWallBlock) connection);
                        }
                        else if (tangibleType == 'u') {
                            connection = this.getPortal(true);
                            ((PortalWallBlock) transferBlock).setPortal(false, blockDirection, (PortalWallBlock) connection);
                        }
                    }
                    addTransferBlock(transferBlock);
                }
            }
        }
        if (directionsRemaining > 0) {
            throw new IllegalArgumentException("The direction layout can not contain more directions than the sum of players, rocks, and portals. Direction count was: " + blockDirections.length + " and remaining directions count was: " + directionsRemaining);
        }
        this.updateActivePressurePlatesCount();
        System.out.println(this.coordinateString());
        this.notifyObservers();
    }

    public void resetLevel() {
        System.out.println("Reset level.");
        this.buildWorld(this.levelMapLayout, this.levelDirectionLayout, false);
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
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }


    private void addTransferBlock(TransferBlock block) {
        this.transferBlocks[-block.getY()][block.getX()] = block;
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

    private boolean isCoordinateWithinBounds(int x, int y) {
        if(x < 0 || x >= this.width) {
            return false;
        }
        if(-y < 0 || -y >= this.height) {
            return false;
        }
        return true;
    }
    private TraversableBlock getTraversableBlock(int x, int y) {
        if (!isCoordinateWithinBounds(x, y)) {
            return null;
        }
        return traversableBlocks[-y][x];
    }
    //Returns a copy of the traversable block at the given coordinate.
    public TraversableBlock getTraversableBlockCopy(int x, int y) {
        TraversableBlock block = this.getTraversableBlock(x, y);
        if (block == null) {
            return null;
        }
        return new TraversableBlock(x, y, block.getType());
    }

    private MoveableBlock getMoveableBlock(int x, int y) {
        for (MoveableBlock block : this.moveableBlocks) {
            if ((block.getY() == y) && (block.getX() == x)) {
                return block;
            }
        }
        return null;
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
    //Returns a copy of the given player block if it exists
    public MoveableBlock getPlayerCopy() {
        MoveableBlock player = this.getPlayer();
        MoveableBlock playerCopy = new MoveableBlock(player.getX(), player.getY(), player.getType(), player.getDirection());
        playerCopy.setState(player.getState());
        return playerCopy;
    }

    private TransferBlock getTransferBlock(int x, int y) {
        if (!isCoordinateWithinBounds(x, y)) {
            return null;
        }
        return transferBlocks[-y][x];
    }
    //Searches for the first transfer block in the given direction from the given coordinates
    private TransferBlock findTransferInDirection(String direction, int x, int y) {
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
        //transfer blocks in the given direction.
        if ((newX < 0 || newX >= this.width) || (newY > 0 || newY <= -this.height)) {
            return null;
        }
        //If a transfer block is found, then return that transfer block
        else if (getTransferBlock(newX, newY) != null) {
            return getTransferBlock(newX, newY);
        }
        //Otherwise the search is repeated
        else {
            return findTransferInDirection(direction, newX, newY);
        }
    }

    private DirectedBlock getDirectedBlock(int x, int y) {
        if (this.getMoveableBlock(x, y) != null) {
            return getMoveableBlock(x, y);
        }
        if (this.getTransferBlock(x, y) != null) {
            return getTransferBlock(x, y);
        }
        return null;
    }

    //Returns any and all blocks that are placed at the given coordinates in a list
    private List<BlockAbstract> getAllBlocks(int x, int y) {
        List<BlockAbstract> allBlocks = new ArrayList<BlockAbstract>();
        //There can only be a single directed block at any given coordinate
        if (this.getDirectedBlock(x, y) != null) {          
            allBlocks.add(this.getDirectedBlock(x, y));
        }
        //There can only be a single traversable block at any given coordinate
        if (this.getTraversableBlock(x, y) != null) {       
            allBlocks.add(getTraversableBlock(x, y));
        }
        //Together they will make out all blocks placed at the given coordinate
        return allBlocks;                                   
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
        if (block == null) {
            return null;
        }
        BlockAbstract blockCopy;
        if (block instanceof MoveableBlock) {
            blockCopy = new MoveableBlock(x, y, block.getType(), ((MoveableBlock) block).getDirection());
            blockCopy.setState(block.getState());
        }
        else if (block instanceof TransferBlock) {
            TransferBlock blockConnectionCopy = null; 
            if (((TransferBlock) block).isTransporter() && block.getState()) {
                TransferBlock blockConnection = ((TransferBlock) block).getConnection();
                if (blockConnection instanceof TeleporterBlock) {
                    blockConnectionCopy = new TeleporterBlock(blockConnection.getX(), blockConnection.getY());
                }
                else {
                    blockConnectionCopy = new PortalWallBlock(blockConnection.getX(), blockConnection.getY());
                    ((PortalWallBlock) blockConnectionCopy).setPortal(((PortalWallBlock) blockConnection).isPortalOne(), blockConnection.getDirection(), null);
                }
            }
            if (block instanceof TeleporterBlock) {
                blockCopy = new TeleporterBlock(x, y);
                ((TeleporterBlock) blockCopy).setConnection(blockConnectionCopy);
            }
            else {
                blockCopy = new PortalWallBlock(x, y);
                if (((PortalWallBlock) block).isPortal()) {
                    ((PortalWallBlock) blockCopy).setPortal(((PortalWallBlock) block).isPortalOne(), ((DirectedBlock) block).getDirection(), (PortalWallBlock) blockConnectionCopy);
                }
            }
        }
        else {
            blockCopy = new TraversableBlock(x, y, block.getType());
        }
        return blockCopy;
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
        if (directionX == 0 && directionY == 0) {
            return chain;
        }
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
                //a block chain should be cut off once it reaches a transporter
                chain.add(nextBlock);
            }
            //Otherwise it must not be of the same type, which means the chain has ended, and thus the search for more blocks ends
            else {
                isSearchDone = true;
            }
        }
        return chain;
    }

    //Updates the teleporter connections according to the current activePressurePlatesCount, and thus activates/deactivates them depending on wether they are connected or not
    private void updateTeleporters() {
        if (this.teleporters.size() < 2) {
            return;
        }
        //Connects two teleporters together based on the game's current activePressurePlatesCount. A previous connection is removed once a new one is made.
        //Where there are n teleporters, and s is the activePressurePlatesCount, the two teleporters that will connect are:
        //Teleporter one: index = s / n
        //Teleporter two: index = s % n
        this.teleporters.get(this.activePressurePlatesCount / this.teleporters.size()).setConnection(this.teleporters.get(this.activePressurePlatesCount % this.teleporters.size()));
    }
    //Updates the activePressurePlatesCount according to the current state of the game. The activePressurePlatesCount increments by one for each movable block that is placed ontop
    //of a pressure plate.
    private void updateActivePressurePlatesCount() {
        int activePressurePlatesCountOld = this.activePressurePlatesCount;
        int activePressurePlatesCountNew = 0;
        for (MoveableBlock block : moveableBlocks) {
            //An active-state moveable block indicates that they are placed ontop of a pressure plate.
            if (block.getState()) { 
                activePressurePlatesCountNew++;
            }
        }
        //Teleporters change their connection based on how many pressure plates have weight on them, thus these will need to be updated if the activePressurePlatesCount changed.
        if (activePressurePlatesCountNew != activePressurePlatesCountOld) {
            this.activePressurePlatesCount = activePressurePlatesCountNew;
            this.updateTeleporters();
        }
    }
    //Returns the current amount of activated pressure plates
    private int getActivePressurePlatesCount() {
        return this.activePressurePlatesCount;
    }
    private int getPressurePlateCount() {
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

    //Returns true if the game is over. The game is over once there is weight placed
    //ontop of every pressure plate, in other words: all pressure plates must be activated.
    private void checkGameOver() {
        this.updateActivePressurePlatesCount();
        if (this.getActivePressurePlatesCount() >= this.getPressurePlateCount()) {
            this.endGame();
        }
    }
    private void endGame() {
        this.isGameOver = true;
        System.out.println("Congratulations, you managed to complete this absolutely meaningless test.");
        System.out.println("Reset the game if you want to do it again.");
    }
    public boolean isGameOver() {
        return this.isGameOver;
    }
    
    
    //Attempts to place a portal (number 1 or 2), at the next wall in the direction the player is aiming/looking. 
    //We assume that the player is able to aim the portal through other moveable blocks such as rocks. 
    //They can however aim at or past a teleporter. Aiming through other active teleporters and portals is also 
    //not supported.
    public void placePortal(boolean inputIsPortalOne) {
        //Can not place portals when the game is over.
        if (isGameOver) {
            throw new IllegalStateException("Portals can not be placed while the game is over.");
        }
        MoveableBlock player = this.getPlayer();
        if (player == null) {
            throw new IllegalStateException("Portals can not be placed while no player exists to place them."); 
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
        TransferBlock wall = this.findTransferInDirection(direction, x, y);
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
                System.out.println(this.coordinateString());
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
            
            System.out.println(this.coordinateString());
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
    private boolean pushMoveable(MoveableBlock pushingBlock, MoveableBlock block, String direction, int strength, MoveableBlock movementSource) { 
        int oldX = block.getX();
        int oldY = block.getY();
        int[] directionXY = directionStringToXY(direction);
        List<BlockAbstract> blockChain = this.getBlockChain(pushingBlock, directionXY[0], directionXY[1]);
        if (blockChain.size() <= strength+1 && blockChain.size() >= 1) {
            BlockAbstract lastBlock = blockChain.get(blockChain.size()-1);
            BlockAbstract blockFollowingLastBlock = this.getTopBlock(lastBlock.getX() + directionXY[0], lastBlock.getY() + directionXY[1]);
            if (blockFollowingLastBlock == null) {
                throw new IllegalStateException("Can not push blocks out of bounds.");
            }
            if (blockFollowingLastBlock instanceof TransferBlock && ((TransferBlock) blockFollowingLastBlock).canBlockEnter(lastBlock)) {
                int[] exitPointXY = ((TransferBlock) blockFollowingLastBlock).getExitPointXY(lastBlock);
                BlockAbstract exitPointBlock = this.getTopBlock(exitPointXY[0], exitPointXY[1]);
                if (exitPointBlock == null) {
                    throw new IllegalStateException("Can not push blocks out of bounds.");
                }
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
        //If the push is not made by a player, then the pushing strength is reduced by one
        if (!pushingBlock.isPlayer()) {
            strength--;
        }

        //If the block to be pushed is able to be moved in the given direction, then the push has been successful, thus return true
        if (moveBlock(block, direction, strength, movementSource)) {
            return true;
        }
        //Otherwise the block could not be pushed, thus false is returned
        else {
            return false;
        }
    }

    private boolean moveBlock(MoveableBlock block, String direction, int strength, MoveableBlock movementSource) {
        boolean hasTakenPortal = false;
        MoveableBlock blockOld = new MoveableBlock(block.getX(), block.getY(), block.getType(), block.getDirection());
        MoveableBlock blockNew = new MoveableBlock(block.getX(), block.getY(), block.getType(), block.getDirection());

        //Attempts to move the copy-block in the given direction, if the movement was not successfull, then it would not be successfull for the original block either.
        if (!blockNew.moveInDirection(direction)) {
            return false;
        }
        //Otherwise the movement must have been successfull. We thus check if the new coordinates for the copy block placed it at a location
        //already occupied by another transfer block
        TransferBlock transferBlock = getTransferBlock(blockNew.getX(), blockNew.getY());
        if (transferBlock != null) {
            //Since a transfer block was found at the copy-block's new location, it could potentially be a transporter type:
            //Block should be teleported if the given movement would place it at a connected teleporter
            if (transferBlock.isTransporter()) {
                //To enter a transporter the block must be standing at one of the transporter's entry points and 
                //the transporter must be active (meaning it must be connected to another transporter)

                //If the block to be moved can not enter the transporter, then the transporter will instead be treated
                //as if it was a wall, thus hindering the movement of the block.
                if (!transferBlock.canBlockEnter(blockOld)) {
                    return false;
                }
                //Otherwise the transporter must be connected, thus the moving block should be transported out of the connected transporter in
                //direction of the movement if possible
                else {
                    int[] transportExit = transferBlock.getExitPointXY(blockOld);
                    blockNew.setX(transportExit[0]);
                    blockNew.setY(transportExit[1]);
                    hasTakenPortal = true;
                    if (transferBlock instanceof PortalWallBlock && ((PortalWallBlock) transferBlock).isPortal()) {
                        //When the transporter is a portal, the direction of the block should be set to that of the exit portal
                        blockNew.setDirection(transferBlock.getConnection().getDirection());
                    }
                    else {  
                        //When the transporter is a teleporter, the direction of the block should match that of the movement into the transporter
                        blockNew.setDirection(direction);
                    }
                }
            }
            transferBlock = getTransferBlock(blockNew.getX(), blockNew.getY());
            //If there exists a transfer block at the current coordinates of the moveable block copy, and that transfer
            //block is a wall, then it would not be possible for the actual moveable block to be moved there.
            if (transferBlock instanceof PortalWallBlock && ((PortalWallBlock) transferBlock).isWall()) {
                return false;
            } 
        }

        // Block should not be moved if the given movement would place it out of bounds
        if (blockNew.getX() < 0 || blockNew.getX() >= width) {
            if (blockNew.isPlayer()) {
                throw new IllegalStateException("Can not move further " + direction + " as it would be out of bounds for the map.");
            }
            return false;
        }
        if (blockNew.getY() > 0 || blockNew.getY() <= -height) {
            if (blockNew.isPlayer()) {
                throw new IllegalStateException("Can not move further " + direction + " as it would be out of bounds for the map.");
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
            block.setDirection(blockNew.getDirection());
            //Update the state to of the moveable block to reflect wether or not it is placed ontop of a pressure plate
            //TODO: The pressure plate should be activated, not the block ontop of it, find an alternate way to represent blocks ontop of pressure plates.
            block.setState(getTraversableBlock(block.getX(), block.getY()).isPressurePlate());
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
            else if (pushMoveable(block, blockAtNewCoordinates, blockNew.getDirection(), strength, movementSource)) {
                //Since the copy-block was able to navigate to its coordinates without breaking any rules, then 
                //the coordinates should be legal for the original as well
                block.setX(blockNew.getX());
                block.setY(blockNew.getY());
                if (block.isPlayer()) {
                    block.setDirection(blockNew.getDirection());
                }
                //Then set the state of the moved block to true if they are now standing at a pressure plate
                block.setState(getTraversableBlock(block.getX(), block.getY()).isPressurePlate());
                return true;
            }
            // do not move this block if the block ahead could not be moved
            return false;
        }
        return false;
    }

    //Move count should be incremented every time the player moves successfully.
    private void incrementMoveCount() {
        this.moveCount++;
    }
    public int getMoveCount() {
        return this.moveCount;
    }
    //Issues to move the given player block and return boolean reflecting wether or not the player was moved. 
    public boolean movePlayer(String direction) {
        //Can no longer move once the game is over.
        if (this.isGameOver) {
            throw new IllegalStateException("Player can not move while the game is over.");
        }
        MoveableBlock player = this.getPlayer();
        String oldPlayerDirection = player.getDirection();
        player.setDirection(direction);
        boolean wasMoved = false;

        if (player != null && moveBlock(player, direction, 1, player)) {
                wasMoved = true;
            }
        
        if (wasMoved == true) {
            this.incrementMoveCount();
        }
        this.updateActivePressurePlatesCount();
        this.checkGameOver();
        System.out.println(this.coordinateString());
        if (wasMoved || !oldPlayerDirection.equals(player.getDirection())) {
            this.notifyObservers();
        }
        return wasMoved;
    }

    public String toString() {
        String pushRockString = new String();
        for (int y = 0; y > height*(-1); y--) {
            for (int x = 0; x < width; x++) {
                String type = "?";
                if (getMoveableBlock(x, y) != null) {
                    type = this.getMoveableBlock(x, y).toString();
                }
                else if (getTransferBlock(x, y) != null) {
                    type = this.getTransferBlock(x, y).toString();
                }
                else {
                    type = this.getTraversableBlock(x, y).toString();
                }
                // if (!this.getTraversableBlock(x, y).isBirdView()) {
                //     type = type.toUpperCase();
                // }
                pushRockString += type;
            }
            pushRockString += "@\n";
        } 
        return pushRockString;
    }

    //An alternate String format that includes coordinate values along with some other useful game-state information.
    private String coordinateString() {
        String coordinateString = new String();
        coordinateString += "Score:" + this.getMoveCount() + " isGameOver:" + this.isGameOver() + "\n";
        String originalString = this.toString().replaceAll("@", "");
        for (int i = 0; i < originalString.length(); i++) {
            if (i==0) {
                for (int j = 0; j < this.width + 1; j++) {
                    if (j==0) {
                        coordinateString += "X";
                    }
                    else {
                        coordinateString += " " + (j-1) % 10;
                    }
                }
                coordinateString += "\n";
            }
            if (  i % (width+1) == 0) {
                coordinateString += (i/(this.width + 1)) % 10 + "";
            }
            coordinateString += " " + originalString.charAt(i);
        }
        return coordinateString;
    }

    public static void main(String[] args) {
        String levelName = "LevelName";
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

        PushRock push = new PushRock(levelName, levelLayout1, directionLayout1);
        System.out.println(push.coordinateString());
        System.out.println(push);
        
    }
}
