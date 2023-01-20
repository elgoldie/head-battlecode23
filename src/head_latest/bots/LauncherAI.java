package head_latest.bots;

import battlecode.common.*;

public class LauncherAI extends RobotAI {
    
    public LauncherAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    /**
     * Returns the closest enemy HQ to the robot (according to the comm system).
     * @return The closest enemy HQ to the robot.
     * @throws GameActionException
     */
    public MapLocation closestEnemyHeadquarters() throws GameActionException {
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (MapLocation hqLoc : getPossibleEnemyHQLocations()) {
            int newDist = rc.getLocation().distanceSquaredTo(hqLoc);
            if (newDist < dist) {
                dist = newDist;
                loc = hqLoc;
            }
        }
        return loc;
    }



    // public boolean checkForDistress() throws GameActionException {
    //     MapLocation loc = null;
    //     int dist = Integer.MAX_VALUE;
    //     for (int i = 0; i < 4; i++) {
    //         MapLocation hqLoc = comm.readLocation(i);
    //         if (hqLoc == null) break;
    //         if (comm.readLocationFlags(i) == 0) continue;
    //         int newDist = hqLoc.distanceSquaredTo(rc.getLocation());
    //         if (newDist < dist) {
    //             dist = newDist;
    //             loc = hqLoc;
    //         }
    //     }
    //     if (loc == null) {
    //         wander();
    //         return false;
    //     } else {
    //         tryMoveOrWander(pathing.findPath(loc));
    //         return true;
    //     }
    // }

    @Override
    public void run() throws GameActionException {
        super.run();

        int radius = rc.getType().actionRadiusSquared;

        int maxValue = Integer.MIN_VALUE;
        RobotInfo target = null;
        for (RobotInfo robot : rc.senseNearbyRobots(radius, enemyTeam)) {
            int value = enemyValue(robot);
            if (value > maxValue) {
                maxValue = value;
                target = robot;
            }
        }

        if (target != null) {
            if (rc.canAttack(target.location)) {      
                rc.attack(target.location);
            } else {
                stepTowardsDestination(target.location);
            }
        }

        if (true) {
            MapLocation enemyHQ = closestEnemyHeadquarters();
            if (enemyHQ != null) {
                stepTowardsDestination(enemyHQ);
                return;
            }
        }
    }
}
