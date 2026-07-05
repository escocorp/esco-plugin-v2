package plugin.history

enum class HistoryType {
    Rotate,
    BreakBlock,
    BuildBlock,
    DestroyBlock, // when block DESTROYED
    Configure,
    PayloadPickup, // when block picked up by unit
    PayloadDrop; // when block placed by unit
}