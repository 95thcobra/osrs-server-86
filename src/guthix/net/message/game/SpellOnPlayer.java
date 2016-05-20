package guthix.net.message.game;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.entity.Player;

/**
 * Created by Bart on 8/20/2015.
 */
@PacketInfo(size = 9)
public class SpellOnPlayer implements Action {

	private static final Logger logger = LogManager.getLogger(SpellOnPlayer.class);

	private int slot;
	private int targetIndex;
	private int interfaceId;
	private int child;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		slot = buf.readUShort();
		targetIndex = buf.readULEShort();
		int hash = buf.readIntV1();
		interfaceId = hash >> 16;
		child = hash & 0xFFFF;
		boolean run = buf.readByte() == 1;
	}

	@Override
	public void process(Player player) {
		logger.info("Spell on player ({}); spell from [{}:{}] slot {}.", targetIndex, interfaceId, child, slot);

		player.stopActions(false);

		Player other = player.world().players().get(targetIndex);
		if (other == null) {
			player.message("Unable to find player.");
		} else {
			if (!player.locked() && !player.dead() && !other.dead()) {
				player.face(other);
				player.putattrib(AttributeKey.TARGET, targetIndex);
				//player.world().server().scriptRepository().triggerSpellOnPlayer(player, interfaceId, child);
			}
		}
	}

}
