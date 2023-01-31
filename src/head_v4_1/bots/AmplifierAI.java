package head_v4_1.bots;

import battlecode.common.*;

public class AmplifierAI extends RobotAI {
    
    public AmplifierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        int aggregateX = 0;
        int aggregateY = 0;
        int count = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(34, myTeam)) {
            if (robot.getType() == RobotType.AMPLIFIER) {
                aggregateX += robot.location.x;
                aggregateY += robot.location.y;
                count += 1;
            }
        }

        if (count > 0) {
            MapLocation center = new MapLocation(2 * rc.getLocation().x - aggregateX / count, 2 * rc.getLocation().y - aggregateY / count);
            Direction dir = rc.getLocation().directionTo(center);
            tryMoveOrWander(dir);
        } else {
            wander();
        }
    }
}
