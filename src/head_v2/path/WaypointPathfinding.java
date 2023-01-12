package head_v2.path;

import battlecode.common.*;

public class WaypointPathfinding implements Pathfinding {

    public RobotController rc;

    public Path myPath;
    public Path[] paths;
    
    public WaypointPathfinding(RobotController rc) {
        this.rc = rc;
    }

    public void initiatepathfinding(MapLocation target) {
        
    }

    public Direction findPath(MapLocation target) {
        return Direction.CENTER; // TODO
    }
}
