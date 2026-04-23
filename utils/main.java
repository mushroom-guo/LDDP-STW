package utils;

//import algorithm.ALNS;
import algorithm.ALNS;

public class main {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();
        ALNS alns = new ALNS();
        //String file = "LDDP-STW/src/Instance/Biased/dis/D_4_50_TW.txt";
        //String file = "LDDP-STW/src/Instance/Small-scale Uniform/Hangzhou/Hangzhou_20_2_TW.txt";
        String file = "LDDP-STW/src/Instance/Uniform/uniform_3_60_TW.txt";
        //String file = "LDDP-STW/src/Instance/Ratio/uniform_1_30_TW.txt";
        ReadInstance readInstance = new ReadInstance();
        readInstance.readInstance(file);

        // 将读取的数据传递给ALNS_copy
        alns.Solve(readInstance.data);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Run Time: " + totalTime );
    }


}
