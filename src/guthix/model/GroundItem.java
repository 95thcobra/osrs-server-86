package guthix.model;

import guthix.model.item.Item;

/**
 * Created by Bart on 8/22/2015.
 */
public class GroundItem {

	private final Item item;
	private final Tile tile;
	private final Object ownerId;
	private final long spawned = System.currentTimeMillis();
	private boolean broadcasted = false;

	public GroundItem(Item item, Tile tile, Object owner) {
		this.item = item;
		this.tile = tile;
		this.ownerId = owner;

		if (owner == null)
			broadcasted = true;
	}

	public Item item() {
		return item;
	}

	public Object owner() {
		return ownerId;
	}

	public Tile tile() {
		return tile;
	}

	public boolean broadcasted() {
		return broadcasted;
	}

	public void broadcasted(boolean b) {
		broadcasted = b;
	}

	public boolean shouldBroadcast() {
		return System.currentTimeMillis() >= spawned + 60_000;
	}

	public boolean shouldBeRemoved() {
		return System.currentTimeMillis() >= spawned + 120_000;
	}

}
