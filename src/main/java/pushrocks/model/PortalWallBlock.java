package pushrocks.model;

public class PortalWallBlock extends ObstacleBlock {

    public PortalWallBlock(int x, int y, char type, String direction, ObstacleBlock connection) {
        super(x, y, type, direction, connection);
        if (this.isPortal()) {
            this.setPortal(this.isPortalOne(), direction, connection);
        }
    }
    
    public void setWall() {
        this.setTypeCharacter('w');
        this.setState(false);
        this.setConnection(null);
        this.setDirection(null);
    }
    public boolean isWall() {
        return this.getType() == 'w';
    }
    public void setPortal(boolean isPortalOne, String direction, ObstacleBlock connection) {
        if (isPortalOne) {
            this.setTypeCharacter('v');
        }
        else {
            this.setTypeCharacter('u');
        }
        this.setState(false);
        this.setConnection(connection);
        this.setDirection(direction);
    }
    public boolean isPortal() { 
        return (this.isPortalOne() || this.isPortalTwo());
    }
    public boolean isPortalOne() {
        return (this.getType() == 'v');
    }
    public boolean isPortalTwo() { 
        return (this.getType() == 'u');
    }
    //Removes the connection this block 
    public void clearPortal() {
        if (! this.isPortal()) {
            throw new IllegalArgumentException("Block must be of type 'portal' to be able to clear portal");
        }
        this.removeConnection();
        this.setWall(); 
    }

    @Override
    protected void setType(char type) {
        switch (type) {
            case 'w':
                setWall();
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

    public int[] getPortalEntryPointXY() {
        // Only portals and teleporters can have an exit point
        if ( !(this.isPortal() || this.isTeleporter())) {
            System.out.println("getPortalExitPointXY is only valid for portals and teleporters.");
            return null;
        }
        if ( this.isTeleporter()) {
            return null; //maybe I shouldnt let teleporters use this method..
        }
        // A portal that is not connected will have no exit point
        if ( this.getConnection() == null) {
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
    public int[] getPortalExitPointXY() {
        if (this.getConnection() == null) {
            return null;
        }
        return this.getConnection().getPortalEntryPointXY();
    }
    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point,
    //but only if the given entryBlock matches the coordinates for the portal's entry point
    public int[] getPortalExitPointXY(BlockAbstract entryBlock) {
        if (this.getConnection() == null) {
            return null;
        }
        //If the given entry block is not standing at the portal's entry point, then
        //return null as to indicate that the block would not be able to enter the portal from its current position
        int[] entryBlockCoordinates = new int[]{entryBlock.getX(), entryBlock.getY()};
        if ( !(entryBlockCoordinates[0] == this.getPortalEntryPointXY()[0] && entryBlockCoordinates[1] == this.getPortalEntryPointXY()[1]) ) {
            return null;
        }
        return this.getConnection().getPortalEntryPointXY();
    }

    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point
    public int[] getExitPointXY(BlockAbstract entryBlock) {
        if (this.isPortal()) {
            return this.getPortalExitPointXY(entryBlock);
        }
        else if (this.isTeleporter()) {
            return this.getTeleporterExitPointXY(entryBlock);
        }
        else {
            return null;
        }
    }
    public int[] getExitDirectionXY(BlockAbstract entryBlock) {
        int[] exitPorterXY = this.getCoordinatesXY();
        int[] exitPointXY = this.getExitPointXY(entryBlock);
        return new int[] {exitPointXY[0] - exitPorterXY[0], exitPointXY[1] - exitPorterXY[1]};
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
                System.out.println(this.getPortalEntryPointXY());
                System.out.println(blockPoint == this.getPortalEntryPointXY());
                System.out.println("Port x" + this.getPortalEntryPointXY()[0] + "y" + this.getPortalEntryPointXY()[1]);
                System.out.println("Bloc x" + blockPoint[0] + "y" + blockPoint[1]);
                System.out.println(blockPoint[0] == this.getPortalEntryPointXY()[0] && blockPoint[1] == this.getPortalEntryPointXY()[1]);
                return blockPoint[0] == this.getPortalEntryPointXY()[0] && blockPoint[1] == this.getPortalEntryPointXY()[1];
            }
            else {
                //A teleporter will have four entry points
                int[][] entryPoints = this.getTeleporterEntryPointsXY();
                for (int i = 0; i < 4; i++) {
                    if (blockPoint[0] == entryPoints[i][0] && blockPoint[1] == entryPoints[i][1]) {
                        return true;
                    }
                }
            }
        }
        return false;
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
