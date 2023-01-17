package head_latest.path;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public enum Symmetry {

    ROTATION,
    REFLECTION_X,
    REFLECTION_Y;

    public MapLocation transform(RobotController rc, MapLocation loc) {
        switch (this) {
            case ROTATION:
                return new MapLocation(rc.getMapWidth() - 1 - loc.x, rc.getMapHeight() - 1 - loc.y);
            case REFLECTION_X:
                return new MapLocation(loc.x, rc.getMapHeight() - 1 - loc.y);
            case REFLECTION_Y:
                return new MapLocation(rc.getMapWidth() - 1 - loc.x, loc.y);
            default:
                return loc;
        }
    }
}
