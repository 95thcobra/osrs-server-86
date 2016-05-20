package guthix.services.serializers;

import com.typesafe.config.Config;

import guthix.GameServer;
import guthix.model.entity.Player;
import guthix.model.uid.UIDProvider;
import guthix.services.Service;
import guthix.util.Tuple;

import java.util.function.Consumer;

/**
 * Created by Bart on 4-3-2015.
 *
 * Abstraction for encoding and decoding different methods of player data.
 */
public abstract class PlayerSerializer implements Service {

	protected UIDProvider uidProvider;

	public PlayerSerializer(UIDProvider provider) {
		uidProvider = provider;
	}

	public abstract boolean loadPlayer(Player player, Object uid, String password, Consumer<PlayerLoadResult> fn);

	public abstract void savePlayer(Player player);

	@Override
	public void setup(GameServer server, Config serviceConfig) {
		// Implementation varies per serializer
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

}
