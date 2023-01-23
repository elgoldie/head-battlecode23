package head_latest.comm;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.*;

public class Communication {

    public RobotController rc;

    /*
     * CURRENT SHARED ARRAY ALLOCATION
     * 0-3: Friendly HQ locations
     * 4-33: Island locations, dynamic depending on island count
     * 61-63: Symmetry detection
     */

    public final int HQ_OFFSET = 0;
    public final int ISLAND_OFFSET = 4;
    public int WELL_OFFSET;

    /**
     * The queue is used to store values that cannot be written to the shared array
     * immediately. The queue is flushed when the robot comes within range again.
     */
    public int[] queue;
    public boolean queueActive;

    public ArrayList<Integer> wellsToAdd;

    /**
     * A class to handle communication via the shared array.
     * @param rc The RobotController
     */
    public Communication(RobotController rc) {
        this.rc = rc;
        this.WELL_OFFSET = this.ISLAND_OFFSET + rc.getIslandCount();

        // initialize queue
        this.queue = new int[64];
        for (int i = 0; i < 64; i++) queue[i] = -1;
        this.queueActive = false;
        this.wellsToAdd = new ArrayList<>();
    }

    /**
     * Displays the contents of the shared array to the indicator string.
     * @throws GameActionException
     */
    public String dispArray() throws GameActionException {
        int[] array = new int[64];
        for (int i = 0; i < 64; i++) {
            array[i] = rc.readSharedArray(i);
        }
        return Arrays.toString(array);
    }

    /**
     * Flushes the queue to the shared array and clears it.
     * @throws GameActionException
     */
    public void queueFlush() throws GameActionException {
        queueActive = false;
        for (int i = 0; i < 64; i++) {
            if (queue[i] != -1) {
                rc.writeSharedArray(i, queue[i]);
                queue[i] = -1;
            }
        }
        int index = WELL_OFFSET;
        for (int value : wellsToAdd) {
            append(index, value);
        }
        wellsToAdd.clear();
    }

    public MapLocation decodeLocation(int value) {
        if ((value & 0xFFF) == 0) return null;
        value--;
        return new MapLocation((value >> 6) & 0x3F, value & 0x3F);
    }

    /**
     * Reads from the queue if it is active, otherwise reads from the shared array.
     * @param index The index to read from
     * @return The value at the index
     * @throws GameActionException
     */
    public int read(int index) throws GameActionException {
        if (queueActive && queue[index] != -1)
            return queue[index];
        else
            return rc.readSharedArray(index);
    }

    /**
     * Writes to the queue if out of signal range, otherwise writes to the shared array.
     * @param index The index to write to
     * @param value The value to write
     * @throws GameActionException
     */
    public void write(int index, int value) throws GameActionException {
        if (rc.canWriteSharedArray(index, value)) {
            rc.writeSharedArray(index, value);
        } else {
            queue[index] = value;
            queueActive = true;
        }
    }

    public int append(int index, int value) throws GameActionException {
        // TODO: free up 61 through 63
        for (int i = index; i < 61; i++) {
            int value2 = read(i);
            if (value2 == 0) {
                write(i, value);
                return index;
            } else if ((value2 & 0xFFF) == (value & 0xFFF)) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Reads a MapLocation from the shared array.
     * The MapLocation is stored as a 12-bit value, with the first 6 bits being the x-coordinate
     * and the last 6 bits being the y-coordinate. The value is offset by 1 to allow for null values.
     * @param index The index to read from
     * @return The MapLocation at the index
     * @throws GameActionException
     */
    public MapLocation readLocation(int index) throws GameActionException {
        int value = read(index) - 1;
        if (value == -1) return null;
        return new MapLocation((value >> 6) & 0x3F, value & 0x3F);
    }

    /**
     * Reads the flags from a MapLocation stored in the shared array.
     * The flags are stored in the upper 4 bits.
     * @param index The index to read from
     * @return The flags at the index
     * @throws GameActionException
     */
    public int readFlags(int index) throws GameActionException {
        return read(index) >> 12;
    }

    /**
     * Writes a MapLocation to the shared array without flags.
     * @param index The index to write to
     * @param loc The MapLocation to write
     * @throws GameActionException
     */
    public void writeLocation(int index, MapLocation loc) throws GameActionException {
        write(index, (loc.x << 6) + loc.y + 1);
    }

    /**
     * Writes a MapLocation to the shared array with flags.
     * @param index The index to write to
     * @param loc The MapLocation to write
     * @param flags The flags to write
     * @throws GameActionException
     */
    public void writeLocation(int index, MapLocation loc, int flags) throws GameActionException {
        write(index, (flags << 12) + (loc.x << 6) + loc.y + 1);
    }

    /**
     * Writes flags to a MapLocation stored in the shared array.
     * @param index The index to write to
     * @param flags The flags to write
     * @throws GameActionException
     */
    public void writeFlags(int index, int flags) throws GameActionException {
        int value = read(index);
        write(index, (flags << 12) + (value & 0xFFF));
    }

    /**
     * Reads a MapLocation array from the shared array, ignoring null values.
     * @param startIndex The index to start reading from
     * @param endIndex The index to stop reading at
     * @return The MapLocation array
     * @throws GameActionException
     */
    public MapLocation[] readLocationsUntilNull(int startIndex, int endIndex) throws GameActionException {
        MapLocation[] array = new MapLocation[endIndex - startIndex];
        int count = 0;
        for (int i = 0; i < endIndex - startIndex; i++) {
            MapLocation loc = readLocation(startIndex + i);
            if (loc == null) break;
            array[count] = loc;
            count++;
        }
        return Arrays.copyOf(array, count);
    }

    /**
     * Appends a location to the shared array, if it is not already in the array.
     * Uses null values to indicate empty slots.
     * @param startIndex The index to start writing to
     * @param loc The MapLocation to write
     * @throws GameActionException
     */
    public int appendLocation(int index, MapLocation loc) throws GameActionException {
        return append(index, (loc.x << 6) + loc.y + 1);
    }

    public int appendLocation(int index, MapLocation loc, int flags) throws GameActionException {
        return append(index, (flags << 12) + (loc.x << 6) + loc.y + 1);
    }

    /**
     * Appends a location to the shared array, if it is not already in the array.
     * Uses null values to indicate empty slots.
     * @param startIndex The index to start writing to
     * @param loc The MapLocation to write
     * @param flags The flags to write
     * @throws GameActionException
     */
    public boolean hasLocation(int index) throws GameActionException {
        return (read(index) & 0xFFF) != 0;
    }

    /**
     * Checks if a headquarters is distressed.
     * @param index The index to read from
     * @return True if the headquarters is distressed, false otherwise
     * @throws GameActionException
     */
    public boolean isDistressed(int index) throws GameActionException {
        return (read(index) & 0x1000) != 0;
    }

    public void appendWell(WellInfo well) throws GameActionException {
        appendWell(well.getMapLocation(), well.getResourceType(), well.isUpgraded());
    }

    public void appendWell(MapLocation loc, ResourceType type, boolean upgraded) throws GameActionException {
        int flag = type.ordinal() + (upgraded ? 4 : 0);
        int value = (flag << 12) + (loc.x << 6) + loc.y + 1;
        if (rc.canWriteSharedArray(0, 0)) {
            append(WELL_OFFSET, value);
        } else {
            int storedWell;
            for (int i = 0; i < wellsToAdd.size(); i++) {
                storedWell = wellsToAdd.get(i);
                if ((storedWell & 0xFFF) == (value & 0xFFF)) {
                    if ((storedWell >> 12) == flag) return;
                    else {
                        wellsToAdd.set(i, value);
                        return;
                    }
                }
            }
            for (int index = WELL_OFFSET; index < WELL_OFFSET + 10; index++) {
                storedWell = read(index);
                if ((storedWell & 0xFFF) == (value & 0xFFF)) {
                    if ((storedWell >> 12) == flag) return;
                    else {
                        write(index, value);
                        return;
                    }
                }
            }
            wellsToAdd.add(value);
        }
    }

    public int[] readWells() throws GameActionException {
        int[] wells = new int[64 - WELL_OFFSET + wellsToAdd.size()];
        int count = 0;
        int value;
        for (int i = WELL_OFFSET; i < 64; i++) {
            value = read(i);
            if ((value & 0xFFF) == 0) break;
            wells[count++] = read(i);
        }
        for (int well : wellsToAdd) {
            wells[count++] = well;
        }
        return Arrays.copyOf(wells, count);
    }
}
