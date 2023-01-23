package head_latest.path;

import java.util.HashMap;
import java.util.HashSet;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Alg2Pathfinding implements Pathfinding {
    
    public RobotController rc;
    public MapLocation destination;

    public HashMap<MapLocation, HashSet<MapLocation>> waypointCache;
    public HashSet<MapLocation> waypoints;

    public Alg2Pathfinding(RobotController rc) {
        this.rc = rc;
        this.waypointCache = new HashMap<>();
    }

    public void initPathfinding(MapLocation target) {
        destination = target;
        if (!waypointCache.containsKey(target)) {
            waypointCache.put(target, new HashSet<>());
        }
        waypoints = waypointCache.get(target);
    }

    public Direction findPath() {
        Direction direction = rc.getLocation().directionTo(destination);
        if (direction == Direction.CENTER) return Direction.CENTER;

        for (int i = 0; i < 8; i++) {
            if (rc.canMove(direction)) return direction;
            if (rc.getID() % 2 == 0) direction = direction.rotateLeft();
            else direction = direction.rotateRight();
        }
        return Direction.CENTER;
    }

    public boolean hasArrived() {
        return rc.getLocation().isAdjacentTo(this.destination);
    }
}
