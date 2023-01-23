package head_latest.bots;

import java.util.HashSet;

import battlecode.common.*;

public class CarrierAI extends RobotAI {

    private enum CarrierState {
        SCOUT, // scouting for enemy HQs
        GO_TO_WELL, // on its way to collect resources
        RETURN_HOME, // on its way back to HQ
        DELIVER_ANCHOR // on its way toan island
    }

    public final int SUICIDE_THRESHOLD = 30;
    
    public CarrierState state;

    // the well we are going to
    public MapLocation savedTargetWell;
    public ResourceType targetResource;

    public HashSet<MapLocation> fullWells;

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        state = CarrierState.GO_TO_WELL;
        fullWells = new HashSet<>();
        // TODO: maybe tweak the odds
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

    /**
     * Returns the total weight of the inventory.
     * @return Weight of the inventory in kg.
     * @throws GameActionException
     */
    public int getInventoryWeight() throws GameActionException {
        int weight = 0;
        for (ResourceType type : ResourceType.values()) {
            weight += rc.getResourceAmount(type);
        }
        return weight;
    }

    public int numberOfCarriersNearby(MapLocation loc) throws GameActionException {
        int count = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(loc, 5, myTeam)) {
            if (robot.getType() == RobotType.CARRIER)
                count++;
        }
        return count;
    }

    /**
     * Returns the closest well to the robot (that isn't full).
     * @param type The resource type to look for. If null, looks for any well.
     * @return The closest well to the robot.
     * @throws GameActionException
     */
    public MapLocation closestWell(ResourceType type) throws GameActionException {
        int[] wells = comm.readWells();

        MapLocation well;

        int bestDistance = Integer.MAX_VALUE;
        MapLocation bestWell = null;

        for (int value : wells) {
            well = comm.decodeLocation(value);
            if (fullWells.contains(well))
                continue;
            else if (rc.getLocation().distanceSquaredTo(well) <= 13 && rc.canSenseLocation(well) && numberOfCarriersNearby(well) > 5) {
                fullWells.add(well);
                continue;
            }

            if (type == null || type == ResourceType.values()[(value >> 12) & 3]) {
                int distance = rc.getLocation().distanceSquaredTo(well);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestWell = well;
                }
            }
        }

        return bestWell;
    }



    /**
     * Returns the closest well to the robot.
     * @return The closest well to the robot.
     * @throws GameActionException
     */
    public MapLocation closestWell() throws GameActionException {
        return closestWell(null);
    }

    /**
     * Returns if robot is on enemy island
     * @return Boolean of if on enemy island
     * @throws GameActionException
     */
    public boolean onEnemyIsland() throws GameActionException {
        int islandIndex = rc.senseIsland(rc.getLocation());
        boolean onEnemyIsland = false;
        if (islandIndex != -1) {
            if (rc.senseTeamOccupyingIsland(islandIndex) == rc.getTeam().opponent()) {
                onEnemyIsland = true;
            }
        } 
        return onEnemyIsland;
    }

    /**
     * Behavior for the carrier when it is verifying symmetry.
     * @throws GameActionException
     */
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

    /**
     * Behavior for the carrier when it is going to a well to mine (or finding a well).
     * @throws GameActionException
     */
    public void behaviorGoToWell() throws GameActionException {

        MapLocation targetWell;
        if (savedTargetWell != null) {
            targetWell = savedTargetWell;
        } else {
            targetWell = closestWell(targetResource);
        }
        
        rc.setIndicatorString(state + " | " + savedTargetWell + " | " + fullWells);

        if (getInventoryWeight() == 40) {
            state = CarrierState.RETURN_HOME;
            return;
        }
        
        if (targetWell != null) {
            boolean hasMined = false;
            while (rc.canCollectResource(targetWell, -1)) {
                hasMined = true;
                rc.collectResource(targetWell, -1);
            }

            if (hasMined) {
                savedTargetWell = targetWell;
            } else {
                stepTowardsDestination(targetWell);
                if (rc.isMovementReady())
                    stepTowardsDestination(targetWell);
            }
        } else {
            wander();
        }
    }

    /**
     * Behavior for the carrier when it is returning to HQ.
     * @throws GameActionException
     */
    public void behaviorReturnHome() throws GameActionException {

        MapLocation hqLocation = closestHeadquarters();

        if (rc.getLocation().isAdjacentTo(hqLocation)) {

            if (getInventoryWeight() == 0) {
                if (rc.canTakeAnchor(hqLocation, Anchor.ACCELERATING)) {
                    rc.takeAnchor(hqLocation, Anchor.ACCELERATING);
                } else if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD)) {
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
            }

        } else {
            stepTowardsDestination(hqLocation);
            if (rc.isMovementReady())
                stepTowardsDestination(hqLocation);
        }
    }

    /**
     * Behavior for the carrier when it is delivering an anchor to an island.
     * @throws GameActionException
     */
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

        if (rc.getHealth() <= SUICIDE_THRESHOLD) {
            int radius = rc.getType().actionRadiusSquared;

            int maxValue = Integer.MIN_VALUE;
            RobotInfo target = null;
            for (RobotInfo robot : rc.senseNearbyRobots(radius, enemyTeam)) {
                int value = enemyValue(robot);
                if (value > maxValue) {
                    maxValue = value;
                    target = robot;
                }
            }
            if (target != null) {
                if (rc.canAttack(target.location)) {      
                    rc.attack(target.location);
                }
            }
        }

        if (onEnemyIsland()) {

        } else {
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
}