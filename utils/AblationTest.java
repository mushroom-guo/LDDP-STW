package utils;
import algorithm.ALNS;

public class AblationTest {
//    public static void main(String[] args) throws Exception {
//        String file = "LDDP-STW/src/Instance/Small-scale Uniform/Chengdu/Chengdu_5_1_TW.txt"; // [cite: 7]
//
//        // --- 实验组：完整算法 (ALNS + LP) ---
//        ALNS fullAlns = new ALNS();
//        fullAlns.useLP = true;
//        System.out.println(">>> 运行完整算法 (ALNS + LP)...");
//        ReadInstance ri = new ReadInstance();
//        ri.readInstance(file); // 调用现有的方法 [cite: 14]
//        fullAlns.Solve(ri.data);
//        double fullCost = fullAlns.bestObjVal;
//
//        // --- 对照组：消融算法 (Only ALNS) ---
//        ALNS ablationAlns = new ALNS();
//        ablationAlns.useLP = false;
//        System.out.println("\n>>> 运行消融算法 (Only ALNS)...");
//        fullAlns.Solve(ri.data);
//        double ablationCost = ablationAlns.bestObjVal;
//
//        // --- 结果对比 ---
//        System.out.println("\n============== 消融实验结果 ==============");
//        System.out.println("完整算法总成本: " + fullCost);
//        System.out.println("消融算法总成本: " + ablationCost);
//        double improvement = (ablationCost - fullCost) / ablationCost * 100;
//        System.out.printf("LP 优化带来的成本降幅: %.2f%%\n", improvement);
//    }
}
