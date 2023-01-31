package head_v4.bots;

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

    /**
     * Returns if robot is on enemy island
     * @return Boolean of if on enemy island
     * @throws GameActionException
     */
    public boolean onEnemyIsland() throws GameActionException {
        int islandIndex = rc.senseIsland(rc.getLocation());
        boolean onEnemyIsland = false;
        if (islandIndex != -1) {
            if (rc.senseTeamOccupyingIsland(islandIndex) == rc.getTeam().opponent()) {
                onEnemyIsland = true;
            }
        } 
        return onEnemyIsland;
    }

    // public boolean checkForDistress() throws GameActionException {
    //     MapLocation loc = null;
    //     int dist = Integer.MAX_VALUE;
    //     for (int i = comm.HQ_OFFSET; i < comm.HQ_OFFSET + 4; i++) {
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
    public void stepTowardsDestination(MapLocation destination) throws GameActionException {
        if (rng.nextInt(3) != 0) {
            MapLocation averageNeighbor = averagePositionOfNearbyRobots(myTeam, RobotType.LAUNCHER);
            if (rc.getLocation().distanceSquaredTo(averageNeighbor) > 5) {
                Direction dir = rc.getLocation().directionTo(averageNeighbor);
                for (RobotInfo robot : rc.senseNearbyRobots(-1, enemyTeam)) {
                    if (robot.getType() == RobotType.HEADQUARTERS && rc.getLocation().add(dir).distanceSquaredTo(robot.getLocation()) <= 9) {
                        return;
                    }
                }
                tryMove(dir);
                return;
            }
        }

        super.stepTowardsDestination(destination);
    }

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

        if (!onEnemyIsland()) {
            MapLocation enemyHQ = closestEnemyHeadquarters();
            if (enemyHQ != null) {
                stepTowardsDestination(enemyHQ);
                return;
            }
        }
    }
}
