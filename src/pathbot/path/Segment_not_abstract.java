/* package pathbot.path;

import battlecode.common.*;

import javafx.util.Pair;
import java.util.Random;

//import java.util.ArrayList;

public class Segment_not_abstract {

    // ENUMS

    public enum Handedness {
        FREE,
        LEFT,
        RIGHT
    }

    public enum HandType {
        UNEXPLORED,
        UNCONFIRMED,
        CONFIRMED,
        UNKNOWN,
        FORCEDLEFT,
        FORCEDRIGHT,
        PROVISIONAL,
        UNREACHABLE
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
    public HandType handtype;

    // UTIL

    public RobotController rc;

    public Random rng = new Random();

    // CONSTRUCTORS

    public Segment_not_abstract(RobotController rc, MapLocation start, MapLocation end) {
        this.waypoints[0] = new MapLocation[]{start, start};
        this.waypoints[1] = new MapLocation[]{end, end};
        this.handedness = Handedness.FREE;
        this.handtype = HandType.CONFIRMED;
        this.max_steps = -1;
    }    

    public Segment_not_abstract(RobotController rc, MapLocation[][] waypoints, Handedness handedness, HandType handtype) {
        // for when max_steps isn't known, e.g. creating a Segment_not_abstract to a brand new point
        this.waypoints = waypoints;
        this.handedness = handedness;
        this.handtype = handtype;
        this.max_steps = -1;
    }

    public Segment_not_abstract(RobotController rc, MapLocation[][] waypoints, Handedness handedness, HandType handtype, int max_steps) {
        this.waypoints = waypoints;
        this.handedness = handedness;
        this.handtype = handtype;
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

    public Segment_not_abstract[] split(MapLocation[] split_point, int direction, Handedness new_handedness, HandType new_handtype) {
        // returns left Segment_not_abstract, right Segment_not_abstract
        // TODO: implement handtype logic.
        // TODO: decide whether step function exists outside or inside. Probably inside, meaning max steps logic has to be in here too.
        Segment_not_abstract[] segs = new Segment_not_abstract[2];
        segs[direction] = new Segment_not_abstract(rc, new MapLocation[][]{this.waypoints[direction], split_point}, this.handedness, this.handtype);
        segs[1 - direction] = new Segment_not_abstract(rc, new MapLocation[][]{split_point, this.waypoints[1 - direction]}, new_handedness, new_handtype);
        return segs;
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

    public boolean isReachable(int direction) {
        return this.handtype != HandType.UNREACHABLE;   // includes UNEXPLORED.
                                                        // overwritten by bidirectional Segment_not_abstracts subclass.
    }

    public boolean isTestable() {
        // if forced/provisional, don't want to play with it.
        return this.handtype == HandType.UNEXPLORED || this.handtype == HandType.UNCONFIRMED || this.handtype == HandType.UNKNOWN;
    }

    public boolean countMe() {
        return this.handtype != HandType.CONFIRMED;
    }

    public boolean isPassable() {
        return this.handtype != HandType.UNREACHABLE;
    }

    // actual pathfinding within Segment_not_abstract

    public MapLocation destination(int direction) {
        return this.waypoints[1 - direction][direction];
    }

    public Direction righthand; 
    public Direction lefthand; 

    public Direction myMove; // refers move to be played. After moved, used as a marker for most recent move.

    //public ArrayList<MapLocation> ohNoes = new ArrayList<MapLocation>();
    public MapLocation ohNoes;
    // consider: handle this outside, since it's a multi-Segment_not_abstract thing. 

    public boolean passable(Direction dir, MapInfo info) {
        return false;
    }

    public Pair<Direction, Segment_not_abstract[]> follow(MapLocation myloc, int direction, MapInfo[] scan_info) throws GameActionException {
        // returns direction to step. Segment_not_abstract part of pair is [null, null] if unsplit. Else, is [new left, new right] Segment_not_abstracts.
        // arrival is handled externally

        MapLocation destination = destination(direction);
        Direction objective = myloc.directionTo(destination);

        boolean botbonk = false; // remembers whether go bonk on bot
        
        Segment_not_abstract[] segs = new Segment_not_abstract[]{null, null};

        switch (this.handedness) {
            // CONSIDER: MAKE ABSTRACT, EACH HANDTYPE IS SUBCLASS
            case FREE:
                if (rc.canMove(objective)) {
                    // if free and can step towards objective, then just do it.
                    this.myMove = objective;
                }
                else {
                    this.righthand = objective;
                    this.lefthand = objective;
                    while (!rc.canMove(this.righthand) && !rc.canMove(this.lefthand)) {
                        if (rc.sensePassability(myloc.add(this.righthand)) || rc.sensePassability(myloc.add(this.lefthand))) {
                            // if either RH or LH were passable but neither could move, then a robot was occupying that space.
                            // in that case, we should not put much faith in waypoints found from here on out.
                            // recall botbonk will cause new handtype to be set to unknown later.  
                            botbonk = true;
                        }
                        this.righthand = this.righthand.rotateLeft();
                        this.lefthand = this.lefthand.rotateRight();
                        
                        if (this.lefthand == this.righthand) {
                            // if this condition is satisfied, both point backwards. Stop looking. 
                            break;
                        }
                    }
                    if (rc.canMove(this.righthand)) {
                        // HANDTYPE LOGIC: with botbonk
                            // provisional
                            // unexplored
                            // unconfirmed
                            // TODO
                        if (this.handtype == HandType.UNEXPLORED) {
                            this.handtype = HandType.UNCONFIRMED;
                        }
                        if (rc.canMove(this.lefthand)) {
                            int rd = myloc.add(this.righthand).distanceSquaredTo(destination);
                            int ld = myloc.add(this.lefthand).distanceSquaredTo(destination);
                            
                            if (rd == ld) {
                                // random
                                Handedness h = rng.nextBoolean() ? Handedness.RIGHT : Handedness.LEFT;
                                segs = this.split(new MapLocation[]{myloc, myloc.add(this.myMove.opposite())}, direction, h, HandType.UNKNOWN);
                            }
                            if (rd < ld) {
                                // right handed
                                segs = this.split(new MapLocation[]{myloc, myloc.add(this.myMove.opposite())}, direction, Handedness.RIGHT, HandType.UNKNOWN);
                            }
                            if (ld < rd) {
                                // left handed
                                segs = this.split(new MapLocation[]{myloc, myloc.add(this.myMove.opposite())}, direction, Handedness.LEFT, HandType.UNKNOWN);
                            }
                        }
                        else {
                            segs = this.split(new MapLocation[]{myloc, myloc.add(this.myMove.opposite())}, direction, Handedness.RIGHT, HandType.UNKNOWN);
                        }
                    } else if (rc.canMove(this.lefthand)) {
                        // HANDTYPE LOGIC: with botbonk
                            // provisional
                            // unexplored
                            // unconfirmed
                            // TODO
                        if (this.handtype == HandType.UNEXPLORED) {
                            this.handtype = HandType.UNCONFIRMED;
                        }
                        segs = this.split(new MapLocation[]{myloc, myloc.add(this.myMove.opposite())}, direction, Handedness.LEFT, HandType.UNKNOWN);
                    } else { 
                        // can't move. Should stall. 
                        // handtype logic. 
                        this.myMove = Direction.CENTER; 
                        break; 
                    }
                }
            break;
            case RIGHT:

            break;
            case LEFT:

            break;
        }

        // handle logic for arrival and changing handtype from unexplored etc.

        if (this.countMe() && this.myMove != Direction.CENTER) {
            this.max_steps += 1;
        }

        return new Pair<Direction, Segment_not_abstract[]>(this.myMove, segs);
    }

} */