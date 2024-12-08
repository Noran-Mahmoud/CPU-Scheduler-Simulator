import java.time.Duration;
import java.time.Instant;

public class process extends Thread {
    private int id;
    private String name;
    private String color;
    private int initialBurstTime;
    private int burstTime;
    private int arrivalTime;
    private int priority;
    private int quantum;
    private int FCAIFactor;
    // private int contextSwitch;
    private double variables[];
    private Object monitor;//lock
    private Ready_Queue readyQueue; 
    private Instant startInst;
    private Instant finishInst;
    private int turnaroundTime;
    private int waitingTime;

    public process(){}

    public process(int id, String name, String color,int burstTime,int arrivalTime,int priority,int quantum,
            double v[], Object monitor, Ready_Queue r){
        this.id = id;
        this.name = name;
        this.color = color;
        this.initialBurstTime = burstTime;
        this.burstTime = burstTime;
        this.arrivalTime =arrivalTime;
        this.quantum = quantum;
        this.priority = priority;
        this.monitor = monitor;
        // this.contextSwitch = contextSwitch;
        this.variables = v;
        this.readyQueue = r;
        FCAIFactor = (10-priority) + (int) Math.ceil(arrivalTime/variables[0]) + (int) Math.ceil(burstTime/variables[1]);
        System.out.println("P" + id + " factor is " + this.FCAIFactor);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(arrivalTime*1000);
            startInst = Instant.now();
            // System.out.println("P" + id + " entered Ready Queue!");
            readyQueue.enterReadyQueue(this);
            
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeProcess(){
        try{
            while(burstTime > 0){
                    
                // System.out.println("P" + id + " entered CPU!");

                int preemptiveTime =(int) Math.ceil(0.4*quantum);
                Thread.sleep(preemptiveTime*1000);
                burstTime  -= preemptiveTime;
                int remainingQ = quantum - preemptiveTime;

                while(remainingQ >0 && burstTime >0){
                    if (readyQueue.hasProcess() && readyQueue.getLessFactor() < this.FCAIFactor) {
                        FCAIFactor = (10-priority) + (int) Math.ceil(arrivalTime/variables[0]) + (int) Math.ceil(burstTime/variables[1]);
                        quantum += remainingQ;
                        // Thread.sleep(contextSwitch * 1000);

                        System.out.println("P" + id + " called notifyNextlessFactor");
                        System.out.println("P" + id + " Remaining Burst Time:" + burstTime + " ,Quantum:" + (quantum-remainingQ) + "->" + quantum + " ,FCAI Factor:" + FCAIFactor);
                        readyQueue.notifyNextlessFactor(this);
                        return;
                    }
                    //check again after executing one sec
                    Thread.sleep( 1000);
                    remainingQ--;
                    burstTime--;
                }
              
                if(burstTime <= 0){
                    FCAIFactor =0;
                    quantum = 0;
                    System.out.println("P" + id + " Completed!");
                    finishInst = Instant.now();
                    turnaroundTime = (int) Duration.between(startInst, finishInst).toSeconds();
                    System.out.print("Turnaround Time is:" + turnaroundTime);
                    waitingTime = turnaroundTime - initialBurstTime;
                    System.out.println(" and Waiting Time:" + waitingTime);
                    readyQueue.leaveReadyQueue(this);
                }
                else if(remainingQ == 0){
                    quantum += 2;
                    FCAIFactor = (10-priority) + (int) Math.ceil(arrivalTime/variables[0]) + (int) Math.ceil(burstTime/variables[1]);
                    System.out.println("P" + id + " called notifyNextEntered");
                    System.out.println("P" + id + " Remaining Burst Time:" + burstTime + " ,Quantum:" + (quantum-2) + "->" + quantum + " ,FCAI Factor:" + FCAIFactor);
                    readyQueue.notifyNextEntered(this);
                }
            }
        }catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getID(){
        return id;
    }

    public int getArrivalTime(){
        return arrivalTime;
    }

    public int getBurstTime(){
        return burstTime;
    }

    public int getFCAIFactor(){
        return FCAIFactor;
    }

    public Object getMonitor(){
        return monitor;
    }

    public int getTurnaround(){
        return turnaroundTime;
    }

    public int getWaitingTime(){
        return waitingTime;
    }
}
