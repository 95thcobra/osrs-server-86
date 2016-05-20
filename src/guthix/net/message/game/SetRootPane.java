package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class SetRootPane implements Command {

	private int paneId;

	public SetRootPane(int paneId) {
		this.paneId = paneId;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(4));

		buffer.packet(45);
		buffer.writeLEShortA(paneId);

		return buffer;
	}
}
