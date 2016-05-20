package guthix.net.message.game.action;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.model.entity.player.EquipSlot;
import guthix.model.item.Item;
import guthix.net.message.game.InterfaceText;
import guthix.net.message.game.PacketInfo;
import guthix.util.CombatFormula;
import guthix.util.EquipmentInfo;
import guthix.util.Varp;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 5-2-2015.
 */
@PacketInfo(size = 8)
public class ItemAction2 extends ItemAction {

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		slot = buf.readUShortA();
		hash = buf.readIntV1();
		item = buf.readULEShort();
	}

	@Override
	protected int option() {
		return 1;
	}

	@Override
	public void process(Player player) {
		super.process(player);

		// Not possible when locked
		if (player.locked() || player.dead())
			return;

		// Stop player actions
		player.stopActions(false);

		Item item = player.inventory().get(slot);
		if (item == null || item.id() != this.item) // Avoid reclicking
			return;

		EquipmentInfo info = player.world().equipmentInfo();
		int targetSlot = info.slotFor(item.id());
		int type = info.typeFor(item.id());
		if (targetSlot == -1) // Cannot wear :-(
			return;

		// Begin by setting the used item to null. This is to make it like osrs. Failing is scary but no worries!
		player.inventory().set(slot, player.equipment().get(targetSlot));

		// If type is 5 it is a two-handed weapon
		if (type == 5 && player.equipment().hasAt(EquipSlot.SHIELD)) {
			if (player.inventory().add(player.equipment().get(EquipSlot.SHIELD), false).failed()) {
				player.message("You don't have enough free space to do that.");
				player.inventory().set(slot, item);
				return;
			}
			player.equipment().set(EquipSlot.SHIELD, null);
		}

		// If it is a shield and we have a 2h weapon equipped, unequip it
		if (targetSlot == EquipSlot.SHIELD && player.equipment().hasAt(EquipSlot.WEAPON)) {
			if (info.typeFor(player.equipment().get(EquipSlot.WEAPON).id()) == 5) { // Is this indeed a 2h weapon?
				if (player.inventory().add(player.equipment().get(EquipSlot.WEAPON), false).failed()) {
					player.message("You don't have enough free space to do that.");
					player.inventory().set(slot, item);
					return;
				}
				player.equipment().set(EquipSlot.WEAPON, null);
			}
		}

		// Weapons interrupt special attack
		if (targetSlot == EquipSlot.WEAPON) {
			player.varps().varp(Varp.SPECIAL_ENABLED, 0);
		}

		// Finally, equip the item we had in mind.
		player.equipment().set(targetSlot, item);
		refreshEquipStats(player);
	}

	public void refreshEquipStats(Player p) {
		EquipmentInfo.Bonuses playerBonuses = CombatFormula.totalBonuses(p, p.world().equipmentInfo());

		p.write(new InterfaceText(84, 23, "Stab: " + format(playerBonuses.stab)));
		p.write(new InterfaceText(84, 24, "Slash: " + format(playerBonuses.slash)));
		p.write(new InterfaceText(84, 25, "Crush: " + format(playerBonuses.crush)));
		p.write(new InterfaceText(84, 26, "Magic: " + format(playerBonuses.mage)));
		p.write(new InterfaceText(84, 27, "Range: " + format(playerBonuses.range)));

		p.write(new InterfaceText(84, 29, "Stab: " + format(playerBonuses.stabdef)));
		p.write(new InterfaceText(84, 30, "Slash: " + format(playerBonuses.slashdef)));
		p.write(new InterfaceText(84, 31, "Crush: " + format(playerBonuses.crushdef)));
		p.write(new InterfaceText(84, 32, "Magic: " + format(playerBonuses.magedef)));
		p.write(new InterfaceText(84, 33, "Range: " + format(playerBonuses.rangedef)));

		p.write(new InterfaceText(84, 35, "Melee strength: " + format(playerBonuses.str)));
		p.write(new InterfaceText(84, 36, "Ranged strength: " + format(playerBonuses.rangestr)));
		p.write(new InterfaceText(84, 37, "Magic damage: " + format(playerBonuses.magestr) + "%"));
		p.write(new InterfaceText(84, 38, "Prayer: " + format(playerBonuses.pray)));

		p.write(new InterfaceText(84, 40, "Undead: 0%"));
		p.write(new InterfaceText(84, 41, "Slayer: 0%"));
	}

	public String format(int bonus) {
		String prefix;

		if (String.valueOf(bonus).startsWith("-") || bonus == 0)
			prefix = "";
		else
			prefix = "+";

		return prefix + String.valueOf(bonus);
	}
}