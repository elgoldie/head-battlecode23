package head_v1.util;

import java.util.Arrays;

import battlecode.common.*;

public class Communication {

    // test comment

    public RobotController rc;
    // prevents us from accidentally using more bytecode than necessary, reset every turn
    private int[] cache;

    public Communication(RobotController rc) {
        this.rc = rc;
        this.cache = new int[64];
        clearCache();
    }

    public void clearCache() {
        for (int i = 0; i < 64; i++)
            cache[i] = -1;
    }

    // for debug purposes, consumes 128 bytecode
    public void printArray() throws GameActionException {
        int[] array = new int[64];
        for (int i = 0; i < 64; i++) {
            array[i] = rc.readSharedArray(i);
        }
        System.out.println(Arrays.toString(array));
    }

    public MapLocation intToLocation(int value) {
        if (value == 0)
            return null;
        else
            return new MapLocation((value >> 6) & 0x3F, (value & 0x3F) - 1);
    }

    public int locationToInt(MapLocation loc) {
        return (loc.x << 6) + loc.y + 1;
    }
    
    public int readInt(int index) throws GameActionException {
        if (cache[index] != -1)
            return cache[index];
        else
            return rc.readSharedArray(index);
    }

    public void writeInt(int index, int value) throws GameActionException {
        rc.writeSharedArray(index, value);
        cache[index] = value;
    }

    public int[] readRange(int startIndex, int endIndex) throws GameActionException {
        int[] array = new int[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++)
            array[i - startIndex] = readInt(i);
        return array;
    }

    public int[] readWholeArray() throws GameActionException {
        return readRange(0, 64);
    }

    public MapLocation readLocation(int index) throws GameActionException {
        return intToLocation(readInt(index));
    }

    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        writeInt(index, locationToInt(loc));
    }

    public MapLocation[] readLocationArray(int startIndex) throws GameActionException {
        int length = readInt(startIndex);
        MapLocation[] array = new MapLocation[length];
        for (int i = 0; i < length; i++) {
            array[i] = readLocation(startIndex + i + 1);
        }
        return array;
    }

    public void appendLocation(int startIndex, MapLocation loc) throws GameActionException {
        int length = readInt(startIndex);
        writeLocation(startIndex + length + 1, loc);
        writeInt(startIndex, length + 1);
    }

    public void putLocation(int startIndex, MapLocation loc, int index) throws GameActionException {
        writeLocation(startIndex + index + 1, loc);
        if (index >= readInt(startIndex)) {
            writeInt(startIndex, index + 1);
        }
    }
}
