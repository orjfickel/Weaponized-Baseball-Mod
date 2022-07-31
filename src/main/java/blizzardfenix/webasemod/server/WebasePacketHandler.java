package blizzardfenix.webasemod.server;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// CURRENTLY NOT USED
public class WebasePacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
	    new ResourceLocation(BaseballMod.MODID, "main"),
	    () -> PROTOCOL_VERSION,
	    PROTOCOL_VERSION::equals,
	    PROTOCOL_VERSION::equals
	);
	
	public static final byte SEED_MESSAGE_ID = 22;
	
	public static void init() {
		INSTANCE.registerMessage(0, WebaseMessage.class, WebaseMessage::encode, WebaseMessage::decode, WebaseMessage::handle);
	}
}
