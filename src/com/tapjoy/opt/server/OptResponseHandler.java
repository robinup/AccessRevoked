package com.tapjoy.opt.server;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;

public class OptResponseHandler extends SimpleChannelDownstreamHandler {
	
	private static Logger logger = Logger.getLogger(OptResponseHandler.class);

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		logger.debug("Response --  an channel event: " +  e.getClass() + "; ");
		
		super.handleDownstream(ctx, e);

	}

	
	// TODO -- override the exception processing method here
}
