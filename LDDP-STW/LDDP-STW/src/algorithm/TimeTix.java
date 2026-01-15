package algorithm;

import calculate.Calculate;
import com.gurobi.gurobi.*;
import object.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeTix {
//    public Solution currentSol = new Solution();
    public Problem problem = new Problem();
    public Heuristic heuristic = new Heuristic();
    public Calculate calculate = new Calculate();
    public Para para = new Para();
    public boolean feasible = false;
    public double objVal = 10000;


    public double linerTix(Solution solution) throws Exception
    {
        //currentSol = ALNS_copy.Slove();

        arcLocker(solution);

        double timeCost = linerProgram(solution);
        return timeCost;
    }

    //同一物流柜的动作集合
    public void arcLocker(Solution solution)
    {
        List<Arc> arcList = new ArrayList<>();
        for (Locker locker : problem.lockers){
            for (Drone drone : solution.drones) {
                for (Arc arc : drone.arcs) {
                    if (locker.id == arc.locker.id) {
                        arcList.add(arc);
                    }
                }
            }
        }

        Collections.sort(arcList, new Comparator<Arc>() {
            @Override
            public int compare(Arc o1, Arc o2) {
                double time1 = o1.startTime;
                double time2 = o2.startTime;
                return Double.compare(time1, time2);
            }
        });

        for (Locker locker : problem.lockers){
            for (int i = 0; i < locker.arcList.size(); i++) {
                Arc arc = locker.arcList.get(i);
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
    }

    //Gurobi求解
    public double linerProgram(Solution solution) throws Exception {

        GRBEnv env = new GRBEnv();
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();
        GRBModel model = new GRBModel(env);

        //定义变量
        for (Drone drone : solution.drones) {
            for (Arc arc : drone.arcs) {
                //动作弧的开始 结束时间变量
                arc.start = model.addVar(0.0, 10000.0, 0.0, GRB.CONTINUOUS, "start");
                arc.end = model.addVar(0.0, 10000.0, 0.0, GRB.CONTINUOUS, "end");
            }

            //早到 晚到的时间变量
            for (Task task : drone.taskList) {
                task.earlyTime = model.addVar(0.0, 10000.0, 0.0, GRB.CONTINUOUS, "early");
                task.lateTime = model.addVar(0.0, 10000.0, 0.0, GRB.CONTINUOUS, "late");
            }
        }

        //目标函数
        GRBLinExpr objective = new GRBLinExpr();
        for (Drone drone : solution.drones) {
            for (Task task : drone.taskList) {
                objective.addTerm(task.alpha, task.earlyTime);
                objective.addTerm(task.beta, task.lateTime);
            }
        }
        model.setObjective(objective, GRB.MINIMIZE);

        //1.起飞降落动作的时间约束
        for (Drone drone : solution.drones) {
            for (Arc arc : drone.arcs) {
                GRBLinExpr constr1 = new GRBLinExpr();
                constr1.addTerm(-1.0, arc.start);
                constr1.addTerm(1.0, arc.end);
                model.addConstr(constr1, GRB.GREATER_EQUAL, drone.preTime, "constr1");
            }
        }

        //2.同一路径中相邻动作的时间约束
        for (Drone drone : solution.drones) {
            if (drone.arcs.size() > 1) {
                for (int i = 0; i < drone.arcs.size() - 1; i++) {
                    Arc arc1 = drone.arcs.get(i);
                    Arc arc2 = drone.arcs.get(i + 1);
                    GRBLinExpr constr2 = new GRBLinExpr();
                    constr2.addTerm(-1, arc1.end);
                    constr2.addTerm(1, arc2.start);
                    if (arc1.locker == arc2.locker) {
                        model.addConstr(constr2, GRB.EQUAL, 0.0, "constr2");
                    } else {
                        model.addConstr(constr2, GRB.EQUAL, calculate.distance(arc1.locker, arc2.locker) / para.speed, "constr2");
                    }
                }
            }
        }

        //3.同一物流柜动作的时间约束
        for (Locker locker : problem.lockers) {
            if (locker.arcList.size() > 1) {
                for (int i = 0; i < locker.arcList.size() - 1; i++) {
                    Arc arc1 = locker.arcList.get(i);
                    Arc arc2 = locker.arcList.get(i + 1);
                    GRBLinExpr constr3 = new GRBLinExpr();
                    constr3.addTerm(-1, arc1.end);
                    constr3.addTerm(1, arc2.start);
                    model.addConstr(constr3, GRB.GREATER_EQUAL, 0.0, "constr3");
                }
            }
        }

        //4.早到约束
        for (Drone drone : solution.drones) {
            for (int i = 0; i < drone.arcs.size(); i++) {
                Arc arc = drone.arcs.get(i);
                if (arc.task != null) {
                    Task task = arc.task;
                    GRBLinExpr constr4 = new GRBLinExpr();
                    constr4.addTerm(1.0, task.earlyTime);
                    constr4.addTerm(1.0, arc.end);
                    model.addConstr(constr4, GRB.GREATER_EQUAL, task.startTime, "constr4");
                }
            }
        }

        //5.晚到约束
        for (Drone drone : solution.drones) {
            for (int i = 0; i < drone.arcs.size(); i++) {
                Arc arc = drone.arcs.get(i);
                if (arc.task != null) {
                    Task task = arc.task;
                    GRBLinExpr constr5 = new GRBLinExpr();
                    constr5.addTerm(-1.0, task.lateTime);
                    constr5.addTerm(1.0, arc.end);
                    model.addConstr(constr5, GRB.LESS_EQUAL, task.endTime, "constr5");
                }
            }
        }

        //6.完成所有任务的时间约束
        for (Drone drone : solution.drones) {
            for (Arc arc : drone.arcs) {
                GRBLinExpr constr6 = new GRBLinExpr();
                constr6.addTerm(1.0, arc.end);
                model.addConstr(constr6, GRB.LESS_EQUAL, para.endTime, "constr6");
            }
        }

        model.optimize();
        if (model.get(GRB.IntAttr.Status) == GRB.OPTIMAL) {
            feasible = true;
            // 目标值变为「总违反成本」
            objVal = model.get(GRB.DoubleAttr.ObjVal);
//            System.out.println("Feasible, Total Time Window Violation Cost = " + objVal);
        }else {
            System.out.println("Solution infeasible.");
        }
        double timeCost = objVal;

        model.dispose();
        env.dispose();
        return timeCost;
    }
}
