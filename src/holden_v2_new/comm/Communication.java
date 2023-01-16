package holden_v2_new.comm;

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
        if (value == -1) return null;
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

    public MapLocation[] readLocations(int startIndex, int endIndex) throws GameActionException {
        MapLocation[] array = new MapLocation[endIndex - startIndex];
        for (int i = 0; i < endIndex - startIndex; i++) {
            array[i] = readLocation(startIndex + i);
        }
        return array;
    }

    public MapLocation[] readLocationsNonNull(int startIndex, int endIndex) throws GameActionException {
        MapLocation[] array = new MapLocation[endIndex - startIndex];
        int count = 0;
        for (int i = 0; i < endIndex - startIndex; i++) {
            MapLocation loc = readLocation(startIndex + i);
            if (loc.x != 0 || loc.y != 0) {
                array[count] = loc;
                count++;
            }
        }
        return Arrays.copyOf(array, count);
    }

    public int appendLocation(int startIndex, MapLocation loc) throws GameActionException {
        for (int index = startIndex; index < 64; index++) {
            MapLocation loc2 = readLocation(index);
            if (loc2 == null) {
                writeLocation(index, loc);
                return index;
            } else if (loc2.equals(loc)) {
                return -1;
            }
        }
        return -1;
    }

    public boolean hasNoLocation(int index) throws GameActionException {
        return (rc.readSharedArray(index) & 0xFFF) == 0;
    }

    public boolean isDistressed(int index) throws GameActionException {
        return (rc.readSharedArray(index) & 0x1000) != 0;
    }
}
