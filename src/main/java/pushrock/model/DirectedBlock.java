package pushrock.model;

public abstract class DirectedBlock  extends BlockAbstract {

    private String direction;

    //Constructor with specified direction
    public DirectedBlock(int x, int y, char type, String direction) {
        super(x, y, type);
        this.setDirection(direction); 
    }
    
    //Returns a string list of valid directions for this block, what directions are valid should be determined by the class extending this class.
    abstract protected String[] getValidDirections();
    
    //Validation check for direction input.
    private boolean isValidDirection(String direction) {
        String[] validDirections = this.getValidDirections();
        for (String validDirection : validDirections) {
            if (validDirection == null) {
                if (direction == validDirection) {
                    return true;
                }
            }
            else {
                if (validDirection.equals(direction)) {
                    return true;
                }
            }
        }
        return false;
    }
    //Sets the direction to the given input if valid, a direction set to null indicates that the block is not directed 
    //in a specific direction.
    protected void setDirection(String direction) {
        if (direction != null && !direction.isBlank()) {
            direction = direction.toLowerCase();
        }
        if (!isValidDirection(direction)) {
            throw new IllegalArgumentException("Input direction is in valid for the " + this.getClass().getSimpleName() + ". Direction must be: " + getValidDirections().toString() + ", but was: " + direction + ".");
        }
        else {
            this.direction = direction;
        }
        
    }
    //Returns the direction as a string
    public String getDirection() {
        return this.direction;
    }
    //Returns the direction as an int[], where int[0] is used for the x-coordiante direction and int[1] for the y-coordinate direction.
    public int[] getDirectionXY() {
        if (this.direction == null) {
            return new int[]{0,0};
        }
        switch (this.direction) {
            case "up":
                return new int[]{0,1};
            case "down":
                return new int[]{0,-1};   
            case "right":
                return new int[]{1,0};
            case "left":
                return new int[]{-1,0};
        }
        return null;
    }

    @Override
    public boolean hasCollision() {
        return true;
    }
}
