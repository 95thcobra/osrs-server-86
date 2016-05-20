package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.GroundItem;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 8/23/2015.
 */
@PacketInfo(size = 7)
public class GroundItemAction3 implements Action {

	private int x;
	private int z;
	private int id;
	private boolean run;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		z = buf.readUShort();
		run = buf.readByteS() == 1;
		id = buf.readUShort();
		x = buf.readUShort();
	}

	@Override
	public void process(Player player) {
		GroundItem item = player.world().getGroundItem(x, z, player.tile().level, id);

		if (!player.locked() && !player.dead()) {
			player.putattrib(AttributeKey.GROUNDITEM_TARGET, item);
			//player.world().server().scriptExecutor().executeScript(player, GroundItemTaking.script);
		}
	}

}
