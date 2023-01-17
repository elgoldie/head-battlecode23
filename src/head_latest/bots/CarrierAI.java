package head_latest.bots;

import battlecode.common.*;

public class CarrierAI extends RobotAI {

    private enum CarrierState {
        SCOUT,
        GO_TO_WELL,
        RETURN_HOME,
        DELIVER_ANCHOR
    }
    
    public CarrierState state;

    public MapLocation targetWell;
    public ResourceType targetResource;

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        state = CarrierState.GO_TO_WELL;
        if (rng.nextBoolean())
            targetResource = ResourceType.ADAMANTIUM;
        else
            targetResource = ResourceType.MANA;
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

    public MapLocation closestWell(ResourceType type) throws GameActionException {
        WellInfo[] wells;
        if (type == null) wells = rc.senseNearbyWells();
        else wells = rc.senseNearbyWells(type);

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

    public MapLocation closestWell() throws GameActionException {
        return closestWell(null);
    }

    public void behaviorScout() throws GameActionException {
        
        MapLocation hqToTarget = null;
        int bestDistance = Integer.MAX_VALUE;
        for (MapLocation hqLoc : getPossibleEnemyHQLocations()) {
            int distance = rc.getLocation().distanceSquaredTo(hqLoc);
            if (distance < bestDistance) {
                bestDistance = distance;
                hqToTarget = hqLoc;
            }
        }

        if (hqToTarget == null) {
            // this should never happen
            state = CarrierState.RETURN_HOME;

        } else {
            if (rc.canSenseLocation(hqToTarget)) {

                state = CarrierState.RETURN_HOME;

            } else {

                stepTowardsDestination(hqToTarget);
                if (rc.isMovementReady())
                    stepTowardsDestination(hqToTarget);
            }
        }
    }

    public void behaviorGoToWell() throws GameActionException {

        if (targetWell == null)
            targetWell = closestWell(targetResource);

        if (getInventoryWeight() == 40) {
            state = CarrierState.RETURN_HOME;
            return;
        }
        
        if (targetWell != null) {
            
            if (rc.canCollectResource(targetWell, -1)) {
                rc.collectResource(targetWell, -1);
            } else {
                stepTowardsDestination(targetWell);
                if (rc.isMovementReady())
                    stepTowardsDestination(targetWell);
            }
        } else {
            wander();
        }
    }

    public void behaviorReturnHome() throws GameActionException {

        MapLocation hqLocation = closestHeadquarters();

        if (rc.getLocation().isAdjacentTo(hqLocation)) {

            if (getInventoryWeight() == 0) {
                
                if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD)) {
                    rc.takeAnchor(hqLocation, Anchor.STANDARD);
                    state = CarrierState.DELIVER_ANCHOR;
                } else {
                    state = CarrierState.GO_TO_WELL;
                    wander();
                }

            } else {

                int amount = rc.getResourceAmount(targetResource);
                rc.transferResource(hqLocation, targetResource, amount);
            }

            int validSymmetryCount = getValidSymmetries().length;
            if ((validSymmetryCount == 3 && rng.nextInt(5) == 0)
                || (validSymmetryCount == 2 && rng.nextInt(10) == 0)) {
                state = CarrierState.SCOUT;
                System.out.println(rc.getLocation());
            }

        } else {
            stepTowardsDestination(hqLocation);
            if (rc.isMovementReady())
                stepTowardsDestination(hqLocation);
        }
    }

    public void behaviorDeliverAnchor() throws GameActionException {

        MapLocation destination = closestIsland(Team.NEUTRAL);
        if (destination == null) {
            wander();
        } else {

            if (rc.getLocation().isAdjacentTo(destination)) {
                if (tryMove(rc.getLocation().directionTo(destination))) {
                    if (rc.canPlaceAnchor()) {
                        rc.placeAnchor();
                        state = CarrierState.RETURN_HOME;
                    }
                }
            }

            if (!rc.getLocation().equals(destination)) {
                
                stepTowardsDestination(destination);
                if (rc.isMovementReady())
                    stepTowardsDestination(destination);

            } else if (rc.canPlaceAnchor()) {
                rc.placeAnchor();
                state = CarrierState.RETURN_HOME;
            }
        }
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        rc.setIndicatorString(state.toString());

        switch (state) {
            case SCOUT:
                behaviorScout();
                break;
            case GO_TO_WELL:
                behaviorGoToWell();
                break;
            case RETURN_HOME:
                behaviorReturnHome();
                break;
            case DELIVER_ANCHOR:
                behaviorDeliverAnchor();
                break;
        }
    }
}