package macrobot.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public MapLocation myloc; 
    public int task_low = 5;

    public int carriers_produced = 0;

    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        // early-game behavior, saving headquarter positions
        if (gameTurn == 1) {
            this.myloc = rc.getLocation();
            int towrite = 32768 + this.myloc.x * 64 + this.myloc.y;
            comm.queueWrite(0, towrite);
            System.out.println(towrite);

            WellInfo[] nearby_wells = rc.senseNearbyWells();

            for (int i = 0; i < nearby_wells.length; i++) {
                int[] info = this.well_to_request(nearby_wells[i], 8);
                System.out.println(Integer.toBinaryString(info[1]));
                if (info[0] > 0) {
                    System.out.println("Writing to address "+ (this.task_low + i*2));
                    comm.queueWrite(this.task_low + i*2, info[0]);
                    comm.queueWrite(this.task_low + 2*i + 1, info[1]);
                }
            }
        }for (int i = 0; i < 4; i++){
            MapLocation newloc = new MapLocation(this.myloc.x - i % 2 - 1, this.myloc.y - i / 2 - 1);
            //System.out.println("Spawning new robot at "+newloc.x+" "+newloc.y);
            if (rc.canBuildRobot(RobotType.CARRIER, newloc)) {
                rc.buildRobot(RobotType.CARRIER, newloc);
                this.carriers_produced++;
            }
        }
        
        
       
    }
}