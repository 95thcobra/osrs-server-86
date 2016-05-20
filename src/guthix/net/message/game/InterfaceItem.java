package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.model.item.Item;

/**
 * @author William Talleur <talleurw@gmail.com>
 * @date November 01, 2015
 */
public class InterfaceItem implements Command {

    private final int bitpacked, size, itemId;

    public InterfaceItem(int target, int targetChild, int size, int itemId) {
        this.bitpacked = (target << 16) | targetChild;
        this.size = size;
        this.itemId = itemId;
    }

    @Override
    public RSBuffer encode(Player player) {
        RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(11));
        buffer.packet(51);

        buffer.writeInt(bitpacked);
        buffer.writeShortA(itemId);
        buffer.writeLEInt(size);
        return buffer;
    }
}