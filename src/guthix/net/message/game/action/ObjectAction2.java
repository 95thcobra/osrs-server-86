package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.Tile;
import guthix.model.entity.Player;
import guthix.model.map.MapObj;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 8/23/2015.
 */
@PacketInfo(size = 7)
public class ObjectAction2 implements Action {

	private int id;
	private int x;
	private int z;
	private boolean run;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		id = buf.readUShort();
		run = buf.readByte() == 1;
		z = buf.readUShortA();
		x = buf.readUShortA();
	}

	@Override
	public void process(Player player) {
		MapObj obj = player.world().objById(id, x, z, player.tile().level);

		if (obj == null)
			return;

		if ((boolean) player.attrib(AttributeKey.DEBUG, false))
			player.message("Interacting with object %d at [%d, %d]", id, x, z);

		if (!player.locked() && !player.dead()) {
			player.stopActions(true);
			player.putattrib(AttributeKey.INTERACTION_OBJECT, obj);
			player.putattrib(AttributeKey.INTERACTION_OPTION, 2);
			//player.world().server().scriptExecutor().executeScript(player, ObjectInteraction.script);
		}
	}

}
