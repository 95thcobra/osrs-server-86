package guthix.services.intercom;

import guthix.model.entity.Player;
import guthix.services.Service;

/**
 * Created by Bart on 12-3-2015.
 *
 * A service responsible for providing the private messaging actions as well
 * as online and offline statuses. Defaults to the single world approach.
 */
public interface PmService extends Service {

	public void onUserOnline(Player player);

	public void onUserOffline(Player player);

	public void privateMessageDispatched(Player from, String target, String message);

}
