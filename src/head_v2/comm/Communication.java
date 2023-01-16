package head_v2.comm;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class Communication {

    public final int HQ_FRIENDLY_OFFSET = 0;
    public final int HQ_ENEMY_OFFSET = 4;
    public final int WELL_ADAMANTIUM_OFFSET = 8;
    public final int WELL_MANA_OFFSET = 12;
    public final int ISLAND_OFFSET = 16;
    public int FLEET_OFFSET;

    public RobotController rc;

    public int[] keepAliveCache;
    public int[] queue;
    public boolean queueActive;

    public ArrayList<Integer> appendQueue1;
    public ArrayList<MapLocation> appendQueue2;

    public Communication(RobotController rc) {
        this.rc = rc;

        this.keepAliveCache = new int[64];

        this.queue = new int[64];
        for (int i = 0; i < 64; i++) queue[i] = -1;
        this.queueActive = false;
        this.appendQueue1 = new ArrayList<>();
        this.appendQueue2 = new ArrayList<>();

        FLEET_OFFSET = rc.getIslandCount() + ISLAND_OFFSET;
    }

    public int locationToInt(MapLocation loc, int flags) throws GameActionException {
        return (flags << 12) + (loc.x << 6) + loc.y + 1;
    }

    // for debug purposes
    public String dispArray() throws GameActionException {
        int[] array = new int[64];
        for (int i = 0; i < 64; i++) {
            array[i] = rc.readSharedArray(i);
        }
        return Arrays.toString(array);
    }

    public void queueWrite(int index, int value) throws GameActionException {
        if (rc.canWriteSharedArray(index, value)) {
            int currentValue = rc.readSharedArray(index);
            if ((currentValue & 0x7FFF) != (value & 0x7FFF)) {
                rc.writeSharedArray(index, (currentValue & 0x8000) + (value & 0x7FFF));
            }
        } else {
            queue[index] = value;
            queueActive = true;
        }
    }

    public void queueAppend(int index, MapLocation loc) throws GameActionException {
        if (rc.canWriteSharedArray(index, 0)) {
            appendLocation(index, loc);
        } else {
            appendQueue1.add(index);
            appendQueue2.add(loc);
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
        for (int i = 0; i < appendQueue1.size(); i++) {
            appendLocation(appendQueue1.get(i), appendQueue2.get(i));
        }
        appendQueue1.clear();
        appendQueue2.clear();
        queueActive = false;
    }

    public MapLocation readLocation(int index) throws GameActionException {
        if (!hasLocation(index)) return null;
        int value = rc.readSharedArray(index) - 1;
        return new MapLocation((value >> 6) & 0x3F, value & 0x3F);
    }

    public int readLocationFlags(int index) throws GameActionException {
        return rc.readSharedArray(index) >> 12;
    }

    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        queueWrite(index, locationToInt(loc, 0));
    }

    public void writeLocation(int index, MapLocation loc, int flags) throws GameActionException {
        queueWrite(index, locationToInt(loc, flags));
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
            if (loc != null) {
                array[count] = loc;
                count++;
            }
        }
        return Arrays.copyOf(array, count);
    }

    public void appendLocation(int startIndex, MapLocation loc) throws GameActionException {
        int limit = startIndex + 4;
        if (startIndex == FLEET_OFFSET) limit = 64;
        else if (startIndex == ISLAND_OFFSET) limit = FLEET_OFFSET;

        for (int index = startIndex; index < limit; index++) {
            MapLocation loc2 = readLocation(index);
            if (loc2 == null) {
                writeLocation(index, loc);
                return;
            } else if (loc2.equals(loc)) {
                return;
            }
        }
    }

    public boolean hasLocation(int index) throws GameActionException {
        return (rc.readSharedArray(index) & 0xFFF) != 0;
    }

    public void keepAlive(int index) throws GameActionException {
        int value = rc.readSharedArray(index);
        rc.writeSharedArray(index, value ^ 0x8000);
    }

    public boolean isAlive(int index) throws GameActionException {
        return (rc.readSharedArray(index) >> 15) != (keepAliveCache[index] >> 15);
    }

    public void keepAliveFlush() throws GameActionException {
        
        int i = 63;
        do {
            keepAliveCache[i] = rc.readSharedArray(i);
        } while (--i >= 0);
    }
}
