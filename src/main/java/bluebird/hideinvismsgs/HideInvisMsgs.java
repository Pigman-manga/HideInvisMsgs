package bluebird.hideinvismsgs;

import net.fabricmc.api.ModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HideInvisMsgs implements ModInitializer {

    public static Logger LOGGER = LoggerFactory.getLogger("hideinvismsgs");

    public void onInitialize() {
        LOGGER.info("HideInvisDeaths initialed");
    }

    public static Component hideinvismsgs$ObfuscateOrNormalDeaths(LivingEntity livingEntity) {
        return hideinvismsgs$ObfuscateOrNormalDeaths((Entity) livingEntity);
    }


    public static Component hideinvismsgs$ObfuscateOrNormalDeaths(Entity livingEntity) {
        if (livingEntity == null) return null;
        if (hideinvismsgs$hasInvisibilityTwoOrHigher(livingEntity)) {
            return Component.literal("Obfuscated").withStyle(ChatFormatting.OBFUSCATED);
        }
        return livingEntity.getDisplayName();
    }

    public static boolean hideinvismsgs$hasInvisibilityTwoOrHigher(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return false;
        return livingEntity.getActiveEffects().stream().anyMatch(effect ->
                "effect.minecraft.invisibility".equals(effect.getDescriptionId()) && effect.getAmplifier() >= 1
        );
    }
}
