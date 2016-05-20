package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import guthix.script.TimerKey;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 8/12/2015.
 */
@PacketInfo(size = 3)
public class PlayerAction2 implements Action {

	private boolean run;
	private int index;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		index = buf.readULEShort();
		run = buf.readByteS() == 1;
	}

	@Override
	public void process(Player player) {
		player.stopActions(true);

		Player other = player.world().players().get(index);
		if (other == null) {
			player.message("Unable to find player.");
		} else {
			if (!player.locked() && !player.dead() && !other.dead()) {
				player.face(other);

				player.putattrib(AttributeKey.TARGET, index);
				//player.world().server().scriptExecutor().executeScript(player, PlayerFollowing.script);
			}
		}
	}

}
