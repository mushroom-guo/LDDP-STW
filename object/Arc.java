package object;

import com.gurobi.gurobi.GRBVar;

public class Arc {
    public Task task;  // 关联的任务
    public Locker locker; //对应物流柜
    public boolean action;  //起飞true降落false
    public double startTime;
    public double endTime;

    public GRBVar start;
    public GRBVar end;

    public Arc copy() {
        Arc newArc = new Arc();
        newArc.task = this.task;
        newArc.locker = this.locker;
        newArc.action = this.action;
        newArc.startTime = this.startTime;
        newArc.endTime = this.endTime;
        return newArc;
    }

}
