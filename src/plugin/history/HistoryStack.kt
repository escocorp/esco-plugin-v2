package plugin.history;

import arc.struct.Seq;

public class HistoryStack {
    public Seq<HistoryRecord> stack = new Seq<>();

    public int size() {
        return stack.size;
    }

    public void add(HistoryRecord record) {
        stack.add(record);
    }

    public void removeFirst() {
        stack.remove(0);
    }
}
