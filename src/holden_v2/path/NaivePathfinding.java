package holden_v2.path;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NaivePathfinding implements Pathfinding {
    
    public RobotController rc;

    public MapLocation destination;

    public void initiate_pathfinding(MapLocation target) {
        this.destination = target;
    }

    public Direction findPath() {
        return findPath(this.destination);
    }

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

    public boolean hasArrived() {
        return (rc.getLocation().isAdjacentTo(this.destination));
    }
}
