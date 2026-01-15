package object;

import java.util.ArrayList;
import java.util.List;

public class Drone {
    public int id;
    public Locker currentLocker; //无人机当前所在物流柜
    public double startTime = 400.0;  //无人机开始工作时间
    public double preTime = 2;  //无人机起飞降落准备时间
    public double currentTime;   //无人机的当前时间
    public List<Arc> arcs = new ArrayList<>();  //无人机的起飞降落动作
    public List<Locker> route = new ArrayList<>(); //无人机飞行路径
    public List<Task> taskList = new ArrayList<>(); //无人机执行任务的顺序


    public Drone copy() {
        Drone newDrone = new Drone();
        newDrone.id = this.id;
        newDrone.preTime = this.preTime;
        newDrone.currentTime = this.currentTime;
        newDrone.currentLocker = this.currentLocker; // 引用物流柜实例

        // 深拷贝动作弧列表
        newDrone.arcs = new ArrayList<>();
        for (Arc arc : this.arcs) {
            newDrone.arcs.add(arc.copy());
        }

        // 拷贝路径列表（Locker 是静态资源，复制引用即可）
        newDrone.route = new ArrayList<>(this.route);

        // 拷贝任务列表
        newDrone.taskList = new ArrayList<>(this.taskList);

        return newDrone;
    }

}
