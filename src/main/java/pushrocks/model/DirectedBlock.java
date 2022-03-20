package pushrocks.model;

public abstract class DirectedBlock  extends BlockAbstract {

    private String direction;

    public String getDirection() {
        return this.direction;
    }

    //Sets the direction to the input, a direction set to null indicates that the block is not directed 
    //in a specific direction.
    protected void setDirection(String direction) {
        if (! isValidDirection(direction)) {
            throw new IllegalArgumentException("Invalid input for direction. Direction must be: up, down, left or right, but was: " + direction + ".");
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

    //Returns a string list of valid directions for this block.
    private String[] getValidDirections() {
        String[] validDirections = {"up", "down", "left", "right", null};
        return validDirections;
    }

    //The direction this block should be given when no specific direction is given upon creation.
    protected String getDefaultDirection() {
        return null;
    }

    // //Constructor without specified direction
    // public DirectedBlock(int x, int y, char type) {
    //     super(x, y, type);
    //     this.setDirection(this.getDefaultDirection()); 
    // }

    //Constructor with specified direction
    public DirectedBlock(int x, int y, char type, String direction) {
        super(x, y, type);
        this.setDirection(direction); 
    }
}
