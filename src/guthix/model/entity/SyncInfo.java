package guthix.model.entity;

import guthix.io.RSBuffer;
import guthix.model.ChatMessage;
import guthix.model.Entity;
import guthix.model.Tile;
import guthix.model.entity.Player;
import io.netty.buffer.Unpooled;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public abstract class SyncInfo {

	protected byte[] animationSet = new byte[3];
	protected byte[] graphicSet = new byte[6];
	protected byte[] faceEntitySet = new byte[2];
	protected byte[] hitSet = new byte[5];
	protected byte[] hitSet2 = new byte[5];
	protected byte[] facetile = new byte[4];
	protected byte[] forcemove = new byte[9];

	protected int calculatedFlag;
	protected int primaryStep = -1;
	protected int secondaryStep = -1;
	protected boolean teleported = true;

	protected Entity entity;

	public SyncInfo(Entity entity) {
		this.entity = entity;
	}

	public boolean dirty() {
		return calculatedFlag != 0 || primaryStep != -1 || teleported;
	}

	public void addFlag(int flag) {
		calculatedFlag |= flag;
	}

	public int calculatedFlag() {
		return calculatedFlag;
	}

	public boolean hasFlag(int flag) {
		return (calculatedFlag & flag) != 0;
	}

	public void step(int primary, int secondary) {
		primaryStep = primary;
		secondaryStep = secondary;
	}

	public int primaryStep() {
		return primaryStep;
	}

	public int secondaryStep() {
		return secondaryStep;
	}

	public void teleported(boolean b) {
		teleported = b;
	}

	public boolean teleported() {
		return teleported;
	}

	public abstract void animation(int id, int delay);

	public abstract void graphic(int id, int height, int delay);

	public abstract void faceEntity(Entity e);

	public abstract void hit(int type, int value);

	public abstract void facetile(Tile tile);

	public byte[] animationSet() {
		return animationSet;
	}

	public byte[] graphicSet() {
		return graphicSet;
	}

	public byte[] faceEntitySet() {
		return faceEntitySet;
	}

	public byte[] hitSet() {
		return hitSet;
	}

	public byte[] hitSet2() {
		return hitSet2;
	}

	public byte[] faceTileSet() {
		return facetile;
	}

	public byte[] forceMoveSet() {
		return forcemove;
	}

	public void clear() {
		calculatedFlag = 0;
		primaryStep = -1;
		secondaryStep = -1;
		teleported = false;
	}

	public void clearMovement() {
		primaryStep = secondaryStep = -1;
	}

}
