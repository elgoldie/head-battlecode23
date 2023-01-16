package holden_v2_new.path;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface Pathfinding {

    public Direction findPath(MapLocation target) throws GameActionException;
}