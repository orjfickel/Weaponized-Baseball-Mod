package blizzardfenix.webasemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> tooltip;
	
	static {
		BUILDER.push("Client config for the Weaponized Baseball Mod");

		tooltip = BUILDER.comment("Show which items are throwable in the tooltip. True by default.").define("Toggle Tooltip", true);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
