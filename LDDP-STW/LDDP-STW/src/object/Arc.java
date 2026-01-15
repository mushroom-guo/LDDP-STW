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
        newArc.task = this.task; // Task 通常作为元数据引用，不需要深拷贝其内部业务逻辑
        newArc.locker = this.locker;
        newArc.action = this.action;
        newArc.startTime = this.startTime;
        newArc.endTime = this.endTime;
        return newArc;
    }

}
