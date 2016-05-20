package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class UpdatePlayers implements Command {

	private RSBuffer buffer;

	public UpdatePlayers(RSBuffer payload) {
		buffer = payload;
	}

	@Override
	public RSBuffer encode(Player player) {
		return buffer;
	}
}
