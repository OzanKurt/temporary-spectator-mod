package com.ozankurt;

import net.fabricmc.api.ModInitializer;

public class TemporarySpectatorMod implements ModInitializer {

	private static TemporarySpectatorMod instance = new TemporarySpectatorMod();

	public TemporarySpectatorMod() {
		instance = this;
	}

	@Override
	public void onInitialize() {
		System.out.println("Temporary Spectator Mod: onInitialize");
	}

	public static TemporarySpectatorMod getInstance() {
		return instance;
	}
}
