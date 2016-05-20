package guthix.net.message.game.action;

import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;

import guthix.fs.ObjectDefinition;
import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.entity.Player;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
@PacketInfo(size = 2)
public class ExamineObject implements Action {

	private int id;

	@Override public void process(Player player) {
		if ((boolean) player.attrib(AttributeKey.DEBUG))
			player.message("%s, (%d) %s", player.world().examineRepository().object(id), id,
					Arrays.toString(player.world().definitions().get(ObjectDefinition.class, id).models));
		else
			player.message(player.world().examineRepository().object(id));
	}

	@Override public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		id = buf.readULEShort();
	}

}
