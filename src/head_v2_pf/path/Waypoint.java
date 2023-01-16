package head_v2.path;

import battlecode.common.*;

public class Waypoint {

    public MapLocation waypoint;
    
    public Path.Handedness handedness;
    public Path.HandType handtype;

    public Direction toMe;

    public int max_steps; // assumed to exist in an array where its connected Waypoint (in its direction) is known

    public Waypoint(MapLocation waypoint) {
        this.waypoint = waypoint;
        this.handedness = Path.Handedness.NONE;
        this.handtype = Path.HandType.UNKNOWN;
        this.toMe = Direction.CENTER;
        this.max_steps = 0;
    }

    public Waypoint(MapLocation waypoint, Path.Handedness handedness, Path.HandType handtype) {
        this.waypoint = waypoint;
        this.handedness = handedness;
        this.handtype = handtype;        
        this.toMe = Direction.CENTER;
        this.max_steps = 0;
    }

    public Waypoint(MapLocation waypoint, Path.Handedness handedness, Path.HandType handtype, Direction toMe, int max_steps) {
        this.waypoint = waypoint;
        this.handedness = handedness;
        this.handtype = handtype;        
        this.toMe = toMe;
        this.max_steps = max_steps;
    }

    public Waypoint prior() { // HANDTYPE???? Should be one of (same as prior), forced, or certain
        return new Waypoint(this.waypoint.add(this.toMe.opposite()), this.reversed_handedness(), this.handtype, Direction.CENTER, 0);
    }

    public boolean hasArrived(MapLocation myLoc) {
        return myLoc.isAdjacentTo(this.waypoint);
    }

    public Path.Handedness reversed_handedness() {
        switch (this.handedness) {
            case LEFT: return Path.Handedness.RIGHT;
            case RIGHT: return Path.Handedness.LEFT;
            case NONE: return Path.Handedness.NONE;
        }
        return Path.Handedness.NONE;
    }

}
