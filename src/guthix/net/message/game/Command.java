package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/22/2014.
 *
 * Represents a command, or simply a message from the server to the user.
 */
public interface Command {
	public RSBuffer encode(Player player);
}
