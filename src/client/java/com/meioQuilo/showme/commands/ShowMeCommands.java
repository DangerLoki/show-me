package com.meioQuilo.showme.commands;

import com.meioQuilo.showme.components.CustomSeedStorage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class ShowMeCommands {
    
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // Comando para definir a seed
        dispatcher.register(ClientCommandManager.literal("showmesetseed")
            .then(ClientCommandManager.argument("seed", LongArgumentType.longArg())
                .executes(ShowMeCommands::setSeed))
        );
        
        // Comando para limpar a seed customizada
        dispatcher.register(ClientCommandManager.literal("showmeclearseed")
            .executes(ShowMeCommands::clearSeed)
        );
        
        // Comando para ver a seed atual
        dispatcher.register(ClientCommandManager.literal("showmegetseed")
            .executes(ShowMeCommands::getSeed)
        );
    }
    
    private static int setSeed(CommandContext<FabricClientCommandSource> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        CustomSeedStorage.setCustomSeed(seed);
        
        context.getSource().sendFeedback(
            Text.literal("§aSeed customizada definida para: §f" + seed)
        );
        
        return 1;
    }
    
    private static int clearSeed(CommandContext<FabricClientCommandSource> context) {
        CustomSeedStorage.clearCustomSeed();
        
        context.getSource().sendFeedback(
            Text.literal("§aSeed customizada removida. Usando seed do servidor.")
        );
        
        return 1;
    }
    
    private static int getSeed(CommandContext<FabricClientCommandSource> context) {
        if (CustomSeedStorage.hasCustomSeed()) {
            context.getSource().sendFeedback(
                Text.literal("§aSeed customizada: §f" + CustomSeedStorage.getCustomSeed())
            );
        } else {
            context.getSource().sendFeedback(
                Text.literal("§eNenhuma seed customizada definida.")
            );
        }
        
        return 1;
    }
}