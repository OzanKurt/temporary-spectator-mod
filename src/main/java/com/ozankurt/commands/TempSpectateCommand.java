package com.ozankurt.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ozankurt.interfaces.PlayerEntityMixinInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TempSpectateCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("temp-spectate")
            .then(
                argument("action", word())
                    .suggests((c, b) -> {
                        List<String> suggestions = new ArrayList<>();
                        suggestions.add("start");
                        suggestions.add("stop");
                        return CommandSource.suggestMatching(suggestions, b);
                    })
                    .executes(TempSpectateCommand::execute)
            );

        dispatcher.register(builder);
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        String action = getString(context, "action");

        if (!action.equals("start") && !action.equals("stop")) {
            return 0;
        }

        ServerPlayerEntity serverPlayerEntity;
        PlayerEntityMixinInterface playerEntityMixin;

        try {
            serverPlayerEntity = context.getSource().getPlayer();
            playerEntityMixin = (PlayerEntityMixinInterface) serverPlayerEntity;
        } catch (CommandSyntaxException e) {
            return 0;
        }

        if (action.equals("start")) {
            String message = String.format(
                "Your game mode has been set to \"Spectator\" for the next %d minutes.",
                playerEntityMixin.getTempSpectateDuration()
            );


            serverPlayerEntity.sendMessage(
                new LiteralText(message)
                    .formatted(Formatting.ITALIC, Formatting.GRAY)
            );


            MinecraftServer server = context.getSource().getMinecraftServer();

            playerEntityMixin.startTempSpectate(server.getTicks());

            String logMessage = String.format("Player %s started temporarily spectating.", serverPlayerEntity.getName());
            server.log(logMessage);
        }

        if (action.equals("stop")) {
            serverPlayerEntity.sendMessage(
                new LiteralText("Your game mode has been set to \"Survival\".")
                    .formatted(Formatting.ITALIC, Formatting.GRAY)
            );

            playerEntityMixin.stopTempSpectate();

            MinecraftServer server = context.getSource().getMinecraftServer();

            String logMessage = String.format("Player %s stopped temporarily spectating.", serverPlayerEntity.getName());
            server.log(logMessage);
        }

        return 1;
    }
}
