package guthix.plugin.impl;

import guthix.model.Tile;
import guthix.plugin.PluginContext;

/**
 * The plugin context for the first object click message.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class ObjectFirstClickPlugin implements PluginContext {

    /**
     * The identifier for the object that was clicked.
     */
    private final int id;

    /**
     * The position of the object that was clicked.
     */
    private final Tile position;

    /**
     * The size of the object that was clicked.
     */
   // private final int size;

    /**
     * Creates a new {@link ObjectFirstClickPlugin}.
     *
     * @param id       the identifier for the object that was clicked.
     * @param position the position of the object that was clicked.
     */
    public ObjectFirstClickPlugin(int id, Tile position/*, int size*/) {
        this.id = id;
        this.position = position;
        //this.size = size;
    }

    /**
     * Gets the identifier for the object that was clicked.
     *
     * @return the object identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the position of the object that was clicked.
     *
     * @return the position of the object.
     */
    public Tile getPosition() {
        return position;
    }

    /**
     * Gets the size of the object that was clicked.
     *
     * @return the size of the object.
     */
   /* public int getSize() {
        return size;
    }*/
}
