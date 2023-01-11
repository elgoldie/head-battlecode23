package macrobot.path;

import battlecode.common.*;
import java.util.ArrayList;

public class Path {
    
    public ArrayList<MapLocation> waypoints = new ArrayList<MapLocation>();
    private int waypoint_index;
    private boolean direction;

    public Path(MapLocation origin, MapLocation destination) {
        this.waypoints.add(origin);
        this.waypoints.add(destination);
    }

    public MapLocation origin() {
        return this.waypoints.get(0);
    }

    public MapLocation destination() {
        return this.waypoints.get(this.waypoints.size() - 1);
    }

    public void initiate_travel(MapLocation location, MapLocation destination) {
        this.direction = location.distanceSquaredTo(this.origin()) < location.distanceSquaredTo(destination);
        this.waypoint_index = 0;
    }

    public void advance() {
        if (this.direction) {
            this.waypoint_index += 1;
        } else {
            this.waypoint_index -= 1;
        }
    }

    public int myindex() {
        if (this.direction) {
            return this.waypoint_index + 1;
        }
        return this.waypoints.size() - 2 - this.waypoint_index;
    }

    public MapLocation get_destination() {
        try {
            return this.waypoints.get(this.myindex());
        }
        catch(Exception e) {
            return null;
        }
    }

    public void add_point(MapLocation waypoint) {
        int index;
        if (this.direction) {
            index = this.waypoint_index + 1;
        } else {
            index = this.waypoints.size() - 1 - this.waypoint_index;
        }
        this.waypoints.add(index, waypoint);
    }

}
