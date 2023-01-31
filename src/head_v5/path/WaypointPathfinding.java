package head_v5.path;

import battlecode.common.*;

import java.util.ArrayList;

public class WaypointPathfinding implements Pathfinding {

    public RobotController rc;

    public Path myPath;
    public ArrayList<Path> paths = new ArrayList<Path>();
    public MapLocation destination;
    
    public WaypointPathfinding(RobotController rc) {
        this.rc = rc;
        // System.out.println("Pathfinding created properly");
    }

    public void initPathfinding(MapLocation target) throws GameActionException {

        // TODO: implement threshold radius (e.g. for mine-packing)

        //rc.setIndicatorString("Initiating pathfinding");
        MapLocation myloc = rc.getLocation();

        // System.out.println("Assignment: "+myloc + "->"+target);

        int path_number = -1;
        int[] waypoints = new int[]{-1, -1};
        for (int i = 0; i < this.paths.size(); i++) {
            waypoints = this.paths.get(i).containsWaypoints(myloc, target, waypoint_threshold);
            //System.out.println(waypoints);
            if (waypoints[0] >= 0 && waypoints[1] >= 0) {
                path_number = i;
                //rc.setIndicatorString("Known path found");
                break;
            }
        }
        //System.out.println(Arrays.toString(waypoints));
        //System.out.println(path_number);
        if (path_number == -1 || waypoints[0] == waypoints[1]) {
            this.myPath = new Path(myloc, target, rc);
            this.paths.add(this.myPath);
            this.myPath.initiate_pathfinding();
        } else {
            // System.out.println("I've been down this way before!");
            // System.out.println(Arrays.toString(waypoints));
            this.myPath = this.paths.get(path_number);
            // System.out.println(this.myPath);
            this.myPath.initiate_pathfinding(waypoints);
        }
        this.destination = target;
        // System.out.println("My path: "+this.myPath);
    }

    public Direction findPath() throws GameActionException {
        if (this.hasArrived()) {
            return Direction.CENTER;
        }
        return this.myPath.stepnext();
        
    }

    public boolean hasArrived() {
        MapLocation myloc = rc.getLocation();
        if (myloc.isAdjacentTo(this.destination)) {
            return true;
        }
        if (this.myPath.hasArrived(myloc)) { 
            this.myPath.add_next_waypoint(this.destination); 
        }
        return false;
    }
}
