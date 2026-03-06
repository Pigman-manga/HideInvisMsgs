package com.bluebird.packetmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;

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
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = source.getPlayerOrException();

                                            int reasonId = IntegerArgumentType.getInteger(context, "reasonId");
                                            float value = FloatArgumentType.getFloat(context, "value");

                                            ClientboundGameEventPacket.Type reason = getReasonById(reasonId);
                                            if (reason != null) {
                                                player.connection.send(new ClientboundGameEventPacket(reason, value));
                                                source.sendSuccess(() -> Component.literal("Отправлен пакет GameStateChange."), false);
                                            } else {
                                                source.sendFailure(Component.literal("Неизвестный ID причины."));
                                            }
                                            return 1;
                                        }))))
                .then(Commands.literal("entitystatus")
                        .then(Commands.argument("statusId", IntegerArgumentType.integer(0, 127))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer player = source.getPlayerOrException();
                                    byte statusId = (byte) IntegerArgumentType.getInteger(context, "statusId");

                                    player.connection.send(new ClientboundEntityEventPacket(player, statusId));
                                    source.sendSuccess(() -> Component.literal("Отправлен пакет EntityStatus."), false);
                                    return 1;
                                }))));
    }

    private ClientboundGameEventPacket.Type getReasonById(int id) {
        try {
            Constructor<ClientboundGameEventPacket.Type> constructor = ClientboundGameEventPacket.Type.class.getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            return constructor.newInstance(id);
        } catch (Exception e) {
            return null;
        }
    }
}
