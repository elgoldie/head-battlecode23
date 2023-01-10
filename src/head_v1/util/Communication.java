package head_v1.util;

import battlecode.common.*;

public class Communication {

    public RobotController rc;

    public Communication(RobotController rc) {
        this.rc = rc;
    }
    
    public int readByte(int index) throws GameActionException {
        return rc.readSharedArray(index);
    }

    public void writeByte(int index, int value) throws GameActionException {
        rc.writeSharedArray(index, value);
    }

    public MapLocation readLocation(int index) throws GameActionException {
        int x = rc.readSharedArray(index);
        int y = rc.readSharedArray(index + 1);
        return new MapLocation(x, y);
    }

    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        rc.writeSharedArray(index, loc.x);
        rc.writeSharedArray(index + 1, loc.y);
    }
}
