package blizzardfenix.webasemod.client;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ClientConfig;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
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
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = BaseballMod.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventSubscriber {

	static int throwDelay = 0;
	public static Timer timer;

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
	        int ticks = timer.advanceTime(Util.getMillis());
			ClientPlayerEntity player = mc.player;
	        if (throwDelay <= ticks && player != null) {
	        	throwDelay = 0;
				for (Hand hand : Hand.values()) {
					ActionResultType result = HelperFunctions.tryThrow(mc.level, player, hand, player.getDeltaMovement(), Settings.throwUp, false);							
					if (result.consumesAction()) {
						throwDelay = 4;
						mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
						
						// If the throw was successful, tell the server to perform the throw as well
                        WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand,player.getDeltaMovement(), Settings.throwUp, false));
						return;
					}
				}
	        } else {
	        	throwDelay -= ticks;
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
    		ITagCollection<Item> tags = ItemTags.getAllTags();
			if (tags.getTag(new ResourceLocation("webasemod", "throwable_items")).contains(item) || tags.getTag(new ResourceLocation("webasemod", "vanilla_throwables")).contains(item)) {
				event.getToolTip().add((new TranslationTextComponent("item.webasemod.throwable")).withStyle(TextFormatting.GRAY));
			}
	    }
	}

}
