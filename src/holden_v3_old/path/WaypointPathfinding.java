package holden_v3_old.path;

import battlecode.common.*;

public class WaypointPathfinding implements Pathfinding {

    public RobotController rc;
    
    public WaypointPathfinding(RobotController rc) {
        this.rc = rc;
    }

    public Direction findPath(MapLocation target) {
        return Direction.CENTER; // TODO
    }
}
