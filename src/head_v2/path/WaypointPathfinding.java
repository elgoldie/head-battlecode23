package head_v2.path;

import battlecode.common.*;

import java.util.ArrayList;

public class WaypointPathfinding implements Pathfinding {

    public RobotController rc;

    public Path myPath;
    public ArrayList<Path> paths = new ArrayList<Path>();
    
    public WaypointPathfinding(RobotController rc) {
        this.rc = rc;
    }

    public void initiate_pathfinding(MapLocation target) throws GameActionException {
        MapLocation myloc = rc.getLocation();

        int path_number = -1;
        int[] waypoints = new int[]{-1, 1};
        for (int i = 0; i < this.paths.size(); i++) {
            waypoints = this.paths.get(i).containsWaypoints(myloc, target, waypoint_threshold);
            if (waypoints != new int[]{-1, -1}) {
                path_number = i;
                break;
            }
        }
        if (path_number == -1) {
            this.myPath = new Path(myloc, target, rc);
            this.paths.add(this.myPath);
            this.myPath.initiate_pathfinding();
        } else {
            this.myPath = this.paths.get(path_number);
            this.myPath.initiate_pathfinding(waypoints);
        }
    }

    public Direction findPath() throws GameActionException {
        return this.myPath.stepnext();
        
    }

    public Direction findPath(MapLocation target) { return Direction.CENTER; }
}
