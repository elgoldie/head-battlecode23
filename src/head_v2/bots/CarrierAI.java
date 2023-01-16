package head_v2.bots;

import battlecode.common.*;
import head_v2.path.*;

import java.util.Random;
//import java.util.Arrays;

public class CarrierAI extends RobotAI {

    public MapLocation[] destinations = new MapLocation[20];
    public Random rng = new Random();

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        paths = new WaypointPathfinding(rc);
        int w = rc.getMapWidth();
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
        }
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
        
        MapLocation hqLocation = closestHeadquarters(myTeam);

        if (rc.getLocation().isAdjacentTo(hqLocation)) {

            if (getInventoryWeight() == 0) {

                if (rc.canTakeAnchor(hqLocation, Anchor.STANDARD)) {
                    rc.takeAnchor(hqLocation, Anchor.STANDARD);
                    // System.out.println("I just took an anchor!");
                } else {
                    chooseRandomWell();
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
        }

        if (getInventoryWeight() == 40) {

            tryMove(pathing.findPath(hqLocation));
        } else {
            if (wellTarget == null) chooseRandomWell();
            if (wellTarget != null) {
                
                if (rc.canCollectResource(wellTarget, -1)) {
                    rc.collectResource(wellTarget, -1);
                } else {
                    Direction dir = pathing.findPath(wellTarget);
                    tryMoveOrWander(dir);
                }

            } else {
                wander();
            }
            
        }  */
    }
}
