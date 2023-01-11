package head_v1.task_manager;
import java.util.ArrayList;


public class TaskInfoEntry extends Entry {

    // reads and sets corresponding flags
    public int opcode;
    public int info;
    public int[] myop;
    public static final int[] op0 = new int[]{1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1};
    public static final int[] op1 = new int[]{1,1,1,1,2,1,4}; //AME+|U (Upgrade flag)|xx|L (Lookout flag)|CRRQ (CRRQ = carrier request)

    public static final ArrayList<int[]> opref = new ArrayList<int[]>();

    public TaskInfoEntry(int raw_data){
        super(raw_data);
        int[] opsplit = splitbits(raw_data, new int[]{4, 12});
        opref.add(op0);
        opref.add(op1);
        this.opcode = opsplit[0];
        this.info = opsplit[1];
        assign_myop();
    }

    public void assign_myop() {
        switch (this.opcode) {
            case 1:
                this.myop = op1;
        }
    }

    public int[] interpret() {
        return this.splitbits(this.info, this.myop);
    }

}
