package utils;

import object.Drone;
import object.Locker;
import object.Task;

import java.io.BufferedReader;
import java.io.FileReader;

public class ReadInstance {
    public Data data = new Data();
    public void readInstance(String filePath) throws Exception
    {
        // 移除局部变量data，直接使用静态Data类
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        boolean readLockers = false;
        boolean readTasks = false;
        int droneIdCounter = 1;

        while ((line = br.readLine()) !=null){
            line = line.trim();

            if (line.startsWith("number of locker:")){
                readLockers = true;
                readTasks = false;
                continue;
            }

            if (line.startsWith("number of tasks:")){
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    data.taskNum = Integer.parseInt(parts[1].trim());
                    //System.out.println("任务总数: " + Data.taskNum); // 输出任务总数
                }
                readLockers = false;
                readTasks = true;
                continue;
            }

            //读取物流柜信息
            if (readLockers){
                String[] parts = line.split(",");
                if (parts.length == 4){
                    Locker locker = new Locker();
                    locker.id = Integer.parseInt(parts[0]);
                    locker.y = Double.parseDouble(parts[1]);
                    locker.x = Double.parseDouble(parts[2]);

                    int hasDrone = Integer.parseInt(parts[3]);
                    if (hasDrone == 1){
                        Drone drone = new Drone();
                        drone.id = droneIdCounter;
                        droneIdCounter++;
                        drone.currentLocker = locker;
                        locker.drone = drone;
                        data.drones.add(drone);
                    }else {
                        locker.drone = null;
                    }
                    data.lockers.add(locker);
                }
            }

            //读取task信息
            if (readTasks){
                //读取任务具体信息
                String[] mainParts = line.split(",\\(");
                if (mainParts.length == 2){
                    String[] lockerParts = mainParts[0].split(",");
                    String timeWindowPart = mainParts[1].replace(")", "");
                    String[] timeParts = timeWindowPart.split(",");

                    int startLockerId = Integer.parseInt(lockerParts[0]);
                    int endLockerId = Integer.parseInt(lockerParts[1]);

                    Task task =new Task();
                    task.startLocker = data.lockers.get(startLockerId);
                    task.endLocker = data.lockers.get(endLockerId);
                    task.startTime = Double.parseDouble(timeParts[0]);
                    task.endTime = Double.parseDouble(timeParts[1]);
                    data.tasks.add(task);
                    task.taskId = data.tasks.size();
                }
            }
        }
    }

    //输出测试
    public void InstanceInformation()
    {
        System.out.println("==============Task information==============");
        for (int i = 0 ;i<data.tasks.size(); i++){
            Task task = data.tasks.get(i);
            System.out.println("Task Id: " + task.taskId + ", Start Locker: " + task.startLocker.id
                    + ", End Locker: " + task.endLocker.id
                    + ", Time Window:(" + task.startTime + "," + task.endTime + ")");
        }

        System.out.println("==============Drone information==============");
        for (int i = 0 ; i <data.drones.size();i++){
            Drone drone = data.drones.get(i);
            System.out.println("Drone Id:" + drone.id + ", Locker" + drone.currentLocker.id );
        }

        System.out.println("==============Locker information==============");
        for (int i = 0 ; i < data.lockers.size(); i++){
            Locker locker = data.lockers.get(i);
            if (locker.drone != null)
            {
                System.out.println("Locker Id:" +locker.id + ", X:" + locker.x + ", Y:" + locker.y + ", Drone Id:" + locker.drone.id);
            }else {
                System.out.println("Locker Id:" +locker.id + ", X:" + locker.x + ", Y:" + locker.y + ", Drone Id: null");
            }
        }
    }
}
