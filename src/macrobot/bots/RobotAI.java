package macrobot.bots;

import java.util.Random;
import java.util.ArrayList;

import battlecode.common.*;
import macrobot.util.Communication;
import macrobot.path.Path;

public abstract class RobotAI {

    public Random rng;

    // HANDEDNESS/NAVIGATION
    public enum Handedness {
        NONE,
        LEFT,
        LEFT_NOMEM, // for avoiding other robots while in LH mode
        RIGHT,
        RIGHT_NOMEM // for avoiding other robots while in RH mode
    }
    public Handedness handedness = Handedness.NONE;
    public int lefthand;
    public static final Direction[] RIGHTHANDED = {
        Direction.WEST, 
        Direction.SOUTHWEST, 
        Direction.SOUTH, 
        Direction.SOUTHEAST, 
        Direction.EAST, 
        Direction.NORTHEAST, 
        Direction.NORTH, 
        Direction.NORTHWEST
    };
    public int righthand;
    public static final Direction[] LEFTHANDED = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    

    // SELF INFO
    public Team myTeam;
    public Team enemyTeam;

    public RobotController rc;
    public Communication comm;
    public int id;

    public int gameTurn;
    public int aliveTurns;

    public MapLocation spawnLocation;
    public MapLocation[] hqLocations;
    public MapLocation myloc;

    // STATE SYSTEM INFO--to make enum
    public String assignment = "idle";
    public String command = "assign";
    //public int command_arg = -1;

    // MY KNOWLEDGE
    public ArrayList<Path> paths = new ArrayList<Path>();

    // MY SCANNING
    public RobotInfo[] robotscan;
    public WellInfo[] wellscan;    

    
    // ARRAY VISION
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
        this.hqLocations = comm.readLocationArray(1);

        
    }
    
    public void run() throws GameActionException {
        //System.out.println("SKADOOSH");
        this.arrayvision = comm.readWholeArray();
        this.myloc = rc.getLocation();
        rc.setIndicatorString("Beginning action");
        gameTurn += 1;
        aliveTurns += 1;
        //if (this.gameTurn == 2) {
         //   mining_task();
        //}
        // read all
    }

    public void wander() throws GameActionException {
        Direction dir = RIGHTHANDED[rng.nextInt(RIGHTHANDED.length)];
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

    
    public boolean step_RH(MapLocation waypoint) throws GameActionException {
        if (!rc.isMovementReady()) { // this should be handled elsewhere. If not, do scanning tasks
            return false;
        }
        Direction objective = this.myloc.directionTo((waypoint));

        if (objective == Direction.CENTER) {
            return false;
        }

        Direction movequeue = objective;
        

        if (rc.canMove(objective)){
            // no change to movequeue
            switch (this.handedness) {
                case RIGHT:
                case LEFT:
                    //store myloc in memory as a waypoint
                case LEFT_NOMEM:
                case RIGHT_NOMEM:
                    this.handedness = Handedness.NONE;
                    break;
                default:
                    break;

            }
        } else {
            int counter;
            this.righthand = 0;
            this.lefthand = 0;
            while (this.righthand < 8 && RIGHTHANDED[this.righthand] != objective) {
                this.righthand ++;
            }
            while (this.lefthand < 8 && LEFTHANDED[this.lefthand] != objective) {
                this.lefthand ++;
            }
            switch (this.handedness) {
                case NONE:
                    counter = 0;
                    while (!rc.canMove(RIGHTHANDED[this.righthand]) && counter < 8) {
                        this.righthand = (this.righthand + 1) % 8;
                        counter ++;
                    }
                    if (counter == 8) {
                        return false;
                    }
                    counter = 0;
                    while (!rc.canMove(LEFTHANDED[this.lefthand]) && counter < 8) {
                        this.lefthand = (this.lefthand + 1) % 8;
                        counter ++;
                    }
                    
                    if (this.myloc.add(RIGHTHANDED[this.righthand]).distanceSquaredTo(waypoint) < this.myloc.add(LEFTHANDED[this.lefthand]).distanceSquaredTo(waypoint)) {
                        this.handedness = Handedness.RIGHT;
                        movequeue = RIGHTHANDED[this.righthand];
                    } else { 
                        this.handedness = Handedness.LEFT;
                        movequeue = LEFTHANDED[this.lefthand]; 
                    }
                    // check memory
                    break;
                case RIGHT:
                case RIGHT_NOMEM:
                    counter = 0;
                    this.righthand = (this.righthand + 1) % 8;
                    while (!rc.canMove(RIGHTHANDED[this.righthand]) && counter < 8) {
                        if ((this.handedness == Handedness.RIGHT) && rc.sensePassability(this.myloc.add(RIGHTHANDED[this.righthand]))) {
                            this.handedness = Handedness.RIGHT_NOMEM;
                        }
                        this.righthand = (this.righthand + 1) % 8;
                    }
                    movequeue = RIGHTHANDED[this.righthand];
                    if (movequeue == objective) {
                        return false;
                    }
                    break;
                case LEFT:
                case LEFT_NOMEM:
                    counter = 0;
                    this.lefthand = (this.lefthand + 1) % 8;
                    while (!rc.canMove(LEFTHANDED[this.lefthand]) && counter < 8) { 
                        if ((this.handedness == Handedness.LEFT) && rc.sensePassability(this.myloc.add(LEFTHANDED[this.lefthand]))) {
                            this.handedness = Handedness.LEFT_NOMEM;
                        }
                        this.lefthand = (this.lefthand + 1) % 8;
                    }
                    movequeue = LEFTHANDED[this.lefthand];
                    if (movequeue == objective) {
                        return false;
                    }
                    break;
            }
            
        }
        rc.move(movequeue);
        this.myloc = this.myloc.add(movequeue);
        //rc.setIndicatorString(this.handedness + " " + objective);
        //return false;
        return true;
    }

}
