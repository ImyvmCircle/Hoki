package com.imyvm.hoki;

import com.imyvm.hoki.util.PlayerUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HokiMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Hoki");

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(PlayerUtil::initialize);

		LOGGER.info("Hoki mod initialize");
	}
}
