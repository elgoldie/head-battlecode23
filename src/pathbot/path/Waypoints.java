/* package pathbot.path;

import battlecode.common.*;

import java.util.ArrayList;
import javafx.util.Pair;
import pathbot.path.segment_types.Segment;

public class Waypoints {

    // SEGMENT MEMORY
    
    // FW/BW optimal segments
    @SuppressWarnings("unchecked")
    public ArrayList<Segment>[] mem_segments = (ArrayList<Segment>[]) new ArrayList[2];
    // BACKUP for comparing versions
    @SuppressWarnings("unchecked")
    public ArrayList<Segment>[] backup_segments = (ArrayList<Segment>[]) new ArrayList[2];

    // WORKING ONE FOR ACTUALLY FACILITATING TRAVEL
    public ArrayList<Segment> working_path = new ArrayList<Segment>();

    public int segment_pointer; 
    public int direction;

    // OH NOES
    public ArrayList<MapLocation> ohNoes = new ArrayList<MapLocation>();

    // UTIL
    public RobotController rc;

    // CONSTRUCTOR
    /**
     * Creates a new `Waypoints` object, which contains a collection of lists of `segments`
     * linking locations `left` and `right`. The more this path is followed, the more optimal
     * the pathfinding becomes. In general, things are optimized around finding paths between 
     * these endpoints, rather than subsections of the path connecting them.
     * 
     * @param rc RobotController of robot
     * @param left Point to connect. Interchangeable with `right`.
     * @param right Point to connect. Interchangeable with `left`. 
     */ /*
    public Waypoints(RobotController rc, MapLocation left, MapLocation right) {
        Segment og = new Segment(rc, left, right);
        this.mem_segments[0].add(og);
        this.mem_segments[1].add(og);
    }

    /**
     * Given a starting and ending location, determines whether it knows the way between those two points. 
     * @param start starting location
     * @param end destination
     * @return `int[]` where the first index is the index of the first starting segment and the second 
     * is the index of the ending segment if known. Note these may be the same value. If unknown, returns 
     * `[-1, -1]`.
     */ /*
    public int[] contains_waypoints(MapLocation start, MapLocation end) {
        int[] indices = new int[]{-1, -1};

        for (ArrayList<Segment> arr : this.mem_segments) {
            for (int i = 0; i < arr.size(); i++) {

            }
        }

        return indices;
    }

    /**
     * Given indices of segments, initiates pathfinding between the two by filling out the 
     * `working_path` ArrayList.
     * @param segment_indices as returned by contains_waypoints
     * @return false if destination has been shown to be unreachable, true otherwise
     */ /*
    public boolean initiate_pathfinding(int[] segment_indices) {
        return false;
    }

    /**
     * Given indices of segments, initiates pathfinding between the two by filling out the 
     * `working_path` ArrayList.
     * @param segment_indices as returned by contains_waypoints
     * @param c specific arrival ocndition for the destination
     * @return false if destination has been shown to be unreachable, true otherwise
     */ /* 
    public boolean initiate_pathfinding(int[] segment_indices, ArrivalCondition c) {
        return false;
    }
    
    /**
     * Assuming pathfinding between two waypoints has been initialized, returns the next step
     * if applicable. Also handles all of the learning logic.
     * @param myloc Current location
     * @param scan_info Scan info of nearby map--used to check for currents. 
     * @return direction of next step to take
     * @throws GameActionException
     */ /*
    public Direction stepnext(MapLocation myloc, MapInfo[] scan_info) throws GameActionException {
        Segment seg = this.working_path.get(this.segment_pointer);
        Pair<Direction, Segment[]> ret = seg.follow(myloc, this.direction, scan_info);
        Segment[] newsegs = ret.getValue();
        if (newsegs[0] != null) {
            // logic uwu
        }
        if ()

        return ret.getKey();
    }

}
 */