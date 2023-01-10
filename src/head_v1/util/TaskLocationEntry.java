package head_v1.util;

public class TaskLocationEntry extends LocationEntry {
    
    public String control; 
    public boolean amplifier_flag;
    public boolean hq_flag;

    public TaskLocationEntry(int raw_data) {
        super(raw_data);
        
        if (this.flags[0]) {
            if (this.flags[1]) { this.control = "Neutral"; } else { this.control = "Friendly"; }
        }
        else {
            if (this.flags[1]) { this.control = "Hostile"; } else { this.control = "Unknown"; }
        }

        this.amplifier_flag = flags[2];
        this.hq_flag = flags[3];
    }
    
}
