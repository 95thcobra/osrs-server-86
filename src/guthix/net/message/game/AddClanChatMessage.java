package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.util.SettingsBuilder;
import guthix.util.UsernameUtilities;

/**
 * Created by Bart on 8/11/2015.
 */
public class AddClanChatMessage implements Command {

	private static int msgid = 1;

	private String sender;
	private String chatname;
	private int world;
	private String text;
	private int icon;

	public AddClanChatMessage(String sender, String chat, int icon, int world, String text) {
		this.sender = sender;
		this.chatname = chat;
		this.world = world;
		this.text = text;
		if (icon <= 4)
			this.icon = icon;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(text.length())).writeSize(RSBuffer.SizeType.BYTE);
		buffer.packet(104);

		buffer.writeString(sender);
		buffer.writeLong(UsernameUtilities.encodeUsername(chatname));
		buffer.writeShort(world);
		buffer.writeTriByte(msgid++);
		buffer.writeByte(icon);

		byte[] huffmandata = new byte[256];
		int len = player.world().server().huffman().encode(text, huffmandata);

		buffer.writeCompact(text.length());
		buffer.get().writeBytes(huffmandata, 0, len);

		return buffer;
	}

}
