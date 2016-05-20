package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.GroundItem;
import guthix.model.entity.Player;
import guthix.model.item.Item;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 5-2-2015.
 */
@PacketInfo(size = 8)
public class ItemAction4 extends ItemAction {

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		item = buf.readUShortA();
		hash = buf.readInt();
		slot = buf.readULEShortA();
	}

	@Override
	protected int option() {
		return 3;
	}

	@Override
	public void process(Player player) {
		super.process(player);

		Item item = player.inventory().get(slot);
		if (item != null && item.id() == this.item && !player.locked() && !player.dead()) {
			player.stopActions(false);
			//player.world().server().scriptRepository().triggerItemOption4(player, item.id(), slot);
		}
	}
}
