package blizzardfenix.webasemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> sprite_fix;
	
	static {
		BUILDER.push("Client config for the Bouncy Throwables Mod");

		sprite_fix = BUILDER.comment("Override the throwable item sprite renderer to fix the sprite centering bug. True by default.").define("Sprite Fix", true);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
