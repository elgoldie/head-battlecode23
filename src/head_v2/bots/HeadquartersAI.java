package head_v2.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public int anchorCraftCooldown;
    public final MapLocation[] spawnableLocations;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        anchorCraftCooldown = 0;
        spawnableLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 9);
    }

    boolean build = true;
    @Override
    public void run() throws GameActionException {
        super.run();
        if (build && rc.canBuildRobot(RobotType.CARRIER, rc.getLocation().add(Direction.NORTHEAST))) {
            rc.buildRobot(RobotType.CARRIER, rc.getLocation().add(Direction.NORTHEAST));
            build = false;
        }
        if (anchorCraftCooldown == 0) {return;}
        anchorCraftCooldown -= 1;

        rc.setIndicatorString(comm.dispArray());

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            comm.appendLocation(0, rc.getLocation());
        }

        MapLocation newLoc = spawnableLocations[rng.nextInt(spawnableLocations.length)];
        if (rc.getRobotCount() > 10 && anchorCraftCooldown <= 0 && rc.getNumAnchors(Anchor.STANDARD) == 0) {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                // System.out.println("I just built an anchor!");
                anchorCraftCooldown = 50;
            }
        }
        
        // if (rc.getRoundNum() == 1 && rc.canBuildRobot(RobotType.AMPLIFIER, newLoc)) {
        //     rc.buildRobot(RobotType.AMPLIFIER, newLoc);
        // } else if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
        //     rc.buildRobot(RobotType.LAUNCHER, newLoc);
        // }

        RobotType typeToSpawn = RobotType.CARRIER;
        if (rc.getRoundNum() <= 4) {
            typeToSpawn = RobotType.CARRIER;
        } else if (rc.getRoundNum() <= 7) {
            typeToSpawn = RobotType.LAUNCHER;
        } else {
            if (rng.nextInt(2) == 0) {
                typeToSpawn = RobotType.CARRIER;
            } else {
                typeToSpawn = RobotType.LAUNCHER;
            }
        }

        if (rc.canBuildRobot(typeToSpawn, newLoc)) {
            rc.buildRobot(typeToSpawn, newLoc);
        }

        // if (rc.getRoundNum() >= 500) {
        //     rc.resign();
        // }
    }
}
