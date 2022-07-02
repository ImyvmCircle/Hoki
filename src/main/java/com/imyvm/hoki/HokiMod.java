package com.imyvm.hoki;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HokiMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Hoki");

	@Override
	public void onInitialize() {
		LOGGER.info("Hoki mod initialize");
	}
}
