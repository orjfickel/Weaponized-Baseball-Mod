package blizzardfenix.webasemod.client;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BaseballMod.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyEventHandler {
	@SubscribeEvent
	public static void onKeyPress(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;
		onInput(mc, event.getKey(), event.getAction());
	}

	@SubscribeEvent
	public static void onMouseClick(InputEvent.MouseButton event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;
		onInput(mc, event.getButton(), event.getAction());
	}

	private static void onInput(Minecraft mc, int key, int action) {
		if (mc.screen == null) {// If no UI screen is open
			// If the throw toggle key is pressed and the player holds a bat in their main hand, toggle throwing balls held in the off-hand up
			while (ModKeyBindings.throwUpToggleKey.consumeClick()){
				if (mc.player.getMainHandItem().getItem() instanceof BaseballBat)
					Settings.throwUp = !Settings.throwUp;
			}
		}
	}
}
