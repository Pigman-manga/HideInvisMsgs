package bluebird.hideinvismsgs;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class HideInvisMsgs implements ModInitializer {

    public static Logger LOGGER = LoggerFactory.getLogger("hideinvismsgs");

    public void onInitialize() {
        LOGGER.info("HideInvisDeaths initialed");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("sendpacket")
                    .requires(source -> source.hasPermission(4))
                    .then(argument("packetClassName", StringArgumentType.string())
                            .executes(context -> {
                                String className = StringArgumentType.getString(context, "packetClassName");
                                Entity entity = context.getSource().getEntity();
                                if (entity instanceof ServerPlayer player) {
                                    sendDynamicPacket(player, className);
                                }
                                return 1;
                            })));
        });
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

    private void sendDynamicPacket(ServerPlayer player, String className) {
        try {
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game." + className);
            Constructor<?> constructor = packetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object packetInstance = constructor.newInstance();

            player.connection.send((Packet<?>) packetInstance);
        } catch (Exception e) {
            LOGGER.error("Failed to send packet {}", className, e);
        }
    }
}
