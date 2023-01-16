package head_v2.bots;

import battlecode.common.*;
import head_v2.path.*;

import java.util.Random;
//import java.util.Arrays;

public class CarrierAI extends RobotAI {

    public MapLocation[] destinations = new MapLocation[]{new MapLocation(6,6), new MapLocation(13, 13), new MapLocation(6,6), new MapLocation(23, 23), new MapLocation(16, 16), new MapLocation(23, 23)}; //new MapLocation[20];
    public Random rng = new Random();

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        paths = new WaypointPathfinding(rc);
        /*int w = rc.getMapWidth();
        int h = rc.getMapHeight();
        for (int i = 0; i < 5; i++) {
            this.destinations[i] = new MapLocation(rng.nextInt(w), rng.nextInt(h));
        }
        //this.destinations[4] = this.destinations[3].add(Direction.NORTH);
        for (int i = 0; i < 5; i++) {
            this.destinations[5+i] = new MapLocation(rng.nextInt(w), rng.nextInt(h));
            this.destinations[14 - i] = new MapLocation(rng.nextInt(w), rng.nextInt(h));
        }
        for (int i = 0; i < 5; i++) {
            this.destinations[15 + i] = new MapLocation(rng.nextInt(w), rng.nextInt(h));
        }*/
        paths.initiate_pathfinding(this.destinations[this.destination_counter]);
    }

    // @Override
    // public void wander() throws GameActionException {
    //     while (rc.isMovementReady()) {
    //         super.wander();
    //     }
    // }

    public int getInventoryWeight() throws GameActionException {
        int weight = 0;
        for (ResourceType type : ResourceType.values()) {
            weight += rc.getResourceAmount(type);
        }
        return weight;
    }

    public Pathfinding paths;
    public int destination_counter = 0;
    Direction dir;
    @Override
    public void run() throws GameActionException {
        super.run();
        rc.setIndicatorString(this.destinations[this.destination_counter].toString());
        if (paths.hasArrived()) {
            System.out.println("I've arrived!");
            this.destination_counter = (this.destination_counter + 1) % this.destinations.length;
            paths.initiate_pathfinding(this.destinations[this.destination_counter]);
        }
        while (rc.isMovementReady() && !paths.hasArrived()) {
            dir = paths.findPath();
            if (dir == Direction.CENTER) {
                break;
            }
            rc.move(dir);
            break;
        }
        
        /*
        // place anchor if we have one
        if (rc.getAnchor() != null) {

            MapLocation island = closestIsland(Team.NEUTRAL);
            if (island == null) {
                wander();
                return;
            }

            if (!rc.getLocation().equals(island)) {
                Direction dir = pathing.findPath(island);
                tryMove(dir);
            } else {
                if (rc.canPlaceAnchor())
                    rc.placeAnchor();
            }
        }
        
        MapLocation hqLocation = closestHeadquarters();

        if (rc.getLocation().isAdjacentTo(hqLocation)) {

            if (getInventoryWeight() == 0) {

                if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD)) {
                    rc.takeAnchor(hqLocation, Anchor.STANDARD);
                    // System.out.println("I just took an anchor!");
                } else {
                    wander();
                }

            } else {
                for (ResourceType type : ResourceType.values()) {
                    int amount = rc.getResourceAmount(type);
                    if (amount > 0 && rc.canTransferResource(hqLocation, type, amount)) {
                        rc.transferResource(hqLocation, type, amount);
                        break;
                    }
                }
            }

        } else if (getInventoryWeight() == 40) {

            tryMoveOrWander(pathing.findPath(hqLocation));

        } else {
            
            // If we can see a well, move towards it
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length == 0) {
                wander();
                return;
            }

            WellInfo well_one = null;
            int bestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < wells.length; i++) {
                int distance = rc.getLocation().distanceSquaredTo(wells[i].getMapLocation());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    well_one = wells[i];
                }
            }
            
            if (rc.canCollectResource(well_one.getMapLocation(), -1)) {
                rc.collectResource(well_one.getMapLocation(), -1);
            } else {
                Direction dir = pathing.findPath(well_one.getMapLocation());
                tryMoveOrWander(dir);
            }
            
        }  */
    }
}
