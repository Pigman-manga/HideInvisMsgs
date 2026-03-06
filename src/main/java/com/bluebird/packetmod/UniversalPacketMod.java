package com.bluebird.packetmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class UniversalPacketMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerSendPacketCommand(dispatcher));
    }

    private void registerSendPacketCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sendpacket")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("gamestate")
                        .then(Commands.argument("reasonId", IntegerArgumentType.integer(0, 15))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int id = IntegerArgumentType.getInteger(context, "reasonId");
                                            float value = FloatArgumentType.getFloat(context, "value");

                                            ClientboundGameEventPacket.Type reason = getReasonById(id);
                                            if (reason != null) {
                                                player.connection.send(new ClientboundGameEventPacket(reason, value));
                                                context.getSource().sendSuccess(() -> Component.literal("Отправлен пакет GameStateChange."), false);
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Неизвестный ID причины."));
                                            }
                                            return 1;
                                        }))))
                .then(Commands.literal("shake")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.connection.send(new ClientboundEntityEventPacket(player, (byte) 2));
                            context.getSource().sendSuccess(() -> Component.literal("Камера трясется!"), false);
                            return 1;
                        }))
                .then(Commands.literal("vanish")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");

                                    targetPlayer.connection.send(new ClientboundRemoveEntitiesPacket(sourcePlayer.getId()));
                                    context.getSource().sendSuccess(() -> Component.literal("Теперь " + targetPlayer.getName().getString() + " вас не видит!"), false);
                                    return 1;
                                })))
                .then(Commands.literal("morph")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("mob", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
                                            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
                                            String mobName = StringArgumentType.getString(context, "mob");

                                            ResourceLocation mobId = ResourceLocation.tryParse(mobName.contains(":") ? mobName : "minecraft:" + mobName);
                                            if (mobId == null) {
                                                context.getSource().sendFailure(Component.literal("Некорректный ID моба."));
                                                return 0;
                                            }

                                            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(mobId)) {
                                                context.getSource().sendFailure(Component.literal("Моб не найден!"));
                                                return 0;
                                            }

                                            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(mobId);
                                            if (type == null) {
                                                context.getSource().sendFailure(Component.literal("Моб не найден!"));
                                                return 0;
                                            }

                                            targetPlayer.connection.send(new ClientboundRemoveEntitiesPacket(sourcePlayer.getId()));

                                            Entity fakeMob = type.create(sourcePlayer.level());
                                            if (fakeMob == null) {
                                                context.getSource().sendFailure(Component.literal("Не удалось создать сущность."));
                                                return 0;
                                            }

                                            fakeMob.setId(sourcePlayer.getId());
                                            fakeMob.setUUID(sourcePlayer.getUUID());
                                            fakeMob.moveTo(sourcePlayer.getX(), sourcePlayer.getY(), sourcePlayer.getZ(), sourcePlayer.getYRot(), sourcePlayer.getXRot());
                                            fakeMob.setYHeadRot(sourcePlayer.getYHeadRot());

                                            targetPlayer.connection.send(new ClientboundAddEntityPacket(fakeMob));
                                            context.getSource().sendSuccess(() -> Component.literal("Для " + targetPlayer.getName().getString() + " вы теперь " + mobName), false);
                                            return 1;
                                        })))));
    }

    private ClientboundGameEventPacket.Type getReasonById(int id) {
        return switch (id) {
            case 0 -> ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE;
            case 1 -> ClientboundGameEventPacket.STOP_RAINING;
            case 2 -> ClientboundGameEventPacket.START_RAINING;
            case 3 -> ClientboundGameEventPacket.CHANGE_GAME_MODE;
            case 4 -> ClientboundGameEventPacket.WIN_GAME;
            case 7 -> ClientboundGameEventPacket.RAIN_LEVEL_CHANGE;
            case 10 -> ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT;
            default -> null;
        };
    }
}
