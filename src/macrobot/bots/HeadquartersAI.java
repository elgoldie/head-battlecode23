package macrobot.bots;

import battlecode.common.*;
import macrobot.util.InformationManager;

import java.util.Random;

public class HeadquartersAI extends RobotAI {

    public enum HQassignment {
        IDLE,
        SATURATE,
        SCOUT,
    }
    public boolean produced_amplifier = false;

    public MapLocation myloc; 

    public int carriers_produced = 0;

    public HQassignment assignment;

    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        this.assignment = HQassignment.IDLE;
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        MapLocation spawnloc;
        rc.setIndicatorString(this.assignment.toString());;
        this.myloc = rc.getLocation();
        if (rc.getRoundNum() == 1) {
            comm.appendLocation(0, rc.getLocation());
        } else if (rc.getRoundNum() == 2) {
            this.hqLocations = comm.readLocationArray(0);
            this.info = new InformationManager(this.hqLocations);
        }

        // early-game behavior, saving headquarter positions
        if (gameTurn == 2) {
            
            //int towrite = 32768 + this.myloc.x * 64 + this.myloc.y;
            //comm.queueWrite(0, towrite);
            //System.out.println(towrite);

            WellInfo[] nearby_wells = rc.senseNearbyWells();

            for (int i = 0; i < nearby_wells.length; i++) {
                int[] info = this.info.well_to_request(nearby_wells[i], 8);
                System.out.println(Integer.toBinaryString(info[1]));
                if (info[0] > 0) {
                    System.out.println("Writing to address "+ (this.tasklow + i*2));
                    comm.queueWrite(this.tasklow + i*2, info[0]);
                    comm.queueWrite(this.tasklow + 2*i + 1, info[1]);
                }
            }
        }
        if (gameTurn == 5) {
            System.out.println(this.arrayvision);
        }
        int count;
        if (gameTurn > 2){
            //System.out.println(this.assignment);
            switch (this.assignment) {
                
                case IDLE:
                this.assignment = HQassignment.SATURATE; // proper logic: 
                break;
                case SATURATE:
                count = this.mining_task();
                //System.out.println("Count: "+count);
                if (count == 0) {
                    if (this.gameTurn < 200) {
                        this.assignment = HQassignment.SCOUT;
                    }
                } else {
                //System.out.println("Attempting to spawn");
                spawnloc = this.myloc.add(RIGHTHANDED[this.rng.nextInt(8)]);
                if (rc.canBuildRobot(RobotType.CARRIER, spawnloc)) {
                    rc.buildRobot(RobotType.CARRIER, spawnloc);
                }
                //System.out.println("Spawned");
                }
                break;
                case SCOUT:
                //System.out.println("Scouting");
                count = this.scouting_task();
                System.out.println("Scount: "+count);
                if (count >= 2 || gameTurn > 200) {
                    this.assignment = HQassignment.SATURATE;
                } else {
                    int distance = 8;
                    int x = -64;
                    int y = -64;
                    MapLocation newloc;
                    System.out.println("Searching for new scouting location");
                    do {
                        x = distance*(rng.nextInt(3) - 1);
                        y = distance*(rng.nextInt(3) - 1);
                        //System.out.println(x+", "+y);
                        newloc = this.myloc.translate(x, y);
                    } while (!rc.onTheMap(newloc) || (x == 0 && y == 0));
                    System.out.println("Location found");
                    int[] request = this.info.scouting_request(newloc, true, true, false, false) ;
                    for (int j = this.tasklow; j < 16; j+= 2){//j <= this.taskhigh; j++) {
                        if (this.arrayvision[j] == 0) {
                            System.out.println("Writing scouting request to "+j);
                            this.comm.queueWrite(j, request[0]);
                            this.comm.queueWrite(j+1, request[1]);
                            break;
                        }
                    }
                    
                }
                if (!this.produced_amplifier) {
                    spawnloc = this.myloc.add(RIGHTHANDED[this.rng.nextInt(8)]);
                    if (rc.canBuildRobot(RobotType.AMPLIFIER, spawnloc)) {
                        rc.buildRobot(RobotType.AMPLIFIER, spawnloc);
                        this.produced_amplifier = true;
                    } else if (rc.canBuildRobot(RobotType.CARRIER, spawnloc)) {
                        rc.buildRobot(RobotType.CARRIER, spawnloc);
                    }      
                }      
            }
        }
        
       
    }

    public int mining_task() throws GameActionException {
        //System.out.println("Mining task called in HQ");
        this.arrayvision = this.comm.readWholeArray();
        //System.out.println("Beginning attempt to mine");
        int op = 61440; //1111000000000000
        int mining_op = 4096; // 0001000000000000
        int count = 0; 
        for (int i = this.tasklow; i <= this.taskhigh; i += 2) {           
            //System.out.println("In loop: "+i);
            if ((this.arrayvision[i+1] & op) == mining_op & this.arrayvision[i+1] % 16 > 0 ){//& this.myloc == extractLocation(this.arrayvision[(this.arrayvision[i] % 16384 / 4096 + 1) ] - 1)) {
                count += 1;
            }
        }
        //System.out.println("Returning now");
        return count;
    }

    public int scouting_task() throws GameActionException {
        this.arrayvision = this.comm.readWholeArray();
        //System.out.println("Beginning attempt to mine");
        int op = 61440; //1111000000000000
        int scouting_op = 8192; // 0010000000000000 
        int count = 0;
        for (int i = this.tasklow; i <= this.taskhigh; i += 2) {            
            if ((this.arrayvision[i+1] & op) == scouting_op) { //  && (this.arrayvision[i+1] & 3584) == 0
                count++;
            }
        }
        return count;
    }
}