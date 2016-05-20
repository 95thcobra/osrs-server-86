package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.World;
import guthix.model.entity.Npc;
import guthix.model.entity.PathQueue;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Tom on 9/26/2015.
 * Modified by Sky on 1/3/2016.
 */
@PacketInfo(size = 3)
public class NpcAction1 implements Action {

    private int size = -1;
    private int opcode = -1;
    private boolean run;
    private int index;

    @Override
    public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
        //run = buf.readByteA() == 1;
        //index = buf.readUShortA();

        index = buf.readULEShort();
        run = buf.readByteA() == 1;
    }

    @Override
    public void process(Player player) {
        player.stopActions(true);

        player.message("npcaction1 --- npcindex:" + index + " run:" + run);
        Npc other = player.world().npcs().get(index);
        player.message("npcid:" + other.id() + " run:" + run);

        if (other == null) {
            player.message("Unable to find npc.");
        } else {
            if (!player.locked() && !player.dead() && !other.dead()) {
                player.stepTowards(other, 20);
                player.face(other);

                //player.putattrib(AttributeKey.TARGET_TYPE, 1);
                //player.putattrib(AttributeKey.TARGET, index);
                //player.world().server().scriptExecutor().executeScript(player, PlayerCombat.script);
            }
        }
    }
}
