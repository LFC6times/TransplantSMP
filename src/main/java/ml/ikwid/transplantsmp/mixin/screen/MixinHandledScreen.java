package ml.ikwid.transplantsmp.mixin.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import ml.ikwid.transplantsmp.common.TransplantType;
import ml.ikwid.transplantsmp.common.imixins.ITransplantable;
import ml.ikwid.transplantsmp.common.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen {
	@Shadow protected int x;
	@Shadow protected int y;
	@Shadow protected int backgroundHeight;
	private final ClientPlayerEntity player = MinecraftClient.getInstance().player;
	private final ITransplantable transplantable = (ITransplantable) player;
	@SuppressWarnings("rawtypes")
	private final HandledScreen self = (HandledScreen)(Object) this;

	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 166, ordinal = 0))
	private int renderShorterBackground(int constant) {
		return constant - Constants.HOTBAR_SPACE_IN_INV_SCREEN;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
	private void renderHotbarSection(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		// draw hotbar ourselves with the exact same texture as the HUD because i'm slightly lazy
		RenderSystem.setShaderTexture(0, Constants.WIDGETS_TEXTURE);

		int x = this.x;
		int y = this.y;
		int height = this.backgroundHeight;

		int bottom = y + height - Constants.HOTBAR_SPACE_IN_INV_SCREEN;
		int draws = transplantable.getHotbarDraws();
		for(int i = 0; i < draws; i++) {
			self.drawTexture(matrices, x + transplantable.xShift() + (i * Constants.OUTER_SLOT_WIDTH), bottom, 0, 0, Constants.OUTER_SLOT_WIDTH, Constants.OUTER_SLOT_HEIGHT);
		}

		if(transplantable.getTransplantType() == TransplantType.SKIN_TRANSPLANT) { // Armor slots
			for(int i = 0; i < 4; i++) {
				self.drawTexture(matrices, 8 - Constants.OUTER_SLOT_WIDTH, 8 + i * 22, 0, 0, Constants.OUTER_SLOT_WIDTH, Constants.OUTER_SLOT_HEIGHT);
			}
		}

		RenderSystem.setShaderTexture(0, HandledScreen.BACKGROUND_TEXTURE); // fix it for the rest of the code
	}

	@ModifyConstant(method = "onMouseClick(I)V", constant = @Constant(intValue = 9))
	private int fixHotkey(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "onMouseClick(I)V", constant = @Constant(intValue = 40))
	private int changeOffHand(int constant) {
		return Constants.OFF_HAND;
	}

	@ModifyConstant(method = "handleHotbarKeyPressed", constant = @Constant(intValue = 40))
	private int changeOffHand2(int constant) {
		return Constants.OFF_HAND;
	}

	@ModifyConstant(method = "handleHotbarKeyPressed", constant = @Constant(intValue = 9))
	private int fixHotkey2(int constant) {
		return transplantable.getHotbarDraws();
	}

	/**
	 * @author 6Times
	 * @reason For Arm/Skin Transplant users to access their slots.
	 */
	@Overwrite
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
		return false;
	}
}
