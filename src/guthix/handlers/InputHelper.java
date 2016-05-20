package guthix.handlers;

import guthix.model.entity.Player;
import guthix.net.message.game.InvokeScript;

/**
 * Created by Tom on 11/15/2015.
 */
public class InputHelper {

    public static abstract class Input<T> {
        public abstract void execute(Player player, T value);
    }

    public static abstract class NumericalInput extends Input<Integer> {
    }

    public static abstract class AlphaNumericalInput extends Input<String> {
    }

    private final Player player;
    private Input input;

    public InputHelper(Player player) {
        this.player = player;
    }

    public void provideNumerical(NumericalInput input) {
        player.write(new InvokeScript(108, new Object[]{"Enter Amount:"}));
        this.input = input;
    }

    public void provideAlphaNumerical(String title, AlphaNumericalInput input) {
        player.write(new InvokeScript(110, new Object[]{title}));
        this.input = input;
    }

    public Input input() {
        return input;
    }

}
