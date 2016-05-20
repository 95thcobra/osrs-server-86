package guthix.model.entity.player;

import java.util.HashSet;
import java.util.Set;

import guthix.fs.VarbitDefinition;
import guthix.model.entity.Player;
import guthix.net.message.game.SetVarp;
import guthix.net.message.game.action.IntegerInput;
import guthix.util.Varbit;
import guthix.util.Varp;

/**
 * Created by bart on 7/18/15.
 */
public class Varps {

	public static final int[] BIT_SIZES = new int[32];
	static {
		for (int numbits = 0, size = 2; numbits < 32; numbits++) {
			BIT_SIZES[numbits] = size - 1;
			size += size;
		}
	}

	private Player player;
	private int[] varps = new int[2000];

	public Varps(Player player) {
		this.player = player;
	}

	public void varp(int id, int v) {
		varps[id] = v;
		sync(id);
	}

	public int varp(int id) {
		return varps[id];
	}

	public void varbit(int id, int v) {
		VarbitDefinition def = player.world().definitions().get(VarbitDefinition.class, id);
		if (def != null) {
			int area = BIT_SIZES[def.endbit - def.startbit] << def.startbit;
			varps[def.varp] = (varps[def.varp] & ~area) | (area & (v << def.startbit));

			sync(def.varp);
		}
	}

	public int varbit(int id) {
		VarbitDefinition def = player.world().definitions().get(VarbitDefinition.class, id);
		if (def != null) {
			return (varps[def.varp] >> def.startbit) & BIT_SIZES[def.endbit - def.startbit];
		}

		return 0;
	}

	public void sync(int id) {
		player.write(new SetVarp(id, varps[id]));
	}

	public int[] raw() {
		return varps;
	}

	public void presave() {
		// Turn off prayers
		//Prayers.disableAllPrayers(player);
		varp(Varp.SPECIAL_ENABLED, 0);
	}

	public void syncNonzero() {
		for (int i=0; i<2000; i++) {
			if (varps[i] != 0)
				sync(i);
		}
	}

}
