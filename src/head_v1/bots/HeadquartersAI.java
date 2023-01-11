package head_v1.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        // early-game behavior, saving headquarter positions
        if (gameTurn == 1) {
            comm.appendLocation(0, rc.getLocation());
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
    }
}
