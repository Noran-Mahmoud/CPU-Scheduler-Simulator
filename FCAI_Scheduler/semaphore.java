public class semaphore {
    
    protected int value = 0;

    protected semaphore(int initial) {
        value = initial;
    }

    public void enterCPU(Object monitor) throws InterruptedException {
        if(value <= 0){
            synchronized(monitor){
                monitor.wait();
            }
        }
        if(value >0){
            synchronized(this){
                value --;
            }
        }
    }

    public void leaveCPU(Object monitor){
        if (monitor != null && value == 0) {
            synchronized(monitor){
                monitor.notify();
                // System.out.println("P" + id + " is notified");
            }
        }

    }

}
