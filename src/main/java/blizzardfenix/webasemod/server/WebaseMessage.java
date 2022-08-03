package blizzardfenix.webasemod.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class WebaseMessage {
	private InteractionHand hand;
	private Vec3 shooterVelocity;
	private Boolean throwUp;

	public WebaseMessage() {
	}

	public WebaseMessage(InteractionHand hand, @Nullable Vec3 shooterVelocity, Boolean throwUp) {
		this.hand = hand;
		this.shooterVelocity = shooterVelocity;
		this.throwUp = throwUp;
	}

	public static void encode(WebaseMessage message, FriendlyByteBuf buffer) {
		buffer.writeEnum(message.hand);
		buffer.writeFloat((float) message.shooterVelocity.x);
		buffer.writeFloat((float) message.shooterVelocity.y);
		buffer.writeFloat((float) message.shooterVelocity.z);
		buffer.writeBoolean(Settings.throwUp);
	}

	public static WebaseMessage decode(FriendlyByteBuf buffer) {
		return new WebaseMessage(buffer.readEnum(InteractionHand.class), new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()), buffer.readBoolean());
	}

	public static void handle(WebaseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			ServerPlayer player = contextSupplier.get().getSender();
			HelperFunctions.tryThrow(player.getCommandSenderWorld(), player, message.hand, message.shooterVelocity, message.throwUp);
		});
		contextSupplier.get().setPacketHandled(true);
	}
}
