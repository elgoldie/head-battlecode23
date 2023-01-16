package head_v2.bots;

import battlecode.common.*;

public class LauncherAI extends RobotAI {

    public int closestFleetUnfilled;
    public int closestFleetAny;
    
    public LauncherAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        closestFleetUnfilled = -1;
        closestFleetAny = -1;
    }

    public int enemyValue(RobotInfo robot) {
        if (robot.getType() == RobotType.HEADQUARTERS) return Integer.MIN_VALUE;
        return -robot.health;
    }

    public void scanForFleets() throws GameActionException {
        int distUnfilled = Integer.MAX_VALUE;
        int distAny = Integer.MAX_VALUE;

        for (int i = comm.FLEET_OFFSET; i < 64; i++) {
            if (!comm.hasLocation(i)) break;
            int d = rc.getLocation().distanceSquaredTo(comm.readLocation(i));
            if (d < distAny) {
                distAny = d;
                closestFleetAny = i;
            }
            if (d < distUnfilled && (rc.readSharedArray(i) & 0x4000) == 0) {
                distUnfilled = d;
                closestFleetUnfilled = i;
            }
        }
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
            rc.attack(target.location);
        }

        scanForFleets();

        if (closestFleetUnfilled != -1) {
            MapLocation fleetLocation = comm.readLocation(closestFleetUnfilled);
            Direction dir = pathing.findPath(fleetLocation);
            tryMove(dir);
        } else if (closestFleetAny != -1) {
            MapLocation fleetLocation = comm.readLocation(closestFleetAny);
            if (rc.getLocation().distanceSquaredTo(fleetLocation) < 34) {
                Direction dir = pathing.findPath(fleetLocation);
                tryMove(dir);
            } else {
                wander();
            }
        } else {
            wander();
        }
    }
}
