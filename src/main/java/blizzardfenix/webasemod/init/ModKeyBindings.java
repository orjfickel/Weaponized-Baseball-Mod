package blizzardfenix.webasemod.init;

import java.awt.event.MouseEvent;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {
	public static KeyBinding throwKey = new KeyBinding("key." + BaseballMod.MODID + ".throw",InputMappings.Type.MOUSE, MouseEvent.BUTTON1, "key.category." + BaseballMod.MODID);
}
