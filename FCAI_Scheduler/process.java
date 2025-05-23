import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class process extends Thread {
    private int id;
    private String name;
    private Color color;
    private int initialBurstTime;
    private int burstTime;
    private int arrivalTime;
    private int priority;
    private int quantum;
    private int FCAIFactor;
    private double variables[];
    private Object monitor;//lock
    private Ready_Queue readyQueue; 
    private Instant startInst;
    private Instant finishInst;
    private Instant pause;
    private int turnaroundTime;
    private int waitingTime;
    private ArrayList<ExecutionSegment> timeLine;
    // private int contextSwitch;

    public process(){}

    public process(int id, String name, Color color,int burstTime,int arrivalTime,int priority,int quantum,
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
        this.variables = v;
        this.readyQueue = r;
        pause = null;
        timeLine = new ArrayList<ExecutionSegment>();
        // this.contextSwitch = contextSwitch;
        FCAIFactor = (10-priority) + (int) Math.ceil(arrivalTime/variables[0]) + (int) Math.ceil(burstTime/variables[1]);
        System.out.println(name + " FCAI Factor is " + this.FCAIFactor);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(arrivalTime*1000);
            addSegment(arrivalTime,  Color.WHITE);
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
                    
                if(burstTime == initialBurstTime){
                    Instant now = Instant.now();
                    addSegment((int) Duration.between(startInst, now).toSeconds(), Color.WHITE);
                }
                if(pause != null){
                    Instant now = Instant.now();
                    addSegment((int) Duration.between(pause, now).toSeconds(), Color.WHITE);
                }
                // System.out.println("P" + id + " entered CPU!");

                int preemptiveTime =(int) Math.ceil(0.4*quantum);
                Thread.sleep(preemptiveTime*1000);
                burstTime  -= preemptiveTime;
                int remainingQ = quantum - preemptiveTime;
                addSegment(preemptiveTime, color);

                while(remainingQ >0 && burstTime >0){
                    if (readyQueue.hasProcess() && readyQueue.getLessFactor() < this.FCAIFactor) {
                        FCAIFactor = (10-priority) + (int) Math.ceil(arrivalTime/variables[0]) + (int) Math.ceil(burstTime/variables[1]);
                        quantum += remainingQ;
                        pause = Instant.now();
                        // Thread.sleep(contextSwitch * 1000);

                        System.out.println(name + " called notify Next less Factor");
                        System.out.println(name + " Remaining Burst Time:" + burstTime + " ,Quantum:" + (quantum-remainingQ) + "->" + quantum + " ,FCAI Factor:" + FCAIFactor);
                        readyQueue.notifyNextlessFactor(this);
                        return;
                    }
                    //check again after executing one sec
                    Thread.sleep( 1000);
                    remainingQ--;
                    burstTime--;
                    addSegment(1, color);
                }
              
                if(burstTime <= 0){
                    System.out.println(name + " Completed!");
                    finishInst = Instant.now();
                    turnaroundTime = (int) Duration.between(startInst, finishInst).toSeconds();
                    System.out.print("Turnaround Time:" + turnaroundTime);
                    waitingTime = turnaroundTime - initialBurstTime;
                    System.out.println(" ,Waiting Time:" + waitingTime);
                    readyQueue.leaveReadyQueue(this);
                }
                else if(remainingQ == 0){
                    quantum += 2;
                    FCAIFactor = (10-priority) + (int) Math.ceil(arrivalTime/variables[0]) + (int) Math.ceil(burstTime/variables[1]);
                    System.out.println(name + " called notify Next Entered");
                    System.out.println(name + " Remaining Burst Time:" + burstTime + " ,Quantum:" + (quantum-2) + "->" + quantum + " ,FCAI Factor:" + FCAIFactor);
                    pause = Instant.now();
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

    public String getname(){
        return name;
    }

    public Color getColor(){
        return color;
    }

    public int getArrivalTime(){
        return arrivalTime;
    }

    public int getBurstTime(){
        return burstTime;
    }

    public int getinitBurstTime(){
        return initialBurstTime;
    }

    public int getpriority(){
        return priority;
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

    public ArrayList<ExecutionSegment> getTimeLine(){
        return timeLine;
    }
    private void addSegment(int duration, Color color){
        timeLine.add(new ExecutionSegment(duration, color));
    }
}

class ExecutionSegment {
    int duration;
    Color color;
    public ExecutionSegment(int duration, Color color) {
        this.duration = duration;
        this.color = color;
    }
}
