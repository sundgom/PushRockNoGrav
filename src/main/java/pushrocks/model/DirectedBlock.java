package pushrocks.model;

public abstract class DirectedBlock  extends BlockAbstract {

    private String direction;

    //Constructor with specified direction
    public DirectedBlock(int x, int y, char type, String direction) {
        super(x, y, type);
        this.setDirection(direction); 
    }
    
    //Returns a string list of valid directions for this block.
    protected String[] getValidDirections() {
        return new String[] {"up", "down", "left", "right", null};
    }
    //Validation check for direction input.
    private boolean isValidDirection(String direction) {
        String[] validDirections = this.getValidDirections();
        for (String validDirection : validDirections) {
            if (direction == validDirection) {
                return true;
            }
        }
        return false;
    }
    //Sets the direction to the input, a direction set to null indicates that the block is not directed 
    //in a specific direction.
    protected void setDirection(String direction) {
        if (! isValidDirection(direction)) {
            throw new IllegalArgumentException("Input direction is in valid for the " + this.getClass().getSimpleName() + ". Direction must be: " + getValidDirections() + ", but was: " + direction + ".");
        }
        if (direction != null) {
            switch (direction) {
                case "up":
                    this.direction = direction;
                    return;
                case "down":
                    this.direction = direction;
                    return;
                case "right":
                    this.direction = direction;
                    return;
                case "left":
                    this.direction = direction;
                    return;
            }
        }
        else {
            this.direction = null;
        }
    }
    public String getDirection() {
        return this.direction;
    }
    public int[] getDirectionXY() {
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
        return new int[]{0,0};
    }

    @Override
    public boolean hasCollision() {
        return true;
    }
}
