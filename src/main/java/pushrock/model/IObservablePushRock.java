package pushrock.model;

public interface IObservablePushRock {
    public void addObserver(IObserverPushRock observer);
    public void removeObserver(IObserverPushRock observer);
    public void notifyObservers();
    
    // public BlockAbstract getTopBlock(int x, int y);
    public int getWidth();
    public int getHeight();
    public int getMoveCount();
}
