package head_v3.path;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface Pathfinding {

    public static final int waypoint_threshold = 2;

    public void initPathfinding(MapLocation target) throws GameActionException;

    public Direction findPath() throws GameActionException;

    public boolean hasArrived();
    
}
