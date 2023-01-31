package head_v4_1.path;

import java.util.ArrayList;

import battlecode.common.*;

public class Path {
    
    public RobotController rc;

    // WAYPOINT TRACKING
    public ArrayList<MapLocation> waypoints = new ArrayList<MapLocation>();
    public ArrayList<MapLocation> ohNoes = new ArrayList<MapLocation>();

    public boolean maybeinaccessible = false;
    public boolean inaccessible = false;

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
    public Direction lefthand;
    public Direction righthand;
    public Direction myMove;
    
    

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
        this.handedness = Handedness.NONE;
        this.memory_mode = false;
        this.myMove = Direction.CENTER;
        this.ohNoes.clear();
        this.inaccessible = false;
        this.maybeinaccessible = false;
        rc.setIndicatorString("Destination: "+this.waypoints.get(end).toString());
    }

    public void advance_pointer() {
        //// System.out.println("Old pointer: "+this.waypoint_pointer);
        this.waypoint_pointer -= 2*direction - 1;
        //// System.out.println("New pointer: "+this.waypoint_pointer);
    }

    public int advanced_pointer(int i) {
        return this.waypoint_pointer - i*(2*direction - 1);
    }

    public boolean isEndpoint() {
        return (this.waypoint_pointer == 0 && this.direction == 1) || (this.waypoint_pointer == this.waypoints.size() - 1 && this.direction == 0);
    }

    public boolean isValid(int pointer) {
        return (pointer >= 0 && pointer <= this.waypoints.size() - 1);
    }

    public boolean hasArrived(MapLocation myloc) {
        return this.isEndpoint() && myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer));
    }

    public boolean canMove(MapLocation myloc, Direction dir) throws GameActionException {
        return rc.canMove(dir) && rc.onTheMap(myloc.add(dir));
    }

    public Direction stepnext() throws GameActionException { 

        if (rc.getRoundNum() == 282) {
            //rc.resign();
        }
        
        MapLocation myloc = rc.getLocation();
        MapLocation waypoint = this.waypoints.get(this.waypoint_pointer);

        //if (myloc.isAdjacentTo(waypoint)) {
        if (myloc.isAdjacentTo(waypoint)) {
            if (this.isEndpoint() && myloc == waypoint) {
                // System.out.println("Arrived at final point on waypoint path.");
                return Direction.CENTER;
            }
            if (myloc == waypoint && this.isValid(this.advanced_pointer(3)) && rc.canMove(myloc.directionTo(this.waypoints.get(this.advanced_pointer(3)))) && myloc.distanceSquaredTo(this.waypoints.get(this.advanced_pointer(3))) < myloc.distanceSquaredTo(this.waypoints.get(this.advanced_pointer(2)))) {
                this.waypoint_pointer = this.advanced_pointer(3);
                // reverse handednes 
            }
            else {
                this.advance_pointer();
                if (rc.canMove(myloc.directionTo(waypoint))) {
                    return myloc.directionTo(waypoint);
                }
            }
            this.memory_mode = false;
            this.handedness = Handedness.NONE;
            this.myMove = Direction.CENTER;
            // System.out.println("Waypoint reached! Now pursuing: "+this.waypoints.get(this.waypoint_pointer)+"\nfrom: "+myloc);
            this.ohNoes.clear();
            //// System.out.println("New pointer: "+this.waypoint_pointer);
            waypoint = this.waypoints.get(this.waypoint_pointer);
        }

        Direction objective = myloc.directionTo(waypoint);
        
        //if (myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer + 2*this.direction - 1))) {
        //    // System.out.println("Next objective: "+objective);
        //    // System.out.println("My queue: "+this.movequeue);
        //}

        switch (this.handedness) {
            case RIGHT:
                /* if (this.ohNoes.contains(myloc) && this.myMove != Direction.CENTER) {
                    // System.out.println("This is an oh noes moment");
                    if (this.maybeinaccessible) {
                        // System.out.println("As far as I can tell, I can't get to that place right now");
                        this.inaccessible = true;
                        return Direction.CENTER;
                    }
                    // System.out.println("Switching handedness");
                    this.maybeinaccessible = true;
                    this.handedness = Handedness.LEFT;
                }
                if (this.myMove != Direction.CENTER) {
                    if (this.ohNoes.size() % 2 == 1) {
                        this.ohNoes.add(myloc);
                    }
                } */
                this.righthand = objective;
                while (dot(this.righthand, this.myMove) < 0 || !canMove(myloc, this.righthand)) {
                    this.memory_mode = this.memory_mode && !(dot(this.righthand, this.myMove) >= 0 && rc.sensePassability(myloc.add(this.righthand)));
                    if (!rc.onTheMap(myloc.add(this.righthand))) {
                        this.myMove = Direction.CENTER;
                        this.handedness = Handedness.LEFT;
                        return this.stepnext();
                    }
                    this.righthand = this.righthand.rotateLeft();
                    /* if (rc.getRoundNum() <= 10) {
                        // System.out.println("Now trying: "+this.righthand);
                        // System.out.println(!rc.canMove(this.righthand));
                        // System.out.println(dot(this.righthand, this.myMove));
                        // System.out.println((dot(this.righthand, this.myMove) < 0 || !rc.canMove(this.righthand)));
                    } */
                    //else { rc.resign(); }
                    if (this.righthand == objective) { return Direction.CENTER; }
                }
                //// System.out.println("I will move: "+this.righthand);
                
                if (this.righthand == objective) {
                //if (dot(this.righthand, objective) > 0) {
                    this.handedness = Handedness.NONE;
                    if (this.memory_mode) {
                        if (this.containsWaypoint(myloc, 2) == -1) {
                            this.add_found_waypoint(myloc.add(this.myMove.opposite()));
                            this.add_found_waypoint(myloc);
                        }
                        this.memory_mode = false;
                    }
                } 
                this.myMove = this.righthand;              
            break;
            case LEFT:
                /* if (this.ohNoes.contains(myloc) && this.myMove != Direction.CENTER) {
                    // System.out.println("This is an oh noes moment");
                    if (this.maybeinaccessible) {
                        // System.out.println("As far as I can tell, I can't get to that place right now");
                        this.inaccessible = true;
                        return Direction.CENTER;
                    }
                    // System.out.println("Switching handedness");
                    this.maybeinaccessible = true;
                    this.handedness = Handedness.RIGHT;
                }
                if (this.myMove != Direction.CENTER) {
                    if (this.ohNoes.size() % 2 == 1) {
                        this.ohNoes.add(myloc);
                    }
                } */
                this.lefthand = objective;
                while (dot(this.lefthand, this.myMove) < 0 || !canMove(myloc, this.lefthand)) {
                    this.memory_mode = this.memory_mode && !(dot(this.lefthand, this.myMove) >= 0 && rc.sensePassability(myloc.add(this.lefthand)));
                    if (!rc.onTheMap(myloc.add(this.lefthand))) {
                        this.myMove = Direction.CENTER;
                        this.handedness = Handedness.RIGHT;
                        return this.stepnext();
                    }
                    this.lefthand = this.lefthand.rotateRight();
                    if (this.lefthand == objective) { return Direction.CENTER; }
                }
                
                if (this.lefthand == objective) {
                //if (dot(this.lefthand, this.objective) > 0) {
                    this.handedness = Handedness.NONE;
                    if (this.memory_mode) {
                        if (this.containsWaypoint(myloc, 2) == -1){
                            this.add_found_waypoint(myloc.add(this.myMove.opposite()));
                            this.add_found_waypoint(myloc);
                        }
                        this.memory_mode = false;
                    }
                }
                this.myMove = this.lefthand;
            break;
            case NONE: 
                if (rc.canMove(objective)) {
                    this.myMove = objective;
                } else {
                    // System.out.println(this.ohNoes.size());
                    if (this.ohNoes.contains(myloc) && this.maybeinaccessible) {
                        // System.out.println("As far as I can tell, I can't get to that place right now");
                        this.inaccessible = true;
                        //return Direction.CENTER;
                    } 
                    else if (this.ohNoes.contains(myloc)) {
                        this.maybeinaccessible = true;
                        // System.out.println("I've set maybe inaccessible to true");
                        //rc.resign();
                    }
                    else {
                        // System.out.println("I went bonk while travelling to destination.");
                        this.ohNoes.add(myloc);
                        // System.out.println("I've added something to my ohnoes array");
                    }
                    this.memory_mode = true;
                    this.righthand = objective;
                    this.lefthand = objective;
                    /* if (rc.getRoundNum() <= 10) {
                        // System.out.println(this.righthand);
                        // System.out.println(this.lefthand);
                    } */
                    while (!canMove(myloc, this.righthand) && !canMove(myloc, this.lefthand)) {
                        /* // System.out.println(this.righthand);
                        // System.out.println(myloc.add(this.righthand));
                        // System.out.println(rc.canMove(this.righthand));
                        // System.out.println(rc.sensePassability(myloc.add(this.righthand)));
                        // System.out.println(this.lefthand);
                        // System.out.println(myloc.add(this.lefthand));
                        // System.out.println(rc.canMove(this.lefthand) );
                        // System.out.println(rc.sensePassability(myloc.add(this.lefthand))); */
                        this.memory_mode = this.memory_mode && (!rc.sensePassability(myloc.add(this.righthand)) && !rc.sensePassability(myloc.add(this.lefthand)));
                        this.righthand = this.righthand.rotateLeft();
                        this.lefthand = this.lefthand.rotateRight();
                        /* if (rc.getRoundNum() <= 10) {
                            System.out.println(this.righthand);
                            System.out.println(this.lefthand);
                        } */
                        if (this.lefthand == this.righthand) { break; }
                    }
                    /* if (rc.getRoundNum() <= 10) {
                        System.out.println(this.righthand);
                        System.out.println(this.lefthand);
                    } */
                    /* System.out.println("My current memory status: "+this.memory_mode);
                    System.out.println(this.righthand);
                    System.out.println(this.lefthand);  */
                    if (canMove(myloc, this.righthand)) {
                        //System.out.println("My right hand can move");
                        if (canMove(myloc, this.lefthand) && waypoint.distanceSquaredTo(myloc.add(this.righthand)) >= waypoint.distanceSquaredTo(myloc.add(this.lefthand))) {
                            //System.out.println("My left hand is better");
                            this.handedness = Handedness.LEFT;
                            this.myMove = this.lefthand;
                        } else {
                            this.myMove = this.righthand;
                            this.handedness = Handedness.RIGHT;
                        }
                        
                        this.memory_mode = this.memory_mode && !(!rc.canMove(this.lefthand) && rc.sensePassability(myloc.add(this.lefthand)));
                        //System.out.println("My current memory status: "+this.memory_mode);
                    }
                    else if (canMove(myloc, this.lefthand)) {
                        // System.out.println("My left hand can move");
                        this.handedness = Handedness.LEFT;
                        this.myMove = this.lefthand;
                        this.memory_mode = this.memory_mode && !(canMove(myloc, this.righthand) && rc.sensePassability(myloc.add(this.righthand)));
                        //System.out.println("My current memory status: "+this.memory_mode);
                    } else { /* System.out.println("Something bad happened eom"); */ return Direction.CENTER; }
                    // System.out.println("Handedenss discovered. My handedness: "+this.handedness);
                    if (this.maybeinaccessible) {
                        switch (this.handedness) {
                            case RIGHT:
                            this.handedness = Handedness.LEFT;
                            break;
                            case LEFT:
                            this.handedness = Handedness.RIGHT;
                            break;
                            case NONE:
                        }
                        // System.out.println("I've overwritten my handedness");
                        rc.setIndicatorString("My overwritten handedness: "+this.handedness);
                        this.myMove = Direction.CENTER;
                        return this.stepnext();
                    }
                    
                }
            break;
        }
        //System.out.println(this.myMove);
        // rc.setIndicatorString("Next waypoint: "+waypoint.toString()+" | "+this.handedness+" | Memory: "+this.memory_mode);
        rc.setIndicatorDot(waypoint, 200, 100, 100);

        if (!rc.onTheMap(myloc.add(this.myMove))) {
            System.out.println("I think I went bonk. Switching hands");
            switch (this.handedness) {
                case RIGHT: 
                    this.handedness = Handedness.LEFT;
                    break;
                case LEFT:
                    this.handedness = Handedness.RIGHT;
                    break;
                case NONE:
                    // System.out.println("Something hase gone very wrong. Ima just turn around real quick.");
            }
            
        }

        return this.myMove;
    }

    // memory
    public void add_found_waypoint(MapLocation waypoint) {
        // Waypoint pointer points to present destination. 
        // forwards: add before 
        // backwards: add after
        // System.out.println("I've found a new waypoint!");
        this.waypoints.add(this.waypoint_pointer + this.direction, waypoint);
        this.waypoint_pointer += 1 - this.direction;
        // System.out.println("Now pursuing: "+this.waypoints.get(this.waypoint_pointer));
        //this.waypoint_pointer -= 2*this.direction + 1;
    }

    public void add_next_waypoint(MapLocation waypoint) {
        //could be done in fewer lines but might be open to jank in case this is called when not at an endpoint
        if (this.isEndpoint()) {
            if (this.direction == 1) {
                this.waypoints.add(0, waypoint);
                this.waypoint_pointer = 1;
            } else {
                this.waypoints.add(waypoint);
            }
        }
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

    public float dot(Direction d1, Direction d2) {
        return d1.dx * d2.dx + d1.dy * d2.dy;
    }

    public String toString() {
        String ret = "";
        for (int i = 0; i < this.waypoints.size()-1; i++) {
            ret += this.waypoints.get(i).toString() + "("+i+") -> ";
        }
        ret += this.waypoints.get(this.waypoints.size()-1);
        ret += " ("+(this.waypoints.size()-1)+")";
        ret += " Direction: "+ (this.direction==1 ? "Backwards" : "Forward");

        return ret;
    }
    
}