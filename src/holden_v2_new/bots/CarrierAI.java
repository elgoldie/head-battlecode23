package holden_v2_new.bots;

import battlecode.common.*;

public class CarrierAI extends RobotAI {

    public MapLocation myWell;

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
    }

    @Override
    public void wander() throws GameActionException {
        while (rc.isMovementReady()) {
            super.wander();
        }
    }

    public int getInventoryWeight() throws GameActionException {
        int weight = 0;
        for (ResourceType type : ResourceType.values()) {
            weight += rc.getResourceAmount(type);
        }
        return weight;
    }

    public MapLocation findNearbyWell() throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length == 0) {
            return null;
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
        return well_one.getMapLocation();
    }

    public int countCarriersNear(MapLocation well) throws GameActionException {
        int total = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(myWell, 5, myTeam)) {
            if (robot.type == RobotType.CARRIER) {
                total += 1;
            }
        }
        return total;
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        
        rc.setIndicatorString("Well: " + myWell);

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
            return;
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

            if (myWell == null) {
                myWell = findNearbyWell();
            }

            if (myWell == null) {
                wander();
            } else if (rc.getLocation().isAdjacentTo(myWell)) {
                if (rc.canCollectResource(myWell, -1))
                    rc.collectResource(myWell, -1);
            } else if (countCarriersNear(myWell) >= 8) {
                myWell = null;
            } else {
                tryMoveOrWander(pathing.findPath(myWell));
            }
        }
    }
}
