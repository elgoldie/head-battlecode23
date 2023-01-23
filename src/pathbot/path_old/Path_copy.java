package pathbot.path_old;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class Path_copy {
    
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

    public Direction movequeue;

    

    public Path_copy(MapLocation origin, MapLocation destination, RobotController rc) {
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
        this.movequeue = Direction.CENTER;
        this.handedness = Handedness.NONE;
        this.memory_mode = false;
        System.out.println("Initiate pathfinding called");
        rc.setIndicatorString("Destination: "+this.waypoints.get(end).toString());
    }

    public Direction stepnext() throws GameActionException { 
        
        //TODO implement rotate right/left
        MapLocation myloc = rc.getLocation();
        MapLocation waypoint = this.waypoints.get(this.waypoint_pointer);

        rc.setIndicatorString("Next waypoint: "+waypoint.toString()+" | "+this.handedness+" | Memory: "+this.memory_mode);

        if (myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer))) {
            this.handedness = Handedness.NONE;
            this.movequeue = Direction.CENTER;
            this.memory_mode = false;
            this.waypoint_pointer -= 2*this.direction - 1;
            if (this.waypoint_pointer == -1 || this.waypoint_pointer == this.waypoints.size()) {
                System.out.println("Arrived at final point on waypoint path.");
                if (rc.canMove(myloc.directionTo(this.waypoints.get(this.waypoint_pointer + 2*this.direction - 1)))) {
                    return myloc.directionTo(this.waypoints.get(this.waypoint_pointer + 2*this.direction - 1));
                }
                return Direction.CENTER;
            } else{
            System.out.println("Waypoint reached! Now pursuing: "+this.waypoints.get(this.waypoint_pointer)+"\nfrom: "+myloc);
            System.out.println("New pointer: "+this.waypoint_pointer);}
            waypoint = this.waypoints.get(this.waypoint_pointer);
        }

        Direction objective = myloc.directionTo(waypoint);
        
        //if (myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer + 2*this.direction - 1))) {
        //    System.out.println("Next objective: "+objective);
        //    System.out.println("My queue: "+this.movequeue);
        //}
    
        if (this.rc.canMove(objective)){
            if (objective != this.movequeue.opposite() && objective != this.movequeue.opposite().rotateRight() && objective != this.movequeue.opposite().rotateLeft()) {
                if (this.memory_mode && this.handedness != Handedness.NONE) {
                    this.add_found_waypoint(myloc);
                    System.out.println("Remembered new waypoint!");
                    System.out.println(this);
                }
                this.handedness = Handedness.NONE;
                this.movequeue = objective;
            } else {
                if (this.handedness == Handedness.LEFT) {
                    objective = this.movequeue.rotateLeft().rotateLeft();
                }
                if (this.handedness == Handedness.RIGHT) {
                    objective = this.movequeue.rotateRight().rotateRight();
                }
                System.out.println("---");
                System.out.println(objective);
                System.out.println(this.movequeue.opposite());
                System.out.println(this.movequeue.opposite().rotateRight());
                System.out.println(this.movequeue.opposite().rotateLeft());
                rc.setIndicatorString("Handedness change ignored blyat!");
            }     
            
        } 
        if (!this.rc.canMove(objective)) {
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
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(LEFTHANDED[this.lefthand]));
                        //if (rc.sensePassability(myloc.add(RIGHTHANDED[this.righthand]))) {
                        //    System.out.println(myloc.add())
                        //}
                        this.lefthand = (this.lefthand + 1) % 8;
                    }
                    
                    if (myloc.add(RIGHTHANDED[this.righthand]).distanceSquaredTo(waypoint) < myloc.add(LEFTHANDED[this.lefthand]).distanceSquaredTo(waypoint)) {
                        this.handedness = Handedness.RIGHT;
                        this.movequeue = RIGHTHANDED[this.righthand];
                    } else { 
                        this.handedness = Handedness.LEFT;
                        this.movequeue = LEFTHANDED[this.lefthand]; 
                    }
                    
                    break;

                case RIGHT:
                    counter = 0;
                    this.righthand = (this.righthand + 1) % 8;
                    while (!rc.canMove(RIGHTHANDED[this.righthand]) && counter < 8) {
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(RIGHTHANDED[this.righthand]));
                        this.righthand = (this.righthand + 1) % 8;
                    }
                    
                    this.movequeue = RIGHTHANDED[this.righthand];
                    break;
                case LEFT:
                    counter = 0;
                    this.lefthand = (this.lefthand + 1) % 8;
                    while (!rc.canMove(LEFTHANDED[this.lefthand]) && counter < 8) { 
                        this.memory_mode = this.memory_mode && !rc.sensePassability(myloc.add(LEFTHANDED[this.lefthand]));
                        this.lefthand = (this.lefthand + 1) % 8;
                    }

                    this.movequeue = LEFTHANDED[this.lefthand];
                    break;
            }
            
        }
        if (myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer + 2*this.direction - 1))) {
            //System.out.println("I chose: "+this.movequeue);
        }

        return this.movequeue;
    }

    // memory
    public void add_found_waypoint(MapLocation waypoint) {
        // Waypoint pointer points to present destination. 
        // forwards: add before
        // backwards: add after
        this.waypoints.add(this.waypoint_pointer + this.direction, waypoint);
        this.waypoint_pointer += 1 - 2*this.direction;
        System.out.println("Now pursuing: "+this.waypoints.get(this.waypoint_pointer));
        //this.waypoint_pointer -= 2*this.direction + 1;
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
        //System.out.println("Search result: "+Arrays.toString(waypointers));
        return waypointers;
    }

    public String toString() {
        String ret = "";
        for (int i = 0; i < this.waypoints.size()-1; i++) {
            ret += this.waypoints.get(i).toString() + "("+i+") -> ";
        }
        ret += this.waypoints.get(this.waypoints.size()-1);
        ret += " ("+(this.waypoints.size()-1)+")";
        ret += "Direction: "+ (this.direction==1 ? "Backwards" : "Forward");

        return ret;
    }
    
}