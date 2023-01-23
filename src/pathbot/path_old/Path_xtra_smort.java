/* package head_v2_pf.path;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class Path {
    
    public RobotController rc;

    // WAYPOINT 
    @SuppressWarnings("unchecked")
    public ArrayList<Waypoint>[] waypoints = (ArrayList<Waypoint>[]) new ArrayList[2];

    public int waypoint_pointer = -1;
    public int direction = 0; //0 = forward (origin -> destination), 1 = backwards
    

    // HANDS BE LIKE
    public enum Handedness {
        NONE,
        LEFT,
        RIGHT
    }

    public enum HandType {
        NONE,
        UNKNOWN,
        CERTAIN,
        FORCED,
        PROVISIONAL
    }

    public boolean memory_mode;
    
    public Handedness handedness = Handedness.NONE;
    public Direction lefthand;
    public Direction righthand;
    public int stepcount;

    public Direction myMove;
    public MapLocation myBonk;
    
    

    public Path(MapLocation origin, MapLocation destination, RobotController rc) {
        this.waypoints[0].add(new Waypoint(origin)); this.waypoints[0].add(new Waypoint(destination));
        this.waypoints[1].add(new Waypoint(origin)); this.waypoints[1].add(new Waypoint(destination));
        this.rc = rc;
    }

    public void initiate_pathfinding() {
        initiate_pathfinding(new int[]{0,1});
    }

    public void initiate_pathfinding(int[] waypoints) {
        
        int start = waypoints[0];
        int end = waypoints[1];
        this.direction = start - end > 0 ? 1 : 0;
        this.handedness = Handedness.NONE;
        this.memory_mode = false;
        this.myMove = Direction.CENTER;

        this.waypoint_pointer = advanced_pointer(start, 1);

        int[] index_shift = new int[2];

        // skip past CERTAIN entries

        // i = 0: start (with direction) | 1: end (against direction). 
        // j = 0: FW index | 1: BW index.
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {   
                index_shift[j] = 1 - i - j;
            }            
            //FW            start index     corresp. shift                                      BW                  start index
            while (this.waypoints[0].get(waypoints[i] + index_shift[0]).handtype == HandType.CERTAIN && this.waypoints[1].get(waypoints[i] + index_shift[1]).handtype == HandType.CERTAIN) {
                waypoints[i] = advanced_pointer(waypoints[i], 1);
            }
        }
        
        int next_uncertain_pointer = waypoints[this.direction] + index_shift[this.direction];
        if (isValid(advanced_pointer(next_uncertain_pointer, 1))) {
            // need to find next handed WP
            Waypoint first_fw_uncertain = this.waypoints[this.direction].get(next_uncertain_pointer);
            Waypoint second_fw_uncertain = this.waypoints[this.direction].get(advanced_pointer(next_uncertain_pointer, 1));
            Waypoint first_bw_uncertain = this.waypoints[1 - this.direction].get(next_uncertain_pointer);
            Waypoint second_bw_uncertain = this.waypoints[1 - this.direction].get(advanced_pointer(next_uncertain_pointer, 1));
            
            if ((first_fw_uncertain.handtype == HandType.NONE || first_fw_uncertain.handtype == HandType.UNKNOWN)) {
                if (first_bw_uncertain.handtype == HandType.NONE && second_bw_uncertain.handtype == HandType.NONE) {
                    if (second_bw_uncertain.handtype == HandType.NONE) {
                        // forced
                    }
                }
            }
        }
        
        // if unknown, work with maxes. Else, mins.




        //this.waypoint_pointer = start - 2*direction + 1;
        
    }

    public int direction_sign(int d) {
        return -2*d+1;
    }

    public void advance_pointer() {
        //System.out.println("Old pointer: "+this.waypoint_pointer);
        this.waypoint_pointer -= 2*direction - 1;
        //System.out.println("New pointer: "+this.waypoint_pointer);
    }

    // advance to next handed? 

    public int advanced_pointer(int i) {
        return this.waypoint_pointer - i*(2*direction - 1);
    }

    public int advanced_pointer(int pointer, int i) {
        return pointer - i*(2*direction - 1);
    }

    public boolean isEndpoint() {
        // TODO : revamp
        return (this.waypoint_pointer == 0 && this.direction == 1) || (this.waypoint_pointer == this.waypoints[0].size() - 1 && this.direction == 0);
    }

    public boolean isValid(int pointer) {
        return (pointer >= 0 && pointer <= this.waypoints[this.direction].size() - 1);
    }

    public boolean hasArrived(MapLocation myloc) {
        // might need full endpoint/destination tracking
        return this.isEndpoint() && myloc.isAdjacentTo(this.waypoints[this.direction].get(this.waypoint_pointer));
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
                System.out.println("Arrived at final point on waypoint path.");
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
            System.out.println("Waypoint reached! Now pursuing: "+this.waypoints.get(this.waypoint_pointer)+"\nfrom: "+myloc);
            //System.out.println("New pointer: "+this.waypoint_pointer);
            waypoint = this.waypoints.get(this.waypoint_pointer);
        }

        Direction objective = myloc.directionTo(waypoint);
        
        //if (myloc.isAdjacentTo(this.waypoints.get(this.waypoint_pointer + 2*this.direction - 1))) {
        //    System.out.println("Next objective: "+objective);
        //    System.out.println("My queue: "+this.movequeue);
        //}

        switch (this.handedness) {
            case RIGHT:
                this.righthand = objective;
                while (dot(this.righthand, this.myMove) < 0 || !rc.canMove(this.righthand)) {
                    this.memory_mode = this.memory_mode && !(dot(this.righthand, this.myMove) >= 0 && rc.sensePassability(myloc.add(this.righthand)));
                    this.righthand = this.righthand.rotateLeft();
                    /* if (rc.getRoundNum() <= 10) {
                        System.out.println("Now trying: "+this.righthand);
                        System.out.println(!rc.canMove(this.righthand));
                        System.out.println(dot(this.righthand, this.myMove));
                        System.out.println((dot(this.righthand, this.myMove) < 0 || !rc.canMove(this.righthand)));
                    } :)
                    //else { rc.resign(); }
                    if (this.righthand == objective) { return Direction.CENTER; }
                }
                //System.out.println("I will move: "+this.righthand);
                
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
                this.lefthand = objective;
                while (dot(this.lefthand, this.myMove) < 0 || !rc.canMove(this.lefthand)) {
                    this.memory_mode = this.memory_mode && !(dot(this.lefthand, this.myMove) >= 0 && rc.sensePassability(myloc.add(this.lefthand)));
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
                    this.memory_mode = true;
                    this.righthand = objective;
                    this.lefthand = objective;
                    /* if (rc.getRoundNum() <= 10) {
                        System.out.println(this.righthand);
                        System.out.println(this.lefthand);
                    } :)
                    while (!rc.canMove(this.righthand) && !rc.canMove(this.lefthand)) {
                        /* System.out.println(this.righthand);
                        System.out.println(myloc.add(this.righthand));
                        System.out.println(rc.canMove(this.righthand));
                        System.out.println(rc.sensePassability(myloc.add(this.righthand)));
                        System.out.println(this.lefthand);
                        System.out.println(myloc.add(this.lefthand));
                        System.out.println(rc.canMove(this.lefthand) );
                        System.out.println(rc.sensePassability(myloc.add(this.lefthand))); :)
                        this.memory_mode = this.memory_mode && (!rc.sensePassability(myloc.add(this.righthand)) && !rc.sensePassability(myloc.add(this.lefthand)));
                        if (!rc.sensePassability(myloc.add(this.righthand)) ^ !rc.sensePassability(myloc.add(this.lefthand))) { System.out.println(this.lefthand); System.out.println(this.righthand); System.out.println("Whomst the fuck"); }
                        this.righthand = this.righthand.rotateLeft();
                        this.lefthand = this.lefthand.rotateRight();
                        /* if (rc.getRoundNum() <= 10) {
                            System.out.println(this.righthand);
                            System.out.println(this.lefthand);
                        } :)
                        if (this.lefthand == this.righthand) { break; }
                    }
                    /* if (rc.getRoundNum() <= 10) {
                        System.out.println(this.righthand);
                        System.out.println(this.lefthand);
                    } :)
                    /* System.out.println("My current memory status: "+this.memory_mode);
                    System.out.println(this.righthand);
                    System.out.println(this.lefthand);  :)
                    if (rc.canMove(this.righthand)) {
                        //System.out.println("My right hand can move");
                        if (rc.canMove(this.lefthand) && waypoint.distanceSquaredTo(myloc.add(this.righthand)) >= waypoint.distanceSquaredTo(myloc.add(this.lefthand))) {
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
                    else if (rc.canMove(this.lefthand)) {
                        System.out.println("My left hand can move");
                        this.handedness = Handedness.LEFT;
                        this.myMove = this.lefthand;
                        this.memory_mode = this.memory_mode && !(!rc.canMove(this.righthand) && rc.sensePassability(myloc.add(this.righthand)));
                        //System.out.println("My current memory status: "+this.memory_mode);
                    } else { /* System.out.println("Something bad happened eom"); :) return Direction.CENTER; }
                    
                }
            break;
        }
        //System.out.println(this.myMove);
        rc.setIndicatorString("Next waypoint: "+waypoint.toString()+" | "+this.handedness+" | Memory: "+this.memory_mode);
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
                    System.out.println("Something hase gone very wrong. Ima just turn around real quick.");
            }
            return this.myMove.opposite();
        }
        //if (rc.getRoundNum() == 4) { rc.resign(); }

        return this.myMove;
    }

    // memory
    public void add_found_waypoint(MapLocation waypoint) {
        // Waypoint pointer points to present destination. 
        // forwards: add before
        // backwards: add after
        System.out.println("I've found a new waypoint!");
        this.waypoints.add(this.waypoint_pointer + this.direction, waypoint);
        this.waypoint_pointer += 1 - this.direction;
        System.out.println("Now pursuing: "+this.waypoints.get(this.waypoint_pointer));
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

    
    // HELPERS

    public float dot(Direction d1, Direction d2) {
        return d1.dx * d2.dx + d1.dy * d2.dy;
    }

    public float min_steps(MapLocation m1, MapLocation m2) {
        return Math.min(Math.abs(m1.x - m2.x), Math.min(Math.abs(m1.y - m2.y)));
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

 */