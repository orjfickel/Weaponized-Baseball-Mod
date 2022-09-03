package blizzardfenix.webasemod.client;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ClientConfig;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
			ClientPlayerEntity player = mc.player;
	        if (player != null) {
				for (Hand hand : Hand.values()) {
					ActionResultType result;
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
    		Item item = itemstack.getItem();
			if (item.is(ItemHelperFunctions.THROWABLEITEMTAG()) || item.is(ItemHelperFunctions.VANILLATHROWABLETAG())) {
				event.getToolTip().add((new TranslationTextComponent("item.webasemod.throwable")).withStyle(TextFormatting.GRAY));
			}
	    }
	}

}
