package guthix.model;

import com.google.common.base.MoreObjects;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class Tile {

	public final int x;
	public final int z;
	public final int level;

	public Tile(Tile t) {
		x = t.x;
		z = t.z;
		level = t.level;
	}

	public Tile(int x, int z) {
		this.x = x;
		this.z = z;
		this.level = 0;
	}

	public Tile(int x, int z, int level) {
		this.x = x;
		this.z = z;
		this.level = level;
	}

	public int distance(Tile tile) {
		int dx = tile.x - x;
		int dz = tile.z - z;
		return (int) Math.sqrt(dx * dx + dz * dz);
	}

	public int region() {
		return ((x >> 6) << 8) | (z >> 6);
	}

	public static int coordsToRegion(int x, int z) {
		return ((x >> 6) << 8) | (z >> 6);
	}

	public Tile transform(int dx, int dz, int dh) {
		return new Tile(x + dx, z + dz, level + dh);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tile))
			return false;
		Tile o = (Tile) obj;
		return o.x == x && o.z == z && o.level == level;
	}

	public boolean equals(int x, int z, int level) {
		return this.x == x && this.z == z && this.level == level;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("x", x).add("z", z).add("level", level).toString();
	}
}
