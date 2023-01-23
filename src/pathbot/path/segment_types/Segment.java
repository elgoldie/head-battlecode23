/* package pathbot.path.segment_types;

import battlecode.common.*;

import javafx.util.Pair;
import pathbot.path.ArrivalCondition;

import java.util.Random;

//import java.util.ArrayList;

public abstract class Segment {

    // ENUMS

    public enum Handedness {
        FREE,
        LEFT,
        RIGHT
    }

    // STORAGE

    //public MapLocation[] left = new MapLocation[2]; // as usual, direction 0 is forward (L->R), direction 1 is backward (R->L))
    //public MapLocation[] right = new MapLocation[2];
    //public MapLocation[][] waypoints = new MapLocation[2][2];
    //waypoints = {left, right}. i.e. first index is LEFT/RIGHT, second index is FW/BW.

    //recall that for backwards maplocations, must reverse handedness for pathfinding.

    public MapLocation[][] waypoints = new MapLocation[2][2];

    public int max_steps; // Values: -1 ~ unknown, > 0 = counted

    public Handedness handedness;
    // public HandType handtype;

    // UTIL

    public RobotController rc;
    public Team myteam;

    public Random rng = new Random();

    // CONSTRUCTORS

    public Segment(RobotController rc, MapLocation start, MapLocation end) {
        this.rc = rc;
        this.myteam = rc.getTeam();
        this.waypoints[0] = new MapLocation[]{start, start};
        this.waypoints[1] = new MapLocation[]{end, end};
        this.handedness = Handedness.FREE;
        this.max_steps = -1;
    }   

    public Segment(RobotController rc, MapLocation[] left, MapLocation[] right, Handedness handedness) {
        this.rc = rc;
        this.myteam = rc.getTeam();
        this.waypoints[0] = left;
        this.waypoints[1] = right;
        this.handedness = handedness;
        this.max_steps = -1;
    } 

    public Segment(RobotController rc, MapLocation[] left, MapLocation[] right, Handedness handedness, int max_steps) {
        this.rc = rc;
        this.myteam = rc.getTeam();
        this.waypoints[0] = left;
        this.waypoints[1] = right;
        this.handedness = handedness;
        this.max_steps = max_steps;
    }  

    public boolean hasArrived(int direction, MapLocation myLoc) {
        // default: adjacency is good enough. 
        return myLoc.isAdjacentTo(this.waypoints[1 - direction][direction]);
    }

    public boolean hasArrived(int direction, MapLocation myLoc, ArrivalCondition ac) {
        // https://stackoverflow.com/questions/4685563/how-to-pass-a-function-as-a-parameter-in-java
        // used for more strict conditions, e.g. strict occupancy or something to do with robot scan
        return ac.hasArrived(myLoc, this.waypoints[1 - direction][direction]);
    }

    // helpers

    public Handedness reversed_handedness() {
        // self-explanatory
        switch (this.handedness) {
            case LEFT: return Handedness.RIGHT;
            case RIGHT: return Handedness.LEFT;
            case FREE: return Handedness.FREE;
        }
        return Handedness.FREE;
    }

    abstract boolean isReachable(int direction);

    abstract boolean isTestable();

    abstract boolean countMe();

    abstract boolean isTraversible();

    // actual pathfinding within segment

    public MapLocation destination(int direction) {
        return this.waypoints[1 - direction][direction];
    }

    public Direction righthand; 
    public Direction lefthand; 

    public Direction myMove; // refers move to be played. After moved, used as a marker for most recent move.

    public boolean passability(MapLocation myloc, Direction dir, MapInfo[] map_info, RobotInfo[] robot_info, FollowInfo f) throws GameActionException {
        MapLocation destination = myloc.add(dir);

        if (!rc.onTheMap(destination)) {
            f.edgebonk = true;
            return false;
        }
        if (rc.canMove(dir)) {
            for (MapInfo m : map_info) {
                if (m.getMapLocation() == destination) {
                    if (m.getCurrentDirection() != Direction.CENTER) {
                        f.currentbonk = true;
                        return true;
                    }
                }
            }
            for (RobotInfo r : robot_info) {
                // avoid enemy HQ
                if (r.getType() == RobotType.HEADQUARTERS && r.getTeam() != myteam && destination.distanceSquaredTo(r.getLocation()) <= 3) {
                    // HQ threat range: 3. Couldn't find in gameconstants as of v2.0.3.
                    return false;
                }
            }
        }
        else {
            if (rc.sensePassability(destination)) {
                f.botbonk = true;
            }
            return false;
        }
        return false;
    }

    abstract Pair<Direction, Segment[]> follow(MapLocation myloc, int direction, MapInfo[] map_info, RobotInfo[] robot_info) throws GameActionException;

    public FollowInfo basic_follow(MapLocation myloc, MapLocation destination, MapInfo[] map_info, RobotInfo robot_info[]) throws GameActionException {
        // returns direction to step. Segment_not_abstract part of pair is [null, null] if unsplit. Else, is [new left, new right] Segment_not_abstracts.
        // arrival is handled externally

        Direction objective = myloc.directionTo(destination);

        FollowInfo f = new FollowInfo(this.handedness);

        switch (this.handedness) {
            case FREE:
                if (rc.canMove(objective)) {
                    // if free and can step towards objective, then just do it.
                    f.move = objective;
                }
                else {
                    this.righthand = objective;
                    this.lefthand = objective;
                    while (!passability(myloc, this.righthand, map_info, robot_info, f) && !passability(myloc, this.lefthand, map_info, robot_info, f)) {
                        this.righthand = this.righthand.rotateLeft();
                        this.lefthand = this.lefthand.rotateRight();
                        if (this.lefthand == this.righthand) {
                            // if this condition is satisfied, both point backwards. Stop looking. 
                            break;
                        }
                    }
                    if (passability(myloc, this.righthand, map_info, robot_info, f)) {
                        if (passability(myloc, this.lefthand, map_info, robot_info, f)) {
                            int rd = myloc.add(this.righthand).distanceSquaredTo(destination);
                            int ld = myloc.add(this.lefthand).distanceSquaredTo(destination);
                            
                            if (rd == ld) {
                                // random
                                f.new_handedness = rng.nextBoolean() ? Handedness.RIGHT : Handedness.LEFT;
                                if (f.new_handedness == Handedness.RIGHT) {
                                    f.move = this.righthand;
                                } else {
                                    f.move = this.lefthand;
                                }
                            }
                            if (rd < ld) {
                                // right handed
                                f.new_handedness = Handedness.RIGHT;
                                f.move = this.righthand;
                            }
                            if (ld < rd) {
                                // left handed
                                f.new_handedness = Handedness.LEFT;
                                f.move = this.lefthand;
                            }
                        }
                        else {
                            f.new_handedness = Handedness.RIGHT;
                            f.move = this.righthand;
                        }
                    } else if (rc.canMove(this.lefthand)) {
                        f.new_handedness = Handedness.LEFT;
                        f.move = this.lefthand;
                    } else { 
                        // can't move. Should stall. 
                        // handtype logic. 
                        f.move = Direction.CENTER; 
                        f.stuck = true;
                        break; 
                    }
                }
            break;
            case RIGHT:
                this.righthand = objective;

                while (dot(this.righthand, this.myMove) < 0 || !passability(myloc, this.righthand, map_info, robot_info, f)) {
                    this.righthand = this.righthand.rotateLeft();
                    if (this.righthand == objective) {
                        f.stuck = true;
                    }
                }

                if (this.righthand == objective) {
                    f.new_handedness = Handedness.FREE;
                    f.move = this.righthand;
                }
            break;
            case LEFT:
                this.lefthand = objective;

                while (dot(this.lefthand, this.myMove) < 0 || !passability(myloc, this.lefthand, map_info, robot_info, f)) {
                    this.lefthand = this.lefthand.rotateRight();
                    if (this.lefthand == objective) {
                        f.stuck = true;
                    }
                }

                if (this.lefthand == objective) {
                    f.new_handedness = Handedness.FREE;
                    f.move = this.lefthand;
                }
            break;
        }

        return f;
    }

    public MapLocation[] myPair(MapLocation myloc, int direction) {
        MapLocation[] ret = new MapLocation[2];
        ret[direction] = myloc;
        ret[1 - direction] = myloc.add(this.myMove.opposite());
        return ret;
    }

    public float dot(Direction d1, Direction d2) {
        return d1.dx * d2.dx + d1.dy * d2.dy;
    }
} */