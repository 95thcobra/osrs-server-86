package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class SetVarp implements Command {

	private int id;
	private int value;
	private boolean small;

	public SetVarp(int id, int value) {
		this.id = id;
		this.value = value;
		small = value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(small ? 4 : 7));
		buffer.packet(small ? 190 : 47);

		if (small) {
			buffer.writeByteS(value);
			buffer.writeLEShortA(id);
		} else {
			buffer.writeLEShortA(id);
			buffer.writeLEInt(value);
		}

		return buffer;
	}
}
