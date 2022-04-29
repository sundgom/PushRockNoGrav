package pushrock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pushrock.model.IObservableIntervalNotifier;
import pushrock.model.IObserverIntervalNotifier;
import pushrock.model.IntervalNotifier;

public class IntervalNotifierTest {

    @Test
    @DisplayName("Check that setting the interval too low throws IllegalArgumentException.")
    public void testConstructorIntervalTooLow() {
        assertThrows(
            IllegalArgumentException.class, 
            () -> new IntervalNotifier(499),
            "Interval notifier should throw IllegalArgumentException when interval is set lower than the minimum value: 500.");
    }
    @Test
    @DisplayName("Check that setting the interval too high throws IllegalArgumentException.")
    public void testConstructorIntervalTooHigh() {
        assertThrows(
            IllegalArgumentException.class, 
            () -> new IntervalNotifier(10001),
            "Interval notifier should throw IllegalArgumentException when interval is set higher than the maximum value: 10000.");
    }
    @Test
    @DisplayName("Check that setting the interval within bounds throws no exception.")
    public void testConstructorIntervalWithinBounds() {
        Assertions.assertDoesNotThrow(
            () -> new IntervalNotifier(500),
            "Interval notifier should not throw any exceptions when interval is set to the lowest limit.");
        Assertions.assertDoesNotThrow(
            () -> new IntervalNotifier(10000),
            "Interval notifier should not throw any exceptions when interval is set to the highest limit.");
    }

    //Create a simple nested class that implements the IObserverIntervalNotifier interface as to test the intervalNotifier without dependencies on more complex classes
    class TestObserver implements IObserverIntervalNotifier {
        private int updateCount;
        public TestObserver() {
            this.updateCount = 0;
        }
        public int getUpdateCount() {
            return this.updateCount;
        }
        @Override
        public void update(IObservableIntervalNotifier observable) {
            this.updateCount++;
        }
    }
    @Test
    @DisplayName("Test that observers are only notified once they have been added as observers.")
    public void testAddObserver() {
        IntervalNotifier intervalNotifier = new IntervalNotifier(1000);
        TestObserver testObserver = new TestObserver();
        assertEquals(0, testObserver.getUpdateCount());
        //make sure an observer is not notified before being added as an observer of this intervalNotifier
        intervalNotifier.notifyObservers();
        assertEquals(0, testObserver.getUpdateCount(), "the observer should not be notified until it has been added as an observer.");
        //Add the testObserver as an observer of the interval notifier
        intervalNotifier.addObserver(testObserver);
        intervalNotifier.notifyObservers();
        //the added observer should now have been notified, and thus ran its update method
        assertEquals(1, testObserver.getUpdateCount(), "the observer should have been added as an observer, and should thus have been notified to update once.");
        intervalNotifier.notifyObservers();
        //The observer should keep getting notified by the interval notifier for as long as it is one of its observers
        assertEquals(2, testObserver.getUpdateCount(), "the observer should have kept getting notified while being one of the interval notifier's observers.");
    }
    @Test
    @DisplayName("Test that observers no longer get notified once they have been removed as observers.")
    public void testRemoveObserver() {
        IntervalNotifier intervalNotifier = new IntervalNotifier(1000);
        TestObserver testObserver = new TestObserver();
        assertEquals(0, testObserver.getUpdateCount());
        //make sure an observer is not notified before being added as an observer of this intervalNotifier
        intervalNotifier.notifyObservers();
        assertEquals(0, testObserver.getUpdateCount());
        //Add the testObserver as an observer of the interval notifier
        intervalNotifier.addObserver(testObserver);
        intervalNotifier.notifyObservers();
        //the added observer should now have been notified, and thus ran its update method
        assertEquals(1, testObserver.getUpdateCount());
        intervalNotifier.removeObserver(testObserver);
        intervalNotifier.notifyObservers();
        //The observer should no longer be notified by the interval notifier once it was removed as an observer.
        assertEquals(1, testObserver.getUpdateCount(), "the observer should not have kept getting notified after it was removed as an observer.");
    }
}