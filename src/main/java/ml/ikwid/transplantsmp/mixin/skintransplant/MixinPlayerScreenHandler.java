package ml.ikwid.transplantsmp.mixin.skintransplant;

import com.mojang.datafixers.util.Pair;
import ml.ikwid.transplantsmp.common.TransplantType;
import ml.ikwid.transplantsmp.common.imixins.ITransplantable;
import ml.ikwid.transplantsmp.common.util.Constants;
import ml.ikwid.transplantsmp.common.util.Utils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public class MixinPlayerScreenHandler {
	@Shadow @Final private PlayerEntity owner;
	@Shadow @Final private static EquipmentSlot[] EQUIPMENT_SLOT_ORDER;
	@Shadow @Final
	static Identifier[] EMPTY_ARMOR_SLOT_TEXTURES;

	private final ITransplantable transplantable = (ITransplantable) owner;

	private final PlayerScreenHandler self = (PlayerScreenHandler)(Object) this;

	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 9, ordinal = 2))
	private int drawMoreOrLessSlots(int constant) {
		if(transplantable == null) {
			return constant;
		}
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
	private int armorIndex(int constant) {
		return Constants.NEW_ARMOR_START_LOC + 3; // for SOME REASON it goes backwards instead of forwards
	}

	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
	private int offHandIndex(int constant) {
		return Constants.OFF_HAND;
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 4))
	public Slot changeSlot(PlayerScreenHandler instance, Slot slot) {
		int x = slot.x + Utils.innerSlotXShift(this.owner);

		return ((MixinScreenHandlerInvoker) self).addSlotAccess(new Slot(slot.inventory, slot.getIndex(), x, slot.y));
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 3))
	public Slot changeInventorySlotIndices(PlayerScreenHandler instance, Slot slot) {
		int newIndex = slot.getIndex() + 9;
		return ((MixinScreenHandlerInvoker) self).addSlotAccess(new Slot(slot.inventory, newIndex, slot.x, slot.y));
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void addSecondSetOfArmor(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
		if(transplantable == null) {
			return;
		}
		if(transplantable.getTransplantType() == TransplantType.SKIN_TRANSPLANT) {
			for(int i = 0; i < 4; i++) { // copy the code
				final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
				((MixinScreenHandlerInvoker) self).addSlotAccess(new Slot(inventory, Constants.SECOND_ARMOR_START_LOC + i, 8, 8 + i * 18) {
					@Override
					public void setStack(ItemStack stack) {
						ItemStack itemStack = this.getStack();
						super.setStack(stack);
						owner.onEquipStack(equipmentSlot, itemStack, stack);
					}

					@Override
					public int getMaxItemCount() {
						return 1;
					}

					@Override
					public boolean canInsert(ItemStack stack) {
						return equipmentSlot == MobEntity.getPreferredEquipmentSlot(stack);
					}

					@Override
					public boolean canTakeItems(PlayerEntity playerEntity) {
						ItemStack itemStack = this.getStack();
						if (!itemStack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasBindingCurse(itemStack)) {
							return false;
						}
						return super.canTakeItems(playerEntity);
					}

					@Override
					public Pair<Identifier, Identifier> getBackgroundSprite() {
						return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
					}
				});
			}
		}
	}

	public int xShift() {
		if(transplantable == null) {
			return 0;
		}
		return -((transplantable.getHotbarDraws() - 9) * Constants.INNER_SLOT_WIDTH / 2);
	}
}
