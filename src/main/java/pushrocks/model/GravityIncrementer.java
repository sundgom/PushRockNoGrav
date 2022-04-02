package pushrocks.model;

public class GravityIncrementer implements Runnable, IObserverPushRocks{
    private PushRocks pushRocks;
    private int interval;

    public GravityIncrementer(PushRocks pushRocks, int interval) {
        this.pushRocks = pushRocks;
        this.interval = interval;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(this.interval);
                this.pushRocks.gravityStep(false);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(IObservablePushRocks observable) {
        // TODO Auto-generated method stub
        
    }
    
}
