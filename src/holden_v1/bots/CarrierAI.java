package holden_v1.bots;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

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

    @Override
    public void run() throws GameActionException {
        super.run();
        
        // place anchor if we have one
        if (rc.getAnchor() != null) {

            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    tryMove(dir);
                }
                if (rc.canPlaceAnchor())
                    rc.placeAnchor();
            }
        }
        
        MapLocation hqLocation = closestHeadquarters();

        if (rc.getLocation().isAdjacentTo(hqLocation)) {

            if (getInventoryWeight() == 0) {

                if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD) && rng.nextBoolean()) {
                    rc.takeAnchor(hqLocation, Anchor.STANDARD);
                    System.out.println("I just took an anchor!");
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

            return;
        }

        if (getInventoryWeight() == 40) {

            tryMoveOrWander(rc.getLocation().directionTo(hqLocation));
            
            if (rc.getLocation().isAdjacentTo(hqLocation)) {

            }

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
            Direction dir = rc.getLocation().directionTo(well_one.getMapLocation());
            tryMoveOrWander(dir);
            
            if (rc.canCollectResource(well_one.getMapLocation(), -1))
                rc.collectResource(well_one.getMapLocation(), -1);
        }
    }
}
