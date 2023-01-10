package head_v1.task_manager;

public class AmplifierEntry extends LocationEntry{
    
    public int grouped; //?
    
    public AmplifierEntry(int raw_data) {
        super(raw_data);
        this.grouped = splitbits(raw_data, new int[]{4,6,6})[0];
    }

}

