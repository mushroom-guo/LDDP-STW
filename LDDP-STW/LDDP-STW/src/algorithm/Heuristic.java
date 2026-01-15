package algorithm;

import calculate.Calculate;
import object.*;
import utils.Data;
import utils.ReadInstance;

import java.util.*;

public class Heuristic {

    public Calculate calculate = new Calculate();
    public Para para = new Para();
    public Problem problem = new Problem();


    //生成粗略解
    public Solution roughSol(Data data) {
        Solution solution = new Solution();
        //初始化数据
        iniData(problem, data);

        //初始化无人机路线列表
        initDroneRoute(problem);

        //任务排序
        sortTask(problem);

        //生成无人机路线
        solution = droneRoute(problem, solution);

        //输出解
//        printSolution(solution, problem);

        return solution;
    }

    //初始化数据
    public void iniData(Problem problem, Data data)
    {
        // 1. 创建映射表，确保同一个原始对象只对应一个新对象副本
        Map<Integer, Locker> lockerMap = new java.util.HashMap<>();

        // 2. 首先深拷贝所有 Locker (基础节点)
        for (Locker oldLocker : data.lockers) {
            Locker newLocker = new Locker();
            newLocker.id = oldLocker.id;
            newLocker.x = oldLocker.x;
            newLocker.y = oldLocker.y;
            // 注意：此时不要拷贝 locker.drone，因为 Drone 副本还没创建

            problem.lockers.add(newLocker);
            lockerMap.put(newLocker.id, newLocker);
        }

        // 3. 深拷贝所有 Drone，并关联到新的 Locker 副本
        for (Drone oldDrone : data.drones) {
            Drone newDrone = new Drone();
            newDrone.id = oldDrone.id;
            newDrone.preTime = oldDrone.preTime;
            newDrone.currentTime = oldDrone.currentTime;

            // 关键：通过 ID 从映射表中找到新 Locker 实例
            if (oldDrone.currentLocker != null) {
                Locker newLocker = lockerMap.get(oldDrone.currentLocker.id);
                newDrone.currentLocker = newLocker;
                newLocker.drone = newDrone; // 建立双向关联
            }

            // 集合必须 new 出新实例，防止共享列表
            newDrone.arcs = new ArrayList<>();
            newDrone.route = new ArrayList<>();
            newDrone.taskList = new ArrayList<>();

            problem.drones.add(newDrone);
        }

        // 4. 深拷贝所有 Task，并关联到新的 Locker 副本
        for (Task oldTask : data.tasks) {
            Task newTask = new Task();
            newTask.taskId = oldTask.taskId;
            newTask.startTime = oldTask.startTime;
            newTask.endTime = oldTask.endTime;
            newTask.arrivalTime = oldTask.arrivalTime;
            newTask.timeCost = oldTask.timeCost;

            // 关键：关联到新创建的 Locker 实例，而不是原始实例
            newTask.startLocker = lockerMap.get(oldTask.startLocker.id);
            newTask.endLocker = lockerMap.get(oldTask.endLocker.id);

            problem.tasks.add(newTask);
        }



//        for (Task task : data.tasks)
//        {
//            problem.tasks.add(deepCopyTask(task));
//        }
//        for (Drone drone : data.drones)
//        {
//            problem.drones.add(deepCopyDrone(drone));
//        }
//        for (Locker locker : data.lockers)
//        {
//            problem.lockers.add(deepCopyLocker(locker));
//        }





//        for (int i = 0; i < Data.tasks.size(); i++) {
//            problem.tasks.add(Data.tasks.get(i));
//        }
//
//        for (int i = 0; i < Data.lockers.size(); i++) {
//            problem.lockers.add(Data.lockers.get(i));
//        }
//
//        for (int i = 0; i < Data.drones.size(); i++) {
//            problem.drones.add(Data.drones.get(i));
//        }
    }

    //将无人机的初始停靠物流柜加入到无人机路线中
    public void initDroneRoute(Problem problem)
    {
        for (Drone drone : problem.drones) {
            drone.route.add(drone.currentLocker);
        }
    }

    //将任务按照时间窗开始时间升序排序
    public void sortTask(Problem problem) {
        Collections.sort(problem.tasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                double time1 = o1.startTime;
                double time2 = o2.startTime;

                return Double.compare(time1, time2);
            }
        });
    }

    //生成无人机路线
    public Solution droneRoute(Problem problem, Solution solution) {
        double currentTime = para.startTime;

        for (Task task : problem.tasks) {
            //任务的起点物流柜没有无人机停靠
            if (task.startLocker.drone == null) {
                Drone drone = selectDrone(task,problem,currentTime);

                //计算时间窗违反成本
                task.arrivalTime = currentTime + 4 * drone.preTime +
                        (calculate.distance(drone.currentLocker, task.startLocker) +
                                calculate.distance(task.startLocker, task.endLocker)) / para.speed;

                //任务起始物流柜的降落动作弧
                Arc arc1 = new Arc();
                arc1.task = null;
                arc1.locker = task.startLocker;
                arc1.action = false;
                arc1.startTime = drone.currentTime;
                arc1.endTime = arc1.startTime + drone.preTime;
                drone.arcs.add(arc1);

                drone.currentTime = arc1.endTime;

                //任务起点物流柜的起飞动作弧
                Arc arc2 = new Arc();
                arc2.task = null;
                arc2.locker = task.startLocker;
                arc2.action = true;
                arc2.startTime = drone.currentTime;
                arc2.endTime = arc2.startTime + drone.preTime;
                drone.arcs.add(arc2);

                drone.currentTime = arc2.endTime;

                drone.route.add(task.startLocker);
                drone.currentLocker = task.startLocker;

                //处理终点物流柜冲突
                if (task.endLocker.drone != null) {
                    handleConflict(task,problem,drone);
                }

                drone.currentTime += calculate.distance(task.startLocker, task.endLocker) / para.speed;

                //增加终点物流柜的降落动作弧
                Arc arc3 = new Arc();
                arc3.task = task;
                arc3.locker = task.endLocker;
                arc3.action = false;
                arc3.startTime = drone.currentTime;
                arc3.endTime = arc3.startTime + drone.preTime;
                drone.arcs.add(arc3);

                drone.currentTime = arc3.endTime;

                drone.taskList.add(task);
                drone.route.add(task.endLocker);

                drone.currentLocker = task.endLocker;
                task.endLocker.drone = drone;
                addDrones(drone, solution);
            }else {
                //任务的起点物流柜有无人机停靠
                Drone drone = task.startLocker.drone;
                task.arrivalTime = currentTime + calculate.distance(task.startLocker, task.endLocker) / para.speed + 2 * drone.preTime;

                //增加起点物流柜的起飞动作弧
                Arc arc1 = new Arc();
                arc1.task = null;
                arc1.action = true;
                arc1.locker = task.startLocker;
                arc1.startTime = currentTime;
                arc1.endTime = arc1.startTime + drone.preTime;
                drone.arcs.add(arc1);

                drone.currentTime = arc1.endTime;
                //处理终点物流柜冲突
                if (task.endLocker.drone != null) {
                    handleConflict(task,problem,drone);
                }

                drone.currentTime += calculate.distance(task.startLocker, task.endLocker) / para.speed;

                //增加终点物流柜的降落动作弧
                Arc arc2 = new Arc();
                arc2.task = task;
                arc2.locker = task.endLocker;
                arc2.action = false;
                arc2.startTime = drone.currentTime;
                arc2.endTime = arc2.startTime + drone.preTime;
                drone.arcs.add(arc2);

                drone.currentTime = arc2.endTime;

                task.startLocker.drone = null;
                drone.currentLocker = task.endLocker;

                drone.taskList.add(task);
                drone.route.add(task.endLocker);

                task.endLocker.drone = drone;

                addDrones(drone, solution);
            }
            currentTime = task.arrivalTime;
        }
        return solution;
    }

    //选择距离任务起始物流柜最近的无人机
    public Drone selectDrone(Task task,Problem problem,Double current)
    {
        double distance = Double.MAX_VALUE;
        Drone drone1 = new Drone();
        for (Drone drone : problem.drones) {
            double cuDistance = calculate.distance(drone.currentLocker, task.startLocker);
            if (cuDistance < distance) {
                distance = cuDistance;
                drone1 = drone;
            }
        }

        //为选择好的无人机添加起飞动作弧
        if (drone1.arcs.size() ==0){

            Arc arc = new Arc();
            arc.task = null;
            arc.action = true;
            arc.locker = drone1.currentLocker;
            arc.startTime = current;
            arc.endTime = current + drone1.preTime;
            drone1.arcs.add(arc);

            drone1.currentTime = arc.endTime;
        }

        drone1.currentTime += calculate.distance(drone1.currentLocker, task.startLocker) / para.speed;

        drone1.currentLocker.drone = null;
        return drone1;
    }

    //处理终点物流柜冲突
    public void handleConflict(Task task,Problem problem,Drone drone)
    {
        double minDistance = Double.MAX_VALUE;
        Locker objLocker = new Locker();
        for (Locker locker : problem.lockers) {
            if (locker.drone == null) {
                double distance = calculate.distance(locker, task.endLocker);
                if (distance < minDistance) {
                    minDistance = distance;
                    objLocker = locker;
                }
            }
        }
        Drone drone1 = task.endLocker.drone;
        //增加终点物流柜的起飞动作弧
        Arc arc = new Arc();
        arc.task =null;
        arc.action = true;
        arc.locker = task.endLocker;
        arc.startTime = drone.currentTime;
        arc.endTime = arc.startTime + drone.preTime;
        drone1.arcs.add(arc);

        drone1.currentTime = arc.endTime + calculate.distance(task.endLocker, objLocker) / para.speed;

        //增加转移后的降落动作弧
        Arc arc1 = new Arc();
        arc1.task = null;
        arc1.action = false;
        arc1.locker = objLocker;
        arc1.startTime = drone1.currentTime;
        arc1.endTime = arc1.startTime + drone.preTime;
        drone1.arcs.add(arc1);

        task.endLocker.drone = null;
        objLocker.drone = drone1;
        drone1.currentLocker = objLocker;
        drone1.route.add(objLocker);

    }

    //计算单个任务的时间窗违反成本
    public double calculateSingleTimeCost(Task task, double arrivalTime)
    {
        //早到的惩罚成本
        if (arrivalTime < task.startTime) {
            task.timeCost = (task.startTime - arrivalTime) * para.cost_early;
        }
        //晚到的惩罚成本
        else if (arrivalTime > task.endTime) {
            task.timeCost = (arrivalTime - task.endTime) * para.cost_late;
        } else {
            task.timeCost = 0.0;
        }
        return task.timeCost;
    }

    //将无人机加入解
    public void addDrones(Drone drone, Solution solution)
    {
        // 检查无人机是否已存在
        boolean exists = false;
        for (int i = 0; i < solution.drones.size(); i++)
        {
            Drone solDrone = solution.drones.get(i);
            if (drone.id == solDrone.id)
            {
                exists = true;
                break;
            }
        }
        // 只有当无人机不存在且总数不超过3个时才添加
        if (!exists) {
            solution.drones.add(drone);
        }
    }

    //计算目标函数值
    public double objVal(Solution solution)
    {
        if (problem == null || problem.tasks == null) {
            return 0.0;  // 如果问题为null，返回0
        }
        
        double totTimeCost = 0.0;
        for (Drone drone : solution.drones)
        {
            for (Task task : drone.taskList)
            {
                task.timeCost = calculateSingleTimeCost(task, task.arrivalTime);
                totTimeCost += task.timeCost;
            }
        }
//        for (Task task : problem.tasks){
//            task.timeCost = calculateSingleTimeCost(task, task.arrivalTime);
//            totTimeCost += task.timeCost;
//        }

        double travelCost = 0.0;
        for (Drone drone : solution.drones){
            for (int i = 0; i < drone.route.size() - 1; i++) {
                travelCost += calculate.distance(drone.route.get(i), drone.route.get(i + 1)) / para.speed * para.Cost_perMinute;
            }
        }
        return totTimeCost + travelCost;
    }

    // 深拷贝Task
    private Task deepCopyTask(Task original) {
        Task copy = new Task();
        copy.taskId = original.taskId;
        copy.startLocker = original.startLocker;
        copy.endLocker = original.endLocker;
        copy.startTime = original.startTime;
        copy.endTime = original.endTime;
        copy.arrivalTime = original.arrivalTime;
        copy.timeCost = original.timeCost;
        return copy;
    }

    public Locker deepCopyLocker(Locker original) {
        Locker copy = new Locker();
        copy.id = original.id;
        copy.x = original.x;
        copy.y = original.y;
        copy.drone = original.drone;
        return copy;
    }

    public Drone deepCopyDrone(Drone original){
        Drone copy = new Drone();
        copy.id = original.id;
        copy.currentTime = original.currentTime;
        copy.currentLocker = original.currentLocker;
        copy.preTime = original.preTime;
        copy.arcs = original.arcs;
        copy.route = original.route;
        copy.taskList = original.taskList;
        return copy;
    }


    //输出解
    public void printSolution(Solution solution,Problem problem)
    {
        System.out.println("============== Drone Tasks List ==============");
        for (Drone drone : solution.drones) {
            //输出无人机任务列表
            System.out.print("Drone " + drone.id + " Tasks List: ");
            for (int i = 0; i < drone.taskList.size(); i++) {
                Task task = drone.taskList.get(i);
                System.out.print(task.taskId + ",");
            }
            System.out.println();
        }

        System.out.println("============ Drone Route List ===========");
        for (Drone drone : solution.drones) {
            //输出无人机路线列表
            System.out.print("Drone " + drone.id + " Route List: ");
            for (int i = 0; i < drone.route.size(); i++) {
                Locker locker = drone.route.get(i);
                System.out.print(locker.id + ",");
            }
            System.out.println();
        }

        System.out.println("============无人机动作弧===============");
        for (Drone drone : solution.drones){
            //输出无人机的起飞降落动作弧
            System.out.println("Drone " + drone.id);
            for (int i = 0; i < drone.arcs.size(); i++) {
                Arc arc = drone.arcs.get(i);
                if (arc.task != null){
                    System.out.print("任务：  " +  arc.task.taskId + "  物流柜：" + arc.locker.id + " 动作："
                    + arc.action + " 时间：(" + arc.startTime + " , " + arc.endTime + ")");
                }else {
                    System.out.print("任务：null"  + " 物流柜：" + arc.locker.id + " 动作："
                            + arc.action + " 时间：(" + arc.startTime + " , " + arc.endTime + ")");
                }
                System.out.println();
            }
        }

        //输出总成本
        System.out.println("============= Total cost ================");
        System.out.println("总成本" + objVal(solution));

    }
}

