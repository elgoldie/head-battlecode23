package holden_v3.path_old;

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
