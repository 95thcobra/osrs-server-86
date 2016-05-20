package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class CloseInterface implements Command {

	private int hash;

	public CloseInterface(int target, int targetChild) {
		hash = (target << 16) | targetChild;
	}

	public CloseInterface(int hash) {
		this.hash = hash;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(5));
		buffer.packet(181);
		buffer.writeInt(hash);
		return buffer;
	}

}
