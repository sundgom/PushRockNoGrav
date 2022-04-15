package pushrocks.model;

public class PortalWallBlock extends ObstacleBlock {

    public PortalWallBlock(int x, int y, char type, String direction, ObstacleBlock connection) {
        super(x, y, type, direction, connection);
        if (this.isPortal()) {
            this.setPortal(this.isPortalOne(), direction, connection);
        }
    }
    //Valid types include: wall 'w', portal one 'u', and portal two 'v'.
    @Override
    protected String getValidTypes() {
        return "wuv";
    }
    @Override
    protected String[] getValidDirections() {
        if (this.isWall()) {
            return new String[]{null};
        }
        else {
            return new String[]{"up", "down", "right", "left"};
        }
    }
    private void setWall() {
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
    
    //The portal wall has its portal cleared if it inhabits one, and then turning it into its wall form.
    public void clearPortal() {
        if (!this.isPortal()) {
            //If the block is already not a portal, then there is no need for any change.
            return;
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
    @Override
    public boolean isTransporter() {
        return this.isPortal();
    }

    private int[] getPortalEntryPointXY() {
        // Only transporters can have an exit point
        if (!this.isTransporter()) {
            return null;
        }
        // A transporter that is not connected will have no exit point
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

    public int[][] getEntryPointsXY() {
        int[][] entryPoints = new int[1][2];
        int[] entryPoint = this.getPortalEntryPointXY();
        entryPoints[0][0] = entryPoint[0];
        entryPoints[0][1] = entryPoint[1];
        return entryPoints;
    }

    //The exit point of a given teleporter/portal will be equal to the connected portal's entry point,
    //but only if the given entryBlock matches the coordinates for the portal's entry point
    public int[] getExitPointXY(BlockAbstract entryBlock) {
        if (!this.isTransporter()) {
            return null;
        }
        if (this.getConnection() == null) {
            return null;
        }
        //If the given entry block is not standing at the portal's entry point, then
        //return null as to indicate that the block would not be able to enter the portal from its current position
        int[] entryBlockCoordinates = new int[]{entryBlock.getX(), entryBlock.getY()};
        if ( !(entryBlockCoordinates[0] == this.getPortalEntryPointXY()[0] && entryBlockCoordinates[1] == this.getPortalEntryPointXY()[1]) ) {
            return null;
        }
        return ((PortalWallBlock) this.getConnection()).getPortalEntryPointXY();
    }

    @Override
    public String toString() {
        switch (this.getType()) {
            case 'w':
                return "#";
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
