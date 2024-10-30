package pushrock.model;

public class TraversableBlock extends BlockAbstract {
    
    //Constructor
    public TraversableBlock(int x, int y, char type) {
        super(x, y, type);
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
                  return "d";
            default:
                return "";
        }
    }
}
