package head_v1.task_manager;

public class Task {
    // wrapper
    public LocationEntry;
    public OrdersEntry;

    public Task(int raw_1, int raw_2) {
        this.LocationEntry = new LocationEntry(raw_1);
        this.OrdersEntry = new OrdersEntry(raw_2);
    }

}
// consider: make individual opcode interpreters for the task?