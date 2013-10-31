package com.tapjoy.opt.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import com.maxmind.geoip.LookupService;
import com.tapjoy.opt.OptimizationService;
import com.tapjoy.opt.ModelController;
import com.tapjoy.opt.common.GeoIpService;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.model_assigner.AssignerManager;
import com.tapjoy.opt.model_assigner.ModelAssigner;

public class HttpOptRequestHandler extends SimpleChannelUpstreamHandler {
    private HttpRequest request;
    private StringBuilder buf = new StringBuilder();
    
	private static Logger logger = Logger.getLogger(HttpOptRequestHandler.class);
	
	private String reqid = "";
	private String algoid = "";
	private String offertype = "";
	private String udid = "";
	private String control_test = "";
	private String exp_label = "";
	private boolean backupflag = false;
	
	// parse the ip address
	private void processIP(HashMap <String, String> specs){
		if (specs == null) {
			return;
		}
		
		if(specs.containsKey("ad_view_id"))
		{
			specs.put("reqid", specs.get("ad_view_id"));
			specs.remove("ad_view_id");
		}
		
		if (specs.get("server_to_server") != null) {
			return;
		}
		
		String ip = specs.get("ip_addr");
		if (ip == null) {
			return;
		}
		
		LookupService cl = GeoIpService.getGeoIpService();
		if (cl == null) {
			return;
		}
		
		com.maxmind.geoip.Location loc = cl.getLocation(ip); 
		if (loc == null) {
			return;
		}
				
		specs.put("primaryCountry", loc.countryCode);
		specs.put("region", loc.region);
		specs.put("city", loc.city);
		specs.put("dma_code", loc.dma_code + "");
		specs.put("lat", loc.latitude + "");
		specs.put("long", loc.longitude + "");
		specs.put("postal_code", loc.postalCode);
		specs.put("area_code", loc.area_code + "");
	}
	
	// All prepprocessing logic goes here
	// Allow overwrite
	private void preprocessRequest(HashMap <String, String> specs){
		if (specs.get("primary_country") != null) {
			specs.put("primaryCountry", specs.get("primary_country"));
			specs.remove("primary_country");
		}
				
		if (specs.get("carrier_country_code") != null){
			specs.put("primaryCountry", specs.get("carrier_country_code"));
			specs.remove("carrier_country_code");
		}
		
	}

	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			long sTS = System.nanoTime();
			buf.setLength(0);
			String logpath = "";
		
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			HttpRequest request = this.request = (HttpRequest) e.getMessage();

			if (HttpHeaders.is100ContinueExpected(request)) {
				logger.warn("request is is100ContinueExpected");
				send100Continue(e);
			}

			String reqStr = request.getUri();
			
			// Deny empty requests
			if (reqStr == null || reqStr.length() < 5){		
				logger.info("\rNULL"+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"Illegal URL!");
				writeResponse(e, HttpResponseStatus.BAD_REQUEST, sTS, logpath);
				return;
			}
			
			
			// Substitute the health check with a valid check request
			boolean checkflag = false;
			if ("/healthz".equals(reqStr) || "/healthz/".equals(reqStr) || "/health".equals(reqStr) || "/health/".equals(reqStr)) {
				reqStr = OverallConfig.healthCheck;
				checkflag = true;
			}
			
			
			// Deny all other requests 404
			//if it is a web crawler with "robots.txt", we serve it with 200 but let it know that we allow you to do nothing. 
			if (! "/?".equals(reqStr.substring(0, 2)) && !"?".equals(reqStr.substring(0, 1))) {	
				if(!reqStr.endsWith("robots.txt"))
				{	
					logger.info("\rNULL"+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"404:Not Found!");
					writeResponse(e, HttpResponseStatus.NOT_FOUND, sTS, logpath);
				}
				else
				{
					SocketAddress remoteAddress = ctx.getChannel().getRemoteAddress();
					logger.info("\rNULL"+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"abnormal"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"200:Bad request from web crawler with an IP "+remoteAddress.toString());
					buf.append("User-agent: *\nDisallow: /\n");
					writeResponse(e, HttpResponseStatus.OK, sTS, logpath);
				}
				return;
			}
			
			logger.debug("REQUEST_URL:" + reqStr);
			
			// Parse the requests
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(reqStr);
			Map<String, List<String>> params = queryStringDecoder.getParameters();
			HashMap<String, String> specs = new HashMap<String, String>();
			if (!params.isEmpty()) {
				for (Entry<String, List<String>> p: params.entrySet()) {
					String key = p.getKey();
					List<String> vals = p.getValue();
					if(vals.size() == 1) {
						specs.put(key, vals.get(0));
						//logger.debug("PARAM: " + key + " = " + vals.get(0) + "\r\n");
					}
				}
			}

			processIP(specs);
		    preprocessRequest(specs);
		    		
			reqid = specs.get("reqid");  //the tracking id in URL is called ad_view_id 10-17 LJ
			
			if(reqid == null)
			{
				String testflag = specs.get("test");
				if(testflag != null && testflag.equals("true"))
					reqid = "TEST";
				else if(checkflag)
				{
					reqid = "CHECK";
				}
				//added for test LJ 09/17
				else if(specs.containsKey("monitor") && specs.containsKey("logname") && specs.get("monitor").equalsIgnoreCase("true"))  
				{
					//write a log file with processing time as denoted by url 
					reqid = "MONITOR";
					logpath += OverallConfig.MONITOR_PATH_DIR+"/"+specs.get("logname")+".log";
				}			
				else
				{
					logger.info("\rNULL"+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"400:no tracking id!");
					writeResponse(e, HttpResponseStatus.BAD_REQUEST, sTS, logpath);
					return;
				}
			}
			
		    algoid = null;
		    String cmd = specs.get("command");	   
		    
		    // High level bad requests
		    if (cmd == null) {
		    	buf.append("Invalid Request. ErrCode 4.1\n"); 	
				logger.info("\r"+reqid+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"400:no command!");
				writeResponse(e, HttpResponseStatus.BAD_REQUEST, sTS, logpath);
		    	return ;
		    }
		    
		    String platform = specs.get("platform");
		    String dtype = specs.get("device_type");	   
		    
		    if (!(platform != null && dtype != null)) {
		    	if(platform == null && dtype == null)
		    	{
		    		buf.append("Invalid Request. ErrCode 4.1\n"); 	
					logger.info("\r"+reqid+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"400:no platform info!");
		    		writeResponse(e, HttpResponseStatus.BAD_REQUEST, sTS, logpath);
		    		return;
		    	}
		    	else if(dtype != null)
		    	{
		    		if(dtype.equals("android"))
		    			specs.put("platform", "Android");
		    		else if(dtype.equals("iphone") || dtype.equals("ipad") || dtype.equals("itouch"))
		    			specs.put("platform","iOS");
		    		else
		    		{	
						logger.info("\r"+reqid+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"400:no platform info!");
		    			writeResponse(e, HttpResponseStatus.BAD_REQUEST, sTS, logpath);
			    		return;
		    		}
		    	}
		    	else
		    	{
		    		logger.info("\r"+reqid+(char)1+""+(char)1+""+(char)1+""+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+""+(char)1+""+(char)1+"400:no platform info!");
	    			writeResponse(e, HttpResponseStatus.BAD_REQUEST, sTS, logpath);
		    		return;
		    	}
		    }
		    
		    offertype = specs.get("type");
		    udid = specs.get("udid");
		    if(udid == null)
		    	udid = "";
		    int algoind = -1;
		    
		    if(offertype != null && (offertype.equals("2") || offertype.equals("6") || offertype.equals("14")))
		    {
		        if(! OverallConfig.FEATURED_MODEL_OVERRIDE.isEmpty() && (!specs.containsKey("monitor") || specs.get("monitor").equalsIgnoreCase("false")) && (!specs.containsKey("test") || specs.get("test").equalsIgnoreCase("false")))
			    {
			    	 algoid = OverallConfig.FEATURED_MODEL_OVERRIDE;
			    	 logger.warn("Featured algo overridden to"+algoid);
			    	 specs.put("algorithm", algoid);
			    	 backupflag = true;
			    }
		        else if(reqid.equals("TEST") || reqid.equals("MONITOR") || reqid.equals("CHECK"))
		        {
		        	algoid = specs.get("algorithm");
		        }
			    else
			    {
			    	ModelAssigner assgn = AssignerManager.getFeaturedRegAssigner();
			    	if(assgn != null)
			    	{
			    		algoid = assgn.assign(specs);
			    		//retrieve control_test and exp_label from specs
					    if(specs.containsKey("control_test"))
					    	control_test = specs.get("control_test");
					    if(specs.containsKey("exp_label"))
					    	exp_label = specs.get("exp_label");
			    	}
			    	else
			    		algoid = specs.get("algorithm");
			    }
		    }
		    else if(offertype != null && !offertype.equals("1"))
		    {
		    	buf.setLength(0);
	    		buf.append("{\n");
	    		buf.append("\"key\":\"0.0."+specs.get("platform")+"..."+specs.get("device_type")+".Unsupported_featured_ads_request\",\n");
	    	    buf.append("\"enabled\":\"true\",\n");
	    	    buf.append("\"offers\":\n");
	    	    buf.append("[\n");
	    	    buf.append("]\n");
	    	    buf.append("}\n");
	    	    logger.info("\r"+reqid+(char)1+algoid+(char)1+control_test+(char)1+udid+(char)1+""+(char)1+"abnormal"+(char)1+sdf.format(new Date())+(char)1+offertype+(char)1+exp_label+(char)1+"200:no offer type/context!");
	    		writeResponse(e, HttpResponseStatus.OK, sTS, logpath); //a work-around for req type other than 1,2,6,14 - Oct 7
	    		return;
		    }
		    else
		    {
			    // Model Controller can over ride it via OverAllConfig.MODEL_OVERRIDE for performance considerations, then Model Assigner
			    offertype = "1";
		    	if(! OverallConfig.MODEL_OVERRIDE.isEmpty() && (!specs.containsKey("monitor") || specs.get("monitor").equalsIgnoreCase("false")) && (!specs.containsKey("test") || specs.get("test").equalsIgnoreCase("false")))
			    {
			    	 algoid = OverallConfig.MODEL_OVERRIDE;
			    	 logger.warn("Algo overridden to"+algoid);
			    	 specs.put("algorithm", algoid);
			    }
		    	else if(reqid.equals("TEST") || reqid.equals("MONITOR") || reqid.equals("CHECK"))
			    {
			        	algoid = specs.get("algorithm");
			    }
			    else
			    {
			    	ModelAssigner assgn = AssignerManager.getRegAssigner();
			    	if(assgn != null)
			    	{
			    		algoid = assgn.assign(specs);
			    	    if(specs.containsKey("control_test"))
					    	control_test = specs.get("control_test");
					    if(specs.containsKey("exp_label"))
					    	exp_label = specs.get("exp_label");
			    	}
			    	else
			    		algoid = specs.get("algorithm");
			    }
		    }
		    
		    algoind = ModelController.getAlgoIndex(algoid);
		    
		    if (algoind >= 0) {
		    	buf.append(ModelController.getRegModel(algoind).serveRequest(cmd, specs));
		     } 
		    else {
		    	buf.append("Invalid Request. ErrCode 4.2.1\n");
		    	logger.info("\r"+reqid+(char)1+algoid+(char)1+control_test+(char)1+udid+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+offertype+(char)1+exp_label+(char)1+"422:unprocessable");
		    	writeResponse(e, HttpResponseStatus.UNPROCESSABLE_ENTITY, sTS, logpath);
		    	return ;
		    }
		    
		    // Error Happened - 500
		    if (buf.length() == 0) {
		    	buf.append("Invalid Request. ErrCode 5.1\n");
		    	logger.info("\r"+reqid+(char)1+algoid+(char)1+control_test+(char)1+udid+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+offertype+(char)1+exp_label+(char)1+"500:internal server error");
		    	writeResponse(e, HttpResponseStatus.INTERNAL_SERVER_ERROR, sTS, logpath);
		    	return ;
		    }
		    		    
		    // Invalid Request - 204/422
		    if ("Invalid Request.".equals(buf.substring(0, 16))) {
		    	if(OptimizationService.platform_whitelist.contains(specs.get("platform").toLowerCase()) || OptimizationService.platform_whitelist.contains(specs.get("device_type").toLowerCase()))
		    	{
		    		buf.setLength(0);
		    		buf.append("{\n");
		    		buf.append("\"key\":\"0.0.some_known_platform...platform.a_secure_empty_return\",\n");
		    	    buf.append("\"enabled\":\"true\",\n");
		    	    buf.append("\"offers\":\n");
		    	    buf.append("[\n");
		    	    buf.append("]\n");
		    	    buf.append("}\n");
		    	    logger.info("\r"+reqid+(char)1+algoid+(char)1+control_test+(char)1+udid+(char)1+""+(char)1+"abnormal"+(char)1+sdf.format(new Date())+(char)1+offertype+(char)1+exp_label+(char)1+"200:known platform");
		    		writeResponse(e, HttpResponseStatus.OK, sTS, logpath); //a work-around for windows phone --serve with empty offer list Sep 26
		    	}
		    	else
		    	{
		    		logger.info("\r"+reqid+(char)1+algoid+(char)1+control_test+(char)1+udid+(char)1+""+(char)1+"error"+(char)1+sdf.format(new Date())+(char)1+offertype+(char)1+exp_label+(char)1+"422:unprocessable");
		    		writeResponse(e, HttpResponseStatus.UNPROCESSABLE_ENTITY, sTS, logpath);  //changed from 400 BAD_REQUEST
		    	//trigger failover proc for empty return --- LJ
		    	}
		    	return;
		    }
		
		    // Good - 200
		    String tmpstatus = "normal";
		    if(!algoid.equals(specs.get("algorithm")))
		    {
		    	backupflag = true;
		    }
		    if(backupflag)
		    	tmpstatus = "backup";
			long tmptime = System.nanoTime() - sTS;
			if(tmptime > OverallConfig.OPTSOA_TIMEOUT_THRES*1000000)
				tmpstatus = "abnormal";
			
			String tmptag = "regular";
			if(checkflag)
				tmptag = "check";
			else if(reqid.equals("MONITOR"))
				tmptag = "monitor";
			else if(reqid.equals("TEST"))
				tmptag = "test";
			
			logger.info("\r"+reqid+(char)1+algoid+(char)1+control_test+(char)1+udid+(char)1+tmptime+(char)1+tmpstatus+(char)1+sdf.format(new Date())+(char)1+offertype+(char)1+exp_label+(char)1+"200:"+tmptag);
			writeResponse(e, HttpResponseStatus.OK, sTS, logpath);
	}

	private void writeResponse(MessageEvent e, HttpResponseStatus status, long startTS, String logpath) {
		
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpHeaders.isKeepAlive(request);

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
			// Add keep alive header as per:
			// - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		// Write the response.
		ChannelFuture future = e.getChannel().write(response);
		
		long endTS = System.nanoTime();
		
		if(logpath != null && logpath.length() > 0 )
		{		
			if(logpath.length() > 0)
			{
				//open a logfile for writing LJ
				File file = new File(logpath);
				if(file.exists()) //each launch of OptSOA, we have a new config file
				{
					file.delete();
				}

				try {
					file.createNewFile();
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);	
					bw.write(buf.toString());
					bw.write("time="+(endTS-startTS)+"\n");
					bw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.warn("reqid="+reqid+" log can't be written for monitor request "+ logpath);
					e1.printStackTrace();
				}	
			}
		}

		// Close the non-keep-alive connection after the write operation is done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private static void send100Continue(MessageEvent e) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		e.getChannel().write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
