package blizzardfenix.webasemod.init;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.mojang.blaze3d.platform.InputConstants;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {
	public static KeyMapping throwUpToggleKey = new KeyMapping("key." + BaseballMod.MODID + ".throwuptoggle", KeyEvent.VK_NUMPAD0, "key.category." + BaseballMod.MODID);
	public static KeyMapping throwKey = new KeyMapping("key." + BaseballMod.MODID + ".throw",InputConstants.Type.MOUSE, MouseEvent.BUTTON1, "key.category." + BaseballMod.MODID);
}
