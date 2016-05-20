package guthix.fs;

import guthix.io.RSBuffer;
import io.netty.buffer.Unpooled;

/**
 * Created by bart on 7/18/15.
 */
public class VarbitDefinition implements Definition {

	private int id;
	public int varp;
	public int endbit;
	public int startbit;

	public VarbitDefinition(int id, byte[] data) {
		this.id = id;

		if (data != null && data.length > 0)
			decode(new RSBuffer(Unpooled.wrappedBuffer(data)));
	}

	void decode(RSBuffer buffer) {
		while (true) {
			int op = buffer.readUByte();
			if (op == 0)
				break;
			decode(buffer, op);
		}
	}

	void decode(RSBuffer buffer, int code) {
		if (code == 1) {
			varp = buffer.readUShort();
			startbit = buffer.readUByte();
			endbit = buffer.readUByte();
		} else {
			throw new RuntimeException("unrecognized varbit code " + code);
		}
	}

}
