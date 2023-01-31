package head_v4.path;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NaivePathfinding implements Pathfinding {
    
    public RobotController rc;

    public MapLocation destination;

    public NaivePathfinding(RobotController rc) {
        this.rc = rc;
    }

    public void initPathfinding(MapLocation target) {
        this.destination = target;
    }

    // private int dotProduct(Direction a, Direction b) {
    //     return a.dx * b.dx + a.dy * b.dy;
    // }

    public Direction findPath() throws GameActionException {
        Direction direction = rc.getLocation().directionTo(destination);
        if (direction == Direction.CENTER) return Direction.CENTER;

        // boolean currentsMatter = rc.getMovementCooldownTurns() < 2 * rc.getType().movementCooldown;

        for (int i = 0; i < 8; i++) {
            if (rc.canMove(direction)) {
                // if (!currentsMatter || dotProduct(rc.senseMapInfo(rc.getLocation().add(direction)).getCurrentDirection(), direction.opposite()) >= 0)
                return direction;
            }
            if (rc.getID() % 2 == 0) direction = direction.rotateLeft();
            else direction = direction.rotateRight();
        }
        return Direction.CENTER;
    }

    public boolean hasArrived() {
        return rc.getLocation().isAdjacentTo(this.destination);
    }
}
