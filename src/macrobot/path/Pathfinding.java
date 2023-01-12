package macrobot.path;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public interface Pathfinding {

    public Direction findPath(MapLocation target);
}
