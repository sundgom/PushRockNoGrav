package pushrock.model;

public class MoveableBlock extends DirectedBlock {

    //Constructor with specified direction
    public MoveableBlock(int x, int y, char type, String direction) {
        super(x, y, type, direction);
    }

    //setters for coordinates have their visibillity increased to public for moveable blocks as their
    //coordinates should able to be changed dynamically.
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
    //setDirection(..) is a protected method inherited from DirectedBlock, which has it's visibillity increased to public as to allow moveable blocks to change their direction dynamically.
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
        //setDirection(..) will perform a validation check of the input direction before setting it.
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
    public boolean isPlayer() {
        return this.getType() == 'p';
    }
    //Rocks can move around in the world, but only if they have been pushed by another object
    private void setRock() {
        this.setTypeCharacter('r');
        this.setState(false);
        this.setDirection("right"); //chosen default value;
    }
    public boolean isRock() {
        return this.getType() == 'r';
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

    //setState() inherited by BlockAbstract and has its visibillity increased to public as to allow moveable blocks to change their
    //state according to their current situation.
    @Override
    public void setState(boolean state) {
        super.setState(state);
    }

    @Override
    public String toString() {
        switch (this.getType()) {
            case 'p':
                if (this.getState()) {
                    return "q";
                }
                else {
                    return "p";
                }
            case 'r':
                if (this.getState()) {
                    return "o";
                }
                else {
                    return "r";
                }
            default:
                return "";
        }
    }
}
