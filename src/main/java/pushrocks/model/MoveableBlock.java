package pushrocks.model;

public class MoveableBlock extends DirectedBlock {

    //Constructor with specified direction
    public MoveableBlock(int x, int y, char type, String direction) {
        super(x, y, type, direction);
    }

    //setters for coordinates have their visibillity increased to public for moveable blocks as they
    //are allowed to change coordinates after construction.

    //should potentially be set to protected, as to deny complete outsiders from changing it however they wish
    @Override
    public void setX(int x) {
        super.setX(x);
    }
    @Override
    public void setY(int y) {
        super.setY(y);
    }
    //Moveable blocks must have a non-neutral direction.
    @Override
    protected String[] getValidDirections() {
        return new String[] {"up", "down", "left", "right"};
    }
    //setType() is a method a protected method inherited by DirectedBlock, which has it's visibillity increased to public as to allow moveable blocks to change their direction dynamically.
    @Override
    public void setDirection(String direction) {
        super.setDirection(direction);
    }

    //Moves this block one step in the given direction
    public boolean up() {
        this.setY(this.getY()+1);
        this.setDirection("up");
        return true;
    }
    public boolean down() {
        this.setY(this.getY()-1);
        this.setDirection("down");
        return true;
    }
    public boolean left() {
        this.setX(this.getX()-1);
        this.setDirection("left");
        return true;
    }
    public boolean right() {
        this.setX(this.getX()+1);
        this.setDirection("right");
        return true;
    }
    //Move this block in the given direction, return true if the block was moved successfully, false if not.
    public boolean moveInDirection(String direction) {
        this.setDirection(direction);
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

    //Valid types for this class are player 'p' and rock 'r'.
    @Override
    protected String getValidTypes() {
        return "pr"; 
    }
    //Players can move around in the world independently and push other moveable objects
    private void setPlayer() {
        this.setTypeCharacter('p');
        this.setState(false);
        this.setDirection("right"); //chosen default value;
    }
    //Rocks can move around in the world, but only if they have been pushed by another object
    private void setRock() {
        this.setTypeCharacter('r');
        this.setState(false);
        this.setDirection("right"); //chosen default value;
    }
    @Override
    protected void setType(char type) {
        switch (type) {
            case 'p':
                setPlayer();
                break;
            case 'r':
                setRock();
                break;
            default:
                checkForTypeException(type);
                break;
        }
    }

    public boolean isPlayer() {
        return this.getType() == 'p';
    }
    public boolean isRock() {
        return this.getType() == 'r';
    }
    public boolean isMoveable() { 
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
