package head_v1.bots;

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
        
        // if (rc.getAnchor() != null) {

        //     int[] islands = rc.senseNearbyIslands();
        //     Set<MapLocation> islandLocs = new HashSet<>();
        //     for (int id : islands) {
        //         MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
        //         islandLocs.addAll(Arrays.asList(thisIslandLocs));
        //     }
        //     if (islandLocs.size() > 0) {
        //         MapLocation islandLocation = islandLocs.iterator().next();
        //         rc.setIndicatorString("Moving my anchor towards " + islandLocation);
        //         while (!rc.getLocation().equals(islandLocation)) {
        //             Direction dir = rc.getLocation().directionTo(islandLocation);
        //             if (rc.canMove(dir)) {
        //                 rc.move(dir);
        //             }
        //         }
        //         if (rc.canPlaceAnchor()) {
        //             rc.setIndicatorString("Huzzah, placed anchor!");
        //             rc.placeAnchor();
        //         }
        //     }
        // }

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
