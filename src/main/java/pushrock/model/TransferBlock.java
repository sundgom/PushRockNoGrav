package pushrock.model;

public abstract class TransferBlock extends DirectedBlock {

    private TransferBlock connection;

    //Constructor with specified direction and connection
    public TransferBlock(int x, int y, char type, String direction, TransferBlock connection) {
        super(x, y, type, direction);
        this.setConnection(connection);
    }

    //Sub-classes should determine wether or not a connection is considered valid for their types.
    abstract protected String checkConnectionValid(TransferBlock connection);
    //setConnection() draws inspiration from code I made for the Partner-exercise:
    //Connects this block to another block, and makes sure that the other block is in turn connected to this block.
    //Any previous connections either of these blocks had before will be removed, this way a block can only be connected to one
    //other block at a time.
    //If the input is null, then both blocks will instead be disconnected from eachother.
    protected void setConnection(TransferBlock connection) {
        String connectionValidityMessage = this.checkConnectionValid(connection);
        if (connectionValidityMessage != null) {
            throw new IllegalArgumentException(connectionValidityMessage);
        }
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
        TransferBlock connectionOld = this.connection;
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
    //Removes the connection this block has if it exists.
    protected void removeConnection() {
        this.setConnection(null);
    }
    //Returns the block that this block is connected to if it exists
    public TransferBlock getConnection() {
        return this.connection;
    }

    //A transfer block could be considered a transporter if it has the capabillity to offer entering blocks
    //exit points once they have a connection.
    abstract public boolean isTransporter();
    //Returns an int[][] where every contained i[] represents the transporter's entry point coordinates where x = int[0], and y= int[1]
    abstract public int[][] getEntryPointsXY(); 
    //Returns an int[] that represents the transporter's exit point coordinates where x = int[0], and y= int[1]
    abstract public int[] getExitPointXY(BlockAbstract entryBlock); 
    //Returns the direction in which an entering block is moved out from the exit transporter
    public int[] getExitDirectionXY(BlockAbstract entryBlock) {
        if (this.connection == null) {
            return null;
        }
        int[] exitPorterXY = this.getConnection().getCoordinatesXY();
        int[] exitPointXY = this.getExitPointXY(entryBlock);
        if (exitPointXY == null) {
            return null;
        }
        return new int[] {exitPointXY[0] - exitPorterXY[0], exitPointXY[1] - exitPorterXY[1]};
    }

    //Checks if this transfer block can be entered by the given block
    public boolean canBlockEnter(BlockAbstract entryBlock) {
        //transfer blocks can not be entered unless they are transporters
        if (!this.isTransporter()) {
            return false;
        }  
        //Further a transporter can only be entered if it is active
        if (this.getState()) {
            //And at last the entring block is only allowed to enter if it is standing
            //at one of the transporter's entry points

            //first retrieve the entering block's current coordinates
            int[] blockPoint = new int[]{entryBlock.getX(), entryBlock.getY()};
            //Then retrieve every entry point coordinate for the transporter
            int[][] entryPoints = this.getEntryPointsXY();
            for (int i = 0; i < entryPoints.length; i++) {
                //If the block's current coordinates are identical to one of the transporter's entry point coordinates,
                //then that transporter can be entered by the given block.
                if (blockPoint[0] == entryPoints[i][0] && blockPoint[1] == entryPoints[i][1]) {
                    return true;
                }
            }
        }
        return false;
    }
}
