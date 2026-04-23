package algorithm;

public class Para {
    public double Cost_perMinute = 0.1;  //元/分钟
    public double speed = 1000;       //米/分钟
    public double cost_early = 0.2; //早到单位惩罚成本
    public double cost_late = 0.4;  //晚到单位惩罚成本
    public double startTime = 480; //无人机开始执行任务的时间
    public double endTime = 1920; //无人机最晚执行任务结束时间
}
