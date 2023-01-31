package head_v5.path;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NaivePathfinding implements Pathfinding {
    
    public RobotController rc;
    public Random rng;
    public boolean handedness;

    public MapLocation destination;
    
    public Direction facing;

    public NaivePathfinding(RobotController rc, Random rng) {
        this.rc = rc;
        this.rng = rng;
        this.handedness = rng.nextBoolean();
    }

    public void initPathfinding(MapLocation target) {
        this.destination = target;
    }

    // public Direction findPath() throws GameActionException {

    //     Direction direction = rc.getLocation().directionTo(destination);
    //     if (direction == Direction.CENTER) return Direction.CENTER;

    //     // boolean currentsMatter = rc.getMovementCooldownTurns() < 2 * rc.getType().movementCooldown;

    //     for (int i = 0; i < 8; i++) {
    //         if (rc.canMove(direction)) {
    //             // if (!currentsMatter || rc.senseMapInfo(rc.getLocation().add(direction)).getCurrentDirection() != direction.opposite())
    //             return direction;
    //         }
    //         if (handedness) direction = direction.rotateLeft();
    //         else direction = direction.rotateRight();
    //     }
    //     return Direction.CENTER;
    // }

    public Direction findPath() throws GameActionException {
        if (rc.getLocation().equals(destination)) {
            return Direction.CENTER;
        }
        Direction d = rc.getLocation().directionTo(destination);
        if (rc.canMove(d)) {
            facing = null;
            return d;
        } else {
            if (facing == null) {
                facing = d;
            }
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(facing)) {
                    Direction old = facing;
                    facing = handedness ? facing.rotateRight() : facing.rotateLeft();
                    return old;
                } else {
                    facing = handedness ? facing.rotateLeft() : facing.rotateRight();
                }
            }
        }
        return Direction.CENTER;
    }

    public boolean hasArrived() {
        return rc.getLocation().isAdjacentTo(this.destination);
    }
}
