package head_v1.util;

public class HQEntry extends LocationEntry{
    
    public boolean adamantium_flag;
    public boolean mana_flag;
    public boolean elixir_flag;
    public boolean danger_flag; 
    
    public HQEntry(int raw_data) {
        super(raw_data);
        this.adamantium_flag = this.flags[0];
        this.mana_flag = this.flags[1];
        this.elixir_flag = this.flags[2];
        this.danger_flag = this.flags[3];
    }

}
