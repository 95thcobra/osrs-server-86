package guthix.net.message.game;

import java.util.LinkedList;
import java.util.List;

import guthix.io.RSBuffer;
import guthix.model.Tile;
import guthix.model.entity.Player;
import guthix.util.map.MapDecryptionKeys;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class DisplayMap implements Command { // Aka dipsleemap

	private int x;
	private int z;
	private int localX;
	private int localZ;
	private int level;
	private int[][] xteaKeys;

	public DisplayMap(Player player) {
		this(player, player.tile(), true);
	}

	public DisplayMap(Player player, Tile tile, boolean setActive) {
		int x = tile.x;
		int z = tile.z;

		int base_x = x / 8;
		int base_z = z / 8;

		int botleft_x = (base_x - 6) * 8;
		int botleft_z = (base_z - 6) * 8;

		this.x = base_x;
		this.z = base_z;
		this.localX = x - botleft_x;
		this.localZ = z - botleft_z;
		level = tile.level;

		// Update last map
		if (setActive) {
			player.activeMap(new Tile(botleft_x, botleft_z));
		}
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buf = new RSBuffer(player.channel().alloc().buffer(12 + 4 * 4 * 9));

		buf.packet(16).writeSize(RSBuffer.SizeType.SHORT);

		/* Calculate map keys needed */
		List<int[]> keys = new LinkedList<>();
		for (int rx = (x - (104 >> 4)) / 8; ((104 >> 4) + x) / 8 >= rx; rx++) {
			for (int rz = (z - (104 >> 4)) / 8; ((104 >> 4) + z) / 8 >= rz; rz++) {
				int mapid = rz + (rx << 8);
				keys.add(MapDecryptionKeys.get(mapid));
			}
		}

		for (int[] keyset : keys) {
			for (int key : keyset) {
				buf.writeIntV1(key);
			}
		}

		buf.writeShort(z);
		buf.writeShortA(x);
		buf.writeLEShort(localZ);
		buf.writeByteS(level);
		buf.writeLEShortA(localX);

		return buf;
	}
}
