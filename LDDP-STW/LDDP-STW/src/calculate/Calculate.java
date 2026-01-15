package calculate;

import object.Locker;
import object.Problem;
import object.Task;
import utils.Data;

import java.util.ArrayList;
import java.util.List;

public class Calculate {
    double EARTH_RADIUS = 6378137;
    double RAD = Math.PI / 180.0;

    //计算物流柜之间的距离
    public double distance(Locker locker1, Locker locker2)
    {
        double rady1 = locker1.x * RAD;
        double rady2 = locker2.x * RAD;
        double a = rady1 - rady2;
        double b = (locker1.y - locker2.y) * RAD;
        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) + Math.cos(rady1) * Math.cos(rady2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000.0;

        return s;
    }


    //输出所有物流柜之间的距离
//    public void printDistance()
//    {
//        List<Locker> lockers = new ArrayList<>(Data.lockers);
//        System.out.println("======所有物流柜之间的距离=======");
//        int n = lockers.size();
//
//        for (int i = 0; i < n; i++) {
//            Locker lockerA = lockers.get(i);
//            for (int j = i + 1; j < n; j++) {
//                Locker lockerB = lockers.get(j);
//                double dist = distance(lockerA, lockerB);
//                System.out.printf("物流柜%d 到 物流柜%d: %.4f 米%n",
//                        lockerA.id, lockerB.id, dist);
//            }
//        }
//    }

    //计算任务的相似度
    public double calcuSimilar(Task task1, Task task2, double maxOverlap)
    {
        double overlap = overlapTime(task1, task2);
        int lockerSim = lockerSimilar(task1, task2);

        //权重设置
        double w1 = 0.7;  //时间窗权重
        double w2 = 0.3;  //物流柜权重

        // 计算相似度
        double timeSimilarity;
        if (maxOverlap != 0) {
            timeSimilarity = overlap / maxOverlap;
        } else {
            timeSimilarity = 0;
        }
        double similarity = w1 * timeSimilarity + w2 * lockerSim;

        return similarity;
    }

    //计算最大重叠时间
    public double calcMaxOverlap(List<Task> tasks)
    {
        double maxOverlap = 0.0;
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = i + 1; j < tasks.size(); j++) {
                double overlap = overlapTime(tasks.get(i), tasks.get(j));
                if (overlap > maxOverlap) {
                    maxOverlap = overlap;
                }
            }
        }
        return maxOverlap;
    }

    //计算任务重叠时间
    public double overlapTime(Task task1,Task task2)
    {

        double overlapStart = Math.max(task1.startTime,task2.startTime);
        double overlapEnd = Math.min(task1.endTime,task2.endTime);

        if (overlapStart >= overlapEnd) {
            return 0;  // 没有重叠
        }
        return (overlapEnd - overlapStart);
    }

    //计算物流柜相似度
    public int lockerSimilar(Task task1,Task task2)
    {
        if (task1.startLocker == task2.startLocker || task1.endLocker == task2.endLocker){
            return 1;  //相同物流柜
        }
        return 0;  //不同物流柜
    }

    //生成相似度矩阵
    public double[][] SimilarityMatrix(List<Task> tasks)
    {
        int n = tasks.size();
        double[][] similarityMatrix = new double[n][n];

        // 计算最大重叠时间
        double maxOverlap = calcMaxOverlap(tasks);

        // 计算每对任务之间的相似度
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    // 同一任务的相似度设为1
                    similarityMatrix[i][j] = 1.0;
                } else {
                    // 计算任务i和任务j之间的相似度
                    similarityMatrix[i][j] = calcuSimilar(tasks.get(i), tasks.get(j), maxOverlap);
                }
            }
        }

        // 输出相似度矩阵
//        System.out.println("相似度矩阵:");
//        System.out.print("      ");
//        for (int j = 0; j < n; j++) {
//            System.out.print(" Task" + j + "  ");
//        }
//        System.out.println();
//
//        for (int i = 0; i < n; i++) {
//            System.out.print("Task" + i + "  ");
//            for (int j = 0; j < n; j++) {
//                System.out.printf("%.2f    ", similarityMatrix[i][j]);
//            }
//            System.out.println();
//        }

        return similarityMatrix;
    }

}
