package blizzardfenix.webasemod.client;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ClientConfig;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BaseballMod.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventSubscriber {


	@SubscribeEvent
	public static void onClientTick(final TickEvent.ClientTickEvent event) {
		if (event.side != LogicalSide.CLIENT || event.phase == Phase.END)
			return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientForgeEventSubscriber::checkThrowKey);
	}
	
	public static void checkThrowKey() {
		// If the throw key is pressed, throw what you're holding in your main hand and otherwise your off hand
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && ModKeyBindings.throwKey.isDown() && 
				ModKeyBindings.throwKey.getKey() != mc.options.keyUse.getKey() && !mc.options.keyUse.isDown()) {// Use button overrides the throw button
			// Check if enough ticks have passed since the last throw
			LocalPlayer player = mc.player;
	        if (player != null) {
				for (InteractionHand hand : InteractionHand.values()) {
					InteractionResult result;
					ItemStack item = player.getItemInHand(hand);
					if (item.getItem() instanceof BaseballBat) {
						result = item.use(mc.level, player, hand).getResult();
					} else {
						result = ItemHelperFunctions.tryThrow(mc.level, player, hand, false);
					}
					if (result.consumesAction()) {
						mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
						return;
					}
				}
	        }
		}
	}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
    	if (!ClientConfig.tooltip.get())
    		return;
    	
    	ItemStack itemstack = event.getItemStack();
    	if (itemstack != null) {
			if (itemstack.is(ItemHelperFunctions.THROWABLEITEMTAG) || itemstack.is(ItemHelperFunctions.VANILLATHROWABLETAG)) {
				event.getToolTip().add((Component.translatable("item.webasemod.throwable")).withStyle(ChatFormatting.GRAY));
			}
	    }
	}

}
