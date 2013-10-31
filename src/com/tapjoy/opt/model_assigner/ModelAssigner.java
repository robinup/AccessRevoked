package com.tapjoy.opt.model_assigner;

import java.util.HashMap;

public interface ModelAssigner {
	
	public abstract String assign(HashMap<String, String> specs);
}