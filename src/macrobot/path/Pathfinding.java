package macrobot.path;

import java.util.LinkedList;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import macrobot.util.FreeMath;
import macrobot.util.Point;

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
            if (discovered[loc.x][loc.y]) {
                clouds[loc.x][loc.y] = false;
                currents[loc.x][loc.y] = new Point(info.getCurrentDirection());
            }
        }
    }

    public Point dijkstraBestMove(Point target) {
        Point start = new Point(rc.getLocation());

        if (target.equals(start)) {
            return start;
        }

        LinkedList<Point> queue = new LinkedList<Point>();
        queue.add(start);

        int[][] dist = new int[width][height];
        Point[][] prev = new Point[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                dist[x][y] = Integer.MAX_VALUE;
                prev[x][y] = null;
            }
        }

        while (!queue.isEmpty()) {
            Point pt = queue.removeFirst();
            if (pt.equals(target)) {
                Point last;
                do {
                    last = pt;
                    pt = prev[pt.x][pt.y];
                } while (!pt.equals(start));
                return last;
            }
            int distanceFrom = 10;
            for (Point dir : Point.DIRECTIONS) {
                Point next = pt.add(dir);
                next.add(currents[next.x][next.y]);

                if (next.x >= 0 && next.x < width && next.y >= 0 && next.y < height && !walls[next.x][next.y]) {
                    if (currents[next.x][next.y] == Point.CENTER) {
                        if (dist[next.x][next.y] > dist[pt.x][pt.y] + distanceFrom) {
                            dist[next.x][next.y] = dist[pt.x][pt.y] + distanceFrom;
                            prev[next.x][next.y] = pt;
                            queue.add(next);
                        }
                    }
                }
            }
        }

        return start;
    }
}
