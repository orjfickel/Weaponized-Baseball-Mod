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
	private Boolean tryUse;

	public WebaseMessage() {
	}

	public WebaseMessage(InteractionHand hand, @Nullable Vec3 shooterVelocity, Boolean throwUp, Boolean tryUse) {
		this.hand = hand;
		this.shooterVelocity = shooterVelocity;
		this.throwUp = throwUp;
		this.tryUse = tryUse;
	}

	public static void encode(WebaseMessage message, FriendlyByteBuf buffer) {
		buffer.writeEnum(message.hand);
		buffer.writeFloat((float) message.shooterVelocity.x);
		buffer.writeFloat((float) message.shooterVelocity.y);
		buffer.writeFloat((float) message.shooterVelocity.z);
		buffer.writeByte((byte) (((message.throwUp ? 1 : 0) << 1) + (message.tryUse ? 1 : 0)));
	}

	public static WebaseMessage decode(FriendlyByteBuf buffer) {
		InteractionHand hand = buffer.readEnum(InteractionHand.class);
		Vec3 shooterVelocity = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		Byte booleans = buffer.readByte();
		return new WebaseMessage(hand, shooterVelocity, ((booleans & 0b010) >> 1) == 1, (booleans & 0b001) == 1);
	}

	public static void handle(WebaseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			ServerPlayer player = contextSupplier.get().getSender();
			HelperFunctions.tryThrow(player.getCommandSenderWorld(), player, message.hand, message.shooterVelocity, message.throwUp, message.tryUse);
		});
		contextSupplier.get().setPacketHandled(true);
	}
}
