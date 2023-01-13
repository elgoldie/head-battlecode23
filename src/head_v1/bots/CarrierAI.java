package head_v1.bots;

import battlecode.common.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class CarrierAI extends RobotAI {

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    public int getInventoryWeight() throws GameActionException {
        int weight = 0;
        for (ResourceType type : ResourceType.values()) {
            weight += rc.getResourceAmount(type);
        }
        return weight;
    }

    public void mine(MapLocation well_location) {
        // Assumes no storm tiles directly adjacent to well; check before calling function
        x_well = well_location.x;
        y_well = well_location.y;
        String[] directions = {"north", "north", "southeast", "north", "east", "south", "south", "west", "south"};
        bot_location = rc.getLocation();
        x_bot = bot_location.x;
        y_bot = bot_location.y;
        x_relative = x_well - x_bot;
        y_relative = y_well - y_bot;

        if (x_relative == -2) {
            if (y_relative == -2) {
                rc.move("northeast");
                path_index = 0;
            }
            else if (y_relative == -1) {
                rc.move("east");
                path_index = 0;
            }
            else if (y_relative == 0) {
                rc.move("east");
                path_index = 1;
            }
            else if (y_relative == 1) {
                rc.move("east");
                path_index = 2;
            }
            else {
                rc.move("southeast");
                path_index = 2;
            }
        }
        else if (x_relative == -1) {
            if (y_relative = 2) {
                rc.move("south");
                path_index = 2;
            }
            else if (y_relative == -1) {
                incoming_traffic = rc.canSenseRobotAtLocation((x_bot - 1), (y_bot+1));
            }
            else if (y_relative == 0) {
                if rc.canSenseRobotAtLocation((x_bot - 1), (y_bot+1)) || rc.canSenseRobotAtLocation((x_bot - 1), (y_bot+2)) || rc.canSenseRobotAtLocation(x_bot, (y_bot+2)) {
                    incoming_traffic = true;
                }
            }
            else if (y_relative == 1) {
                incoming_traffic = rc.canSenseRobotAtLocation((x_bot + 1), (y_bot + 1));
            }
            else {
                rc.move("north");
                path_index = 0;
            }
        }
        else if (x_relative == 0) {
            if y_relative = 2 {
                rc.move("south");
                path_index = 4;
            }
            else if (y_relative == 1) {
                incoming_traffic = rc.canSenseRobotAtLocation((x_bot + 1), (y_bot + 1));
            }
            else if (y_relative == -1) {
                rc.move("south")
                path_index = 8;
            }
            else {
                rc.move("west");
            }
        }
        else if (x_relative == 1) {
            if (y_relative == 2) {
                rc.move("south");
                path_index = 5;
            }
            else if (y_relative == 1) {
                incoming_traffic = rc.canSenseRobotAtLocation((x_bot + 1), (y_bot - 1));
            }
            else if (y_relative == 0) {
                if rc.canSenseRobotAtLocation((x_bot + 1), (y_bot - 1)) || rc.canSenseRobotAtLocation((x_bot - 1), (y_bot - 2)) || rc.canSenseRobotAtLocation(x_bot, (y_bot - 2)) {
                    incoming_traffic = true;
                }
            }
            else {
                rc.move("north");
                path_index = 7;
            }
        }
        else {
            if (y_relative == -2) {
                rc.move("southwest");
                path_index = 7;
            }
            else if (y_relative == -1) {
                rc.move("west");
                path_index = 7;
            }
            else if (y_relative == 0) {
                rc.move("west");
                path_index = 6;
            }
            else if (y_relative == 1) {
                rc.move("west");
                path_index = 5;
            }
            else {
                rc.move("northwest");
                path_index = 5;
            }
        }

        for (int i = path_index; i < 8; i++) {
            if incoming_traffic {
                i--;
                rc.collectResource(well_location, -1);
            }
            else {
                rc.collectResource(well_location, -1);
                rc.move(directions[i]);
            }
        }
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        
        if (rc.getAnchor() != null) {

            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }
        // picks up anchor if possible
        for (Direction dir : directions) {
            MapLocation newLocation = rc.adjacentLocation(dir);
            if (rc.canTakeAnchor(newLocation, Anchor.STANDARD)) {
                rc.takeAnchor(newLocation, Anchor.STANDARD);
                break;
            }
        


        }

        if (getInventoryWeight() == 40) {

            MapLocation hqLocation = closestHeadquarters();
            tryMoveOrWander(rc.getLocation().directionTo(hqLocation));
            
            if (rc.getLocation().isAdjacentTo(hqLocation)) {
                for (ResourceType type : ResourceType.values()) {
                    int amount = rc.getResourceAmount(type);
                    if (amount > 0 && rc.canTransferResource(hqLocation, type, amount)) {
                        rc.transferResource(hqLocation, type, amount);
                        break;
                    }
                }
            }

        } else {
            
            // If we can see a well, move towards it
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length == 0) {
                wander();
                return;
            }

            WellInfo well_one = wells[0];
            Direction dir = rc.getLocation().directionTo(well_one.getMapLocation());
            tryMoveOrWander(dir);
            
            if (rc.canCollectResource(well_one.getMapLocation(), -1))
                rc.collectResource(well_one.getMapLocation(), -1);
        }
    }
}
