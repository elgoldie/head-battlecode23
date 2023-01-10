package head_v1.bots;

import java.util.Random;

import battlecode.common.*;
import head_v1.util.Communication;

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
    public int spawnIndex;

    public String assignment = "idle";
    public String command = "assign";

    public RobotAI(RobotController rc, int id) throws GameActionException {
        this.rc = rc;
        this.id = id;
        this.rng = new Random(id);
        this.comm = new Communication(rc);

        this.gameTurn = rc.getRoundNum();
        this.aliveTurns = 0;

        this.myTeam = rc.getTeam();
        this.enemyTeam = myTeam.opponent();
        
        if (rc.getType() == RobotType.HEADQUARTERS) {
            // If we are the HQ, we are the spawn location.
            spawnLocation = rc.getLocation();
            spawnIndex = id;
        } else {
            // Otherwise, we need to find the HQ.
            boolean foundHQ = false;
            for (RobotInfo bot : rc.senseNearbyRobots(8, myTeam)) {
                if (bot.team == myTeam && bot.type == RobotType.HEADQUARTERS) {
                    spawnLocation = bot.location;
                    spawnIndex = bot.ID;
                    foundHQ = true;
                    break;
                }
            }

            if (!foundHQ) {
                // this shouldn't happen
                throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Could not find HQ");
            }
        }
    }
    
    public void run() throws GameActionException {
        gameTurn += 1;
        aliveTurns += 1;
    }

    public void wander() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
