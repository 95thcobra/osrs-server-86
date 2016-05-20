package guthix.model.entity.player;

import java.util.HashMap;
import java.util.Map;

import guthix.model.entity.Player;
import guthix.net.message.game.CloseInterface;
import guthix.net.message.game.InterfaceSettings;
import guthix.net.message.game.OpenInterface;
import guthix.net.message.game.SetRootPane;
import guthix.util.SettingsBuilder;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class Interfaces {

    public static final int PANE_FIXED = 548;
    public static final int PANE_RESIZABLE = 161;
    // 164 horizontal bar

    public static final int MAIN_COMPONENT_FIXED = 8;
    public static final int MAIN_COMPONENT_RESIZABLE = 4;

    private Player player;
    private Map<Integer, Integer> visible = new HashMap<>();
    private int activeRoot;
    private boolean resizable;

    public Interfaces(Player player) {
        this.player = player;
    }

    public void resizable(boolean b) {
        resizable = b;
    }

    public boolean resizable() {
        return resizable;
    }

    public void send() {
        if (resizable)
            sendResizable();
        else
            sendFixed();
    }

    public void closeAll() {
        // No questions, this is because of concurrent modification on the visible map.
        Integer[] keys = visible.keySet().stream().toArray(Integer[]::new);
        for (int k : keys) {
            close(k >> 16, k & 0xFFFF);
        }
    }

    public int whereIs(int id) {
        return visible.entrySet().stream().filter(e -> e.getValue() == id).map(Map.Entry::getKey).findAny().orElse(-1);
    }

    public void sendFixed() {
        sendRoot(PANE_FIXED);

        // Send the interfaces
        send(162, PANE_FIXED, 20, true); // chatbox
        send(163, PANE_FIXED, 16, true);
        send(160, PANE_FIXED, 9, true); // orbs
        send(122, PANE_FIXED, 15, true);
        send(378, PANE_FIXED, 28, false); // prelogin interface
        send(50, PANE_FIXED, 27, false);

        send(320, PANE_FIXED, 63, true); // skills
        send(274, PANE_FIXED, 64, true); // quest
        send(149, PANE_FIXED, 65, true);
        send(387, PANE_FIXED, 66, true); // equipment
        send(271, PANE_FIXED, 67, true); // prayer
        send(218, PANE_FIXED, 68, true); // spellbook
        send(429, PANE_FIXED, 70, true); // friends list
        send(432, PANE_FIXED, 71, true); // ignore list
        send(182, PANE_FIXED, 72, true); // logout/world switcher
        send(261, PANE_FIXED, 73, true); // settings
        send(216, PANE_FIXED, 74, true); // emotes
        send(239, PANE_FIXED, 75, true); // music
        send(589, PANE_FIXED, 69, true); // clan chat
        send(593, PANE_FIXED, 62, true); // attack styles

        setInterfaceSettings();
    }

    public void sendResizable() {
        sendRoot(PANE_RESIZABLE);

        send(162, PANE_RESIZABLE, 21, true); // chatbox
        send(160, PANE_RESIZABLE, 20, true); // orbs

        // tabs
        send(320, PANE_RESIZABLE, 61, true); // skills
        send(274, PANE_RESIZABLE, 62, true); // quest
        send(149, PANE_RESIZABLE, 63, true);
        send(387, PANE_RESIZABLE, 64, true); // equipment
        send(271, PANE_RESIZABLE, 65, true); // prayer
        send(218, PANE_RESIZABLE, 66, true); // spellbook
        send(429, PANE_RESIZABLE, 68, true); // friends list
        send(432, PANE_RESIZABLE, 69, true); // ignore list
        send(182, PANE_RESIZABLE, 70, true); // logout/world switcher
        send(261, PANE_RESIZABLE, 71, true); // settings
        send(216, PANE_RESIZABLE, 72, true); // emotes
        send(239, PANE_RESIZABLE, 73, true); // music
        send(589, PANE_RESIZABLE, 67, true); // clan chat
        send(593, PANE_RESIZABLE, 60, true); // attack styles
        setInterfaceSettings();
    }

    private void setInterfaceSettings() {
        // Unlock music buttons
        player.write(new InterfaceSettings(239, 1, 0, 600, new SettingsBuilder().option(0)));

        // Emote buttons
        player.write(new InterfaceSettings(216, 1, 0, 38, new SettingsBuilder().option(0)));

        // Hotkeys TODO proper!
        player.varps().varp(1224, 102793221);
        player.varps().varp(1225, 379887844);
        player.varps().varp(1226, 12);
    }

    public void setting(int i, int c, int start, int end, SettingsBuilder b) {
        player.write(new InterfaceSettings(i, c, start, end, b));
    }

    public void setting(int i, int c, int start, int end, int s) {
        player.write(new InterfaceSettings(i, c, start, end, s));
    }

    public void sendMain(int id) {
        sendMain(id, false);
    }

    public void sendMain(int id, boolean clickthrough) {
        send(id, activeRoot, mainComponent(), clickthrough);
    }

    public void send(int id, int target, int targetChild, boolean clickthrough) {
        player.write(new OpenInterface(id, target, targetChild, clickthrough));
        visible.put((target << 16) | targetChild, id);
    }

    public void closeMain() {
        close(activeRoot, mainComponent());
    }

    public void close(int target, int targetChild) {
        close((target << 16) | targetChild);
    }

    public void close(int hash) {
        player.write(new CloseInterface(hash));
        visible.remove(hash);
    }

    public int closeById(int id) {
        int at = whereIs(id);
        if (at != -1)
            close(at);
        return at;
    }

    public boolean visible(int id) {
        return activeRoot == id || visible.containsValue(id);
    }

    public boolean visible(int root, int sub) {
        return visible.containsKey(root << 16 | sub);
    }

    public void sendRoot(int id) {
        player.write(new SetRootPane(id));

        activeRoot = id;
    }

    public int activeRoot() {
        return activeRoot;
    }

    public int mainComponent() {
        return resizable ? 9 : 18;
    }
}
