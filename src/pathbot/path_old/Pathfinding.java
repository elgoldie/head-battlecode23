package pathbot.path_old;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface Pathfinding {

    public static final int waypoint_threshold = 2;

    public void initiate_pathfinding(MapLocation target) throws GameActionException;

    public Direction findPath() throws GameActionException;

    public Direction pursue(MapLocation target) throws GameActionException;

    public boolean hasArrived();
    
}
