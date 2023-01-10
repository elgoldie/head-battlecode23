package head_v1.util;

public abstract class Interpreter {
    
    public int address;
    public Communication communication;

    public Interpreter(Communication communication, int address) {
        this.address = address;
        this.communication = communication;
    }

    

}


