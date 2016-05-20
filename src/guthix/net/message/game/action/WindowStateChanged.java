package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart Pelle on 8/23/2014.
 *
 */
@PacketInfo(size = 1)
public class WindowStateChanged implements Action {

	private boolean visible;

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		visible = buf.readByte() == 1;
	}

	@Override public void process(Player player) {
		/* We could register this for antibotting :D */
	}
}
