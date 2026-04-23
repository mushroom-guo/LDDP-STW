package utils;

import object.Drone;
import object.Locker;
import object.Task;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public int taskNum;
    public List<Drone> drones = new ArrayList<Drone>();
    public List<Locker> lockers = new ArrayList<Locker>();
    public List<Task> tasks = new ArrayList<Task>();
}
