package macrobot.bots;

import macrobot.util.InformationManager;

import battlecode.common.*;
import java.util.ArrayList;

import javax.crypto.spec.RC5ParameterSpec;

public class AmplifierAI extends RobotAI {

    public MapLocation myPost;
    public MapLocation myHQ;
    public MapLocation destination = new MapLocation(-1, -1);

    public enum ampassignment {
        NULL,
        IDLE,
        SCOUT
    }
    public enum ampcommand {
        ASSIGN,
        GOTO,
    }

    public ampassignment assignment;
    public ampcommand command;
    public int myAssignment;

    public ArrayList<WellInfo> knownwells = new ArrayList<WellInfo>();
    
    public AmplifierAI(RobotController rc, int id) throws GameActionException {
        super(rc, id);
        this.assignment = ampassignment.IDLE;
        this.command = ampcommand.ASSIGN;
        this.info = new InformationManager(new MapLocation[]{new MapLocation(5,5)});
    }

    //track known info

    @Override
    public void run() throws GameActionException {
        super.run();
        
        rc.setIndicatorString(this.assignment.toString() +" "+ this.destination.toString()+" "+this.knownwells.size());
        switch (this.assignment) {
            case NULL:
            break;
            case IDLE:
            scouting_task();
            break;
            case SCOUT:
            ArrayList<WellInfo> newwells = new ArrayList<WellInfo>();
            WellInfo[] found = rc.senseNearbyWells();
            rc.setIndicatorString(Integer.toString(found.length) + " " + Integer.toString(this.knownwells.size()));
            boolean foundnew;
            WellInfo winfo;
            for (int j = 0; j < found.length; j++) {
                winfo = found[j];
                foundnew = true;
                for (int i = 0; i < knownwells.size(); i++) {
                    foundnew = foundnew && winfo.getMapLocation() != knownwells.get(i).getMapLocation() && winfo.getMapLocation() != new MapLocation(3,9);
                }
                if (foundnew && winfo != null) {
                    newwells.add(winfo);
                    knownwells.add(winfo);
                }
            }
            if (newwells.size() > 0){
                rc.setIndicatorString("Found a new well!");
                int[] request;
                for (int i = 0; i < newwells.size(); i++) {
                    winfo = newwells.get(i);
                    request = this.info.well_to_request(winfo);
                    for (int j = this.tasklow; j <= this.taskhigh+20; j++) {
                        if (this.arrayvision[j] == 0) {
                            System.out.println("Writing new well request to "+j);
                            this.comm.queueWrite(j, request[0]);
                            this.comm.queueWrite(j+1, request[1]);
                            break;
                        }
                    }
                }
            }
            while (!this.myloc.isAdjacentTo(destination) && rc.isMovementReady() && this.step_RH(this.destination)) {}
            if (this.myloc.isAdjacentTo(destination)) {
                rc.setIndicatorString("Arrived == going idle");
                this.comm.queueWrite(this.myAssignment, 0);
                this.comm.queueWrite(this.myAssignment+1, 0);
                this.assignment = ampassignment.IDLE;
                this.command = ampcommand.ASSIGN;
            }
            break;

        }
    }

    public void scouting_task() throws GameActionException {
        this.arrayvision = this.comm.readWholeArray();
        //System.out.println("Beginning attempt to mine");
        int op = 61440; //1111000000000000
        int scouting_op = 8192; // 0010000000000000 
        for (int i = this.tasklow; i <= this.taskhigh; i += 2) {
            //System.out.println("- index: "+(i+1));
            //System.out.println(Integer.toBinaryString(this.arrayvision[i+1]));
            //System.out.println(Integer.toBinaryString(op));
            //System.out.println(Integer.toBinaryString(scouting_op));
            
            if ((this.arrayvision[i+1] & op) == scouting_op & (this.arrayvision[i+1] % 4096) / 2048 > 0) {
                this.myAssignment = i;
                this.assignment = ampassignment.SCOUT;
                this.myPost = extractLocation(this.arrayvision[i]);
                //System.out.println("-"+i+"-");
                //System.out.println(this.arrayvision[i] % 16384 / 4096);
                //for (int j = 0; j < 5; j++){
                //System.out.println("Read from index "+j+": "+this.arrayvision[j]);}
                //this.myHQ = this.hqLocations[(this.arrayvision[i] % 16384 / 4096)];
               
                this.myHQ = extractLocation(this.arrayvision[(this.arrayvision[i] % 16384 / 4096) + 1] - 1);
                this.command = ampcommand.GOTO;
                this.destination = this.myPost;
                //this.arg1 = //location
                //this.arg2 = //HQ
                comm.queueWrite(i+1, this.arrayvision[i+1] - 2048);
                System.out.println("Scouting task found. Embarking");
                System.out.println("My destination: "+this.myPost.toString());
                System.out.println("My HQ: "+this.myHQ.toString());
                break;
            }
        }
    }
}
