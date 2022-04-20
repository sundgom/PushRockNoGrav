package pushrock.model;

public class TeleporterBlock extends ObstacleBlock {

    //Teleporters only have one valid type ('t') and one valid direction (null), these are therefore set as parameters in
    //the inherited constructor by default and are thus omitted as parameters for the TeleporterBlock constructor.
    //The connection parameter is also omitted for teleporters, as they should start out with no connection.
    public TeleporterBlock(int x, int y) {
        super(x, y, 't', null, null);
    }

    //Valid types include: teleporter 't'.
    @Override
    protected String getValidTypes() {
        return "t";
    }
    @Override
    protected String[] getValidDirections() {
        return new String[]{null};
    }
    
    //Made public as to allow teleporters to be freely connect to a single other teleporter at a time.
    @Override
    public void setConnection(ObstacleBlock connection) {
        if (!(connection instanceof TeleporterBlock || connection == null)) {
            throw new IllegalArgumentException("A teleporter can only be connected to other teleporter blocks.");
        }
        super.setConnection(connection);
    }
    @Override 
    public void removeConnection() {
        super.removeConnection();
    }

    private void setTeleporter() {
        this.setTypeCharacter('t');
        this.setState(false);
        this.setConnection(null);
        this.setDirection(null);
    }
    public boolean isTeleporter() {
        return this.getType() == 't';
    }
    @Override
    protected void setType(char type) {
        switch (type) {
            case 't':
                setTeleporter();
                break;
            default:
                checkForTypeException(type);
        }
    }
    @Override
    public boolean isTransporter() {
        return true;
    }

    //Returns the entry points of this teleporter if they exist
    @Override
    public int[][] getEntryPointsXY() {
        // Only transporters can have an exit point
        if ( !this.isTransporter()) {
            return null;
        }
        // A transporter that is not connected will have no entry point
        if ( this.getConnection() == null) {
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
    //The exit point of a given transporter will be equal to the connected portal's entry point,
    //but only if the given entryBlock matches the coordinates for the portal's entry point
    @Override
    public int[] getExitPointXY(BlockAbstract entryBlock) {
        if (this.getConnection() == null) {
            return null;
        }
        int entryBlockX = entryBlock.getX();
        int entryBlockY = entryBlock.getY();
        int[][] entryPoints = this.getEntryPointsXY();
        for (int x = 0; x < 4; x++) {
        //If the given entry block is not standing at one of the teleporter's four entry points, then
        //return null as to indicate that the block would not be able to enter the portal from its current position
            if ((entryPoints[x][0] == entryBlockX) && (entryPoints[x][1] == entryBlockY)) {
                int[] exitPoint = new int[2];
                exitPoint[0] = ((TeleporterBlock) this.getConnection()).getEntryPointsXY()[3-x][0];
                exitPoint[1] = ((TeleporterBlock)this.getConnection()).getEntryPointsXY()[3-x][1];
                return exitPoint;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        switch (this.getType()) {
            case 't':
                if (this.getState()) {
                    return "+";
                }
                else {
                    return "-";
                }
            default:
                return "";
        }
    }
}
