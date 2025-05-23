import static java.lang.Math.max;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FCAIscheduler extends JFrame{
    public static void main(String[] args) throws FileNotFoundException {
        Ready_Queue readyOueue = new Ready_Queue();
        ArrayList<String[]> Input = new ArrayList<>();
        ArrayList<process> processes = new ArrayList<>();

        Scanner scan = new Scanner(new File("processes.txt"));

        int nProcesses = Integer.parseInt(scan.nextLine());

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
        System.out.println("V1 = " + variables[0] + " ,V2 = " + variables[1]);
        scan.close();

        for(int i =0; i < nProcesses; ++i){
            //[0]Name, [1]Color, [2]Arrival Time, [3]Burst Time, [4]Priority, [5]Quantum
            String name = Input.get(i)[0];
            // Color color = Color.decode(Input.get(i)[1]);
            String colorName = Input.get(i)[1].toUpperCase();
            Color color;
            try {
                color = (Color) Color.class.getField(colorName).get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                color = Color.GRAY; // Default to gray if invalid color name
            }
            int arrival = Integer.parseInt(Input.get(i)[2]);
            int burst = Integer.parseInt(Input.get(i)[3]);
            int prio = Integer.parseInt(Input.get(i)[4]);
            int quantum = Integer.parseInt(Input.get(i)[5]);

            //int id, String name, String color,int burstTime,int arrivalTime,int priority,int quantum,int v[], Object monitor, Ready_Queue readyQueue
            processes.add(new process(i+1, name, color, burst,arrival,prio,quantum,variables,new Object(), readyOueue));
            processes.get(i).start();
        }

        //Clock
        int seconds = 0;
		while(seconds < 40) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			seconds++;			
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
        System.out.println("\nAverage Turnaround Time is: " + aveTurnaround);
        System.out.println("Average Waiting Time is: " + aveWaiting);
        System.out.println();

        FCAIscheduler fcaIscheduler = new FCAIscheduler();
        fcaIscheduler.display(data, aveTurnaround, aveWaiting);

    }

    public void display(ArrayList<process_data> pd, double aveWaiting, double aveTurnaround) {
        setTitle("FCAI Factor Scheduling Graph");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g, pd);
            }
        };
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setPreferredSize(new Dimension(700, 400));

        JPanel infoPanel = createProcessInfoPanel(pd);
        infoPanel.setPreferredSize(new Dimension(260, 400));

        JPanel statsPanel = createStatsPanel(aveWaiting, aveTurnaround);
        statsPanel.setPreferredSize(new Dimension(1000, 50));

        add(graphPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        add(statsPanel, BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private JPanel createProcessInfoPanel(ArrayList<process_data> pd) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setLayout(new GridLayout(0, 1));
        JLabel title = new JLabel("Processes Information", JLabel.CENTER);
        title.setForeground(Color.BLACK);
        panel.add(title);

        for (process_data process : pd) {
            JLabel label = new JLabel(
                String.format("%s (Waiting Time: %d, Turnaround Time: %d)", process.name, process.waitingTime, process.turnaroundTime),
                JLabel.CENTER);
            label.setForeground(process.color);
            panel.add(label);
        }
        return panel;
    }

    private JPanel createStatsPanel(double aveWaiting, double aveTurnaround) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setLayout(new GridLayout(3, 1));

        JLabel statsLabel = new JLabel("Statistics", JLabel.LEFT);
        statsLabel.setForeground(Color.BLACK);
        JLabel avgLabel = new JLabel(
            String.format("AWT: %.2f, ATAT: %.2f", aveWaiting, aveTurnaround));
        avgLabel.setForeground(Color.BLACK);

        panel.add(statsLabel);
        panel.add(avgLabel);
        return panel;
    }

    private void drawGraph(Graphics g, ArrayList<process_data> pd) {
        int x = 40; // Start X position for drawing
        int yStart = 60; // Initial Y position for processes
        int processHeight = 35; // Height of each process bar
        int processSpacing = 40; // Vertical space between processes
        int unitWidth = 14; // Represents 1 second of execution
        int maxTime = 40;

        int rulerY = yStart - 20; // Position ruler above the processes
        g.setColor(Color.BLACK);
        g.drawLine(x, rulerY, x + maxTime * unitWidth, rulerY); // Horizontal ruler line

        for (int i = 0; i <= maxTime; i++) {
            int markerX = x + i * unitWidth;
            g.drawLine(markerX, rulerY - 5, markerX, rulerY + 5); // Tick marks
            if (i % 5 == 0) { // Label every 5 seconds
                g.drawString(String.valueOf(i), markerX - 5, rulerY - 10); // Time labels
            }
        }
    
        for (process_data process : pd) {
            int y = yStart + (process.id - 1) * processSpacing; // Position based on process ID
            int currentTime = 0;
    
            // Simulate execution with preemptive time slices
            for (ExecutionSegment segment : process.timeline) {
                g.setColor(segment.color);
                int segmentWidth = segment.duration * unitWidth;
                g.fillRect(x + currentTime * unitWidth, y, segmentWidth, processHeight);
                if(segment.color == Color.WHITE)g.setColor(Color.WHITE);
                else g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x + currentTime * unitWidth, y, segmentWidth, processHeight);
    
                currentTime += segment.duration; // Advance current time
            }
        }
    }
    
}
