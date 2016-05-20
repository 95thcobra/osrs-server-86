package guthix.net.message.game.action;

import guthix.fs.ItemDefinition;
import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import guthix.util.L10n;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart Pelle on 8/9/2015.
 */
@PacketInfo(size = 2)
public class ExamineItem implements Action {

	private int id;

	@Override public void process(Player player) {
		player.message(player.world().examineRepository().item(id));
	}

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		id = buf.readUShortA();
	}

}
