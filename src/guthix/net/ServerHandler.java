package guthix.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.GameServer;
import guthix.crypto.IsaacRand;
import guthix.io.RSBuffer;
import guthix.model.Tile;
import guthix.model.entity.Player;
import guthix.net.future.ClosingChannelFuture;
import guthix.net.message.*;
import guthix.net.message.game.Action;
import guthix.net.message.game.DisplayMap;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Bart on 8/4/2014.
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

	/**
	 * The logger instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(ServerHandler.class);

	/**
	 * The attribute key for the Player attachment of the channel.
	 */
	public static final AttributeKey<Player> ATTRIB_PLAYER = AttributeKey.valueOf("player");

	/**
	 * A reference to the server instance.
	 */
	private GameServer server;

	public ServerHandler(GameServer server) {
		this.server = server;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);

		logger.trace("A new client has connected: {}", ctx.channel());
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);

		logger.trace("A client has disconnected: {}", ctx.channel());

		if (ctx.channel().attr(ATTRIB_PLAYER).get() != null) {
			ctx.channel().attr(ATTRIB_PLAYER).get().putattrib(guthix.model.AttributeKey.LOGOUT, true);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getStackTrace()[0].getMethodName().equals("read0"))
			return;

		if (cause instanceof ReadTimeoutException) {
			logger.info("Channel disconnected due to read timeout (30s): {}.", ctx.channel());
			ctx.channel().close();
		} else {
			logger.error("An exception has been caused in the pipeline: ", cause);
		}
	}

}
