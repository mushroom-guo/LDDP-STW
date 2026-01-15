package object;

import com.gurobi.gurobi.GRBVar;

public class Task {
    public int taskId;
    public Locker  startLocker; //任务的起始物流柜
    public Locker  endLocker;  //任务的目的物流柜
    public double startTime; //任务的开始时间
    public double endTime;  //任务的结束时间
    public double arrivalTime;  //任务的实际到达时间
    public double timeCost;  //任务的时间窗成本
    public double alpha = 0.6;   //早到惩罚成本系数
    public double beta = 0.4;   //晚到惩罚成本系数

    public GRBVar earlyTime;
    public GRBVar lateTime;
}
