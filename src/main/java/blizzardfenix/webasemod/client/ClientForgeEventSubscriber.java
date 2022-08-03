package blizzardfenix.webasemod.client;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.server.ModEventSubscriber;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BaseballMod.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventSubscriber {

	static int throwDelay = 0;
	public static Timer timer;

	@SubscribeEvent
	public static void onClientTick(final TickEvent.ClientTickEvent event) {
		if (event.side != LogicalSide.CLIENT || event.phase == Phase.END)
			return;
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientForgeEventSubscriber::checkThrowKey);
	}
	
	public static void checkThrowKey() {
		// If the throw key is pressed, throw what you're holding in your main hand and otherwise your off hand
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && ModKeyBindings.throwKey.isDown() && 
				ModKeyBindings.throwKey.getKey() != mc.options.keyUse.getKey() && !mc.options.keyUse.isDown()) {// Use button overrides the throw button
			// Check if enough ticks have passed since the last throw
	        int ticks = timer.advanceTime(Util.getMillis());
			LocalPlayer player = mc.player;
	        if (throwDelay <= ticks && player != null) {
	        	throwDelay = 0;
				for (InteractionHand hand : InteractionHand.values()) {
					InteractionResult result = HelperFunctions.tryThrow(mc.level, player, hand, player.getDeltaMovement(), Settings.throwUp);							
					if (result.consumesAction()) {
						throwDelay = 4;
						mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
						
						// If the throw was successful, tell the server to perform the throw as well
						WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand,player.getDeltaMovement(), Settings.throwUp));
						return;
					}
				}
	        } else {
	        	throwDelay -= ticks;
	        }
		}
	}
}
