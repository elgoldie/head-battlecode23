package macrobot.bots;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import battlecode.common.*;

public class CarrierAI extends RobotAI {

    // Implemented assignments:
    //  -Mining
    String assignment; 
    String command;
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
                this.myHQ = extractLocation(this.arrayvision[0]); //assume only one HQ for now.
                this.command = "goto";
                this.destination = this.myMine;
                //this.arg1 = //location
                //this.arg2 = //HQ
                comm.writeInt(i+1, this.arrayvision[i+1] - 1);
                System.out.println("Mining task found. New request amount: "+(this.arrayvision[i+1] % 16 - 1));
            }
        }
    }
    public MapLocation extractLocation(int raw_entry) {
        int loc = raw_entry % 4096;
        return new MapLocation(loc / 64, loc % 64);
    }
    @Override
    public void run() throws GameActionException {
        super.run();
        rc.setIndicatorString(this.assignment+": "+this.command+", "+this.destination+" "+this.RH_mode);
        if (this.gameTurn < 5){System.out.println(this.assignment);}
        switch (this.assignment) {
            case "idle":
                if (this.gameTurn < 5) {mining_task();}
                
                if (this.gameTurn < 5){System.out.println(this.assignment);}
                break;
            case "mining":
                switch (this.command) {
                    case "goto":
                        if (this.step_RH(this.destination)) {
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
                        //rc.setIndicatorString(Integer.toString(this.getInventoryWeight()));
                        //System.out.println(this.getInventoryWeight());
                        if (this.getInventoryWeight() == 0) {
                            this.command = "goto";
                        }
                        break;
                    
                }
        }
        /*if (this.assignment == "mining") {
            if (this.step_RH(this.destination)) {
                System.out.println("Destination reached!");
                this.destination = new MapLocation(10, 10);
            }
            
        }
        if (rc.getLocation().isAdjacentTo(new MapLocation(10, 10))) {
            this.assignment = "inactive";
        }
        */
        


    
        
        /*if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the first island I see
            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }
        // Try to gather from squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
                if (rc.canCollectResource(wellLocation, -1)) {
                    if (rng.nextBoolean()) {
                        rc.collectResource(wellLocation, -1);
                        rc.setIndicatorString("Collecting, now have, AD:" + 
                            rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
                            " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
                            " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                    }
                }
            }
        }
        // Occasionally try out the carriers attack
        if (rng.nextInt(20) == 1) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemyRobots.length > 0) {
                if (rc.canAttack(enemyRobots[0].location)) {
                    rc.attack(enemyRobots[0].location);
                }
            }
        }
        
        // If we can see a well, move towards it
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 1 && rng.nextInt(3) == 1) {
            WellInfo well_one = wells[1];
            Direction dir = me.directionTo(well_one.getMapLocation());
            if (rc.canMove(dir)) 
                rc.move(dir);
        }
        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }*/
    }
    public int getInventoryWeight() throws GameActionException {
        int weight = 0;
        for (ResourceType type : ResourceType.values()) {
            weight += rc.getResourceAmount(type);
        }
        return weight;
    }
}
