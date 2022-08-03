package blizzardfenix.webasemod.items;

import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ThrowableProperties;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BaseballItem extends Item {
	ThrowableProperties properties;

	public BaseballItem(Item.Properties builder, ThrowableProperties properties) {
		super(builder);
		this.properties = properties;
	}
		
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if(level.isClientSide()) {
			BouncyBallEntity throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
			ActionResultType result = HelperFunctions.throwBall(level, player, itemstack, throwableentity, player.getDeltaMovement(), Settings.throwUp).getResult();
			if (result.consumesAction()) {			
				// If the throw was successful, tell the server to perform the throw as well
				WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand, player.getDeltaMovement(), Settings.throwUp));
			}
		}
		
		return ActionResult.sidedSuccess(itemstack, level.isClientSide());
	}
}
