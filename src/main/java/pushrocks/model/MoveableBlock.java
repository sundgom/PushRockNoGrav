package pushrocks.model;

public class MoveableBlock extends DirectedBlock {

    
    //This line of code could be used as a replacement for the "isMoveable()" check method
    //https://stackoverflow.com/questions/541749/how-to-determine-an-objects-class

    // if (obj instanceof C) {
    //     //your code
    //     }

    //setters for coordinates are have their visibillity increased to public for moveable blocks to enable
    //them to be moved freely after construction.

    //should potentially be set to protected, as to deny complete outsiders from changing it however they wish
    @Override
    public void setX(int x) {
        super.setX(x);
    }
    @Override
    public void setY(int y) {
        super.setY(y);
    }

    public void teleport(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    //Moves this block one step in the given direction
    private boolean up() {
        this.setY(this.getY()+1);
        this.setDirection("up");
        return true;
    }
    private boolean down() {
        this.setY(this.getY()-1);
        this.setDirection("down");
        return true;
    }
    private boolean left() {
        this.setX(this.getX()-1);
        this.setDirection("left");
        return true;
    }
    private boolean right() {
        this.setX(this.getX()+1);
        this.setDirection("right");
        return true;
    }

    //Move this block in the given direction, return true if the block was moved successfully.
    public boolean moveInDirection(String direction) {
        
        this.setDirection(direction); //Contains a validation check for direction

        switch (this.getDirection()) {
            case "up":
                this.up();
                return true;
            case "down":
                this.down();
                return true;
            case "left":
                this.left();
                return true;
            case "right":
                this.right();
                return true;
        }
        return false;
    }


    //Players can move around in the world independently and push other moveable objects
    public void setPlayer() {
        this.setType('p');
        this.setState(false);
        this.setDirection("right"); //chosen default value;
    }

    //Rocks can move around in the world, but only if they have been pushed by another object
    public void setRock() {
        this.setType('r');
        this.setState(false);
        this.setDirection("down");
    }

    //Valid types for this class are player 'p' and rock 'r', expand the pool of valid types 
    //established by the BlockAbstract superclass accordingly.
    @Override
    protected String getValidTypes() {
        return super.getValidTypes() + "pr"; //!!! instead of expanding the pool, restrict it to these values
    }

    @Override
    public void setDirection(String direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Moveable blocks must have a non-neutral direction. Valid directions: up, down, left, right. Given input was: " + direction + ".");
        }
        super.setDirection(direction);
    }


    public boolean isPlayer() {
        return this.getType() == 'p';
    }

    public boolean isRock() {
        return this.getType() == 'r';
    }

    // //Constructor without specified direction
    // public MoveableBlock(int x, int y, char type) {
    //     super(x, y, type);
    // }
    //Constructor with specified direction
    public MoveableBlock(int x, int y, char type, String direction) {
        super(x, y, type, direction);
    }

    //All moveable blocks will also have collision, thus expand the pool of collision types given by the super class accordingly.
    @Override
    public String getCollisionTypes() {
        return super.getCollisionTypes() + getValidTypes();
    }

    public boolean isMoveable() { //THIS SHOULD BE CHANGED, ITS A BANDAID
        return true;
    }

    @Override
    public String toString() {
        switch (this.getType()) {
            case 'p':
                if (this.getState()) {
                    return "p";
                }
                else {
                    return "q";
                }
            case 'r':
                if (this.getState()) {
                    return "Ø";
                }
                else {
                    return "O";
                }
            default:
                return "";
        }
    }
}
