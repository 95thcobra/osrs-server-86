package guthix.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
public class Js5Handler extends ChannelInboundHandlerAdapter {

	/**
	 * The logger instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(Js5Handler.class);

	/**
	 * A reference to the server instance.
	 */
	private GameServer server;

	/**
	 * Cached contents from the generated 255,255 request
	 */
	private byte[] cachedIndexInfo;

	public Js5Handler(GameServer server) {
		this.server = server;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);

		if (msg instanceof HandshakeMessage) {
			HandshakeMessage mes = (HandshakeMessage) msg;

			if (mes.revision() != server.config().getInt("server.revision")) {
				if (server.config().getBoolean("server.forcerevision")) {
					logger.trace("Rejected incoming js5 channel because their revision ({}) was not {}", mes.revision(), server.config().getInt("server.revision"));

					ctx.writeAndFlush(HandshakeResponse.OUT_OF_DATE).addListener(new ClosingChannelFuture());
					return;
				} else {
					logger.trace("Accepted js5 connection with invalid revision ({}, wanted {})", mes.revision(), server.config().getInt("server.revision"));
				}
			}  else {
				logger.trace("Accepted js5 handshake from {}", ctx.channel());
			}

			ctx.writeAndFlush(HandshakeResponse.ALL_OK);
		} else if (msg instanceof Js5DataRequest) {
			Js5DataRequest req = ((Js5DataRequest) msg);

			if (req.index() == 255 && req.container() == 255) {
				ctx.writeAndFlush(new Js5DataMessage(255, 255, getIndexInfo(), req.priority()));
			} else if (req.index() == 255) {
				ctx.writeAndFlush(new Js5DataMessage(255, req.container(), getDescriptorData(req.container()), req.priority()));
			} else {
				ctx.writeAndFlush(new Js5DataMessage(req.index(), req.container(), getFileData(req.index(), req.container()), req.priority()));
			}
		}
	}

	private byte[] getDescriptorData(int index) {
		return trim(server.store().getDescriptorIndex().getArchive(index));
	}

	private byte[] getFileData(int index, int file) {
		return trim(server.store().getIndex(index).getArchive(file));
	}

	private byte[] trim(byte[] b) {
		if (b == null || b.length <= 5) {
			return new byte[5];
		}

		ByteBuffer buffer = ByteBuffer.wrap(b);
		int compression = buffer.get();
		int size = buffer.getInt();

		byte[] n = new byte[size + (compression == 0 ? 5 : 9)];
		System.arraycopy(b, 0, n, 0, size + (compression == 0 ? 5 : 9));
		return n;
	}

	private byte[] getIndexInfo() {
		if (cachedIndexInfo != null)
			return cachedIndexInfo;

		cachedIndexInfo = new byte[5 + server.store().getIndexCount() * 8];
		ByteBuffer buffer = ByteBuffer.wrap(cachedIndexInfo);
		buffer.put((byte) 0);
		buffer.putInt(server.store().getIndexCount() * 8);

		for (int index = 0; index < server.store().getIndexCount(); index++) {
			buffer.putInt(server.store().getIndex(index).getCRC());
			buffer.putInt(server.store().getIndex(index).getDescriptor().getRevision());
		}

		return cachedIndexInfo;
	}

}
