package pushrocks.model;

public interface IObservablePushRocks {
    public void addObserver(IObserverPushRocks observer);
    public void removeObserver(IObserverPushRocks observer);
    public void notifyObservers();
    
    // public BlockAbstract getTopBlock(int x, int y);
    public int getWidth();
    public int getHeight();
    public int getMoveCount();
}
