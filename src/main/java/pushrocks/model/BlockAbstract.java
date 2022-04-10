package pushrocks.model;


public abstract class BlockAbstract {

    private char type;
    //Attributes x and y keeps track of current coordinates (x,y) of the block
    private int x;
    private int y;
    private boolean state; 

    //Constructor for coordinates with specified coordinates and type
    public BlockAbstract(int x, int y, char type) {
        this.setState(false);
        this.setType(type);
        this.setX(x);
        this.setY(y);
    }
    
    protected void setX(int x) {
        this.x = x;
    }
    public int getX() {
        return this.x;
    }
    protected void setY(int y) { 
        this.y = y;
    }
    public int getY() {
        return this.y;
    }
    public int[] getCoordinatesXY() {
        return new int[]{this.x, this.y};
    }

    //Set to protected because what characters are considered valid will depend on the sub-class calling the method. 
    protected String getValidTypes() {
        return "";
    }
    private boolean isValidType(char type) {
        return this.getValidTypes().contains(type + "");
    }  
    protected void checkForTypeException(char type) {
        if (!isValidType(type)) {
            throw new IllegalArgumentException("Type '" + type + "' is not a valid for the " + this.getClass().getSimpleName() + " class. Must be one of the : " + this.getValidTypes());
        }
    }
    protected void setTypeCharacter(char type) {
        checkForTypeException(type);
        this.type = type;
    }
    //Setting a block's type will entail more than just setting its character representation, what exactly needs to be 
    //set for each type will be highly individual, thus it is left to the sub-classes to determine the requirements
    //for each type beyond just their character-representation.
    abstract protected void setType(char type);
    public char getType() {
        return this.type;
    }

    protected void setState(boolean state) {
        this.state = state;
    }
    public boolean getState() {
        return this.state;
    }

    abstract public boolean hasCollision(); //Any block will either have collision or not, wether or not they do will depend on the sub-class



}
