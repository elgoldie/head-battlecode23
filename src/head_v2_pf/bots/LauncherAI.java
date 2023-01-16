package head_v2.bots;

import battlecode.common.*;

public class LauncherAI extends RobotAI {

    private enum LauncherSquadron {
        DEFENSE_BASE,
        DEFENSE_ISLAND,
        OFFENSE
    }

    public LauncherSquadron squadron;
    
    public LauncherAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        if (rng.nextInt(5) < 2) {
            squadron = LauncherSquadron.OFFENSE;
        } else {
            squadron = LauncherSquadron.DEFENSE_BASE;
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

        if (target != null && rc.canAttack(target.location)) {
            rc.setIndicatorString("Attacking");        
            rc.attack(target.location);
        }

        if (squadron == LauncherSquadron.OFFENSE) {
            MapLocation enemyHQ = closestEnemyHeadquarters();
            if (enemyHQ == null) {
                wander();
            } else {
                tryMoveOrWander(pathing.findPath(enemyHQ));
            }
        } else {
            wander();
        }
    }
}
