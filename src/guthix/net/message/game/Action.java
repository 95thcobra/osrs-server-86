package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart Pelle on 8/22/2014.
 *
 * Represents an incoming action sent by the client to the server.
 */
public interface Action {

	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size);

	public void process(Player player);

}
