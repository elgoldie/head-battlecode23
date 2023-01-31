package head_v5.bots;

import battlecode.common.*;

public class AmplifierAI extends RobotAI {
    
    public AmplifierAI(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        MapLocation hqToTarget = null;
        int bestDistance = Integer.MAX_VALUE;
        for (MapLocation hqLoc : getPossibleEnemyHQLocations()) {
            int distance = rc.getLocation().distanceSquaredTo(hqLoc);
            if (distance < bestDistance) {
                bestDistance = distance;
                hqToTarget = hqLoc;
            }
        }

        if (hqToTarget != null) {

            stepTowardsDestination(hqToTarget);
            if (rc.isMovementReady())
                stepTowardsDestination(hqToTarget);
        }
    }
}
