package blizzardfenix.webasemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.entity.ThrowableBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class SetThrowableVarCommand {

    public static void setThrowableVarCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("throwable").requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.literal("bounciness")
        		.then(Commands.argument("value",FloatArgumentType.floatArg())
        			.executes((command) -> {
			            return setBounciness(command.getSource(), command.getArgument("value", Float.class));
        }))));
        
        dispatcher.register(Commands.literal("throwable").requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.literal("friction")
        		.then(Commands.argument("value",FloatArgumentType.floatArg())
        			.executes((command) -> {
			            return setFriction(command.getSource(), command.getArgument("value", Float.class));
        }))));
    }
    
    private static int setBounciness(CommandSource source, float value) {
    	BaseballMod.LOGGER.info("set global bounciness to " + value);
    	source.getLevel().getEntities(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), (input) -> { return true; }).forEach((entity) -> {
    		((ThrowableBallEntity) entity).bounciness = value;
    		Settings.bounciness = value;
    		Settings.overrideBounciness = true;
    		return;
    	});
    	return 1;
    }
    
    private static int setFriction(CommandSource source, float value) {
    	BaseballMod.LOGGER.info("set global friction to " + value);
    	source.getLevel().getEntities(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), (input) -> { return true; }).forEach((entity) -> {
    		((ThrowableBallEntity) entity).friction = value;
    		Settings.friction = value;
    		Settings.overrideFriction = true;
    		return;
    	});
    	return 1;
    }
}
