package guthix.net.future;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * A {@link io.netty.channel.ChannelFutureListener} which automatically closes the channel on completion.
 *
 * @author Bart Pelle
 */
public class ClosingChannelFuture implements ChannelFutureListener {

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		future.channel().close();
	}

}
