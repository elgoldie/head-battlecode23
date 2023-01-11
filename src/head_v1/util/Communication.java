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
        int value = rc.readSharedArray(index) - 1;
        return new MapLocation((value >> 6) & 0x3F, value & 0x3F);
    }

    public int readLocationFlags(int index) throws GameActionException {
        return rc.readSharedArray(index) >> 12;
    }

    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        rc.writeSharedArray(index, (loc.x << 6) + loc.y + 1);
    }

    public void writeLocation(int index, MapLocation loc, int flags) throws GameActionException {
        rc.writeSharedArray(index, (flags << 12) + (loc.x << 6) + loc.y + 1);
    }

    public void writeLocationFlags(int index, int flags) throws GameActionException {
        int value = rc.readSharedArray(index);
        rc.writeSharedArray(index, (flags << 12) + (value & 0xFFF));
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
