package pushrocks.model;

public class TeleporterBlock extends ObstacleBlock {

    public TeleporterBlock(int x, int y, char type, String direction, ObstacleBlock connection) {
        super(x, y, type, direction, connection);
        //TODO Auto-generated constructor stub
    }

    //Valid obstacle types include: wall 'w', teleporter 't', portal one 'u', and portal two 'v'.
    @Override
    protected String getValidTypes() {
        return "t";
    }
    public void setTeleporter() {
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
            case 'w':
                setWall();
                break;
            case 't':
                setTeleporter();
                break;
            //portals must always have a specified direction, "right" will be given as a default value
            case 'v':
                setPortal(true, "right", null); 
                break;
            case 'u':
                setPortal(false, "right", null);
                break;
            default:
                checkForTypeException(type);
        }
    }

    //Returns the entry point of the given portal if it exists
    public int[][] getTeleporterEntryPointsXY() {
        // Only portals and teleporters can have an exit point
        if ( !this.isTeleporter()) {
            System.out.println("getTeleporterEntryPoint is only valid for portals and teleporters.");
            return null;
        }
        // A portal that is not connected will have no entry point
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

    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point
    public int[] getTeleporterExitPointXY(BlockAbstract entryBlock) {
        if (this.getConnection() == null) {
            return null;
        }
        if (!this.isTeleporter()) {
            return null;
        }
        int entryBlockX = entryBlock.getX();
        int entryBlockY = entryBlock.getY();
        int[][] entryPoints = this.getTeleporterEntryPointsXY();
        for (int x = 0; x < 4; x++) {
            if ((entryPoints[x][0] == entryBlockX) && (entryPoints[x][1] == entryBlockY)) {
                int[] exitPoint = new int[2];
                exitPoint[0] = this.getConnection().getTeleporterEntryPointsXY()[3-x][0];
                exitPoint[1] = this.getConnection().getTeleporterEntryPointsXY()[3-x][1];
                return exitPoint;
            }
        }
        return null;
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

            //A teleporter will have four entry points
            int[][] entryPoints = this.getTeleporterEntryPointsXY();
            for (int i = 0; i < 4; i++) {
                if (blockPoint[0] == entryPoints[i][0] && blockPoint[1] == entryPoints[i][1]) {
                    return true;
                }
            }
        }
        return false;
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
