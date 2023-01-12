package holden_v1.path;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NaivePathfinding implements Pathfinding {
    
    public RobotController rc;

    public NaivePathfinding(RobotController rc) {
        this.rc = rc;
    }

    public Direction findPath(MapLocation target) {
        Direction direction = rc.getLocation().directionTo(target);
        if (direction == Direction.CENTER) return Direction.CENTER;

        for (int i = 0; i < 8; i++) {
            if (rc.canMove(direction)) return direction;
            direction = direction.rotateLeft();
        }
        return Direction.CENTER;
    }
}
