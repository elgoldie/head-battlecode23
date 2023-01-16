package holden_v2.bots;

import battlecode.common.*;

public class HeadquartersAI extends RobotAI {

    public int anchorCraftCooldown;
    public int myIndex;
    
    public HeadquartersAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        anchorCraftCooldown = 0;
        myIndex = -1;
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        anchorCraftCooldown -= 1;

        // early-game behavior, saving headquarter positions
        if (rc.getRoundNum() == 1) {
            myIndex = comm.appendLocation(0, rc.getLocation());
        } else {
            int amountEnemies = rc.senseNearbyRobots(20, enemyTeam).length;
            if (amountEnemies >= 3) {
                comm.writeLocationFlags(myIndex, 1);
            } else {
                comm.writeLocationFlags(myIndex, 0);
            }
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