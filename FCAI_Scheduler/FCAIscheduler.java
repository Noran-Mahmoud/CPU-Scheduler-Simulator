import static java.lang.Math.max;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FCAIscheduler {
    public static void main(String[] args) throws FileNotFoundException {
        Ready_Queue readyOueue = new Ready_Queue();
        ArrayList<String[]> Input = new ArrayList<>();
        ArrayList<process> processes = new ArrayList<>();

        Scanner scan = new Scanner(new File("processes.txt"));

        int nProcesses = Integer.parseInt(scan.nextLine());
        // int quantum = Integer.parseInt(scan.nextLine());
        // int contextSwitch = Integer.parseInt(scan.nextLine());

        double lastArrivalTime = 0;
        double maxBurstTime = 0;
        double variables[] = {1,1};     //last arrival time/10[0], max burst time/10[1]

        for(int i =0; i < nProcesses; ++i){
            // Name, Color, Arrival Time, Burst Time, Priority, Quantum;
            String processData = scan.nextLine().trim();
            String[] arr = processData.split(" ");
            Input.add(arr);

            lastArrivalTime = max(lastArrivalTime, Integer.parseInt(arr[2]));
            maxBurstTime = max(maxBurstTime, Integer.parseInt(arr[3]));
        }

        variables[0] = lastArrivalTime/10;
        variables[1] = maxBurstTime/10;
        System.out.println("V1=" + variables[0] + " ,V2=" + variables[1]);
        scan.close();

        for(int i =0; i < nProcesses; ++i){
            //[0]Name, [1]Color, [2]Arrival Time, [3]Burst Time, [4]Priority, [5]Quantum
            int arrival = Integer.parseInt(Input.get(i)[2]);
            int burst = Integer.parseInt(Input.get(i)[3]);
            int prio = Integer.parseInt(Input.get(i)[4]);
            int quantum = Integer.parseInt(Input.get(i)[5]);

            //int id, String name, String color,int burstTime,int arrivalTime,int priority,int quantum,int v[], Object monitor, Ready_Queue readyQueue
            processes.add(new process(i+1, Input.get(i)[0], Input.get(i)[1], burst,arrival,prio,quantum,variables,new Object(), readyOueue));
            processes.get(i).start();
        }

        int seconds = 0;
		
		while(seconds < 40) {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			seconds++;
			if(seconds % 60 == 0) {
				seconds = 0;
			}
			
			System.out.println("Second: " + seconds);
		}

        for(process p :processes){
            try {
                p.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ArrayList<process_data> data = readyOueue.getData();
        double aveTurnaround = 0;
        double aveWaiting = 0;
        for(process_data pd: data){
            aveTurnaround += pd.turnaroundTime;
            aveWaiting += pd.waitingTime;
        }

        aveTurnaround /= data.size();
        aveWaiting /= data.size();
        System.out.println("\nAverage Turnaround Time is:" + aveTurnaround);
        System.out.println("Average Waiting Time is:" + aveWaiting);

    }
}