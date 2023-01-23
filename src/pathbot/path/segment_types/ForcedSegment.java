/* package pathbot.path.segment_types;

import javafx.util.Pair;

import battlecode.common.*;

public class ForcedSegment extends Segment {

    public Handedness forced_handedness;

    public ForcedSegment(RobotController rc, MapLocation start, MapLocation end, Handedness forced_handedness) {
        super(rc, start, end);
        this.handedness = Handedness.FREE;
        this.forced_handedness = forced_handedness;
        this.max_steps = -1;
    }   

    public ForcedSegment(RobotController rc, MapLocation[] left, MapLocation[] right, Handedness handedness, Handedness forced_handedness, int max_steps) {
        super(rc, left, right, handedness, max_steps);
        this.forced_handedness = forced_handedness;
        this.max_steps = -1;
    }  

    public boolean isTestable() {
        return false;
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

    // gotta do follow logic. If objective hits a wall, then procs the force. 

}
 */