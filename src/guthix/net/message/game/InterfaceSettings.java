package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;
import guthix.util.SettingsBuilder;

/**
 * Created by Bart on 8/11/2015.
 */
public class InterfaceSettings implements Command {

	private int hash;
	private int from;
	private int to;
	private int setting;

	public InterfaceSettings(int target, int targetChild, int from, int to, SettingsBuilder setting) {
		this(target, targetChild, from, to, setting.build());
	}

	public InterfaceSettings(int target, int targetChild, int from, int to, int setting) {
		hash = (target << 16) | targetChild;
		this.from = from;
		this.to = to;
		this.setting = setting;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(13));
		buffer.packet(159);

		buffer.writeLEShort(to);
		buffer.writeIntV1(hash);
		buffer.writeLEInt(setting);
		buffer.writeShort(from);

		return buffer;
	}

}
