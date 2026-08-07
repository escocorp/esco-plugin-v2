package plugin.history

import arc.struct.Seq

class HistoryStack {
    var stack: Seq<HistoryRecord> = Seq<HistoryRecord>()

    fun size(): Int {
        return stack.size
    }

    fun add(record: HistoryRecord) {
        stack.add(record)
    }

    fun removeFirst() {
        stack.remove(0)
    }
}
