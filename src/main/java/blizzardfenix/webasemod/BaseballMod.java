package blizzardfenix.webasemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.config.ClientConfig;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModItems;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import net.minecraft.client.Timer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BaseballMod.MODID)
public class BaseballMod
{
    public static final String MODID = "webasemod";
    public static final Logger LOGGER = LogManager.getLogger();
    	
	public BaseballMod() {
        // Register the setup method for modloading
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::doClientStuff);

		ModItems.ITEMS.register(modEventBus);
		ModEntityTypes.ENTITY_TYPES.register(modEventBus);
		
		ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.SPEC, "webasemod-client.toml");
		ModLoadingContext.get().registerConfig(Type.SERVER, ServerConfig.SPEC, "webasemod-server.toml");
    }

    private void setup(final FMLCommonSetupEvent event)
    {
		WebasePacketHandler.init();
    }

    private void doClientStuff(final FMLClientSetupEvent event) 
    {
    	ItemHelperFunctions.timer = new Timer(20.0F, 0L);
    }
}
