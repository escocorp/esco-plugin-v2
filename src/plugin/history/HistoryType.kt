package plugin.history

enum class HistoryType {
    Rotate,
    BreakBlock,
    BuildBlock,
    DestroyBlock, // when block DESTROYED
    Configure;
}