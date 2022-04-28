package pushrock.model;

import java.util.ArrayList;
import java.util.List;

public class IntervalNotifier implements Runnable, IObservableIntervalNotifier{
    private List<IObserverIntervalNotifier> observers = new ArrayList<IObserverIntervalNotifier>();
    private int interval;
    private boolean isActive;

    public IntervalNotifier(int interval) {
        if (interval < 100) {
            throw new IllegalArgumentException("Interval must be at least 100 milliseconds.");
        }
        if (interval > 10000) {
            throw new IllegalArgumentException("Interval must be at most 10,000 milliseconds.");
        }
        this.interval = interval;
        this.isActive = true;
    }

    @Override
    public void run() {
        while (this.isActive) {
            try {
                Thread.sleep(this.interval);
                this.notifyObservers();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void addObserver(IObserverIntervalNotifier observer) {
        if (observer == null) {
            return;
        }
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    @Override
    public void removeObserver(IObserverIntervalNotifier observer) {
        if (this.observers.contains(observer)) {
            this.observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        this.observers.forEach(observer -> observer.update(this));
    }
}
