# CPU-Scheduler-Simulator
This is a special kind of CPU-Scheduler called FCAI-Scheduler.
FCAI Scheduling :
-Traditional CPU scheduling algorithms, like Round Robin (RR) or Priority
Scheduling, often suffer from starvation or inefficiency when handling a mix
of short- and long-burst processes with varying priorities. To address these
limitations, we introduce FCAI Scheduling, an adaptive scheduling algorithm
that combines priority, arrival time, and remaining burst time into a single
FCAI Factor to dynamically manage the execution order and quantum
allocation for processes.

Key Components:
 Dynamic FCAI Factor:
 A composite metric calculated for each process, considering:
o Priority (P)
o Arrival time (AT)
o Remaining burst time (RBT)

FCAI Factor = (10−Priority) + (Arrival Time/V1) + (Remaining Burst Time/V2)
Where:

o V1 = last arrival time of all processes/10
o V2 = max burst time of all processes/10
 Quantum Allocation Rules:
 Each process starts with a unique quantum.

 When processes are preempted or added back to the queue, their quantum is
updated dynamically:
 Q= Q + 2 (if process completes its quantum and still has remaining work)
 Q=Q + unused quantum (if process is preempted)

 Non-Preemptive and Preemptive Execution:
 A process executes non-preemptively for the first 40% of its quantum.
 After 40% execution, preemption is allowed.

Note:
1. All calculations are performed using the ceil function
2. A queue is used for process ordering. If a process executes 40% and is preempted
by another process with a better factor, the preempted process is re-added to the
queue. If the process is not preempted, the next process in the queue will execute.

Program Input:
 Number of processes

For Each Process you need to receive the following parameters from the user:
 Process Name
 Process Color(Graphical Representation)
 Process Arrival Time
 Process Burst Time
 Process Priority Number
 Process Quantum

Program Output:
For each scheduler output the following:
 Processes execution order
 Waiting Time for each process
 Turnaround Time for each process
 Average Waiting Time
 Average Turnaround Time
 Print all history update of quantum time for each process
