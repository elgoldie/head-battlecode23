package macrobot.bots;

//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;

//import javax.annotation.Resource;

import battlecode.common.*;
import macrobot.util.InformationManager;

public class CarrierAI extends RobotAI {

    // Implemented assignments:
    //  -Mining
    MapLocation myMine;   
    MapLocation myHQ;
    MapLocation destination;


    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        this.assignment = "idle";
        this.command = "getting assignment";
    }

    public void mining_task() throws GameActionException {
        this.arrayvision = this.comm.readWholeArray();
        //System.out.println("Beginning attempt to mine");
        int op = 61440; //1111000000000000
        int mining_op = 4096; // 0001000000000000 
        for (int i = this.tasklow; i <= this.taskhigh; i += 2) {
            //System.out.println("- index: "+(i+1));
            //System.out.println(Integer.toBinaryString(this.arrayvision[i+1]));
            //System.out.println(Integer.toBinaryString(op));
            //System.out.println(Integer.toBinaryString(mining_op));
            //System.out.println(Integer.toBinaryString(this.arrayvision[i+1] % 10000));
            
            if ((this.arrayvision[i+1] & op) == mining_op & this.arrayvision[i+1] % 16 > 0) {
                this.assignment = "mining";
                this.myMine = extractLocation(this.arrayvision[i]);
                //System.out.println("-"+i+"-");
                //System.out.println(this.arrayvision[i] % 16384 / 4096);
                //for (int j = 0; j < 5; j++){
                //System.out.println("Read from index "+j+": "+this.arrayvision[j]);}
                //this.myHQ = this.hqLocations[(this.arrayvision[i] % 16384 / 4096)];
               
                this.myHQ = extractLocation(this.arrayvision[(this.arrayvision[i] % 16384 / 4096) + 1] - 1);
                this.command = "goto";
                this.destination = this.myMine;
                //this.arg1 = //location
                //this.arg2 = //HQ
                comm.queueWrite(i+1, this.arrayvision[i+1] - 1);
                //System.out.println("Mining task found. New request amount: "+(this.arrayvision[i+1] % 16 - 1));
                //System.out.println("My mine: "+this.myMine.toString());
                //System.out.println("My HQ: "+this.myHQ.toString());
            }
        }
    }
    
    

    @Override
    public void run() throws GameActionException {
        super.run();
        //this.myloc = rc.getLocation();
        if (this.hqLocations == new MapLocation[]{}) {
            this.hqLocations = comm.readLocationArray(0);
            this.info = new InformationManager(this.hqLocations);
        }
        rc.setIndicatorString(this.assignment+": "+this.command+", "+this.destination+" "+this.handedness);
        if (this.gameTurn < 5){System.out.println(this.assignment);}
        switch (this.assignment) {
            case "idle":
                mining_task();
                break;
            case "mining":
                switch (this.command) {
                    case "goto":
                        while (!this.myloc.isAdjacentTo(destination) && rc.isMovementReady() && this.step_RH(this.destination)) {}
                        if (this.myloc.isAdjacentTo(destination)) {
                            rc.setIndicatorString("I've arrived!");
                            if (this.destination == this.myMine) {
                                this.command = "gather";
                                this.destination = this.myHQ;
                            }
                            else if (this.destination == this.myHQ) {
                                this.command = "deposit";
                                this.destination = this.myMine;
                            }
                        }
                        break;
                    case "gather":
                        if (rc.canCollectResource(this.myMine, -1)) {
                            rc.collectResource(this.myMine, -1);
                            if (this.getInventoryWeight() == 40) {
                                this.command = "goto";
                                this.handedness = Handedness.NONE;
                            }
                        } else {
                            rc.setIndicatorString("hlep cnot min");
                        }
                        break;
                    case "deposit":
                        for (ResourceType type : ResourceType.values()) {
                            int amount = rc.getResourceAmount(type);
                            if (amount > 0 && rc.canTransferResource(this.myHQ, type, amount)) {
                                rc.transferResource(this.myHQ, type, amount);
                                break;
                            }
                        }
                        if (this.getInventoryWeight() == 0) {
                            this.command = "goto";
                            this.handedness = Handedness.NONE;
                        }
                        break;
                    
                }
        }
    }

    public int getInventoryWeight() throws GameActionException {
        int weight = 0;
        for (ResourceType type : ResourceType.values()) {
            weight += rc.getResourceAmount(type);
        }
        return weight;
    }
}
