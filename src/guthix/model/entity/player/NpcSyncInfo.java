package guthix.model.entity.player;

import guthix.io.RSBuffer;
import guthix.model.ChatMessage;
import guthix.model.Entity;
import guthix.model.Tile;
import guthix.model.entity.Npc;
import guthix.model.entity.Player;
import guthix.model.entity.SyncInfo;
import io.netty.buffer.Unpooled;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class NpcSyncInfo extends SyncInfo {

	public NpcSyncInfo(Npc npc) {
		super(npc);
	}

	public void animation(int id, int delay) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(animationSet));
		buffer.get().writerIndex(0);
		buffer.writeShort(id);
		buffer.writeByteN(delay);

		addFlag(Flag.ANIMATION.value);
	}

	public void graphic(int id, int height, int delay) {
		RSBuffer buffer = new RSBuffer(Unpooled.wrappedBuffer(graphicSet));
		buffer.get().writerIndex(0);
		buffer.writeLEShort(id);
		buffer.writeInt(height << 16 | delay);

		addFlag(Flag.GRAPHIC.value);
	}

	@Override
	public void hit(int type, int value) {
		// TODO
	}

	@Override
	public void facetile(Tile tile) {

	}

	@Override
	public void faceEntity(Entity e) {
		//TODO
	}

	public byte[] animationSet() {
		return animationSet;
	}

	public byte[] graphicSet() {
		return graphicSet;
	}

	public static enum Flag {
		ANIMATION(0x10),
		GRAPHIC(0x40);

		public int value;

		private Flag(int v) {
			value = v;
		}
	}

}
