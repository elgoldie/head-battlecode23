package head_v1.path;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import head_v1.util.Point;

public class Pathfinding {

    public RobotController rc;
    public int width;
    public int height;

    public boolean[][] discovered;
    public boolean[][] walls;
    public boolean[][] clouds;
    public Point[][] currents;
    
    public Pathfinding(RobotController rc, int width, int height) {
        this.rc = rc;
        this.width = width;
        this.height = height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                discovered[x][y] = false;
                walls[x][y] = false;
                clouds[x][y] = false;
                currents[x][y] = Point.CENTER;
            }
        }
    }

    public void scan() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 64)) {
            if (!discovered[loc.x][loc.y]) {
                discovered[loc.x][loc.y] = true;
                walls[loc.x][loc.y] = rc.sensePassability(loc);
            }
        }

        for (MapInfo info : rc.senseNearbyMapInfos()) {
            MapLocation loc = info.getMapLocation();
            clouds[loc.x][loc.y] = false;
            currents[loc.x][loc.y] = new Point(info.getCurrentDirection());
        }
    }
}
