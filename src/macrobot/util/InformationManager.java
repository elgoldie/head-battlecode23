package macrobot.util;

import battlecode.common.*;

import java.util.Random;

public class InformationManager {
    //temp. Will make specific task-reading/writing stuff
    MapLocation[] HQ_locations;
    int[] HQ_flags;

    public InformationManager(MapLocation[] HQ_locations) {
        this.HQ_locations = HQ_locations;
    }
    
    public int nearestHQ(MapLocation poi) {
        int mindist = -1;
        int minhq = -1;
        for (int i = 0; i < this.HQ_locations.length; i++) {
            if (mindist == -1 || this.HQ_locations[i].distanceSquaredTo(poi) < mindist) {
                minhq = i;
            }
        }
        return minhq;
    }

    //TODO
    public int nearestHQWithFlag(MapLocation poi, int[] flags) {
        int mindist = -1;
        int minhq = -1;
        for (int i = 0; i < this.HQ_locations.length; i++) {
            if (mindist == -1 || this.HQ_locations[i].distanceSquaredTo(poi) < mindist) {
                minhq = i;
            }
        }
        return minhq;
    }

    public int[] well_to_request(WellInfo w) throws GameActionException {
        return this.well_to_request(w, 9);
    }

    public int[] well_to_request(WellInfo w, int num) throws GameActionException {
        MapLocation loc = w.getMapLocation();
        int location = 32768 + loc.x * 64 + loc.y + 4096 * this.nearestHQ(loc); //
        int request = num + 4096;
        System.out.println("Nearest HQ: "+this.nearestHQ(loc));
        //System.out.println("Processing well");
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

    public int[] scouting_request(MapLocation target, boolean amplifier, boolean carrier, boolean fleet, boolean pull) {
        int location = target.x * 64 + target.y + 4096 * this.nearestHQ(target);
        int request = 8192; 
        if (amplifier) { request += 2048; }
        if (carrier) { request += 1024; }
        if (fleet) { request += 512; }
        if (pull) { request += 256; }
        return new int[]{location, request};
    }

    
}
