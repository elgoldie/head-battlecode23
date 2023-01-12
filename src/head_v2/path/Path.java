package head_v2.path;

import java.util.ArrayList;

import battlecode.common.*;

public class Path {
    
    public RobotController rc;

    // WAYPOINT TRACKING
    public ArrayList<MapLocation> waypoints = new ArrayList<MapLocation>();

    public int waypoint_pointer = -1;
    public int direction = 0; //0 = forward (origin -> destination), 1 = backwards
    

    // HANDS BE LIKE
    public enum Handedness {
        NONE,
        LEFT,
        RIGHT
    }
    public boolean memory_mode;
    
    public Handedness handedness = Handedness.NONE;
    public int lefthand;
    public static final Direction[] RIGHTHANDED = {
        Direction.WEST, 
        Direction.SOUTHWEST, 
        Direction.SOUTH, 
        Direction.SOUTHEAST, 
        Direction.EAST, 
        Direction.NORTHEAST, 
        Direction.NORTH, 
        Direction.NORTHWEST
    };
    public int righthand;
    public static final Direction[] LEFTHANDED = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    

    public Path(MapLocation origin, MapLocation destination, RobotController rc) {
        this.waypoints.add(origin); this.waypoints.add(destination);
        this.rc = rc;
    }

    public void initiate_pathfinding() {
        initiate_pathfinding(new int[]{0,1});
    }

    public void initiate_pathfinding(int[] waypoints) {
        int start = waypoints[0];
        int end = waypoints[1];
        this.direction = start - end > 0 ? 1 : 0;
        this.waypoint_pointer = start - 2*direction + 1;
    }

    public Direction stepnext() throws GameActionException {

        MapLocation myloc = rc.getLocation();
        MapLocation waypoint = this.waypoints.get(this.waypoint_pointer);

        if (myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer))) {
            this.waypoint_pointer -= 2*this.direction + 1;
        }

        Direction objective = myloc.directionTo(waypoint);
        
        Direction movequeue = objective;
    
        if (this.rc.canMove(objective)){
            // no change to movequeue
            if (this.memory_mode && this.handedness != Handedness.NONE) {
                this.add_found_waypoint(myloc);
            }
            this.handedness = Handedness.NONE;
            
        } else {
            int counter;

            this.righthand = 0;
            this.lefthand = 0;
            while (this.righthand < 8 && RIGHTHANDED[this.righthand] != objective) {
                this.righthand ++;
            }
            while (this.lefthand < 8 && LEFTHANDED[this.lefthand] != objective) {
                this.lefthand ++;
            }
            
            switch (this.handedness) {
                case NONE:
                    this.memory_mode = true;
                    counter = 0;
                    while (!rc.canMove(RIGHTHANDED[this.righthand]) && counter < 8) {
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(RIGHTHANDED[this.righthand]));
                        this.righthand = (this.righthand + 1) % 8;
                        counter ++;
                    }
                    if (counter == 8) {
                        return Direction.CENTER;
                    }

                    while (!rc.canMove(LEFTHANDED[this.lefthand])) {
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(RIGHTHANDED[this.righthand]));
                        this.lefthand = (this.lefthand + 1) % 8;
                    }
                    
                    if (myloc.add(RIGHTHANDED[this.righthand]).distanceSquaredTo(waypoint) < myloc.add(LEFTHANDED[this.lefthand]).distanceSquaredTo(waypoint)) {
                        this.handedness = Handedness.RIGHT;
                        movequeue = RIGHTHANDED[this.righthand];
                    } else { 
                        this.handedness = Handedness.LEFT;
                        movequeue = LEFTHANDED[this.lefthand]; 
                    }
                    
                    break;

                case RIGHT:
                    counter = 0;
                    this.righthand = (this.righthand + 1) % 8;
                    while (!rc.canMove(RIGHTHANDED[this.righthand]) && counter < 8) {
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(RIGHTHANDED[this.righthand]));
                        this.righthand = (this.righthand + 1) % 8;
                    }
                    
                    movequeue = RIGHTHANDED[this.righthand];
                    break;
                case LEFT:
                    counter = 0;
                    this.lefthand = (this.lefthand + 1) % 8;
                    while (!rc.canMove(LEFTHANDED[this.lefthand]) && counter < 8) { 
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(LEFTHANDED[this.lefthand]));
                        this.lefthand = (this.lefthand + 1) % 8;
                    }

                    movequeue = LEFTHANDED[this.lefthand];
                    break;
            }
            
        }

        return movequeue;
    }

    // memory
    public void add_found_waypoint(MapLocation waypoint) {
        // Waypoint pointer points to present destination. 
        // forwards: add before
        // backwards: add after
        this.waypoints.add(this.waypoint_pointer + this.direction, waypoint);
    }

    // info
    public int containsWaypoint(MapLocation location, int threshold) throws GameActionException {
        for (int i = 0; i < this.waypoints.size(); i++) {
            if (location.distanceSquaredTo(this.waypoints.get(i)) <= threshold) { return i; }
        }
        return -1;
    }

    public int[] containsWaypoints(MapLocation location, MapLocation destination, int threshold) throws GameActionException {
        int[] waypointers = {-1, -1};
        for (int i = 0; i < this.waypoints.size(); i++) {
            if (location.distanceSquaredTo(this.waypoints.get(i)) <= threshold) { waypointers[0] = i; }
            if (destination.distanceSquaredTo(this.waypoints.get(i)) <= threshold) { waypointers[1] = i; }
        }
        return waypointers;
    }
    
}