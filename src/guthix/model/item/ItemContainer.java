package guthix.model.item;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import guthix.fs.ItemDefinition;
import guthix.model.World;
import guthix.model.item.Item;
import guthix.util.Tuple;

/**
 * Created by Bart on 7/10/2015.
 */
public class ItemContainer {

	private World world;
	private final Item[] items;
	private final Type type;
	private boolean dirty = true;

	public ItemContainer(World world, int size, Type type) {
		items = new Item[size];
		this.world = world;
		this.type = type;
	}

	public Type type() {
		return type;
	}

	public int size() {
		return items.length;
	}

	public int nextFreeSlot() {
		for (int i=0; i<size(); i++) {
			if (items[i] == null)
				return i;
		}

		return -1;
	}

	public int freeSlots() {
		int slots = 0;

		for (int i=0; i<size(); i++) {
			if (items[i] == null)
				slots++;
		}

		return slots;
	}

	public int occupiedSlots() {
		return size() - freeSlots();
	}

	public boolean full() {
		return nextFreeSlot() == -1;
	}

	public boolean isEmpty() {
		return nextFreeSlot() == 0;
	}

	public void empty() {
		for (int i=0; i<size(); i++) {
			items[i] = null;
		}
		makeDirty();
	}

	public void makeDirty() {
		dirty = true;
	}

	public void clean() {
		dirty = false;
	}

	public boolean dirty() {
		return dirty;
	}

	public Result add(Item item, boolean force) {
		if (item.amount() < 0)
			return new Result(item.amount(), 0);

		ItemDefinition def = item.definition(world);
		boolean stackable = !item.hasProperties() && (def.stackable() || type == Type.FULL_STACKING);
		int amt = count(item.id());

		// Determine if this is going to work in advance
		if (!force) {
			if (stackable && item.amount() > Integer.MAX_VALUE - amt) {
				return new Result(item.amount(), 0);
			} else if (!stackable && item.amount() > size() - amt) {
				return new Result(item.amount(), 0);
			}
		}

		// And complete the actual operation =)
		if (stackable) {
			int index = findFirst(item.id()).first();

			if (index == -1) {
				if (nextFreeSlot() == -1)
					return new Result(item.amount(), 0);

				items[nextFreeSlot()] = new Item(item.id(), item.amount());
				makeDirty();
				return new Result(item.amount(), item.amount());
			} else {
				long cur = amt;
				long target = cur + item.amount();
				int add = (int) (target > Integer.MAX_VALUE ? Integer.MAX_VALUE - cur : item.amount());

				items[index] = new Item(item.id(), amt + add);
				makeDirty();
				return new Result(item.amount(), add);
			}
		} else {
			int left = item.amount();
			for (int i=0; i<size(); i++) {
				if (items[i] == null) {
					items[i] = new Item(item, 1);

					if (--left == 0) {
						break;
					}
				}
			}

			makeDirty();
			return new Result(item.amount(), item.amount() - left);
		}
	}

	public Result remove(Item item, boolean force) {
		return remove(item, force, 0);
	}

	public Result remove(Item item, boolean force, int start) {
		if (item.amount() < 0)
			return new Result(item.amount(), 0);

		ItemDefinition def = item.definition(world);
		boolean stackable = !item.hasProperties() && (def.stackable() || type == Type.FULL_STACKING);
		int amt = count(item.id());

		// Do we even have this item?
		if (amt < 1) {
			return new Result(item.amount(), 0);
		}

		// Determine if this is going to work in advance
		if (!force) {
			if (item.amount() > amt) {
				return new Result(item.amount(), 0);
			}
		}

		// And complete the actual operation =)
		if (stackable) {
			int index = findFirst(item.id()).first();
			int remove = Math.min(item.amount(), items[index].amount());
			items[index] = new Item(items[index], items[index].amount() - remove);

			if (items[index].amount() == 0)
				items[index] = null;

			makeDirty();
			return new Result(item.amount(), remove);
		} else {
			int left = item.amount();

			for (int x=0; x<size(); x++) {
				int i = (x + start) % size();
				if (items[i] != null && items[i].id() == item.id()) {
					items[i] = null;

					if (--left == 0) {
						break;
					}
				}
			}

			makeDirty();
			return new Result(item.amount(), item.amount() - left);
		}
	}

	public void set(int slot, Item item) {
		if (item != null && item.amount() < 1)
			item = null;
		items[slot] = item;
		makeDirty();
	}

	public int count(int item) {
		long count = 0;

		for (Item i : items) {
			if (i != null && i.id() == item)
				count += i.amount();
		}

		return (int) Math.min(Integer.MAX_VALUE, count);
	}

	public int count(Integer... matches) {
		List<Integer> list = Arrays.asList(matches);

		long count = 0;

		for (Item i : items) {
			if (i != null && list.contains(i.id()))
				count += i.amount();
		}

		return (int) Math.min(Integer.MAX_VALUE, count);
	}

	public boolean has(int item) {
		return findFirst(item).first() != -1;
	}

	public boolean hasAny(int... items) {
		for (int i : items)
			if (findFirst(i).first() != -1)
				return true;
		return false;
	}

	public Item get(int slot) {
		return items[slot];
	}

	public boolean hasAt(int slot) {
		return slot >= 0 & slot < size() && items[slot] != null;
	}

	public Tuple<Integer, Item> findFirst(int item) {
		for (int i = 0; i < size(); i++) {
			if (items[i] != null && items[i].id() == item)
				return new Tuple<>(i, items[i]);
		}

		return new Tuple<>(-1, null);
	}

	public List<Tuple<Integer, Item>> findAll(int item) {
		List<Tuple<Integer, Item>> results = new LinkedList<>();

		for (int i = 0; i < size(); i++) {
			if (items[i] != null && items[i].id() == item)
				results.add(new Tuple<>(i, items[i]));
		}

		return results;
	}

	public Item[] copy() {
		return items.clone();
	}

	public void restore(Item[] copy) {
		for (int i = 0; i < items.length; i++) {
			if (i < copy.length) {
				items[i] = copy[i];
			} else {
				items[i] = null;
			}
		}
	}

	public static enum Type {
		REGULAR, FULL_STACKING
	}

	public static class Result {
		private int requested;
		private int completed;
		public Result(int requested, int completed) {
			this.requested = requested;
			this.completed = completed;
		}
		public int requested() {
			return requested;
		}
		public int completed() {
			return completed;
		}
		public boolean success() {
			return completed == requested;
		}
		public boolean failed() {
			return !success();
		}
	}

}
