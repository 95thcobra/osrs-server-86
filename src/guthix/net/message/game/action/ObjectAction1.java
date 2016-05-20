package guthix.net.message.game.action;

import guthix.event.Event;
import guthix.event.EventContainer;
import guthix.io.RSBuffer;
import guthix.model.AttributeKey;
import guthix.model.ForceMovement;
import guthix.model.Tile;
import guthix.model.entity.PathQueue;
import guthix.model.entity.Player;
import guthix.model.entity.player.Privilege;
import guthix.model.entity.player.Skills;
import guthix.model.map.MapObj;
import guthix.net.message.game.Action;
import guthix.net.message.game.PacketInfo;
import guthix.plugin.impl.LoginPlugin;
import guthix.plugin.impl.ObjectFirstClickPlugin;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Bart on 8/23/2015.
 * Modified by Simon on 4/3/2016.
 */
@PacketInfo(size = 7)
public class ObjectAction1 implements Action {

    private int id;
    private int x;
    private int z;
    private boolean run;

    @Override
    public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
        id = buf.readUShortA();
        run = buf.readByteS() == 1;
        z = buf.readUShort();
        x = buf.readUShortA();
    }

    @Override
    public void process(Player player) {
        MapObj obj = player.world().objById(id, x, z, player.tile().level);

        if (obj == null)
            return;

        if ((boolean) player.attrib(AttributeKey.DEBUG, false)) {
            player.message("Interacting with object %d at [%d, %d]", id, x, z);
        }

        if (!player.locked() && !player.dead()) {
            player.stopActions(true);
            player.putattrib(AttributeKey.INTERACTION_OBJECT, obj);
            player.putattrib(AttributeKey.INTERACTION_OPTION, 1);

            // player.world().server().scriptExecutor().executeScript(player, ObjectInteraction.script);

            // Execute groovy plugin
            // player.world().getPluginHandler().execute(player, ObjectFirstClickPlugin.class, new ObjectFirstClickPlugin(id, new Tile(x, z)));

            player.walkTo(obj, PathQueue.StepType.REGULAR);
            player.faceObj(obj);

            PathQueue.Step step = player.pathQueue().peekLast();

            Tile lastTile;
            if (step == null)
                lastTile = player.tile();
            else
                lastTile = player.pathQueue().peekLast().toTile();

            player.world().getEventHandler().addEvent(player, 1, new Event() {
                @Override
                public void execute(EventContainer container) {
                    if (player.tile().equals(lastTile)) {
                        container.stop();
                    }
                }

                @Override
                public void stop() {
                    handleObject(player, obj);
                }
            });
        }
    }

    private void handleObject(Player player, MapObj obj) {
        switch (id) {
            case 6817: // Prayer altar
                player.skills().restorePrayer();
                player.animate(645);
                player.message("You have recharged your prayer.");
                break;
            case 6552: // Spellbook altar
                //player.varps().varbit(4070, 0);
                //player.varps().varbit(4070, 1);
                //player.varps().varbit(4070, 2);

                // player.animate(645);

                player.message("TODO");
                //TODO
                //need to sendchatboxinterface and find interface for dialogue
                break;
            case 10084: // Draynor wall climb
                handleDraynorWallClimb(player);
                break;
            case 23271: // Wilderness ditch
                handleWildernessDitch(player);
                break;

        }

    }

    private void handleWildernessDitch(Player player) {
        boolean below = player.tile().z <= 3520;
        int targetY = (below ? 3523 : 3520);

        player.event(new Event() {
            int count = 0;

            @Override
            public void execute(EventContainer container) {
                switch (count) {
                    case 0:
                        player.lock();
                        int x = player.tile().x;
                        player.pathQueue().step(x, targetY);
                        player.faceTile(new Tile(x, targetY));
                        player.animate(2586);
                        player.forceMove(new ForceMovement(0, 1, 25, 30));
                        break;
                    case 1:
                        player.animate(2588);
                        break;
                    case 2:
                        x = player.tile().x;
                        player.pathQueue().step(x, targetY);
                        player.forceMove(new ForceMovement(0, 1, 17, 26));
                        break;
                    default:
                        container.stop();
                        break;
                }
                count++;
            }

            @Override
            public void stop() {
                player.animate(-1);
                player.unlock();
            }
        });
    }

    /**
     * TODO: Fix concurrent modification exception.
     * @param player
     */
    private void handleDraynorWallClimb(Player player) {
        // Is the player on the correct side of the wall?
        if (player.tile().z <= 3255)
            return;

        // Start event
        player.event(new Event() {
            int count = 0;

            @Override
            public void execute(EventContainer container) {
                switch (count) {
                    case 0:
                        player.lock();
                        player.pathQueue().step(3088, 3256);
                        player.animate(2583, 20);
                        player.forceMove(new ForceMovement(0, 1, 25, 30));
                        break;
                    case 1:
                        player.animate(2585);
                        break;
                    case 2:
                        player.pathQueue().step(3088, 3255);
                        player.forceMove(new ForceMovement(0, 1, 17, 26));
                        break;
                    case 3:
                        player.skills().addXp(Skills.AGILITY, 10.0);
                        player.unlock();
                        container.stop();
                        break;
                }
                count++;
            }
        });
    }

}
