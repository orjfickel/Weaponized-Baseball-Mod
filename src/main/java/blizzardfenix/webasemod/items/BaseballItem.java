package blizzardfenix.webasemod.items;

import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ThrowableProperties;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BaseballItem extends Item {
	ThrowableProperties properties;

	public BaseballItem(Item.Properties builder, ThrowableProperties properties) {
		super(builder);
		this.properties = properties;
	}
		
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if(level.isClientSide()) {
			BouncyBallEntity throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
			InteractionResult result = HelperFunctions.throwBall(level, player, itemstack, throwableentity, player.getDeltaMovement(), Settings.throwUp).getResult();						
			if (result.consumesAction()) {			
				// If the throw was successful, tell the server to perform the throw as well
				WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand, player.getDeltaMovement(), Settings.throwUp, false));
			}
		}
		
		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}
}
