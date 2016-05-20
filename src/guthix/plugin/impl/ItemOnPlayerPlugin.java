package guthix.plugin.impl;

import guthix.model.entity.Player;
import guthix.model.item.Item;
import guthix.plugin.PluginContext;

/**
 * The plugin context for the item on player message.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class ItemOnPlayerPlugin implements PluginContext {

    /**
     * The player that the item is being used on.
     */
    private final Player player;

    /**
     * The item that is being used on the player.
     */
    private final Item item;

    /**
     * Creates a new {@link ItemOnPlayerPlugin}.
     *
     * @param player the player that the item is being used on.
     * @param item   the item that is being used on the player.
     */
    public ItemOnPlayerPlugin(Player player, Item item) {
        this.player = player;
        this.item = item;
    }

    /**
     * Gets the player that the item is being used on.
     *
     * @return the player the item is used on.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item that is being used on the player.
     *
     * @return the item being used.
     */
    public Item getItem() {
        return item;
    }
}
