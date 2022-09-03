package blizzardfenix.webasemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> sprite_fix;
	public static final ForgeConfigSpec.ConfigValue<Boolean> tooltip;
	
	static {
		BUILDER.push("Client config for the Weaponized Baseball Mod");

        sprite_fix = BUILDER.comment("Override the throwable item sprite renderer to fix the sprite centering bug. True by default.").define("Toggle Sprite Fix", true);
		tooltip = BUILDER.comment("Show which items are throwable in the tooltip. True by default.").define("Toggle Tooltip", true);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
