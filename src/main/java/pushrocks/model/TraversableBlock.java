package pushrocks.model;

public class TraversableBlock extends BlockAbstract {
    private boolean birdView;

    public void setAir() {
        this.setType(' ');
        this.setState(false);
    }

    public void setPlate() {
        this.setType('d');
        this.setState(false);
    }

    public boolean isBirdView() {
        return this.birdView;
    }

    private void setBirdView(boolean birdView) {
        this.birdView = birdView;
    }

    public TraversableBlock(int x, int y, char type, boolean birdView) {
        super(x, y, type);
        this.setBirdView(birdView);
    }

    public boolean isFloor() {
        return this.getType() == ' ';
    }

    public boolean isPlate() {
        return this.getType() == 'd';
    }

    @Override
    protected String getValidTypes() {
        return " d";
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
    public static void main(String[] args) {
        System.out.println(Character.isUpperCase('.'));

        String directionLayout = "Hello There";
        
        for (int i = 0; i < directionLayout.length(); i++) {
            String direction = directionLayout.substring(i, i+1);
            System.out.println(direction);
        }
        
    }
}
