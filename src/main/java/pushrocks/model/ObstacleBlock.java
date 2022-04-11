package pushrocks.model;

public abstract class ObstacleBlock extends DirectedBlock {

    private ObstacleBlock connection; //used by portals and teleporters

    //Constructor with specified direction and connection
    public ObstacleBlock(int x, int y, char type, String direction, ObstacleBlock connection) {
        super(x, y, type, direction);
        this.setConnection(connection);
    }

    //setConnection() is based on code I made for the Partner-exercise:
    //Connects this block to another block, and makes sure that the other block is in turn connected to this block.
    //Any previous connections either of these blocks had before will be removed, this way a block can only be connected to one
    //other block at a time.
    //If the input is null, then both blocks will instead be disconnected from eachother.
    public void setConnection(ObstacleBlock connection) {
        //A block can not be connected to itself, given such input set the block's connection to null.
        if (connection == this) {
            this.setConnection(null);
            return;
        }
        // Do nothing if connection of this block is already set to the connection-input
        if (this.connection == connection) {
            //Connected blocks should have their state set to true, and false otherwise
            this.setState(connection != null);
            return;
        }
        //Saves previous connection of the block, and sets the connection of block to the new connection input
        ObstacleBlock connectionOld = this.connection;
        this.connection = connection;

        //check if the old connection had a previous connection and if this previous connection was this block, remove association if true
        if ( (connectionOld != null) && (connectionOld.getConnection() == this) ) {
            connectionOld.setConnection(null);
        }
        //If the new connection of this block is not null, then the connection of this block should have their connection set to this block.
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
    //Returns the block-block this block has a connection with if it exists. Used to keep track of which 
    //portals/teleporters are connected to eachother.
    public ObstacleBlock getConnection() {
        return this.connection;
    }

    abstract public boolean isTransporter();
    abstract public int[][] getEntryPointsXY(); 
    abstract public int[] getExitPointXY(BlockAbstract entryBlock); 
    public int[] getExitDirectionXY(BlockAbstract entryBlock) {
        int[] exitPorterXY = this.getCoordinatesXY();
        int[] exitPointXY = this.getExitPointXY(entryBlock);
        return new int[] {exitPointXY[0] - exitPorterXY[0], exitPointXY[1] - exitPorterXY[1]};
    }

    // //Checks if this obstacle block can be entered by the given block
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

            //A teleporter will have four entry points
            int[][] entryPoints = this.getEntryPointsXY();
            for (int i = 0; i < entryPoints.length; i++) {
                if (blockPoint[0] == entryPoints[i][0] && blockPoint[1] == entryPoints[i][1]) {
                    return true;
                }
            }
        }
        return false;
    }
}
