import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

class MyComparator implements Comparator<process> {
    public int compare(process x, process y){
        return x.getFCAIFactor() - y.getFCAIFactor();
    }
}

class process_data{
    int id;
    String name;
    Color color;
    int burstTime;
    int turnaroundTime;
    int waitingTime;
    ArrayList<ExecutionSegment> timeline;
    process_data(int ID, String n,Color c, int b, int t, int w, ArrayList<ExecutionSegment> e){
        id = ID;
        name = n;
        color = c;
        burstTime = b;
        turnaroundTime = t;
        waitingTime = w;
        timeline = e;
    }
}

public class Ready_Queue {
    private final int size = 1;
    private semaphore CPU = new semaphore(size);
    private LinkedList<process> QueueByArrival = new LinkedList<process>();
    private PriorityQueue<process> QueueByFactor = new PriorityQueue<>(new MyComparator());
    private final Map<process, Object> processMonitors = new HashMap<>();
    private ArrayList<process_data> data = new ArrayList<>();

    public void enterReadyQueue(process p){
        Object monitor = p.getMonitor();
        synchronized(processMonitors){
            processMonitors.put(p, monitor);
        }
        synchronized(QueueByArrival){
            QueueByArrival.addLast(p);
        }
        synchronized(QueueByFactor){
            QueueByFactor.add(p);
        }

        try {
            CPU.enterCPU(p.getMonitor());
            p.executeProcess();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void notifyNextlessFactor(process p){
        Object monitor;
        process newProcess;
        synchronized(QueueByFactor){
            QueueByFactor.remove(p);
            if(!hasProcess()){
                newProcess = p;
            }
            else{
                newProcess = QueueByFactor.poll();
            }
            QueueByFactor.add(p);
        }

        synchronized(QueueByArrival){
            QueueByArrival.remove(p);
            QueueByArrival.addLast(p);
        }
        synchronized(processMonitors){
            monitor = processMonitors.get(newProcess);
        }
        
        CPU.leaveCPU(monitor);
        if(newProcess != p){
            try {
                CPU.enterCPU(p.getMonitor());
                p.executeProcess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void notifyNextEntered(process p){
        Object monitor;
        process newProcess;
        int index;
        synchronized(QueueByArrival){
            index = QueueByArrival.indexOf(p);
            QueueByArrival.remove(p);
            if(QueueByArrival.size() < 1){
                newProcess = p;
            }
            else if(index == QueueByArrival.size()){
                newProcess = QueueByArrival.getFirst();
            }
            else{
                newProcess = QueueByArrival.get(index);
            }
            QueueByArrival.addLast(p);
        }
        //resort
        synchronized(QueueByFactor){
            QueueByFactor.remove(p);
            QueueByFactor.add(p);
        }
        synchronized(processMonitors){
            monitor = processMonitors.get(newProcess);
        }

        CPU.leaveCPU(monitor);
        if(newProcess != p){
            try {
                CPU.enterCPU(p.getMonitor());
                p.executeProcess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void leaveReadyQueue(process p){
        Object monitor;
        process newProcess;
        data.add(new process_data(p.getID(), p.getname(), p.getColor(), p.getinitBurstTime(),
                p.getTurnaround(), p.getWaitingTime(), p.getTimeLine()));
        synchronized(QueueByFactor){
            QueueByFactor.remove(p);
        }
        synchronized(QueueByArrival){
            QueueByArrival.remove(p);
            if(!QueueByArrival.isEmpty())
                newProcess = QueueByArrival.getFirst();
            else newProcess = null;
        }
        synchronized(processMonitors){
            processMonitors.remove(p);
            if(newProcess != null)
                monitor = processMonitors.get(newProcess);
            else monitor = null;
        }
        if(monitor != null){
            CPU.leaveCPU(monitor);
        }
    }

    
    public int getLessFactor(){
        return  QueueByFactor.peek().getFCAIFactor();
    }

    public boolean hasProcess(){
        return !QueueByFactor.isEmpty();
    }

    public ArrayList<process_data> getData(){
        return data;
    }
}
