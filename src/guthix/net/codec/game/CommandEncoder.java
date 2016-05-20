package guthix.net.codec.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.net.ServerHandler;
import guthix.net.message.game.Command;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
@ChannelHandler.Sharable
public class CommandEncoder extends MessageToByteEncoder<Command> {

	private static final Logger logger = LogManager.getLogger(CommandEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
		Player player = ctx.channel().attr(ServerHandler.ATTRIB_PLAYER).get();
		RSBuffer buffer = msg.encode(player);
		buffer.finish();

		if (buffer.packet() != -1) {
			out.writeByte((byte) buffer.packet()/* + (byte)player.outrand().nextInt()*/);
			out.writeBytes(buffer.get());
		}

		buffer.get().release();
	}

}
