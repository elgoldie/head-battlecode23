package holden_v3.bots;

import battlecode.common.*;

public class CarrierAI extends RobotAI {

    private enum CarrierState {
        EXPLORE,
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

    public void behaviorExplore() throws GameActionException {
        wander();
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
                tryMove(pathing.findPath(targetWell));
                if (rc.isMovementReady())
                    tryMove(pathing.findPath(targetWell));
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
        } else {
            tryMove(pathing.findPath(hqLocation));
            if (rc.isMovementReady())
                tryMove(pathing.findPath(hqLocation));
        }
    }

    public void behaviorDeliverAnchor() throws GameActionException {

        MapLocation destination = closestIsland(Team.NEUTRAL);
        if (destination == null) {
            wander();
        } else {
            if (!rc.getLocation().equals(destination)) {
                
                tryMove(pathing.findPath(destination));
                if (rc.isMovementReady())
                    tryMove(pathing.findPath(destination));

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
            case EXPLORE:
                behaviorExplore();
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