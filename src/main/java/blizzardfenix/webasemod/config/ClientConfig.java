package blizzardfenix.webasemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	
	static {
		BUILDER.push("Client config for the Weaponised Baseball Mod");
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
