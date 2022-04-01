package pushrocks.model;

public class ObstacleBlock extends DirectedBlock {

    private ObstacleBlock connection; //used by portals and teleporters

    //Returns the block-object this block has a connection with if it exists. Used to keep track of which 
    //portals/teleporters are connected to eachother.
    public ObstacleBlock getConnection() {
        return this.connection;
    }

    //setConnection() is based on code I made for the Partner-exercise:
    //Connects this block to another block, and makes sure that the other block is in turn connected to this block.
    //Any previous connections either of these objects had before will be removed, this way a block can only be connected to one
    //other block at a time.
    //If the input is null, then both blocks will instead be disconnected from eachother.
    public void setConnection(ObstacleBlock connection) {

        //A block can not be connected to itself, given such input set the block's connection to null.
        if (connection == this) {
            this.setConnection(null);
            return;
        }
        // Do nothing if connection of this object is already set to the connection-input
        if (this.connection == connection) {
            //Connected blocks should have their state set to true, and false otherwise
            this.setState(connection != null);
            return;
        }
        //Saves previous connection of the object, and sets the connection of object to the new connection input
        ObstacleBlock connectionOld = this.connection;
        this.connection = connection;

        //check if old connection had a previous connection and if this previous connection was this object, remove association if true
        if ( (connectionOld != null) && (connectionOld.getConnection() == this) ) {
            connectionOld.setConnection(null);
        }
        //If the new partner of this object is not null, then the partner of this object should have their partner set to this object.
        if (this.connection != null) {
            this.connection.setConnection(this);
        }
        //Connected blocks should have their state set to true, and false otherwise
        this.setState(connection != null);
    }

    //Removes the connection this transport block has if it exists.
    public void removeConnection() {
        this.setConnection(null);
    }

    //Removes the connection this block 
    public void clearPortal() {
        if (! this.isPortal()) {
            throw new IllegalArgumentException("Block must be of type 'portal' to be able to clear portal");
        }
        this.removeConnection();
        this.setWall(); 
    }


    public void setWall() {
        this.setType('w');
        this.setState(false);
        this.setConnection(null);
        this.setDirection(null);
    }

    public void setTeleporter() {
        this.setType('t');
        this.setState(false);
        this.setConnection(null);
        this.setDirection(null);
    }
    
    public void setPortal(boolean isPortalOne, String direction, ObstacleBlock connection) {
        if (isPortalOne) {
            this.setType('v');
        }
        else {
            this.setType('u');
        }
        this.setState(false);
        this.setConnection(connection);
        this.setDirection(direction);
    }
    
    // public void setType(char type, String direction) {
    //     super.setType(type);
    //     switch (type) {
    //         case 'w':
    //             this.setWall();
    //             break;
    //         case 't':
    //             this.setTeleporter();
    //             break;
    //         case 'u':
    //             this.setPortal(true, direction, null);
    //             break;
    //         case 'v':
    //             this.setPortal(false, direction, null);
    //             break;
    //     }
    // }


    //Method made public as to allow Obstacle blocks to change their type dynamically.
    @Override
    public void setDirection(String direction) {
        if (!this.isPortal() && direction != null) {
            throw new IllegalArgumentException("Obstacles can have a non-neutral direction, but only when they have been turned into a portal. Input was: " + direction);
        }
        if (this.isPortal() && direction == null) {
            throw new IllegalArgumentException("Portals must have a non-neutral direction. Input was: " + direction);
        }
        super.setDirection(direction);
    }

    //Returns the entry point of the given portal if it exists

    public int[][] getTeleporterEntryPoints() {
        // Only portals and teleporters can have an exit point
        if ( !this.isTeleporter()) {
            System.out.println("getTeleporterEntryPoint is only valid for portals and teleporters.");
            return null;
        }
        // A portal that is not connected will have no entry point
        if ( this.connection == null) {
            return null;
        }
        int[][] entryPoints = new int[4][2];
        //Entry from above
        entryPoints[0][0] = this.getX();
        entryPoints[0][1] = this.getY() + 1;
        //Entry from below
        entryPoints[3][0] = this.getX();
        entryPoints[3][1] = this.getY() - 1;
        //Entry from right
        entryPoints[1][0] = this.getX() + 1;
        entryPoints[1][1] = this.getY();
        //Entry from left
        entryPoints[2][0] = this.getX() - 1;
        entryPoints[2][1] = this.getY();

        return entryPoints;
    }

    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point
    public int[] getTeleporterExitPoint(BlockAbstract entryBlock) {
        if (this.connection == null) {
            return null;
        }
        if (!this.isTeleporter()) {
            return null;
        }
        int entryBlockX = entryBlock.getX();
        int entryBlockY = entryBlock.getY();
        int[][] entryPoints = this.getTeleporterEntryPoints();
        for (int x = 0; x < 4; x++) {
            if ((entryPoints[x][0] == entryBlockX) && (entryPoints[x][1] == entryBlockY)) {
                int[] exitPoint = new int[2];
                exitPoint[0] = this.getConnection().getTeleporterEntryPoints()[3-x][0];
                exitPoint[1] = this.getConnection().getTeleporterEntryPoints()[3-x][1];
                return exitPoint;
            }
        }
        return null;
    }

    public int[] getPortalEntryPoint() {
        // Only portals and teleporters can have an exit point
        if ( !(this.isPortal() || this.isTeleporter())) {
            System.out.println("getPortalExitPoint is only valid for portals and teleporters.");
            return null;
        }
        if ( this.isTeleporter()) {
            return null; //maybe I shouldnt let teleporters use this method..
        }
        // A portal that is not connected will have no exit point
        if ( this.connection == null) {
            return null;
        }
        int[] entryPoint = new int[2];
        int entryX;
        int entryY;
        String portalDirection;
        entryX = this.getX();
        entryY = this.getY();
        portalDirection = this.getDirection();

        switch (portalDirection) {
            case "up":
                entryY++;
                break;
            case "down":
                entryY--;
                break;
            case "left":
                entryX--;
                break;
            case "right":
                entryX++;
                break;
        }
        entryPoint[0] = entryX;
        entryPoint[1] = entryY;
        return entryPoint;
    }



    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point
    public int[] getPortalExitPoint() {
        if (this.connection == null) {
            return null;
        }
        return this.connection.getPortalEntryPoint();
    }

    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point,
    //but only if the given entryBlock matches the coordinates for the portal's entry point
    public int[] getPortalExitPoint(BlockAbstract entryBlock) {
        if (this.connection == null) {
            return null;
        }
        //If the given entry block is not standing at the portal's entry point, then
        //return null as to indicate that the block would not be able to enter the portal from its current position
        int[] entryBlockCoordinates = new int[]{entryBlock.getX(), entryBlock.getY()};
        if ( !(entryBlockCoordinates[0] == this.getPortalEntryPoint()[0] && entryBlockCoordinates[1] == this.getPortalEntryPoint()[1]) ) {
            return null;
        }
        return this.connection.getPortalEntryPoint();
    }

    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point
    public int[] getExitPoint(BlockAbstract entryBlock) {
        if (this.isPortal()) {
            return this.getPortalExitPoint(entryBlock);
        }
        else if (this.isTeleporter()) {
            return this.getTeleporterExitPoint(entryBlock);
        }
        else {
            return null;
        }
    }

    //Checks if this obstacle block can be entered by the given block
    public boolean canBlockEnter(BlockAbstract entryBlock) {
        //Obstacle blocks can not be entered unless they are transporters
        if (!this.isTransporter()) {
            return false;
        }  
        //Further a transporter can only be entered if it is active
        if (this.getState()) {
            //And at last the entring block is only allowed to enter if it is standing
            //at one of the transporter's entry points
            int[] blockPoint = new int[]{entryBlock.getX(), entryBlock.getY()};
            if (this.isPortal()) {
                //A portal will only have a single entry point
                System.out.println("WHATINTHEWORLD");
                System.out.println(blockPoint);
                System.out.println(this.getPortalEntryPoint());
                System.out.println(blockPoint == this.getPortalEntryPoint());
                System.out.println("Port x" + this.getPortalEntryPoint()[0] + "y" + this.getPortalEntryPoint()[1]);
                System.out.println("Bloc x" + blockPoint[0] + "y" + blockPoint[1]);
                System.out.println(blockPoint[0] == this.getPortalEntryPoint()[0] && blockPoint[1] == this.getPortalEntryPoint()[1]);
                return blockPoint[0] == this.getPortalEntryPoint()[0] && blockPoint[1] == this.getPortalEntryPoint()[1];
            }
            else {
                //A teleporter will have four entry points
                int[][] entryPoints = this.getTeleporterEntryPoints();
                for (int i = 0; i < 4; i++) {
                    if (blockPoint[0] == entryPoints[i][0] && blockPoint[1] == entryPoints[i][1]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    //Expands the pool of valid types established in the BlockAbstract superclass to include those valid for this sub-class.
    //Valid obstacle types include: wall 'w', teleporter 't', portal one 'u', and portal two 'v'.
    @Override
    protected String getValidTypes() {
        return super.getValidTypes() + "wtuv";
    }

    public boolean isWall() {
        return this.getType() == 'w';
    }

    public boolean isTeleporter() {
        return this.getType() == 't';
    }

    public boolean isPortalOne() {
        return (this.getType() == 'v');
    }

    public boolean isPortalTwo() { 
        return (this.getType() == 'u');
    }

    public boolean isPortal() { 
        return (this.isPortalOne() || this.isPortalTwo());
    }

    public boolean isTransporter() {
        return (this.isTeleporter() || this.isPortal());
    }

    @Override
    public String getCollisionTypes() {
        return super.getCollisionTypes() + "wtuv"; //can potentially replace "wt" with getValidTypes() if portals can have collision
    }



    // //Constructor without specified direction
    // public ObstacleBlock(int x, int y, char type) {
    //     super(x, y, type);
    //     this.setConnection(null);
    // }

    // //Constructor with specified direction
    // public ObstacleBlock(int x, int y, char type, String direction) {
    //     super(x, y, type, direction);
    //     this.setConnection(null);
    //     if (this.isPortal()) {
    //         this.setPortal(this.isPortalOne(), direction, null)
    //     }
    // }
    
    //Constructor with specified direction and connection
    public ObstacleBlock(int x, int y, char type, String direction, ObstacleBlock connection) {
        super(x, y, type, direction);
        this.setConnection(null);
        if (this.isPortal()) {
            this.setPortal(this.isPortalOne(), direction, connection);
        }
    }
    

    @Override
    public String toString() {
        switch (this.getType()) {
            case 'w':
                return "#";
            case 't':
                if (this.getState()) {
                    return "+";
                }
                else {
                    return "-";
                }
            case 'v':
                if (this.getState()) {
                    return "v";
                }
                else {
                    return "V";
                }
            case 'u':
                if (this.getState()) {
                    return "u";
                }
                else {
                    return "U";
                }

            default:
                return "";
        }
    }
    
}
