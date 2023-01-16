package holden_v2.bots;

import battlecode.common.*;

public class LauncherAI extends RobotAI {
    
    public boolean offense;
    public String indicatorString;
    
    public LauncherAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        if (rng.nextInt(10) == 0) {
            offense = false;
            indicatorString = "defense";
        } else {
            offense = true;
            indicatorString = "offense";
            
        }
        
    }

    public MapLocation closestEnemyHeadquarters() throws GameActionException {
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = 4; i < 8; i++) {
            MapLocation hqLoc = comm.readLocation(i);
            if (hqLoc == null) break;
            int newDist = rc.getLocation().distanceSquaredTo(hqLoc);
            if (newDist < dist) {
                dist = newDist;
                loc = hqLoc;
            }
        }
        return loc;
    }

    public int enemyValue(RobotInfo robot) {
        if (robot.getType() == RobotType.HEADQUARTERS) return Integer.MIN_VALUE;
        return -robot.health;
    }

    public boolean baseDefenseMovement() throws GameActionException {
        MapLocation loc = null;
        int dist = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            MapLocation hqLoc = comm.readLocation(i);
            if (hqLoc == null) break;
            if (comm.readLocationFlags(i) == 0) continue;
            int newDist = hqLoc.distanceSquaredTo(rc.getLocation());
            if (newDist < dist) {
                dist = newDist;
                loc = hqLoc;
            }
        }
        if (loc == null) {
            wander();
            return false;
        } else {
            tryMoveOrWander(pathing.findPath(loc));
            return true;
        }
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        rc.setIndicatorString(indicatorString);

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
                tryMoveOrWander(pathing.findPath(target.location));
            }
        }

        if (!baseDefenseMovement()) {
            MapLocation enemyHQ = closestEnemyHeadquarters();
            if (enemyHQ != null) {
                tryMoveOrWander(pathing.findPath(enemyHQ));
                return;
            }

            if (offense) {
                MapLocation enemyIsland = closestIsland(enemyTeam);
                if (enemyIsland == null) {
                    wander();
                } else {
                    tryMoveOrWander(pathing.findPath(enemyIsland));
                }
            } else {
                MapLocation island = closestIsland(myTeam);
                if (island == null) {
                    wander();
                } else {
                    tryMoveOrWander(pathing.findPath(island));
                }
            }
        }
    }
}
