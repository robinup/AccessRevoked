package com.tapjoy.opt.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.tapjoy.opt.config.OverallConfig;


public class ServiceEngine {
	
	private static ServiceEngine servEng;
	private static Logger logger = Logger.getLogger(ServiceEngine.class);
	

	private ServiceEngine() {
		
	}
	
	
	public static ServiceEngine getInstance(){
		if (servEng == null) {
			synchronized(ServiceEngine.class){
				if (servEng == null){
					servEng = new ServiceEngine();
					servEng.initilize();
				}
			}
		}
		
		return servEng;
	}

	 
	private void initilize(){
		
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		
		
		// Use this when expecting less than 1K concurrent connections
		//ChannelFactory factory = new OioServerSocketChannelFactory();

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		
		//bootstrap.setPipeline(pipeline);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();
				/* TCP version
				pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(1000, true,  Delimiters.lineDelimiter()));
				pipeline.addLast("stringDecoder",  new StringDecoder(CharsetUtil.UTF_8));
				pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
				pipeline.addLast("RequestHandler", new OptRequestHandler());
				pipeline.addLast("ResponseHandler", new OptResponseHandler());
				*/
				
				/* http version */
				pipeline.addLast("decoder", new HttpRequestDecoder());
				// Uncomment the following line if you don't want to handle HttpChunks.
				//pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				// Remove the following line if you don't want automatic content compression.
				pipeline.addLast("deflater", new HttpContentCompressor());
				pipeline.addLast("handler", new HttpOptRequestHandler());
				
				return pipeline;
			}
		});
		

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		bootstrap.bind(new InetSocketAddress(OverallConfig.OPTSOA_PORT));
		logger.info("Server is up, ready to serve requests");
		System.out.println("Server is up, ready to serve requests");
	}
	

	// TODO - Shutdown hook for house keeping
	public void shutDown(){
		logger.info("Shutting down main server");
	}
	
}
