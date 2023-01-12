package head_v2.path;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public interface Pathfinding {

    public static final int waypoint_threshold = 9;

    public void initiatepathfinding(MapLocation target);

    public Direction findPath();

    public Direction findPath(MapLocation target);
    
    
}
