package object;

import java.util.ArrayList;
import java.util.List;

public class Locker {
    public int id;
    public double x; //物流柜的纬度
    public double y; //物流柜的经度
    public Drone drone;
    public List<Arc> arcList = new ArrayList<>();

}
