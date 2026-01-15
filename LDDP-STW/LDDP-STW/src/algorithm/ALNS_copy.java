package algorithm;

import calculate.Calculate;
import object.*;
import utils.Data;
import utils.ReadInstance;

import java.util.*;

public class ALNS_copy {
    public Solution currentSol = new Solution();   //当前解
    public Solution bestSol = new Solution();  //最优解
    public Solution candidateSol = new Solution(); //候选解
    public double currentTem = 1200;  //当前温度
    public double minTem = Math.pow(10, -5);  //最低温度
    public double coolingRate = 0.8;   //降温速率
    public int maxIterations = 300;   //最大迭代次数
    public double[] destroyWeights = new double[]{1.0, 1.0, 1.0};   //破坏算子权重
    public double[] repairWeights = new double[]{1.0, 1.0, 1.0};    //修复算子权重
    public int[] destroyCount = new int[destroyWeights.length];  //破坏算子计数
    public int[] repairCount = new int[repairWeights.length];   //修复算子计数
    public double[] destroyScore = new double[destroyWeights.length];  //破坏算子得分
    public double[] repairScore = new double[repairWeights.length];   //修复算子得分
    public int updateFrequency = 100;  // 每100次迭代更新一次权重
    public double reactionFactor = 0.3; // 反应因子，控制权重调整速度
    public double minWeight = 0.1;      // 权重最小值
    public double maxWeight = 10.0;     // 权重最大值
    public Calculate calculate = new Calculate();
    public Para para = new Para();
    public Random random = new Random();
    public Heuristic heuristic = new Heuristic();
    public TimeTix timeTix = new TimeTix();

    //求解主要流程
    public void Solve(Data data) throws Exception{
        Solution solution = heuristic.roughSol(data);
        double currentObjVal = totalCost(solution, timeTix.linerTix(solution));

        bestSol = solution;
        double bestObjVal = currentObjVal;
        currentSol = solution;

        List<Task> tasks = new ArrayList<>(heuristic.problem.tasks);

//        for (Task task : tasks)
//        {
//            System.out.print(task.taskId + ",");
//        }

        // 记录迭代次数，用于周期性更新权重
        int iterationCount = 0;
        while (currentTem > minTem)
        {
            for (int i = 0; i < maxIterations; i++)
            {
                /**
                 * 复制的任务列表！！！！！！！！！！！！！！！！！！
                 *
                 *
                 *
                 * ！！！！！！！！！！！！！！！！！！！！！！！！！
                 */
                List<Task> copiedTasks = new ArrayList<>(tasks);

                //选择破坏算子
                int destroyOperatorIndex = weightRandomSelect(destroyWeights);

                //选择修复算子
                int repairOperatorIndex = weightRandomSelect(repairWeights);

                // 增加算子被选中的次数
                destroyCount[destroyOperatorIndex]++;
                repairCount[repairOperatorIndex]++;

                //应用破坏算子
                List<Task> removedTasks = applyDestroy(destroyOperatorIndex, tasks);

                //应用修复算子
                applyRepair(repairOperatorIndex, tasks, removedTasks);

                //启发式方法得到新解
                translate(candidateSol, tasks, data);
                double timeCost = timeTix.linerTix(candidateSol);

                //计算新解的目标函数值
                double candObjVal = totalCost(candidateSol, timeCost);


                //蒙特卡洛准则
                double score = 0;
                if (candObjVal < bestObjVal)
                {
                    bestObjVal = candObjVal;
                    bestSol = candidateSol.copy();
                    currentSol = candidateSol.copy();
                    currentObjVal = candObjVal;
                    score = 33;
                }
                else if (candObjVal < currentObjVal)
                {
                    // 第二种情况：比当前解好，但不是全局最优
                    currentSol = candidateSol;
                    currentObjVal = candObjVal;
                    score = 20;
                }
                else
                {
                    // 第三种情况：尝试蒙特卡洛准则接受较差解
                    double delta = candObjVal - bestObjVal;
                    double probability = Math.exp(-delta / currentTem);

                    if (random.nextDouble() < probability)
                    {
                        currentObjVal = candObjVal;
                        currentSol = candidateSol;
                        score = 13;
                    }
                    else
                    {
                        // 不接受新解，回滚任务列表
                        tasks = copiedTasks;
                        score = 0;
                    }
                }
                // 更新算子得分
                destroyScore[destroyOperatorIndex] += score;
                repairScore[repairOperatorIndex] += score;

                // 6. 周期性更新权重 (自适应核心)
                if (iterationCount % updateFrequency == 0) {
                    updateWeights();
                }
            }
            currentTem *= coolingRate;
            System.out.println(bestObjVal);
        }
        printSol(bestSol, bestObjVal);
    }

    private void updateWeights() {
        // 更新破坏算子权重
        for (int i = 0; i < destroyWeights.length; i++) {
            if (destroyCount[i] > 0) {
                destroyWeights[i] = (1 - reactionFactor) * destroyWeights[i] +
                        reactionFactor * (destroyScore[i] / destroyCount[i]);
                destroyWeights[i] = Math.max(minWeight, Math.min(maxWeight, destroyWeights[i]));
                // 重置计数和得分
                destroyScore[i] = 0;
                destroyCount[i] = 0;
            }
        }
        // 修复算子同理...
        for (int i = 0; i < repairWeights.length; i++) {
            if (repairCount[i] > 0) {
                repairWeights[i] = (1 - reactionFactor) * repairWeights[i] +
                        reactionFactor * (repairScore[i] / repairCount[i]);
                repairWeights[i] = Math.max(minWeight, Math.min(maxWeight, repairWeights[i]));
                // 重置计数和得分
                repairScore[i] = 0;
                repairCount[i] = 0;
            }
        }
    }

    //根据权重选择
    public int weightRandomSelect(double[] weights) {
        double totalWeight = 0.0;
        for (double weight : weights) {
            totalWeight += weight;
        }
        double randomValue = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue <= cumulativeWeight) {
                return i;
            }
        }
        return weights.length - 1; //默认返回最后一个算子
    }

    //应用破坏算子
    public List<Task> applyDestroy(int operatorIndex, List<Task> tasks) {
        switch (operatorIndex) {
            case 0:
                return randomDestroy(tasks);
            case 1:
                return timeWindowDestroy(tasks);
            case 2:
                return similarDestroy(tasks);
            default:
                throw new IllegalArgumentException("无效的破坏算子索引： " + operatorIndex);
        }
    }

    //应用修复算子
    public void applyRepair(int operatorIndex, List<Task> tasks, List<Task> removedTasks) {
        switch (operatorIndex) {
            case 0:
                randomRepair(tasks, removedTasks);
                break;
            case 1:
                greedyRepair(tasks, removedTasks);
                break;
            case 2:
                regret(tasks,removedTasks);
                break;
            default:
                throw new IllegalArgumentException("无效的修复算子索引： " + operatorIndex);
        }
    }

    //随机破坏算子
    public List<Task> randomDestroy(List<Task> tasks) {
        //随机选择20%的任务编号
        List<Integer> selectTask = new ArrayList<>();
        while (selectTask.size() < tasks.size() * 0.2) {
            int taskId = random.nextInt(tasks.size()) + 1; // 任务编号从1到20
            if (!selectTask.contains(taskId)) {
                selectTask.add(taskId);
            }
        }
//        System.out.println("随机选择的任务： " + selectTask );

        // 创建被移除任务的列表
        List<Task> removedTasks = new ArrayList<>();

        // 遍历任务列表的副本，以避免在遍历时修改列表
//        List<Task> tasksCopy = new ArrayList<>(tasks);
        for (int i = tasks.size() - 1; i >= 0; i--)
        {
            Task task = tasks.get(i);
            if (selectTask.contains(task.taskId)) {
                tasks.remove(task);
                removedTasks.add(task);  // 添加到移除列表
            }
        }
//        for (Task task : tasks) {
//            if (selectTask.contains(task.taskId)) {
//                tasks.remove(task);
//                removedTasks.add(task);  // 添加到移除列表
//            }
//        }
//        System.out.println("==============移除后的无人机任务列表==============");
//        for (int i = 0; i < tasks.size(); i++) {
//            Task task = tasks.get(i);
//            System.out.print(task.taskId + ",");
//        }
//        System.out.println();

        return removedTasks;
        //输出移除后的任务列表

    }

    //时间窗违反程度破坏算子
    public List<Task> timeWindowDestroy(List<Task> tasks) {
        //计算每个任务的时间窗惩罚成本
        List<Task> timeWindowCost = new ArrayList<>();
        Heuristic heuristic = new Heuristic();
        for (Task task : tasks) {
            task.timeCost = heuristic.calculateSingleTimeCost(task, task.arrivalTime);
            timeWindowCost.add(task);
        }

        //按照时间窗违反成本降序排序
        Collections.sort(timeWindowCost, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                double cost1 = o1.timeCost;
                double cost2 = o2.timeCost;

                // 按照降序排序
                if (cost1 < cost2) {
                    return 1; // 如果 cost1 小于 cost2，返回正数，表示 o2 应该排在 o1 前面
                } else if (cost1 > cost2) {
                    return -1; // 如果 cost1 大于 cost2，返回负数，表示 o1 应该排在 o2 前面
                } else {
                    return 0; // 如果 cost1 等于 cost2，返回 0，表示两者顺序不变
                }
            }
        });

        //选择违反成本前20%的任务
        List<Integer> selectTask = new ArrayList<>();
        int num = (int) Math.ceil(tasks.size() * 0.2);
        for (int i = 0; i < num; i++) {
            selectTask.add(timeWindowCost.get(i).taskId);
        }
//        System.out.println("时间窗违反前20%的任务： " + selectTask );

        // 创建被移除任务的列表
        List<Task> removedTasks = new ArrayList<>();

        // 创建任务列表的副本，以避免在遍历时修改列表
//        List<Task> tasksCopy = new ArrayList<>(tasks);
        for (int i = tasks.size() - 1; i >= 0; i--)
        {
            Task task = tasks.get(i);
            if (selectTask.contains(task.taskId)) {
                tasks.remove(task);
                removedTasks.add(task);  // 添加到移除列表
            }
        }
//        for (Task task : tasks) {
//            if (selectTask.contains(task.taskId)) {
//                tasks.remove(task);
//                removedTasks.add(task);  // 添加到移除列表
//            }
//        }
//        System.out.println("==============移除后的无人机任务列表==============");
//        for (int i = 0; i < tasks.size(); i++) {
//            Task task = tasks.get(i);
//            System.out.print(task.taskId + ",");
//        }
//        System.out.println();

        return removedTasks;

        //输出移除后的任务列表

    }

    //相似度破坏算子
//    public List<Task> similarDestroy(List<Task> tasks) {
//        // 计算相似度矩阵
//        double[][] similarityMatrix = calculate.SimilarityMatrix(tasks);
//
//        //随机选择1个任务
//        List<Integer> selectTask = new ArrayList<>();
//        int taskId = random.nextInt(tasks.size());
//        //System.out.println(taskId);
//
//        //创建列表存储所有相似度
//        List<double[]> similarities = new ArrayList<>();
//
//        for (int i = 0; i < tasks.size(); i++) {
//            if (i != taskId) {
//                similarities.add(new double[]{i, similarityMatrix[taskId][i]});
//            }
//        }
//
//        //按相似度降序排序
//        similarities.sort((a, b) -> Double.compare(b[1], a[1]));
//
//        //取前20%
//        int num = (int) Math.ceil(tasks.size() * 0.2);
//        for (int i = 0; i < num; i++) {
//            selectTask.add((int) similarities.get(i)[0]);
//        }
////        System.out.println("相似度前20%的任务： " + selectTask );
//
//        // 创建被移除任务的列表
//        List<Task> removedTasks = new ArrayList<>();
//
//        // 创建任务列表的副本，以避免在遍历时修改列表
////        List<Task> tasksCopy = new ArrayList<>(tasks);
//        for (int i = tasks.size() - 1; i >= 0; i--)
//        {
//            Task task = tasks.get(i);
//            if (selectTask.contains(task.taskId)) {
//                tasks.remove(task);
//                removedTasks.add(task);  // 添加到移除列表
//            }
//        }
////        for (Task task : tasks) {
////            if (selectTask.contains(task.taskId)) {
////                tasks.remove(task);
////                removedTasks.add(task);  // 添加到移除列表
////            }
////        }
//
//        //输出移除后的任务列表
////        System.out.println("==============移除后的无人机任务列表==============");
////        for (int i = 0; i < tasks.size(); i++) {
////            Task task = tasks.get(i);
////            System.out.print(task.taskId + ",");
////        }
////        System.out.println();
//        return removedTasks;
//
//    }

    public List<Task> similarDestroy(List<Task> tasks) {

        // 1. 计算相似度矩阵
        double[][] similarityMatrix = calculate.SimilarityMatrix(tasks);

        // 2. 随机选择一个“种子任务”的索引
        int seedIndex = random.nextInt(tasks.size());
        Task seedTask = tasks.get(seedIndex);

        // 存储最终决定移除的任务ID（使用Set提高查询效率）
        Set<Integer> selectTaskIds = new HashSet<>();
        // 种子任务本身必须移除
        selectTaskIds.add(seedTask.taskId);

        // 3. 创建列表存储其他任务与种子任务的相似度
        // 存储格式：[任务的实际ID, 相似度值]
        List<double[]> similarities = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (i != seedIndex) {
                similarities.add(new double[]{tasks.get(i).taskId, similarityMatrix[seedIndex][i]});
            }
        }

        // 4. 按相似度降序排序
        similarities.sort((a, b) -> Double.compare(b[1], a[1]));

        // 5. 计算总共需要移除的数量 (例如 20%)
        int totalToRemove = (int) Math.ceil(tasks.size() * 0.2);

        // 已经移除了种子任务，还需要再找 totalToRemove - 1 个最相似的任务
        int extraNeeded = totalToRemove - 1;
        for (int i = 0; i < Math.min(extraNeeded, similarities.size()); i++) {
            selectTaskIds.add((int) similarities.get(i)[0]);
        }

        // 6. 执行移除操作
        List<Task> removedTasks = new ArrayList<>();
        // 反向遍历安全删除
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            if (selectTaskIds.contains(task.taskId)) {
                removedTasks.add(task);
                tasks.remove(i);
            }
        }

        return removedTasks;
    }

    //随机修复算子
    public void randomRepair(List<Task> tasks, List<Task> removedTasks) {
        // 将被移除的任务随机插入回问题的任务列表中
        for (int i = 0; i < removedTasks.size(); i++) {
            Task task = removedTasks.get(i);
            int index = -1;
            if (tasks.size() == 0) {
                index = 0;  // 如果任务列表为空，则插入到位置0
            } else {
                index = random.nextInt(tasks.size() + 1);  // +1 以允许插入到末尾
            }
            tasks.add(index, task);
        }
    }

    //计算插入位置的成本
    public class InsertCost {
        public int pos;
        public double totalCost;

        public InsertCost(int pos, double totalCost) {
            this.pos = pos;
            this.totalCost = totalCost;
        }
    }

    //计算每个插入位置的成本
    public List<InsertCost> calculateInsertCost(List<Task> tasks, Task task)
    {
        List<InsertCost> insertCosts = new ArrayList<>();
        for (int j = 0; j <= tasks.size(); j++) {
            double travelCost = 0.0;
            double timeCost = 0.0;
            //插入到第一个位置
            if (j == 0 && tasks.size() > 0) {
                double currentTime = para.startTime;
                travelCost += calculate.distance(task.endLocker, tasks.get(j).startLocker) / para.speed * para.Cost_perMinute;
                task.arrivalTime = currentTime + calculate.distance(task.startLocker, task.endLocker) / para.speed + 4;
                timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
                //插入到最后一个位置
            } else if (j == tasks.size() && tasks.size() > 0) {
                double distance = calculate.distance(tasks.get(j - 1).endLocker, task.startLocker)
                        + calculate.distance(task.startLocker, task.endLocker);
                travelCost += distance / para.speed * para.Cost_perMinute;
                task.arrivalTime = tasks.get(j - 1).arrivalTime + distance / para.speed;
                timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
                //插入到中间位置
            } else if (j > 0 && j < tasks.size()) {
                double distance = calculate.distance(tasks.get(j - 1).endLocker, task.startLocker)
                        + calculate.distance(task.startLocker, task.endLocker);
                travelCost += (distance + calculate.distance(task.endLocker, tasks.get(j).startLocker)
                        - calculate.distance(tasks.get(j - 1).endLocker, tasks.get(j).startLocker))  // 减去原来的距离
                        / para.speed * para.Cost_perMinute;
                task.arrivalTime = tasks.get(j - 1).arrivalTime + distance / para.speed;
                timeCost += heuristic.calculateSingleTimeCost(task, task.arrivalTime);
            }
            double totalCost = travelCost + timeCost;
            insertCosts.add(new InsertCost(j, totalCost));
        }
        return insertCosts;
    }

    //贪婪修复算子
    public void greedyRepair(List<Task> tasks, List<Task> removedTasks)
    {
        for (Task task : removedTasks) {
            // 计算所有插入位置的成本
            List<InsertCost> insertionCosts = calculateInsertCost(tasks, task);

            // 找到成本最小的插入位置
            InsertCost bestInsertion = null;
            double minCost = Double.MAX_VALUE;

            for (InsertCost insertionCost : insertionCosts) {
                if (insertionCost.totalCost < minCost) {
                    minCost = insertionCost.totalCost;
                    bestInsertion = insertionCost;
                }
            }
            // 在最佳位置插入任务
            if (bestInsertion != null) {
                tasks.add(bestInsertion.pos, task);
            }
        }
    }

//    public List<InsertCost> calculateInsertCost(List<Task> tasks, Task task) {
//        List<InsertCost> insertCosts = new ArrayList<>();
//        // 找到最佳插入位置
//        for (int j = 0; j <= tasks.size(); j++) {
//            // 创建临时任务列表用于计算成本
//            List<Task> tempTasks = new ArrayList<>(tasks);
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

    //贪婪修复算子
//    public void greedyRepair(List<Task> tasks, List<Task> removedTasks)
//    {
//        for (Task task : removedTasks) {
//            // 计算所有插入位置的成本
//            List<InsertCost> insertionCosts = calculateInsertCost(problem, task);
//
//            // 找到成本最小的插入位置
//            ALNS.InsertCost bestInsertion = null;
//            double minCost = Double.MAX_VALUE;
//
//            for (ALNS.InsertCost insertionCost : insertionCosts) {
//                if (insertionCost.totalCost < minCost) {
//                    minCost = insertionCost.totalCost;
//                    bestInsertion = insertionCost;
//                }
//            }
//            // 在最佳位置插入任务
//            if (bestInsertion != null) {
//                tasks.add(bestInsertion.pos, task);
//            }
//        }
//    }

    //K-Regret修复算子
    public void regret(List<Task> tasks, List<Task> removedTasks)
    {
        int k = 2;
        while (!removedTasks.isEmpty()) {
            double maxRegret = -Double.MAX_VALUE;
            Task bestTask = null;
            int bestPosition = -1;

            // 计算每个待插入任务的k-regret值
            for (Task task : removedTasks) {
                // 计算所有插入位置的成本
                List<InsertCost> insertionCosts = calculateInsertCost(tasks, task);

                // 按成本升序排序
                insertionCosts.sort(Comparator.comparingDouble(c -> c.totalCost));

                // 计算k-regret值
                double regretValue = 0;
                for (int i = 1; i < k && i < insertionCosts.size(); i++) {
                    regretValue += insertionCosts.get(i).totalCost - insertionCosts.get(0).totalCost;
                }

                // 更新最佳任务和位置
                if (regretValue > maxRegret) {
                    maxRegret = regretValue;
                    bestTask = task;
                    bestPosition = insertionCosts.get(0).pos;
                }
            }

            // 插入regret值最大的任务
            if (bestTask != null) {
                tasks.add(bestPosition, bestTask);
                removedTasks.remove(bestTask);
            }
        }
    }

    //初始化
    public void initDroneRoute(Solution solution, Data data)
    {
        for (Drone drone : data.drones){

            solution.drones.add(drone);
        }
    }

    //解码
//    public void translate(Solution canSol, List<Task> tasks, Data data)
//    {
//        initDroneRoute(canSol, data);
//        double currentTime = para.startTime;
//        List<Locker> lockers = new ArrayList<>();
//        for (Locker locker : data.lockers)
//        {
//            lockers.add(heuristic.deepCopyLocker(locker));
//        }
//
//        for (Task task : tasks) {
//            //任务的起点物流柜没有无人机停靠
//            if (task.startLocker.drone == null) {
//                Drone drone = selectDrone(canSol,task,currentTime);
//
//                //计算时间窗违反成本
//                task.arrivalTime = currentTime + 4 * drone.preTime +
//                        (calculate.distance(drone.currentLocker, task.startLocker) +
//                                calculate.distance(task.startLocker, task.endLocker)) / para.speed;
//
//                //任务起始物流柜的降落动作弧
//                Arc arc1 = new Arc();
//                arc1.task = null;
//                arc1.locker = task.startLocker;
//                arc1.action = false;
//                arc1.startTime = drone.currentTime;
//                arc1.endTime = arc1.startTime + drone.preTime;
//                drone.arcs.add(arc1);
//
//                drone.currentTime = arc1.endTime;
//
//                //任务起点物流柜的起飞动作弧
//                Arc arc2 = new Arc();
//                arc2.task = null;
//                arc2.locker = task.startLocker;
//                arc2.action = true;
//                arc2.startTime = drone.currentTime;
//                arc2.endTime = arc2.startTime + drone.preTime;
//                drone.arcs.add(arc2);
//
//                drone.currentTime = arc2.endTime;
//
//                drone.route.add(task.startLocker);
//                drone.currentLocker = task.startLocker;
//
//                //处理终点物流柜冲突
//                if (task.endLocker.drone != null) {
//                    handleConflict(task,lockers,drone);
//                }
//
//                task.startLocker.drone = null;
//
//                drone.currentTime += calculate.distance(task.startLocker, task.endLocker) / para.speed;
//
//                //增加终点物流柜的降落动作弧
//                Arc arc3 = new Arc();
//                arc3.task = task;
//                arc3.locker = task.endLocker;
//                arc3.action = false;
//                arc3.startTime = drone.currentTime;
//                arc3.endTime = arc3.startTime + drone.preTime;
//                drone.arcs.add(arc3);
//
//                drone.currentTime = arc3.endTime;
//
//                drone.taskList.add(task);
//                drone.route.add(task.endLocker);
//
//                drone.currentLocker = task.endLocker;
//                task.endLocker.drone = drone;
//                heuristic.addDrones(drone, canSol);
//                updateConflict(lockers, task.endLocker, drone);
//            }else {
//                //任务的起点物流柜有无人机停靠
//                Drone drone = task.startLocker.drone;
//                task.arrivalTime = currentTime + calculate.distance(task.startLocker, task.endLocker) / para.speed + 2 * drone.preTime;
//
//                //增加起点物流柜的起飞动作弧
//                Arc arc1 = new Arc();
//                arc1.task = null;
//                arc1.action = true;
//                arc1.locker = task.startLocker;
//                arc1.startTime = currentTime;
//                arc1.endTime = arc1.startTime + drone.preTime;
//                drone.arcs.add(arc1);
//
//                drone.currentTime = arc1.endTime;
//                //处理终点物流柜冲突
//                if (task.endLocker.drone != null) {
//                    handleConflict(task,lockers,drone);
//                }
//                task.startLocker.drone = null;
//
//                drone.currentTime += calculate.distance(task.startLocker, task.endLocker) / para.speed;
//
//                //增加终点物流柜的降落动作弧
//                Arc arc2 = new Arc();
//                arc2.task = task;
//                arc2.locker = task.endLocker;
//                arc2.action = false;
//                arc2.startTime = drone.currentTime;
//                arc2.endTime = arc2.startTime + drone.preTime;
//                drone.arcs.add(arc2);
//
//                drone.currentTime = arc2.endTime;
//
//                task.startLocker.drone = null;
//                drone.currentLocker = task.endLocker;
//
//                drone.taskList.add(task);
//                drone.route.add(task.endLocker);
//
//                task.endLocker.drone = drone;
//
//                heuristic.addDrones(drone, canSol);
//                updateConflict(lockers, task.endLocker, drone);
//            }
//            currentTime = task.arrivalTime;
//        }
//    }


    // 解码方法：将任务序列转换为具体的无人机路径和动作弧
    public void translate(Solution canSol, List<Task> tasks, Data data) {
        // 1. 全局状态重置：确保每一轮迭代都是干净的开始
        canSol.drones.clear();

        // 重置原始 Data 中所有物流柜的占用状态
        for (Locker locker : data.lockers) {
            locker.drone = null;
        }

        // 初始化无人机状态并恢复初始停靠点
        for (Drone d : data.drones) {
            // 使用深拷贝创建一个干净的无人机副本
            Drone droneCopy = heuristic.deepCopyDrone(d);
            droneCopy.currentTime = para.startTime;
            droneCopy.arcs = new ArrayList<>();
            droneCopy.route = new ArrayList<>();
            droneCopy.taskList = new ArrayList<>();

            // 关键：将 droneCopy 的 currentLocker 映射到 data.lockers 中的实例，确保引用唯一
            for (Locker l : data.lockers) {
                if (l.id == droneCopy.currentLocker.id) {
                    droneCopy.currentLocker = l;
                    l.drone = droneCopy; // 初始位置被该无人机占用
                    break;
                }
            }
            droneCopy.route.add(droneCopy.currentLocker);
            canSol.drones.add(droneCopy);
        }

        // 2. 任务分配逻辑
        double currentTime = para.startTime; // 【保留全局时间依赖】

        for (Task task : tasks) {
            // 步骤 A：确保当前任务引用的 Locker 与 data.lockers 里的实例一致
            Locker startLocker = null;
            Locker endLocker = null;
            for (Locker l : data.lockers) {
                if (l.id == task.startLocker.id) startLocker = l;
                if (l.id == task.endLocker.id) endLocker = l;
            }
            // 更新任务对象的内部引用
            task.startLocker = startLocker;
            task.endLocker = endLocker;

            Drone assignedDrone;

            // 情况 1：任务起点目前没有无人机停靠
            if (startLocker.drone == null) {
                // 选择距离起点最近的无人机，传入全局时间作为起飞基准
                assignedDrone = selectDrone(canSol, task, currentTime);

                // 计算 arrivalTime (基于全局 currentTime)
                task.arrivalTime = currentTime + 4 * assignedDrone.preTime +
                        (calculate.distance(assignedDrone.currentLocker, startLocker) +
                                calculate.distance(startLocker, endLocker)) / para.speed;

                // 记录动作弧：降落到起点 + 从起点起飞
                addArc(assignedDrone, null, startLocker, false, assignedDrone.currentTime);
                addArc(assignedDrone, null, startLocker, true, assignedDrone.currentTime);

                assignedDrone.route.add(startLocker);
                assignedDrone.currentLocker = startLocker;

            }
            // 情况 2：任务起点已经有无人机停靠
            else {
                assignedDrone = startLocker.drone;

                // 计算 arrivalTime (基于全局 currentTime)
                task.arrivalTime = currentTime +
                        calculate.distance(startLocker, endLocker) / para.speed +
                        2 * assignedDrone.preTime;

                // 记录起点起飞动作弧
                addArc(assignedDrone, null, startLocker, true, currentTime);
            }

            // 步骤 B：处理终点物流柜冲突
            if (endLocker.drone != null && endLocker.drone != assignedDrone) {
                handleConflict(task, data.lockers, assignedDrone);
            }

            // 步骤 C：执行运输，更新状态
            startLocker.drone = null; // 无人机离开，释放起点

            // 移动到终点
            double flyDistance = calculate.distance(assignedDrone.currentLocker, endLocker);
            assignedDrone.currentTime += flyDistance / para.speed;

            // 记录终点降落动作弧
            addArc(assignedDrone, task, endLocker, false, assignedDrone.currentTime);

            // 更新无人机和物流柜状态
            assignedDrone.taskList.add(task);
            assignedDrone.route.add(endLocker);
            assignedDrone.currentLocker = endLocker;
            endLocker.drone = assignedDrone;

            heuristic.addDrones(assignedDrone, canSol);

            // 步骤 D：更新全局时间依赖，强制下一个任务基于当前结束时间开始
            currentTime = task.arrivalTime;
        }
    }

    // 辅助方法：添加动作弧并更新无人机时间
    private void addArc(Drone drone, Task task, Locker locker, boolean action, double startTime) {
        Arc arc = new Arc();
        arc.task = task;
        arc.locker = locker;
        arc.action = action;
        arc.startTime = startTime;
        arc.endTime = startTime + drone.preTime;
        drone.arcs.add(arc);
        drone.currentTime = arc.endTime;
    }

    //选择距离任务起始物流柜最近的无人机
    public Drone selectDrone(Solution canSol,Task task,Double current)
    {

        double distance = Double.MAX_VALUE;
        Drone drone1 = new Drone();
        for (Drone drone : canSol.drones) {
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
    public void handleConflict(Task task,List<Locker> lockers,Drone drone)
    {
        double minDistance = Double.MAX_VALUE;
        Locker objLocker = new Locker();
        for (Locker locker : lockers) {
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

        updateConflict(lockers, objLocker, drone1);
    }

    //更新物流柜占用信息
    private void updateConflict(List<Locker> lockers, Locker objLocker, Drone drone1) {
        for (Locker locker : lockers)
        {
            if (objLocker.id != locker.id)
            {
                continue;
            }
            else
            {
                locker.drone = drone1;
                break;
            }
        }
    }

    public void printSol(Solution solution, double objVal)
    {
        for (Drone drone : solution.drones)
        {
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

        System.out.println("============ Drone Route List ===========");
        double travelCost = 0.0;
        for (Drone drone : solution.drones) {
            //输出无人机路线列表
            for (int i = 0; i < drone.route.size() - 1; i++) {
                Locker locker1 = drone.route.get(i);
                Locker locker2 = drone.route.get(i + 1);
                travelCost += (calculate.distance(locker1, locker2) / para.speed) * para.Cost_perMinute;
            }
        }
        System.out.println("Travel cost: " + travelCost);
        System.out.println("Total cost: " + objVal);
    }

    //计算目标函数值
    public double totalCost(Solution solution, double timeCost)
    {
        double travelCost = 0.0;
        for (Drone drone : solution.drones) {
            //输出无人机路线列表
            for (int i = 0; i < drone.route.size() - 1; i++) {
                Locker locker1 = drone.route.get(i);
                Locker locker2 = drone.route.get(i + 1);
                travelCost += (calculate.distance(locker1, locker2) / para.speed) * para.Cost_perMinute;
            }
        }
        return travelCost + timeCost;
    }


}