package guthix.migration;

import guthix.model.entity.Player;

/**
 * Created by Bart on 8/1/2015.
 *
 * <p>A migration is a simple change in a player's data that will be applied when the player with this migration still
 * pending logs in. The migration state is kept track of in the player data and should increase by one each time you
 * add a new migration. This theoretically allows you to make 2147 million migrations before you'll run in trouble :-).</p>
 *
 * <p>Migrations are ran from lowest ID to highest ID, and explicitly use that order. This means that a migration with
 * ID 3 can safely depend on changes made by migration with ID 2.</p>
 *
 * <p>Examples of migrations can be items that need removal from the game (think holiday events) or teleporting players
 * out of areas that should no longer be entered. The possibilities are endless, and this system should ease making
 * changes to characters without running either expensive database queries or modifying data files one by one.</p>
 *
 * <p>A downside to this system is that the number of classes in this package may very quickly increase as you
 * make new migrations. A possible solution to this needs to yet be added.</p>
 */
public interface Migration {

	/**
	 * <p>Gets the ID of this migration. Migrations have an ID sequence which lets you ensure migrations always process
	 * in incremental (and most likely chronological depending on if you use it properly) order.</p>
	 *
	 * <p>A migration ID should start at 1, and should increment by one each time you add a new migration. You, in theory,
	 * CAN use the epoch timestamp in seconds (not in ms) as the ID but it is cleaner to use incremental IDs as it
	 * easens looking up which migration was last applied, for example.</p>
	 *
	 * @return The ID of this migration which is used in determining if a player needs to have this one applied.
	 */
	public int id();

	/**
	 * Attempts to apply this migration to a player.
	 *
	 * @param player The player to apply this migration to.
	 * @return <code>true</code> if this migration succeeded, <code>false</code> if shit hit the fan. If that happens,
	 * the player is blocked from logging in and is shown a login message saying that his or her character is damaged.
	 */
	public boolean apply(Player player);

}
