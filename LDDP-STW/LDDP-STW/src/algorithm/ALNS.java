//package algorithm;
//
//import calculate.Calculate;
//import object.*;
//import utils.Data;
//
//import java.util.*;
//
//public class ALNS {
//    public Solution currentSol = new Solution();   //当前解
//    public Solution bestSol = new Solution();  //最优解
//    public double currentTem;  //当前温度
//    public double minTem;  //最低温度
//    public double coolingRate;   //降温速率
//    public int maxIterations;   //最大迭代次数
//    public double[] destroyWeights;   //破坏算子权重
//    public double[] repairWeights;    //修复算子权重
//    public int[] destroyCount;  //破坏算子计数
//    public int[] repairCount;   //修复算子计数
//    public double[] destroyScore;  //破坏算子得分
//    public double[] repairScore;   //修复算子得分
//    public int iterationsUpdate = 0;
//    public int updateFrequency = 100;  // 每100次迭代更新一次权重
//    public double reactionFactor = 0.3; // 反应因子，控制权重调整速度
//    public double minWeight = 0.1;      // 权重最小值
//    public double maxWeight = 10.0;     // 权重最大值
//    public Calculate calculate = new Calculate();
//    public Para para = new Para();
//    public static Data data = new Data();
//    public Random random = new Random();
//    public Heuristic heuristic = new Heuristic();
//
//
//    //求解主要流程
//    public void Solve() {
//        Problem addproblem = new Problem();
//        heuristic.roughSol(data);
//
//        initialProblem(addproblem);
//        initialSol();
//        initialPara();
//
//        //初始解
//        double currentObjVal = heuristic.objVal(currentSol, addproblem);
//        double bestObjVal = currentObjVal;
//        bestSol = deepCopySolution(currentSol);
//
//        int totalIterations = 0;
//        while (currentTem >= minTem && totalIterations < 10000) {
//
//            for (int i = 0; i < maxIterations; i++) {
//                Problem problem = new Problem();
//                initialProblem(problem);
//                totalIterations++;
//
//                //选择破坏算子
//                int destroyOperatorIndex = weightRandomSelect(destroyWeights);
//
//                //选择修复算子
//                int repairOperatorIndex = weightRandomSelect(repairWeights);
//
//                // 增加算子被选中的次数
//                destroyCount[destroyOperatorIndex]++;
//                repairCount[repairOperatorIndex]++;
//
//
//
//
//                // 保存当前状态以便回滚
//                Problem originalProblem = deepCopyProblem(problem);
//                Solution originalSolution = deepCopySolution(currentSol);
//                double originalObjVal = currentObjVal;
//
//
//
//
//
//                //应用破坏算子
//                List<Task> removedTasks = applyDestroy(destroyOperatorIndex, problem);
//
//                //应用修复算子
//                applyRepair(repairOperatorIndex, problem, removedTasks);
//
//                //启发式方法得到新解
//                Solution newSol = heuristic.droneRoute(problem);
//
//                //计算新解的目标函数值
//                double newObjVal = evaluateSol(newSol,problem);
//
//                //模拟退火接受准则
//                boolean accepted = acceptSol(newObjVal, currentObjVal);
//
//
//
//
//                double reward = 0.0;
//
//
//
//
//                if (accepted) {
//                    currentSol = deepCopySolution(newSol);
//                    currentObjVal = newObjVal;
//
//                    // 更新最优解
//                    if (newObjVal < bestObjVal) {
//                        bestSol = deepCopySolution(newSol); // 假设Solution有clone方法
//                        bestObjVal = newObjVal;
//                        System.out.println("找到新的最优解: " + bestObjVal);
//
//
//                        reward = 3.0; // 找到新的全局最优解
//                    }else if(newObjVal < originalObjVal){
//                        reward = 1.0; // 改进当前解
//                    } else {
//                        reward = 0.5; // 接受了更差的解
//                    }
//                } else {
//                    // 回滚到原始状态
//                    problem = deepCopyProblem(originalProblem);
//                    currentSol = deepCopySolution(originalSolution);
//                    currentObjVal = originalObjVal;
//                    reward = 0.1;
//                }
//
//                // 计算奖励分数
//                //double reward = calculateReward(currentObjVal, newObjVal, accepted, bestObjVal);
//
//                // 给对应算子加分
//                destroyScore[destroyOperatorIndex] += reward;
//                repairScore[repairOperatorIndex] += reward;
//
//                // 定期更新算子权重
//                iterationsUpdate++;
//                if (iterationsUpdate >= updateFrequency) {
//                    updateWeights();
//                    iterationsUpdate = 0;
//                }
//
//                // 清理被移除的任务列表，准备下一次迭代
//                removedTasks.clear();
//
//            }
//            //降温
//            currentTem *= coolingRate;
//            System.out.println("当前温度: " + currentTem + ", 最优解: " + bestObjVal);
//        }
//        printSol(bestSol, bestObjVal);
//    }
//
//
//
//
//
//    //初始化
//    public void initialProblem(Problem problem) {
//        problem.taskNum = Data.taskNum;
//        problem.tasks.addAll(heuristic.problem.tasks);
//        problem.drones.addAll(heuristic.problem.drones);
//        problem.lockers.addAll(heuristic.problem.lockers);
//    }
//
//    //初始化解
//    public void initialSol() {
//        currentSol = heuristic.solution;
//        bestSol = heuristic.solution;
//    }
//
//    //初始化参数
//    public void initialPara() {
//        currentTem = 1000.0;  //当前温度
//        minTem = 50;
//        coolingRate = 0.5;   //冷却速率
//        maxIterations = 10;  //最大迭代次数
//
//        destroyWeights = new double[]{1.0, 1.0, 1.0};  //破坏算子权重
//        repairWeights = new double[]{1.0, 1.0, 1.0};   //修复算子权重
//
//        destroyCount = new int[destroyWeights.length]; // 3个破坏算子
//        repairCount = new int[repairWeights.length];  // 3个修复算子
//        destroyScore = new double[destroyWeights.length];
//        repairScore = new double[repairWeights.length];
//    }
//
//
//
//
//
//    // 深拷贝Solution
//    private Solution deepCopySolution(Solution original) {
//        // 实现Solution的深拷贝逻辑
//        // 这里需要根据您的Solution类结构实现
//        Solution copy = new Solution();
//        for (int i = 0; i < original.drones.size(); i++)
//        {
//            Drone drone = original.drones.get(i);
//            copy.drones.add(drone);
//        }
//        // 示例：深拷贝drones等
//        return copy;
//    }
//
//    // 深拷贝Problem
//    private Problem deepCopyProblem(Problem original) {
//        // 实现Problem的深拷贝逻辑
//        Problem copy = new Problem();
//        copy.taskNum = original.taskNum;
//        // 深拷贝tasks
//        for (Task task : original.tasks) {
//            copy.tasks.add(deepCopyTask(task));
//        }
//        for (Locker locker : original.lockers){
//            copy.lockers.add(deepCopyLocker(locker));
//        }
//        for (Drone drone : original.drones){
//            copy.drones.add(deepCopyDrone(drone));
//        }
//        // 深拷贝其他属性...
//        return copy;
//    }
//
//    // 深拷贝Task
//    private Task deepCopyTask(Task original) {
//        Task copy = new Task();
//        copy.taskId = original.taskId;
//        copy.startLocker = original.startLocker;
//        copy.endLocker = original.endLocker;
//        copy.startTime = original.startTime;
//        copy.endTime = original.endTime;
//        copy.arrivalTime = original.arrivalTime;
//        copy.timeCost = original.timeCost;
//        return copy;
//    }
//
//    private Locker deepCopyLocker(Locker original) {
//        Locker copy = new Locker();
//        copy.id = original.id;
//        copy.x = original.x;
//        copy.y = original.y;
//        copy.drone = original.drone;
//        return copy;
//    }
//
//    private Drone deepCopyDrone(Drone original){
//        Drone copy = new Drone();
//        copy.id = original.id;
//        copy.currentTime = original.currentTime;
//        copy.currentLocker = original.currentLocker;
//        copy.preTime = original.preTime;
//        copy.arcs = original.arcs;
//        copy.route = original.route;
//        copy.taskList = original.taskList;
//        return copy;
//    }
//
//
//
//
//
//
//    //根据权重选择
//    public int weightRandomSelect(double[] weights) {
//        double totalWeight = 0.0;
//        for (double weight : weights) {
//            totalWeight += weight;
//        }
//        double randomValue = Math.random() * totalWeight;
//        double cumulativeWeight = 0.0;
//        for (int i = 0; i < weights.length; i++) {
//            cumulativeWeight += weights[i];
//            if (randomValue <= cumulativeWeight) {
//                return i;
//            }
//        }
//        return weights.length - 1; //默认返回最后一个算子
//    }
//
//    //应用破坏算子
//    public List<Task> applyDestroy(int operatorIndex, Problem problem) {
//        switch (operatorIndex) {
//            case 0:
//                return randomDestroy(problem);
//            case 1:
//                return timeWindowDestroy(problem);
//            case 2:
//                return similarDestroy(problem);
//            default:
//                throw new IllegalArgumentException("无效的破坏算子索引： " + operatorIndex);
//        }
//    }
//
//    //应用修复算子
//    public void applyRepair(int operatorIndex, Problem problem, List<Task> removedTasks) {
//        switch (operatorIndex) {
//            case 0:
//                randomRepair(problem, removedTasks);
//                break;
//            case 1:
//                greedyRepair(problem, removedTasks);
//                break;
//            case 2:
//                regret(problem,removedTasks);
//                break;
//            default:
//                throw new IllegalArgumentException("无效的修复算子索引： " + operatorIndex);
//        }
//    }
//
//    // 计算奖励分数
//    public double calculateReward(double currentObjVal, double newObjVal, boolean accepted, double bestObjVal) {
//        double delta = newObjVal - currentObjVal;
//
//        if (!accepted) {
//            return 0.1; // 未接受，给很低的分数
//        } else if (delta < 0) {
//            // 新解比当前解好
//            if (newObjVal < bestObjVal) {
//                return 3.0; // 找到新的全局最优解
//            } else {
//                return 1.0; // 改进当前解
//            }
//        } else {
//            // 接受了更差的解（模拟退火）
//            return 0.5;
//        }
//    }
//
//    // 更新算子权重
//    public void updateWeights() {
//        // 更新破坏算子权重
//        for (int i = 0; i < destroyWeights.length; i++) {
//            if (destroyCount[i] > 0) {
//                double averageScore = destroyScore[i] / destroyCount[i];
//                // 更新权重
//                destroyWeights[i] = destroyWeights[i] * (1 - reactionFactor) +
//                        averageScore * reactionFactor;
//
//                // 权重边界检查
//                destroyWeights[i] = Math.max(minWeight, Math.min(maxWeight, destroyWeights[i]));
//            }
//
//            // 重置计数器和分数
//            destroyCount[i] = 0;
//            destroyScore[i] = 0.0;
//        }
//
//        // 更新修复算子权重
//        for (int i = 0; i < repairWeights.length; i++) {
//            if (repairCount[i] > 0) {
//                double averageScore = repairScore[i] / repairCount[i];
//                // 使用指数平滑更新权重
//                repairWeights[i] = repairWeights[i] * (1 - reactionFactor) +
//                        averageScore * reactionFactor;
//
//                // 权重边界检查
//                repairWeights[i] = Math.max(minWeight, Math.min(maxWeight, repairWeights[i]));
//            }
//
//            // 重置计数器和分数
//            repairCount[i] = 0;
//            repairScore[i] = 0.0;
//        }
//
//        // 打印当前权重信息
//        System.out.print("破坏算子权重: ");
//        for (double w : destroyWeights) {
//            System.out.printf("%.2f ", w);
//        }
//        System.out.print(" | 修复算子权重: ");
//        for (double w : repairWeights) {
//            System.out.printf("%.2f ", w);
//        }
//        System.out.println();
//    }
//
//    //评估解的质量
//    public double evaluateSol(Solution solution,Problem problem) {
//        // 使用启发式算法计算目标值
//        return heuristic.objVal(solution, problem);
//    }
//
//    //模拟退火接受准则
//    public boolean acceptSol(double newObjVal, double currentObjVal) {
//        double delta = newObjVal - currentObjVal;
//        if (delta < 0) {
//            return true;
//        } else {
//            double probability = Math.exp(-delta / currentTem);
//            return Math.random() < probability;
//        }
//    }
//
//    //随机破坏算子
//    public List<Task> randomDestroy(Problem problem) {
//        //随机选择20%的任务编号
//        List<Integer> selectTask = new ArrayList<>();
//        while (selectTask.size() < problem.taskNum * 0.2) {
//            int taskId = random.nextInt(problem.taskNum) + 1; // 任务编号从1到20
//            if (!selectTask.contains(taskId)) {
//                selectTask.add(taskId);
//            }
//        }
////        System.out.println("随机选择的任务： " + selectTask );
//
//        // 创建被移除任务的列表
//        List<Task> removedTasks = new ArrayList<>();
//
//        // 遍历任务列表的副本，以避免在遍历时修改列表
//        List<Task> tasksCopy = new ArrayList<>(problem.tasks);
//        for (Task task : tasksCopy) {
//            if (selectTask.contains(task.taskId)) {
//                problem.tasks.remove(task);
//                removedTasks.add(task);  // 添加到移除列表
//            }
//        }
//        return removedTasks;
//
//        //输出移除后的任务列表
////        System.out.println("==============移除后的无人机任务列表==============");
////        for (int i = 0; i < newTaskList.size(); i++) {
////            Task task = newTaskList.get(i);
////            System.out.print(task.taskId + ",");
////        }
//    }
//
//    //时间窗违反程度破坏算子
//    public List<Task> timeWindowDestroy(Problem problem) {
//        //计算每个任务的时间窗惩罚成本
//        List<Task> timeWindowCost = new ArrayList<>();
//        Heuristic heuristic = new Heuristic();
//        for (Task task : problem.tasks) {
//            task.timeCost = heuristic.calculateSingleTimeCost(task, task.arrivalTime);
//            timeWindowCost.add(task);
//        }
//
//        //按照时间窗违反成本降序排序
//        Collections.sort(timeWindowCost, new Comparator<Task>() {
//            @Override
//            public int compare(Task o1, Task o2) {
//                double cost1 = o1.timeCost;
//                double cost2 = o2.timeCost;
//
//                // 按照降序排序
//                if (cost1 < cost2) {
//                    return 1; // 如果 cost1 小于 cost2，返回正数，表示 o2 应该排在 o1 前面
//                } else if (cost1 > cost2) {
//                    return -1; // 如果 cost1 大于 cost2，返回负数，表示 o1 应该排在 o2 前面
//                } else {
//                    return 0; // 如果 cost1 等于 cost2，返回 0，表示两者顺序不变
//                }
//            }
//        });
//
//        //选择违反成本前20%的任务
//        List<Integer> selectTask = new ArrayList<>();
//        int num = (int) Math.ceil(problem.taskNum * 0.2);
//        for (int i = 0; i < num; i++) {
//            selectTask.add(timeWindowCost.get(i).taskId);
//        }
////        System.out.println("时间窗违反前20%的任务： " + selectTask );
//
//        // 创建被移除任务的列表
//        List<Task> removedTasks = new ArrayList<>();
//
//        // 创建任务列表的副本，以避免在遍历时修改列表
//        List<Task> tasksCopy = new ArrayList<>(problem.tasks);
//        for (Task task : tasksCopy) {
//            if (selectTask.contains(task.taskId)) {
//                problem.tasks.remove(task);
//                removedTasks.add(task);  // 添加到移除列表
//            }
//        }
//
//        return removedTasks;
//
//        //输出移除后的任务列表
////        System.out.println("==============移除后的无人机任务列表==============");
////        for (int i = 0; i < newTaskList.size(); i++) {
////            Task task = newTaskList.get(i);
////            System.out.print(task.taskId + ",");
////        }
//    }
//
//    //相似度破坏算子
//    public List<Task> similarDestroy(Problem problem) {
//        // 计算相似度矩阵
//        double[][] similarityMatrix = calculate.SimilarityMatrix(problem);
//
//        //随机选择1个任务
//        List<Integer> selectTask = new ArrayList<>();
//        int taskId = random.nextInt(problem.taskNum);
//        //System.out.println(taskId);
//
//        //创建列表存储所有相似度
//        List<double[]> similarities = new ArrayList<>();
//
//        for (int i = 0; i < problem.taskNum; i++) {
//            if (i != taskId) {
//                similarities.add(new double[]{i, similarityMatrix[taskId][i]});
//            }
//        }
//
//        //按相似度降序排序
//        similarities.sort((a, b) -> Double.compare(b[1], a[1]));
//
//        //取前20%
//        int num = (int) Math.ceil(problem.taskNum * 0.2);
//        for (int i = 0; i < num; i++) {
//            selectTask.add((int) similarities.get(i)[0]);
//        }
////        System.out.println("相似度前20%的任务： " + selectTask );
//
//        // 创建被移除任务的列表
//        List<Task> removedTasks = new ArrayList<>();
//
//        // 创建任务列表的副本，以避免在遍历时修改列表
//        List<Task> tasksCopy = new ArrayList<>(problem.tasks);
//        for (Task task : tasksCopy) {
//            if (selectTask.contains(task.taskId)) {
//                problem.tasks.remove(task);
//                removedTasks.add(task);  // 添加到移除列表
//            }
//        }
//        return removedTasks;
//        //输出移除后的任务列表
////        System.out.println("==============移除后的无人机任务列表==============");
////        for (int i = 0; i < newTaskList.size(); i++) {
////            Task task = newTaskList.get(i);
////            System.out.print(task.taskId + ",");
////        }
//    }
//
//    //随机修复算子
//    public void randomRepair(Problem problem, List<Task> removedTasks) {
//        // 将被移除的任务随机插入回问题的任务列表中
//        for (int i = 0; i < removedTasks.size(); i++) {
//            Task task = removedTasks.get(i);
//            int index;
//            if (problem.tasks.size() == 0) {
//                index = 0;  // 如果任务列表为空，则插入到位置0
//            } else {
//                index = random.nextInt(problem.tasks.size() + 1);  // +1 以允许插入到末尾
//            }
//            problem.tasks.add(index, task);
//        }
//    }
//
//    //贪婪修复算子
////    public void greedyRepair(Problem problem, List<Task> removedTasks)
////    {
////        for (int i = 0; i < removedTasks.size(); i++) {
////            Task task = removedTasks.get(i);
////            double minCost = Double.MAX_VALUE;
////            int bestPos = -1;
////
////            // 找到最佳插入位置
////            for (int j = 0; j <= problem.tasks.size(); j++) {
////                // 创建临时任务列表用于计算成本
////                List<Task> tempTasks = new ArrayList<>(problem.tasks);
////                tempTasks.add(j, task);
////
////                problem.drones.clear();
////                problem.drones.addAll(Data.drones);
////                double travelCost = 0.0;
////                double timeCost = 0.0;
////                //计算该插入位置的成本增量
////                //插入到第一个位置
////                if (j == 0 && problem.tasks.size() > 0) {
////                    double currentTime = para.startTime;
////                    travelCost += calculate.distance(task.endLocker, problem.tasks.get(j).startLocker) / para.speed * para.Cost_perMinute;
////                    task.arrivalTime = currentTime + calculate.distance(task.startLocker, task.endLocker) / para.speed + 4;
////                    timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
////                //插入到最后一个位置
////                } else if (j == problem.tasks.size() && problem.tasks.size() > 0) {
////                    double distance = calculate.distance(problem.tasks.get(j-1).endLocker, task.startLocker)
////                            + calculate.distance(task.startLocker, task.endLocker);
////                    travelCost += distance / para.speed * para.Cost_perMinute;
////                    task.arrivalTime = problem.tasks.get(j-1).arrivalTime + distance / para.speed;
////                    timeCost += heuristic.calculateSingleTimeCost(task,task.arrivalTime);
////                //插入到中间位置
////                } else if (j > 0 && j < problem.tasks.size()) {
////                    double distance = calculate.distance(problem.tasks.get(j-1).endLocker, task.startLocker)
////                            + calculate.distance(task.startLocker, task.endLocker);
////                    travelCost += (distance + calculate.distance(task.endLocker, problem.tasks.get(j).startLocker)
////                            - calculate.distance(problem.tasks.get(j-1).endLocker, problem.tasks.get(j).startLocker))  // 减去原来的距离
////                            / para.speed * para.Cost_perMinute;
////                    task.arrivalTime = problem.tasks.get(j-1).arrivalTime + distance / para.speed;
////                    timeCost += heuristic.calculateSingleTimeCost(task,task.arrivalTime);
////                } else if (problem.tasks.size() == 0) {  // 如果任务列表为空
////                    double currentTime = para.startTime;
////                    travelCost += calculate.distance(task.startLocker, task.endLocker) / para.speed * para.Cost_perMinute;
////                    task.arrivalTime = currentTime + calculate.distance(task.startLocker, task.endLocker) / para.speed + 4;
////                    timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
////                }
////                double totalCost = travelCost + timeCost;
////                if (totalCost < minCost){
////                    minCost = totalCost;
////                    bestPos = j;
////                }
////            }
////            if (bestPos != -1) {
////                problem.tasks.add(bestPos, task);
////            }
////        }
////    }
//
//    //计算插入位置的成本
//    public class InsertCost {
//        public int pos;
//        public double totalCost;
//
//        public InsertCost(int pos, double totalCost) {
//            this.pos = pos;
//            this.totalCost = totalCost;
//
//        }
//    }
//
//    //计算每个插入位置的成本
//    public List<InsertCost> calculateInsertCost(Problem problem, Task task) {
//        List<InsertCost> insertCosts = new ArrayList<>();
//        // 找到最佳插入位置
//        for (int j = 0; j <= problem.tasks.size(); j++) {
//            // 创建临时任务列表用于计算成本
//            List<Task> tempTasks = new ArrayList<>(problem.tasks);
//            tempTasks.add(j, task);
//
//            problem.drones.clear();
//            problem.drones.addAll(Data.drones);
//            double travelCost = 0.0;
//            double timeCost = 0.0;
//            //插入到第一个位置
//            if (j == 0 && problem.tasks.size() > 0) {
//                double currentTime = para.startTime;
//                travelCost += calculate.distance(task.endLocker, problem.tasks.get(j).startLocker) / para.speed * para.Cost_perMinute;
//                task.arrivalTime = currentTime + calculate.distance(task.startLocker, task.endLocker) / para.speed + 4;
//                timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
//                //插入到最后一个位置
//            } else if (j == problem.tasks.size() && problem.tasks.size() > 0) {
//                double distance = calculate.distance(problem.tasks.get(j - 1).endLocker, task.startLocker)
//                        + calculate.distance(task.startLocker, task.endLocker);
//                travelCost += distance / para.speed * para.Cost_perMinute;
//                task.arrivalTime = problem.tasks.get(j - 1).arrivalTime + distance / para.speed;
//                timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
//                //插入到中间位置
//            } else if (j > 0 && j < problem.tasks.size()) {
//                double distance = calculate.distance(problem.tasks.get(j - 1).endLocker, task.startLocker)
//                        + calculate.distance(task.startLocker, task.endLocker);
//                travelCost += (distance + calculate.distance(task.endLocker, problem.tasks.get(j).startLocker)
//                        - calculate.distance(problem.tasks.get(j - 1).endLocker, problem.tasks.get(j).startLocker))  // 减去原来的距离
//                        / para.speed * para.Cost_perMinute;
//                task.arrivalTime = problem.tasks.get(j - 1).arrivalTime + distance / para.speed;
//                timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
//            }
//            double totalCost = travelCost + timeCost;
//            insertCosts.add(new InsertCost(j, totalCost));
//        }
//        return insertCosts;
//    }
//
//    //贪婪修复算子
//    public void greedyRepair(Problem problem, List<Task> removedTasks)
//    {
//        for (Task task : removedTasks) {
//            // 计算所有插入位置的成本
//            List<InsertCost> insertionCosts = calculateInsertCost(problem, task);
//
//            // 找到成本最小的插入位置
//            InsertCost bestInsertion = null;
//            double minCost = Double.MAX_VALUE;
//
//            for (InsertCost insertionCost : insertionCosts) {
//                if (insertionCost.totalCost < minCost) {
//                    minCost = insertionCost.totalCost;
//                    bestInsertion = insertionCost;
//                }
//            }
//            // 在最佳位置插入任务
//            if (bestInsertion != null) {
//                problem.tasks.add(bestInsertion.pos, task);
//            }
//        }
//    }
//
//    //K-Regret修复算子
//    public void regret(Problem problem, List<Task> removedTasks)
//    {
//        int k = 2;
//        while (!removedTasks.isEmpty()) {
//            double maxRegret = -Double.MAX_VALUE;
//            Task bestTask = null;
//            int bestPosition = -1;
//
//            // 计算每个待插入任务的k-regret值
//            for (Task task : removedTasks) {
//                // 计算所有插入位置的成本
//                List<InsertCost> insertionCosts = calculateInsertCost(problem, task);
//
//                // 按成本升序排序
//                insertionCosts.sort(Comparator.comparingDouble(c -> c.totalCost));
//
//                // 计算k-regret值
//                double regretValue = 0;
//                for (int i = 1; i < k && i < insertionCosts.size(); i++) {
//                    regretValue += insertionCosts.get(i).totalCost - insertionCosts.get(0).totalCost;
//                }
//
//                // 更新最佳任务和位置
//                if (regretValue > maxRegret) {
//                    maxRegret = regretValue;
//                    bestTask = task;
//                    bestPosition = insertionCosts.get(0).pos;
//                }
//            }
//
//            // 插入regret值最大的任务
//            if (bestTask != null) {
//                problem.tasks.add(bestPosition, bestTask);
//                removedTasks.remove(bestTask);
//            }
//        }
//    }
//
//    //输出最优解
//    public void printSol(Solution solution, double objVal)
//    {
//        System.out.println("============== Drone Tasks List ==============");
//        for (Drone drone : solution.drones) {
//            //输出无人机任务列表
//            System.out.print("Drone " + drone.id + " Tasks List: ");
//            for (int i = 0; i < drone.taskList.size(); i++) {
//                Task task = drone.taskList.get(i);
//                System.out.print(task.taskId + ",");
//            }
//            System.out.println();
//        }
//
//        System.out.println("============ Drone Route List ===========");
//        for (Drone drone : solution.drones) {
//            //输出无人机路线列表
//            System.out.print("Drone " + drone.id + " Route List: ");
//            for (int i = 0; i < drone.route.size(); i++) {
//                Locker locker = drone.route.get(i);
//                System.out.print(locker.id + ",");
//            }
//            System.out.println();
//        }
//
//        //输出总成本
//        System.out.println("============= Total cost ================");
//        System.out.println("算法结束，最优解: " + objVal);
//    }
//}