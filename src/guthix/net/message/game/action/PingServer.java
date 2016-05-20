package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
@PacketInfo(size = 0)
public class PingServer implements Action {

	@Override
	public void process(Player player) {
		/* Pinged, wow. */
	}

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		/* Empty packet */
	}
}
