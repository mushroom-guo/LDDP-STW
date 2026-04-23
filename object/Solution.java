package object;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    public List<Drone> drones = new ArrayList<>();

    public Solution copy() {
        Solution newSol = new Solution();
        newSol.drones = new ArrayList<>();
        for (Drone drone : this.drones) {
            newSol.drones.add(drone.copy());
        }
        return newSol;
    }
}
