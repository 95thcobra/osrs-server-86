package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class AddMessage implements Command {

	private String text;
	private Type type;

	public AddMessage(String text) {
		this.text = text;
		this.type = Type.GAME; // Default to unfilterable
	}

	public AddMessage(String text, Type type) {
		this.text = text;
		this.type = type;
	}

	@Override public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(1 + 1 + text.length() + 1));
		buffer.packet(78).writeSize(RSBuffer.SizeType.BYTE);

		buffer.writeCompact(type.id);
		buffer.writeByte(0);
		buffer.writeString(text);
		return buffer;
	}

	public static enum Type {
		GAME(29), GAME_FILTER(105);
		int id;
		Type(int id) {this.id = id;}
	}

}
