/* package pathbot.path.segment_types;

import javafx.util.*;

import battlecode.common.*;

public class UnconfirmedSegment extends Segment {
    
    public UnconfirmedSegment(RobotController rc, MapLocation start, MapLocation end, Handedness handedness) {
        super(rc, start, end);
        this.handedness = handedness;
        this.max_steps = -1;
    }   

    public UnconfirmedSegment(RobotController rc, MapLocation start, MapLocation end, Handedness handedness, int max_steps) {
        super(rc, start, end);
        this.handedness = handedness;
        this.max_steps = max_steps;
    }   

    public UnconfirmedSegment(RobotController rc, MapLocation[] left, MapLocation[] right, Handedness handedness, int max_steps) {
        super(rc, left, right, handedness, max_steps);
    }  

    public boolean isTestable() {
        return true;
    }

    public boolean countMe() {
        return true;
    }

    public boolean isTraversible() {
        return true;
    }

    public boolean isReachable(int direction) {
        return true;
    }

    public Pair<Direction, Segment[]> follow(MapLocation myloc, int direction, MapInfo[] map_info, RobotInfo robot_info[]) throws GameActionException {
        // returns direction to step. Segment_not_abstract part of pair is [null, null] if unsplit. Else, is [new left, new right] Segment_not_abstracts.
        // arrival is handled externally

        Segment[] segs = new Segment[]{null, null};

        FollowInfo f = this.basic_follow(myloc, destination(direction), map_info, robot_info);

        if (f.new_handedness != this.handedness) {
            MapLocation[] split_point = this.myPair(myloc, direction);
            if (f.currentbonk) {
                // Unconfirmed and Current
                segs[direction] = new UnconfirmedSegment(rc, this.waypoints[direction], split_point, this.handedness, this.max_steps);
                segs[1 - direction] = new CurrentSegment(rc, split_point, this.waypoints[1 - direction], f.new_handedness, -1, f.botbonk);
            }
            if (f.botbonk) {
                // Unconfirmed and Disturbed
                segs[direction] = new UnconfirmedSegment(rc, this.waypoints[direction], split_point, this.handedness, this.max_steps);
                segs[1 - direction] = new DisturbedSegment(rc, split_point, this.waypoints[1 - direction], f.new_handedness, -1);
            }
        } else if (f.edgebonk) {
            this.handedness = reversed_handedness();
        }

        return new Pair<Direction, Segment[]>(this.myMove, segs);
    }

}
 */