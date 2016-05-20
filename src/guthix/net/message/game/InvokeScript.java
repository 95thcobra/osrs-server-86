package guthix.net.message.game;

import guthix.io.RSBuffer;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public class InvokeScript implements Command {

	private int id;
	private Object[] args;
	private String types;
	private int size;

	public InvokeScript(int id, Object... args) {
		this.id = id;
		this.args = args;

		/* Calculate types */
		size = 1 + 2 + 4;
		char[] chars = new char[args.length];
		for (int i=0; i<args.length; i++) {
			chars[i] = args[i] instanceof String ? 's' : 'i';
			types += args[i] instanceof String ? args[i].toString().length() + 1 : 4;
		}
		types = new String(chars);
		size += types.length() + 1;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buf = new RSBuffer(player.channel().alloc().buffer(size));

		buf.packet(154).writeSize(RSBuffer.SizeType.SHORT);

		buf.writeString(types);
		for (int i=args.length - 1; i >= 0; i--) {
			if (args[i] instanceof String)
				buf.writeString(((String) args[i]));
			else
				buf.writeInt((int) args[i]);
		}
		buf.writeInt(id);
		return buf;
	}
}