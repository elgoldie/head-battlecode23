package head_v1.util;

import java.util.Arrays;

import battlecode.common.*;

public class Communication {

    public RobotController rc;

    public Communication(RobotController rc) {
        this.rc = rc;
    }

    // for debug purposes
    public void printArray() throws GameActionException {
        int[] array = new int[64];
        for (int i = 0; i < 64; i++) {
            array[i] = rc.readSharedArray(i);
        }
        System.out.println(Arrays.toString(array));
    }

    public MapLocation readLocation(int index) throws GameActionException {
        int value = rc.readSharedArray(index);
        return new MapLocation((value >> 6) & 0x3F, value & 0x3F);
    }

    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        rc.writeSharedArray(index, (loc.x << 6) + loc.y);
    }

    public MapLocation[] readLocationArray(int startIndex) throws GameActionException {
        int length = rc.readSharedArray(startIndex);
        MapLocation[] array = new MapLocation[length];
        for (int i = 0; i < length; i++) {
            array[i] = readLocation(startIndex + i + 1);
        }
        return array;
    }

    public void appendLocation(int startIndex, MapLocation loc) throws GameActionException {
        int length = rc.readSharedArray(startIndex);
        writeLocation(startIndex + length + 1, loc);
        rc.writeSharedArray(startIndex, length + 1);
    }

    public void putLocation(int startIndex, MapLocation loc, int index) throws GameActionException {
        writeLocation(startIndex + index + 1, loc);
        if (index >= rc.readSharedArray(startIndex)) {
            rc.writeSharedArray(startIndex, index + 1);
        }
    }
}
