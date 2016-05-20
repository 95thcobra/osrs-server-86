package guthix.services.login;

import io.netty.buffer.ByteBuf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.crypto.IsaacRand;
import guthix.model.Tile;
import guthix.model.entity.Player;
import guthix.net.future.ClosingChannelFuture;
import guthix.net.message.LoginRequestMessage;
import guthix.services.serializers.PlayerLoadResult;

import java.util.Arrays;

/**
 * Created by Bart on 8/1/2015.
 */
public class LoginWorker implements Runnable {

	private static final Logger logger = LogManager.getLogger(LoginWorker.class);

	private LoginService service;

	public LoginWorker(LoginService service) {
		this.service = service;
	}

	@Override
	public void run() {
		while (true) {
			try {
				LoginRequestMessage message = service.messages().take();

				if (message == null) {
					continue;
				}

				logger.info("Attempting to process login request for {}.", message.username());

				// Prepare random gens
				int[] seed = message.isaacSeed();
				IsaacRand inrand = new IsaacRand(seed);
				IsaacRand outrand = new IsaacRand(Arrays.stream(seed).map(i -> i + 50).toArray());

				Tile startTile = new Tile(3094, 3503);
				Player player = new Player(message.channel(), message.username(), service.server().world(), startTile, inrand, outrand);

				boolean success = service.serializer().loadPlayer(player, null, message.password(), result -> {
					// Convert pipeline
					service.server().initializer().initForGame(message.channel());

					// Apply migrations, or panic out if it failed.
					if (!service.server().migrations().process(player))
						result = PlayerLoadResult.ERROR_LOADING;

					// Was the result faulty?
					if (result != PlayerLoadResult.OK) {
						ByteBuf resp = message.channel().alloc().buffer(1).writeByte(result.code());
						message.channel().writeAndFlush(resp).addListener(new ClosingChannelFuture());
						return;
					}

					// Pass this bit of logic to the server processor
					service.server().processor().submitLogic(() -> {
						// Check if we aren't logged in yet :doge:
						if (service.server().world().playerByName(player.name()).isPresent()) {
							ByteBuf resp = message.channel().alloc().buffer(1).writeByte(PlayerLoadResult.ALREADY_ONLINE.code());
							message.channel().writeAndFlush(resp).addListener(new ClosingChannelFuture());
							return;
						}

						// See if we may be registered (world full??)
						if (!service.server().world().registerPlayer(player)) {
							ByteBuf resp = message.channel().alloc().buffer(1).writeByte(PlayerLoadResult.WORLD_FULL.code());
							message.channel().writeAndFlush(resp).addListener(new ClosingChannelFuture());
							return;
						}

						ByteBuf temp = message.channel().alloc().buffer(11);
						temp.writeByte(2);

						temp.writeByte(0); // Something trigger bla?
						temp.writeInt(0); // idk this is 4 bytes of isaac ciphered keys

						temp.writeByte(player.privilege() == null ? 0 : player.privilege().ordinal()); // Rights
						temp.writeBoolean(true); // Member
						temp.writeShort(player.index()); // Index
						temp.writeBoolean(true); // Member

						message.channel().writeAndFlush(temp);

						LoginService.complete(player, service.server(), message);
					});
				});

				// Did everything work nicely?
				if (!success) {
					service.enqueue(message); // Let us retry soon :-)
					Thread.sleep(100); // Avoid overloading the login service. Be gentle.
				}
			} catch (Exception e) {
				logger.error("Error processing login worker job!", e);
			}
		}
	}

}
