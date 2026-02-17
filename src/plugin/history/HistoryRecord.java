package plugin.history;

import mindustry.net.Administration.ActionType;
import mindustry.type.UnitType;
import mindustry.world.Block;

public record HistoryRecord(String playerName, Integer playerId, ActionType type, Block block, UnitType unit) {
    public String getMessage() {
        String actor = playerName == null ?
                unit == null ? "unknown" : unit.emoji() :
                playerName;
        if(playerId != null)
            actor = playerId + " " + actor;
        return switch(type) {
            case rotate -> actor + " [tan]rotated[white] " + block.emoji();
            case breakBlock -> actor + " [tan]broken[white] " + block.emoji();
            case buildSelect -> actor + " [tan]built[white] " + block.emoji();
            case configure -> actor + " [tan]configured[white] " + block.emoji();
            default -> actor + " [tan]"+type.name()+"[white] " + block.emoji();
        };
    }
}
