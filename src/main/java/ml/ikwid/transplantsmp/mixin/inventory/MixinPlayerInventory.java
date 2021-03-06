package ml.ikwid.transplantsmp.mixin.inventory;

import ml.ikwid.transplantsmp.common.imixins.ITransplantable;
import ml.ikwid.transplantsmp.common.util.Constants;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Under this mixin, here are the new slot number layouts:
 * VANILLA HOTBAR: 0 - 8
 * ADDON HOTBAR: 9 - 17
 * MAIN: 18 - 44
 * ARMOR: 45 - 48
 * EXTRA ARMOR SLOTS: 49 - 52
 * OFF HAND: 53
 */
@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {
	@Shadow @Final public PlayerEntity player;

	@Shadow @Final public DefaultedList<ItemStack> armor;

	@Shadow @Final public DefaultedList<ItemStack> main;

	@Shadow
	public static boolean isValidHotbarIndex(int slot) {
		throw new AssertionError();
	}

	private ITransplantable transplantable;

	@Inject(method = "getHotbarSize", at = @At(value = "HEAD"), cancellable = true)
	private static void customHotbarSize(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(18);
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/collection/DefaultedList;", ordinal = 0), index = 0)
	private int fixInvSize(int size) {
		return 45;
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/collection/DefaultedList;", ordinal = 1), index = 0)
	private int fixArmorSlots(int size) {
		return 4;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void setPlayer(PlayerEntity player, CallbackInfo ci) {
		this.transplantable = (ITransplantable) player;
	}

	/**
	 * @author 6Times
	 * @reason cannot inject into bottom of method due to the conditional with return
	 */
	@Overwrite
	public void damageArmor(DamageSource damageSource, float amount, int[] slots) {
		if(amount <= 0.0f) {
			return;
		}
		if((amount /= 4.0f) < 1.0f) {
			amount = 1.0f;
		}
		for(int i : slots) {
			ItemStack inv1 = this.armor.get(i);
			// ItemStack inv2 = this.armor.get(i * 2);
			if(!(damageSource.isFire() && inv1.getItem().isFireproof() || !(inv1.getItem() instanceof ArmorItem))) {
				inv1.damage((int)amount, this.player, player -> player.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i)));
			}
			/*
			if(!(damageSource.isFire() && inv2.getItem().isFireproof() || !(inv2.getItem() instanceof ArmorItem))) {
				inv2.damage((int)amount, this.player, player -> player.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i * 2)));
			}
			*/
		}
	}

	@ModifyConstant(method = "isValidHotbarIndex", constant = @Constant(intValue = 9, ordinal = 0))
	private static int increaseCheckedHotbar(int constant) {
		return 18;
		// this is incredibly janky and it would be more hack-proof to redirect the 8 method calls, but more code
		// the current plan is to just... not enable the hotkeys to disabled slots
	}

	/**
	 * @author 6Times
	 * @reason Pretty much an entire rewrite
	 */
	@Overwrite
	public int getEmptySlot() {
		for(int i = 0; i < this.main.size(); i++) {
			if(isValidHotbarIndex(i)) {
				if(i > this.transplantable.getHotbarDraws() - 1) {
					continue;
				}
			}
			if(this.main.get(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	@ModifyConstant(method = "getSwappableHotbarSlot", constant = @Constant(intValue = 9, ordinal = 0))
	private int increaseCheckedHotbar2(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "getSwappableHotbarSlot", constant = @Constant(intValue = 9, ordinal = 1))
	private int increaseCheckedHotbar3(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "getSwappableHotbarSlot", constant = @Constant(intValue = 9, ordinal = 2))
	private int increaseCheckedHotbar4(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "getSwappableHotbarSlot", constant = @Constant(intValue = 9, ordinal = 3))
	private int increaseCheckedHotbar5(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "scrollInHotbar", constant = @Constant(intValue = 9, ordinal = 0))
	private int increaseCheckedHotbar6(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "scrollInHotbar", constant = @Constant(intValue = 9, ordinal = 1))
	private int increaseCheckedHotbar7(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "scrollInHotbar", constant = @Constant(intValue = 9, ordinal = 2))
	private int increaseCheckedHotbar8(int constant) {
		return transplantable.getHotbarDraws();
	}

	@ModifyConstant(method = "getOccupiedSlotWithRoomForStack", constant = @Constant(intValue = 40, ordinal = 0))
	private int changeOffHandSlot(int constant) {
		return Constants.OFF_HAND;
	}

	@ModifyConstant(method = "getOccupiedSlotWithRoomForStack", constant = @Constant(intValue = 40, ordinal = 1))
	private int changeOffHandSlot2(int constant) {
		return Constants.OFF_HAND;
	}
}
