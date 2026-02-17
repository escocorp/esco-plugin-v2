package plugin.history;

import arc.math.geom.Point2;
import arc.struct.LongMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.net.Administration;
import mindustry.type.UnitType;
import mindustry.world.Block;

public class History {
    public static final LongMap<HistoryStack> history = new LongMap<>();

    public static String getMessage(int pos) {
        StringBuilder sb = new StringBuilder();
        // Point2 pos2 = Point2.unpack(pos);
        int x = Point2.x(pos), y = Point2.y(pos); // int int

        sb.append("["+x+"] "+"["+y+"]");


        HistoryStack stack = history.get(pos); // long
        //stack.stack.each(s->{sb.append("\n").append(s.getMessage());});
        if(stack != null)
            for(int i = 0; i < stack.size(); i++) {
                sb.append("\n").append(stack.stack.get(i).getMessage());
            }

        return sb.toString();
    }

    public static void write(long pos, String playerName, Integer playerId,
                             Administration.ActionType type,
                             Block block, UnitType unit) {

        HistoryStack stack = history.get(pos); // long

        if(stack == null){
            stack = new HistoryStack();
            history.put(pos, stack); // long
        }

        HistoryRecord record = new HistoryRecord(playerName, playerId, type, block, unit);

        if(stack.size() >= 6){
            stack.removeFirst();
        }

        stack.add(record);
    }

    public static void clear() {
        history.clear();
    }
}
