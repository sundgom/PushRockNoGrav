package pushrocks.model;

public class TraversableBlock extends BlockAbstract {
    
    private boolean birdView;

    //Constructor
    public TraversableBlock(int x, int y, char type, boolean birdView) {
        super(x, y, type);
        this.birdView = birdView;
    }

    @Override
    protected String getValidTypes() {
        return " d";
    }
    private void setAir() {
        this.setTypeCharacter(' ');
        this.setState(false);
    }
    public boolean isAir() {
        return this.getType() == ' ';
    }
    private void setPressurePlate() {
        this.setTypeCharacter('d');
        this.setState(false);
    }
    public boolean isPressurePlate() {
        return this.getType() == 'd';
    }
    @Override
    protected void setType(char type) {
        switch (type) {
            case ' ':
                this.setAir();
                break;
            case 'd':
                this.setPressurePlate();
                break;
            default:
                checkForTypeException(type);
                break;
        }
    }
    public boolean isBirdView() {
        return this.birdView;
    }
    //Traversable blocks do not have collision in the sense that they can share coordinates with 
    //other blocks, not hindering movement/placement of others.
    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    public String toString() {
        switch (this.getType()) {
            case ' ':
                return " ";
            case 'd':
                return "/";
            default:
                return "";
        }
    }
}
