package algorithm;

import object.Solution;
import utils.Data;
import utils.ReadInstance;

import java.util.ArrayList;

public class Algorithm {
    private Heuristic heuristic = new Heuristic();
    private ALNS_copy alns_copy = new ALNS_copy();
    public void run() throws Exception
    {
        String file = "LDDP-STW/src/Instance/uniform_1_100_with_timewindow.txt";
        ReadInstance readInstance = new ReadInstance();
        readInstance.readInstance(file);

        // 将读取的数据传递给ALNS_copy
        alns_copy.Solve(readInstance.data);
    }
}
