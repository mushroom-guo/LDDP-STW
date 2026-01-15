package utils;

//import algorithm.ALNS;
import algorithm.ALNS_copy;
import algorithm.Algorithm;
import algorithm.TimeTix;
import object.Problem;

public class main {

    public static Problem problem = new Problem();
//    public static ALNS alns = new ALNS();
    public static ALNS_copy alns_copy = new ALNS_copy();
    public static Algorithm algorithm  = new Algorithm();
    public static TimeTix timeTix = new TimeTix();
    public static void main(String[] args) throws Exception {
//        String file = "LDDP-STW/src/Instance/Hangzhou_10_1_with_timewindow.txt";

//        ReadInstance readInstance = new ReadInstance();
//        readInstance.readInstance(file);
        //readInstance.InstanceInformation(); // 输出读取的实例信息

//        alns.Solve();
        algorithm.run();

//        timeTix.arcLocker();

    }

}
