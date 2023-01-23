/* package pathbot.bots;

import battlecode.common.*;
import pathbot.path.*;

import java.util.Random;
//import java.util.Arrays;

public class CarrierAI extends RobotAI {

    public MapLocation[] destinations = new MapLocation[]{new MapLocation(6,6), new MapLocation(13, 13), new MapLocation(6,6), new MapLocation(23, 23), new MapLocation(16, 16), new MapLocation(23, 23)}; //new MapLocation[20];
    public Random rng = new Random();

    public CarrierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        System.out.println("Proper carrier AI in use");
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
        }*/ /*
        paths.initiate_pathfinding(this.destinations[this.destination_counter]);
    }

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
    }
}
 */