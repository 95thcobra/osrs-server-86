package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.ChatMessage;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import guthix.util.HuffmanCodec;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
@PacketInfo(size = -1)
public class PublicChat implements Action {

	private int effect;
	private int color;
	private int len;
	private byte[] data;

	@Override public void process(Player player) {
		// Decode huffman data
		byte[] stringData = new byte[256];
		HuffmanCodec codec = player.world().server().huffman();
		codec.decode(data, stringData, 0, 0, len);
		String message = new String(stringData, 0, len);

		ChatMessage chatMessage = new ChatMessage(message, effect, color);
		player.sync().publicChatMessage(chatMessage);
	}

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		int unid = buf.readByte();
		color = buf.readByte();
		effect = buf.readByte();

		len = buf.readCompact();
		data = new byte[buf.get().readableBytes()];
		buf.get().readBytes(data);
	}

}
