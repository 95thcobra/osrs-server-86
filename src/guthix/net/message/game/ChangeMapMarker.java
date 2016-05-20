package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import io.netty.buffer.Unpooled;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class ChangeMapMarker implements Command {

	private int x;
	private int z;

	public ChangeMapMarker(int x, int z) {
		this.x = x;
		this.z = z;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());

		buffer.packet(24);

		buffer.writeByte(x);
		buffer.writeByte(z);

		return buffer;
	}
}
