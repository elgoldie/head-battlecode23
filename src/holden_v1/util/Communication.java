package holden_v1.util;

import java.util.Arrays;

import battlecode.common.*;

public class Communication {

    public RobotController rc;

    public int[] queue;
    public boolean queueActive;

    public Communication(RobotController rc) {
        this.rc = rc;
        this.queue = new int[64];
        for (int i = 0; i < 64; i++) queue[i] = -1;
        this.queueActive = false;
    }

    // for debug purposes
    public void dispArray() throws GameActionException {
        int[] array = new int[64];
        for (int i = 0; i < 64; i++) {
            array[i] = rc.readSharedArray(i);
        }
        rc.setIndicatorString(Arrays.toString(array));
    }

    public void queueWrite(int index, int value) throws GameActionException {
        if (rc.canWriteSharedArray(index, value)) {
            rc.writeSharedArray(index, value);
        } else {
            queue[index] = value;
            queueActive = true;
        }
    }

    public void queueFlush() throws GameActionException {
        for (int i = 0; i < 64; i++) {
            if (queue[i] != -1) {
                rc.writeSharedArray(i, queue[i]);
                queue[i] = -1;
            }
        }
        queueActive = false;
    }

    public MapLocation readLocation(int index) throws GameActionException {
        int value = rc.readSharedArray(index) - 1;
        return new MapLocation((value >> 6) & 0x3F, value & 0x3F);
    }

    public int readLocationFlags(int index) throws GameActionException {
        return rc.readSharedArray(index) >> 12;
    }

    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        queueWrite(index, (loc.x << 6) + loc.y + 1);
    }

    public void writeLocation(int index, MapLocation loc, int flags) throws GameActionException {
        queueWrite(index, (flags << 12) + (loc.x << 6) + loc.y + 1);
    }

    public void writeLocationFlags(int index, int flags) throws GameActionException {
        int value = rc.readSharedArray(index);
        queueWrite(index, (flags << 12) + (value & 0xFFF));
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
        queueWrite(startIndex, length + 1);
    }

    public boolean hasNoLocation(int index) throws GameActionException {
        return (rc.readSharedArray(index) & 0xFFF) == 0;
    }
}
