package plugin.packets;

import mindustry.Vars;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.SendChatMessageCallPacket;

public class Packets {
    public static void load() {
        Vars.net.handleServer(AdminRequestCallPacket.class, AdminRequest::handle);
        // Vars.net.handleServer(SendChatMessageCallPacket.class, SendChatMessage::handle);
    }
}
