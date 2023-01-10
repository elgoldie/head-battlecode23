package head_v1.util;
import java.util.Arrays;

public abstract class Entry {
    
    public int B0;
    public int B1;
    public int address;

    public Entry(int raw_data) {
        int[] bytes = this.splitbits(raw_data, new int[]{8,8});
        this.B0 = bytes[0];
        this.B1 = bytes[1];

    }

    public int[] splitbits(int raw_data, int[] partitions) {
        
        int[] partitioned = Arrays.copyOf(partitions, partitions.length);
        // elements of `partitions` have to sum up to the # of bits of the raw data
        int working_data = raw_data;

        for (int i = partitions.length - 1; i >= 0; i--) {
            partitioned[i] = working_data % 2^partitions[i];
            working_data -= partitioned[i]; 
        }

        return partitioned;

    }
    
    
}

