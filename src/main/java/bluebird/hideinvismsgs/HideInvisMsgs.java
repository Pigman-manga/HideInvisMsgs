package bluebird.hideinvismsgs;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
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
                    .then(argument("className", StringArgumentType.string())
                            .then(argument("id", IntegerArgumentType.integer())
                                    .then(argument("value", FloatArgumentType.floatArg())
                                            .executes(context -> {
                                                String className = StringArgumentType.getString(context, "className");
                                                int id = IntegerArgumentType.getInteger(context, "id");
                                                float value = FloatArgumentType.getFloat(context, "value");

                                                Entity entity = context.getSource().getEntity();
                                                if (entity instanceof ServerPlayer player) {
                                                    trySendPacket(player, className, id, value);
                                                }
                                                return 1;
                                            }))));
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

    private void trySendPacket(ServerPlayer player, String className, int id, float value) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.network.protocol.game." + className);

            Object packetInstance;
            if (clazz.equals(ClientboundGameEventPacket.class)) {
                Class<?> typeClass = Class.forName("net.minecraft.network.protocol.game.ClientboundGameEventPacket$Type");
                Constructor<?> typeConstructor = typeClass.getDeclaredConstructor(int.class);
                typeConstructor.setAccessible(true);
                Object type = typeConstructor.newInstance(id);

                Constructor<?> packetConstructor = clazz.getDeclaredConstructor(typeClass, float.class);
                packetConstructor.setAccessible(true);
                packetInstance = packetConstructor.newInstance(type, value);
            } else {
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor(int.class, float.class);
                    constructor.setAccessible(true);
                    packetInstance = constructor.newInstance(id, value);
                } catch (NoSuchMethodException e) {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    packetInstance = constructor.newInstance();
                }
            }

            if (packetInstance instanceof Packet<?> packet) {
                player.connection.send(packet);
                LOGGER.info("Packet {} sent successfully", className);
                return;
            }

            LOGGER.error("Class {} is not a Packet", className);
        } catch (Exception e) {
            LOGGER.error("Error sending packet {}", className, e);
        }
    }
}
