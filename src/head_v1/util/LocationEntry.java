package head_v1.util;

public abstract class LocationEntry extends Entry {
    
    public int[] coordinates = new int[2];
    public boolean[] flags = new boolean[4];

    public LocationEntry(int raw_data) {
        super(raw_data);
        int[] full_split = this.splitbits(raw_data, new int[]{1,1,1,1,6,6});
        this.coordinates = new int[]{full_split[2], full_split[5]};
        for (int i = 0; i < 4; i++) {
            this.flags[i] = full_split[i] == 1;
        }
    }

}

