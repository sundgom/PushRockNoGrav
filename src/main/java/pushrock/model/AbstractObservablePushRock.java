package pushrock.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractObservablePushRock {
    private List<IObserverPushRock> observers = new ArrayList<>();


    public void addObserver(IObserverPushRock observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }
    public void removeObserver(IObserverPushRock observer) {
        if (this.observers.contains(observer)) {
            this.observers.remove(observer);
        }
    }
    public void notifyObservers() {
        this.observers.forEach(observer -> observer.update(this));
    } 
}
