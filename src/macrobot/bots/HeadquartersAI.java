package macrobot.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public MapLocation myloc; 
    public int task_low = 5;

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
            comm.writeInt(0, towrite);
            System.out.println(towrite);

            WellInfo[] nearby_wells = rc.senseNearbyWells();

            for (int i = 0; i < nearby_wells.length; i++) {
                int[] info = this.well_to_request(nearby_wells[i], 3);
                System.out.println(Integer.toBinaryString(info[1]));
                if (info[0] > 0) {
                    System.out.println("Writing to address "+ (this.task_low + i*2));
                    comm.writeInt(this.task_low + i*2, info[0]);
                    comm.writeInt(this.task_low + 2*i + 1, info[1]);
                }
            }
        }
        if (gameTurn <= 8) {
            
            for (int i = 0; i < 4; i++){
                MapLocation newloc = new MapLocation(this.myloc.x - i % 2 - 1, this.myloc.y - i / 2 - 1);
                //System.out.println("Spawning new robot at "+newloc.x+" "+newloc.y);
                if (rc.canBuildRobot(RobotType.CARRIER, newloc)) {
                    rc.buildRobot(RobotType.CARRIER, newloc);
                }
            }

        }
        
        
        /*
        if (gameTurn == 1) {

            comm.appendLocation(0, rc.getLocation());
            comm.writeInt(6, 6153);
            System.out.println("HQ has written the byte to index 6");
            rc.buildRobot(RobotType.CARRIER, rc.getLocation().add(directions[0]));
            System.out.println("Robot spawned");
        } else if (gameTurn == 2) {
            this.hqLocations = comm.readLocationArray(0);
            for (MapLocation loc : hqLocations) {
                System.out.println(loc);
            }
        }
        
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if ((rc.canBuildAnchor(Anchor.STANDARD)) && (rc.getRobotCount()>30)) {
            // If we can build an anchor do it!
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building anchor! " + rc.getAnchor());
        }
        if (rng.nextBoolean()) {
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
            }
        } else {
            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
        */
    }
}
