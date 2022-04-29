package pushrock.model;

import java.util.ArrayList;
import java.util.List;

public class IntervalNotifier implements Runnable, IObservableIntervalNotifier{
    private List<IObserverIntervalNotifier> observers = new ArrayList<IObserverIntervalNotifier>();
    private int interval;
    private boolean isActive;

    public IntervalNotifier(int interval) {
        if (interval < 500) {
            throw new IllegalArgumentException("Interval must be at least 250 milliseconds.");
        }
        if (interval > 10000) {
            throw new IllegalArgumentException("Interval must be at most 10,000 milliseconds.");
        }
        this.interval = interval;
        this.isActive = false;
    }

    @Override
    public void run() {
        this.isActive = true;
        while (this.isActive) {
            try {
                Thread.sleep(this.interval);
                this.notifyObservers();
            } catch (InterruptedException e) {
                //notify all observers that the the class was interrupted by issuing them to update with the parameter set to null;
                this.observers.forEach(observer -> observer.update(null));
                this.isActive = false;
                this.observers.clear();
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
