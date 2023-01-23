/* package pathbot.path.segment_types;

import battlecode.common.*;

public class FollowInfo {

    public Direction move;
    public boolean botbonk;
    public boolean currentbonk;
    public boolean edgebonk;
    public Segment.Handedness new_handedness;
    public boolean stuck;

    public FollowInfo(Segment.Handedness handedness) {
        this.move = Direction.CENTER;
        this.botbonk = false;
        this.currentbonk = false;
        this.edgebonk = false;
        this.new_handedness = handedness;
        this.stuck = false;
    }

    public FollowInfo(Direction move, boolean botbonk, boolean currentbonk, boolean edgebonk, Segment.Handedness new_handedness) {
        this.move = move;
        this.botbonk = botbonk;
        this.currentbonk = currentbonk;
        this.edgebonk = edgebonk;
        this.new_handedness = new_handedness;
        this.stuck = false;
    }

    public boolean bonk() {
        return this.botbonk || this.currentbonk || this.edgebonk;
    }

}
 */