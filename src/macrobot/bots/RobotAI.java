package macrobot.bots;

import java.util.Random;
import java.util.ArrayList;

import battlecode.common.*;
import macrobot.util.Communication;
import macrobot.path.Path;

public abstract class RobotAI {

    public Random rng;
    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public Team myTeam;
    public Team enemyTeam;

    public RobotController rc;
    public Communication comm;
    public int id;

    public int gameTurn;
    public int aliveTurns;

    public MapLocation spawnLocation;
    public MapLocation[] hqLocations;

    public String assignment = "idle";
    public String command = "assign";
    public int command_arg = -1;
    public ArrayList<Path> paths = new ArrayList<Path>();

    public int tasklow = 5;
    public int taskhigh = 6;

    public int[] arrayvision;

    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        this.rng = new Random(id);
        this.comm = new Communication(rc);

        this.gameTurn = rc.getRoundNum() - 1;
        this.aliveTurns = 0;
        this.spawnLocation = rc.getLocation();

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
        this.hqLocations = comm.readLocationArray(0);

        
    }
    
    public void run() throws GameActionException {
        //System.out.println("SKADOOSH");
        rc.setIndicatorString("Beginning action");
        comm.clearCache();
        gameTurn += 1;
        aliveTurns += 1;
        //if (this.gameTurn == 2) {
         //   mining_task();
        //}
        // read all
    }

    public void wander() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    /*public void mining_task() throws GameActionException {
        this.arrayvision = this.comm.readWholeArray();
        System.out.println("Beginning attempt to mine");
        int op = 61440; //1111000000000000
        int mining_op = 4096; // 0001000000000000 
        for (int i = this.tasklow; i <= this.taskhigh; i += 2) {
            System.out.println(Integer.toBinaryString(this.arrayvision[i+1]));
            System.out.println(Integer.toBinaryString(op));
            System.out.println(Integer.toBinaryString(mining_op));
            
            if ((this.arrayvision[i+1] & op) == mining_op) {
                this.assignment = "mining";
                System.out.println("Mining task found");
            }
        }
    }*/

    public int[] well_to_request(WellInfo w) throws GameActionException {
        return this.well_to_request(w, 9);
    }

    public int[] well_to_request(WellInfo w, int num) throws GameActionException {
        MapLocation loc = w.getMapLocation();
        int location = 32768 + loc.x * 64 + loc.y;;
        int request = num + 4096;
        System.out.println("Processing well");
        switch (w.getResourceType()){
            case ADAMANTIUM:
                request += 2048;
                break;
            case MANA:
                request += 1024;
                break;
            case ELIXIR:
                request += 512;
                break;
            case NO_RESOURCE:
                return new int[]{-1, -1};
        }
        
        // for now, assume all other flags are zero. 
        // for now, assume enemy is nowhere to be seen, HQ is 0.
        
        return new int[]{location, request};
    }

    boolean RH_mode = false;
    boolean memory_mode = true;
    int myhand;

    Direction[] circle = new Direction[]{Direction.WEST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST};

    public boolean step_RH(MapLocation waypoint) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }
        try {
        MapLocation myloc = rc.getLocation();
        Direction objective = myloc.directionTo((waypoint));
        if (rc.canMove(objective)){
            rc.move(objective);
            if (RH_mode) {
                //store newloc in memory
                this.RH_mode = false;
                this.memory_mode = true;
            }
        } else {
            //RobotInfo[] scan = rc.senseNearbyRobots(2);
            //Direction[] nearby = new Direction[scan.length];
            //for (int i = 0; i < scan.length; i++) {
            //    nearby[i] = myloc.directionTo(scan[i].getLocation());
           // }

            if (!RH_mode) {
                rc.setIndicatorString("Oh noes");
                this.RH_mode = true;
                this.myhand = 0;
                //System.out.println(objective);
                //System.out.println(scan);
                while (circle[this.myhand] != objective && this.myhand < 8) {
                    this.myhand += 1;
                }
                if (this.myhand == 8) {
                    return false;
                } 
                if (rc.canMove(circle[(this.myhand + 1) % 8])) {
                    rc.move(circle[(this.myhand + 1) % 8]); // change handedness
                } else if (rc.canMove(circle[(this.myhand - 1) % 8])) {
                    rc.move(circle[(this.myhand - 1) % 8]);
                }
            }
            while (!rc.canMove(circle[this.myhand])) {
                //for (Direction dir : nearby) {
                //    this.memory_mode = this.memory_mode || dir == circle[this.myhand];
                //    
                //}
                this.myhand = (this.myhand + 1) % 8;  
            }
            
            rc.move(circle[this.myhand]);
        }
        MapLocation newloc = rc.getLocation();
        if (newloc.isAdjacentTo(waypoint)) {
            this.RH_mode = false;
            return true;
        }
        return false;
        } catch (Exception e) {
            System.out.println("Something went wrong");
            return false;
        }
    }

}
