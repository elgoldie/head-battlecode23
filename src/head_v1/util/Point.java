package head_v1.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Point {

    public int x;
    public int y;

    public static final Point CENTER = new Point(0, 0);
    public static final Point NORTH = new Point(0, 1);
    public static final Point NORTHEAST = new Point(1, 1);
    public static final Point EAST = new Point(1, 0);
    public static final Point SOUTHEAST = new Point(1, -1);
    public static final Point SOUTH = new Point(0, -1);
    public static final Point SOUTHWEST = new Point(-1, -1);
    public static final Point WEST = new Point(-1, 0);
    public static final Point NORTHWEST = new Point(-1, 1);

    public static final Point[] DIRECTIONS = {NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST};

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(MapLocation loc) {
        this.x = loc.x;
        this.y = loc.y;
    }

    public Point(Direction dir) {
        this.x = dir.dx;
        this.y = dir.dy;
    }

    public Point add(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public Point subtract(Point other) {
        return new Point(x - other.x, y - other.y);
    }

    public Point opposite() {
        return new Point(-x, -y);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public Point directionTo(Point other) {
        return new Point(FreeMath.signum(other.x - x), FreeMath.signum(other.y - y));
    }

    public Point directionTo(MapLocation other) {
        return new Point(FreeMath.signum(other.x - x), FreeMath.signum(other.y - y));
    }

    public Point[] neighbors() {
        Point[] result = new Point[8];
        for (int i = 0; i < 8; i++) result[i] = add(DIRECTIONS[i]);
        return result;
    }

    public MapLocation toLocation() {
        return new MapLocation(x, y);
    }

    public Direction toDirection() {
        switch (x) {
            case -1:
                switch (y) {
                    case -1: return Direction.SOUTHWEST;
                    case 0: return Direction.WEST;
                    case 1: return Direction.NORTHWEST;
                }
            case 0:
                switch (y) {
                    case -1: return Direction.SOUTH;
                    case 0: return Direction.CENTER;
                    case 1: return Direction.NORTH;
                }
            case 1:
                switch (y) {
                    case -1: return Direction.SOUTHEAST;
                    case 0: return Direction.EAST;
                    case 1: return Direction.NORTHEAST;
                }
            default:
                // this should never happen
                return Direction.CENTER;
        }
    }
}
