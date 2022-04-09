package pushrocks.model;


public abstract class BlockAbstract {

    private char type;
    //Attributes x and y keeps track of current coordinates (x,y) of the block
    private int x;
    private int y;
    private boolean state; 
    
    public char getType() {
        return this.type;
    }
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public int[] getCoordinatesXY() {
        return new int[]{this.x, this.y};
    }
    public boolean getState() {
        return this.state;
    }
    abstract public boolean hasCollision(); 

    //Constructor for coordinates with specified coordinates and type
    public BlockAbstract(int x, int y, char type) {
        this.setState(false);
        this.setType(type);
        this.setX(x);
        this.setY(y);
    }

    protected void setType(char type) {
        if (! isValidType(type)) {
            throw new IllegalArgumentException("Type '" + type + "' is not a valid for the " + this.getClass().getSimpleName() + " class. Must be one of the : " + this.getValidTypes());
        }
        this.type = type;
    }
    protected void setX(int x) {
        this.x = x;
    }
    protected void setY(int y) { 
        this.y = y;
    }
    protected void setState(boolean state) {
        this.state = state;
    }

    //getValidTypes() is set to protected because what characters are considered valid will depend on the sub-class
    //calling the method. 
    protected String getValidTypes() {
        return "";
    }
    private boolean isValidType(char type) {
        return this.getValidTypes().contains(type + "");
    }  
    
}
