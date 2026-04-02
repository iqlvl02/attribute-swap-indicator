package com.attributeswap.indicator.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attribute Swap Indicator - Client Entrypoint
 *
 * Registers:
 *  - HUD render hook to draw the animated swap banner
 *  - Tick event to detect attribute changes and animate the indicator
 *  - A /swaptest command for easy in-game testing
 */
public class AttributeSwapIndicatorClient implements ClientModInitializer {

    public static final String MOD_ID = "attributeswapindicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Singleton instances accessible to mixin and commands
    public static SwapIndicatorRenderer RENDERER;
    public static AttributeSwapManager SWAP_MANAGER;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[AttributeSwapIndicator] Initializing...");

        RENDERER = new SwapIndicatorRenderer();
        SWAP_MANAGER = new AttributeSwapManager(RENDERER);

        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickDeltaManager) -> {
            float tickDelta = tickDeltaManager.getTickDelta(true);
            RENDERER.render(drawContext, tickDelta);
        });

        // Register tick event for attribute tracking and animation
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RENDERER.tick();
            SWAP_MANAGER.tick();
        });

        // Register /swaptest command for testing the indicator in-game
        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> {
                    dispatcher.register(
                        net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
                            .literal("swaptest")
                            .executes(ctx -> {
                                SWAP_MANAGER.triggerManual(
                                        "Attack Damage", "Armor",
                                        2.0, 5.0
                                );
                                ctx.getSource().sendFeedback(
                                        net.minecraft.text.Text.literal(
                                                "[SwapIndicator] Test indicator triggered!")
                                );
                                return 1;
                            })
                            .then(
                                net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
                                    .argument("from", com.mojang.brigadier.arguments.StringArgumentType.word())
                                    .then(
                                        net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
                                            .argument("to", com.mojang.brigadier.arguments.StringArgumentType.word())
                                            .executes(ctx -> {
                                                String from = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "from");
                                                String to = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "to");
                                                SWAP_MANAGER.triggerManual(from, to, 1.0, 3.0);
                                                ctx.getSource().sendFeedback(
                                                        net.minecraft.text.Text.literal(
                                                                "[SwapIndicator] Swap shown: " + from + " → " + to)
                                                );
                                                return 1;
                                            })
                                    )
                            )
                    );
                }
        );

        LOGGER.info("[AttributeSwapIndicator] Ready! Use /swaptest to preview the indicator.");
    }
}
