package pushrock.model;

public class PortalWallBlock extends TransferBlock {

    public PortalWallBlock(int x, int y) {
        //All portal-walls are walls that can hold portals, but only when a wall has been set to hold a portal will it act as a portal,
        //thus portal-walls are constructed with their type set to 'w', direction set to null and connection set to null.
        super(x, y, 'w', null, null);
    }

    @Override
    protected String[] getValidDirections() {
        if (this.isWall()) {
            //walls can only have their direction set to null.
            return new String[]{null};
        }
        else {
            //portals must have their direction set to up, down, right, or left.
            return new String[]{"up", "down", "right", "left"};
        }
    }
    //Valid types include: wall 'w', portal-one 'v', and portal-two 'u'.
    @Override
    protected String getValidTypes() {
        return "wuv";
    }

    //setConnection is kept protected, as portal-wall blocks should only have a connection once its made into a portal.
    @Override
    protected String checkConnectionValid(TransferBlock connection) {
        if (connection != null) {
            if (!(connection instanceof PortalWallBlock && ((PortalWallBlock) connection).isPortal())) {
                return "Connection invalid. A portal can only connect to another portal.";
            }
        }
        return null;
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
    public void setPortal(boolean isPortalOne, String direction, PortalWallBlock connection) {
        if (connection != null && isPortalOne == ((PortalWallBlock) connection).isPortalOne()) {
            throw new IllegalArgumentException("A portal can only connect to another portal of an opposing type.");
        }
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
    
    //The portal wall has its portal cleared if it inhabits one, and is then turned back into its wall form.
    public void clearPortal() {
        if (!this.isPortal()) {
            //If the block is already not a portal, then there is no need for any change.
            return;
        }
        super.removeConnection();
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
        //A portal-wall block must be a portal to be considered a transporter, as when it is a wall-type
        //it will not be capable of having a connection, which in turn means it could not provide an exit point
        //to an entering block.
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
        if (this.getConnection() == null) {
            return null;
        }
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
                return "w";
            case 'v':
                if (this.getState()) {
                    return "ṿ";
                }
                else {
                    return "v";
                }
            case 'u':
                if (this.getState()) {
                    return "ụ";
                }
                else {
                    return "u";
                }

            default:
                return "";
        }
    }
}
