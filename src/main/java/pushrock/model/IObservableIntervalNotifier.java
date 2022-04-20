package pushrock.model;

public interface IObservableIntervalNotifier {
    public void addObserver(IObserverIntervalNotifier observer);
    public void removeObserver(IObserverIntervalNotifier observer);
    public void notifyObservers();

    public int getInterval();
}
