package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.entity.Player;
import guthix.model.entity.player.Privilege;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 5-2-2015.
 */
@PacketInfo(size = 6)
public class DialogueContinue implements Action {

	private int hash;
	private int slot;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		hash = buf.readLEInt();
		slot = buf.readULEShortA();

		if (slot == 0xFFFF)
			slot = -1;
	}

	@Override
	public void process(Player player) {
		if (player.privilege().eligibleTo(Privilege.ADMIN) && player.<Boolean>attrib(AttributeKey.DEBUG, false))
			player.message("Dialogue [%d:%d], slot: %d", hash>>16, hash&0xFFFF, slot);

		int id = hash >>16;
		int child = hash & 0xFFFF;

		Object returnval = null;
		if (id == 219) {
			returnval = slot;
		}

		//player.world().server().scriptExecutor().continueFor(player, WaitReason.DIALOGUE, returnval);
	}
}
