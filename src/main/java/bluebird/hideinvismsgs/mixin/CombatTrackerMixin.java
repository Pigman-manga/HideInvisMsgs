package bluebird.hideinvismsgs.mixin;

import bluebird.hideinvismsgs.HideInvisMsgs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin {
    @Redirect(method = "getDeathMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    public Component hideInvisMsgs$getDisplayName(LivingEntity livingEntity) {
        return hideinvismsgs$ObfuscateOrNormal(livingEntity);
    }

    @Redirect(method = "getFallMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    public Component hideInvisMsgs$getFallMessage(LivingEntity livingEntity) {
        return hideinvismsgs$ObfuscateOrNormal(livingEntity);
    }

    @Redirect(method = "getFallMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDisplayName(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;"))
    public Component hideInvisMsgs$getFallMessage(Entity entity) {
        if (entity == null) return null;
        boolean enabled = false;
        if (entity.level() instanceof ServerLevel serverLevel) {
            enabled = serverLevel
                    .getGameRules()
                    .get(HideInvisMsgs.OBFUSCATED_INVIS_DEATHS);
        }
        if (enabled && entity instanceof Player && entity.isInvisible()) {
            return Component.literal("Obfuscated").withStyle(ChatFormatting.OBFUSCATED);
        }
        return entity.getDisplayName();
    }

    @Redirect(method = "getMessageForAssistedFall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    public Component hideInvisMsgs$getMessageForAssistedFall(LivingEntity livingEntity) {
        return hideinvismsgs$ObfuscateOrNormal(livingEntity);
    }

    @Unique
    public Component hideinvismsgs$ObfuscateOrNormal(LivingEntity livingEntity) {
        boolean enabled = false;
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            enabled = serverLevel
                    .getGameRules()
                    .get(HideInvisMsgs.OBFUSCATED_INVIS_DEATHS);
        }
        if (enabled && livingEntity instanceof Player && livingEntity.isInvisible()) {
            return Component.literal("Obfuscated").withStyle(ChatFormatting.OBFUSCATED);
        }
        return livingEntity.getDisplayName();
    }
}
