package pushrocks.model;

public interface IObservablePushRocks {
    public void addObserver(IObserverPushRocks observer);
    public void removeObserver(IObserverPushRocks observer);
    public void notifyObservers();
    
    // public BlockAbstract getTopBlock(int x, int y);
    public int getHeight();
    public int getWidth();
    public int getScore();

    public char getTopBlockType(int x, int y);
    public String getTopBlockDirection(int x, int y); 
    public boolean getTopBlockState(int x, int y);
    public boolean getTopBlockBirdView(int x, int y);
    public String getTopBlockClass(int x, int y);
    
}
