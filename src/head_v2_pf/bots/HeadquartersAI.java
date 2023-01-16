package head_v2_pf.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public int anchorCraftCooldown;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        anchorCraftCooldown = 0;
    }

    int build = 0;
    @Override
    public void run() throws GameActionException {
        super.run();
        if (build < 1 && rc.canBuildRobot(RobotType.CARRIER, rc.getLocation().add(Direction.NORTHEAST))) {
            rc.buildRobot(RobotType.CARRIER, rc.getLocation().add(Direction.NORTHEAST));
            build ++;
        }
        if (anchorCraftCooldown == 0) {return;}
        anchorCraftCooldown -= 1;

        comm.dispArray();

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            comm.appendLocation(0, rc.getLocation());
        }
        
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.adjacentLocation(dir);
        if (rc.getRobotCount() > 10 && anchorCraftCooldown <= 0 && rc.getNumAnchors(Anchor.STANDARD) == 0) {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                // System.out.println("I just built an anchor!");
                anchorCraftCooldown = 50;
            }
        }

        RobotType typeToSpawn = RobotType.CARRIER;
        if (rc.getRoundNum() <= 4) {
            typeToSpawn = RobotType.CARRIER;
        } else if (rc.getRoundNum() <= 7) {
            typeToSpawn = RobotType.LAUNCHER;
        } else {
            if (rng.nextInt(4) == 0) {
                typeToSpawn = RobotType.CARRIER;
            } else {
                typeToSpawn = RobotType.LAUNCHER;
            }
        }

        if (rc.canBuildRobot(typeToSpawn, newLoc)) {
            rc.buildRobot(typeToSpawn, newLoc);
        }
    }
}
