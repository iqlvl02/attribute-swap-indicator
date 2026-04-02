package com.attributeswap.indicator.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks player attribute values each tick and fires the indicator
 * whenever a meaningful attribute change (swap) is detected.
 */
public class AttributeSwapManager {

    private static final double CHANGE_THRESHOLD = 0.001;

    // Snapshot of tracked attribute values from last tick
    private final Map<String, Double> previousValues = new HashMap<>();

    // Pairs of attributes considered "swap partners"
    // When one goes down and its partner goes up, that's a "swap"
    private static final String[][] SWAP_PAIRS = {
            {"generic.max_health",       "generic.armor"},
            {"generic.movement_speed",   "generic.attack_speed"},
            {"generic.attack_damage",    "generic.knockback_resistance"},
            {"generic.armor",            "generic.armor_toughness"},
            {"generic.luck",             "generic.attack_damage"},
    };

    private final SwapIndicatorRenderer renderer;

    public AttributeSwapManager(SwapIndicatorRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Called every client tick. Checks for attribute changes and detects swaps.
     */
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            previousValues.clear();
            return;
        }

        PlayerEntity player = client.player;

        // Check each swap pair
        for (String[] pair : SWAP_PAIRS) {
            String attrA = pair[0];
            String attrB = pair[1];

            double currentA = getAttributeValue(player, attrA);
            double currentB = getAttributeValue(player, attrB);
            double prevA = previousValues.getOrDefault(attrA, currentA);
            double prevB = previousValues.getOrDefault(attrB, currentB);

            double deltaA = currentA - prevA;
            double deltaB = currentB - prevB;

            // Detect swap: one goes down significantly, other goes up
            if (deltaA < -CHANGE_THRESHOLD && deltaB > CHANGE_THRESHOLD) {
                renderer.triggerSwap(
                        formatAttributeName(attrA), formatAttributeName(attrB),
                        prevA, currentB
                );
            } else if (deltaB < -CHANGE_THRESHOLD && deltaA > CHANGE_THRESHOLD) {
                renderer.triggerSwap(
                        formatAttributeName(attrB), formatAttributeName(attrA),
                        prevB, currentA
                );
            }

            // Also detect a single attribute change with significant delta (non-paired swap)
            if (Math.abs(deltaA) > CHANGE_THRESHOLD) {
                // Only trigger for single changes if not already triggered as a pair
                // (handled above)
            }

            previousValues.put(attrA, currentA);
            previousValues.put(attrB, currentB);
        }
    }

    /**
     * Manually trigger a swap indicator. Called from mixin or command.
     */
    public void triggerManual(String from, String to, double fromVal, double toVal) {
        renderer.triggerSwap(from, to, fromVal, toVal);
    }

    private double getAttributeValue(PlayerEntity player, String attributeId) {
        try {
            var registry = player.getWorld().getRegistryManager()
                    .get(net.minecraft.registry.RegistryKeys.ATTRIBUTE);
            var attrEntry = registry.getEntryList(
                    net.minecraft.registry.tag.TagKey.of(
                            net.minecraft.registry.RegistryKeys.ATTRIBUTE,
                            net.minecraft.util.Identifier.of(attributeId)
                    )
            );
            // Fallback: get directly
            for (var entry : registry.iterateEntries(
                    net.minecraft.registry.tag.TagKey.of(
                            net.minecraft.registry.RegistryKeys.ATTRIBUTE,
                            net.minecraft.util.Identifier.of(attributeId)))) {
                EntityAttributeInstance inst = player.getAttributeInstance(entry);
                if (inst != null) return inst.getValue();
            }
        } catch (Exception ignored) {}

        // Direct lookup fallback using attribute registry
        try {
            var registryManager = player.getWorld().getRegistryManager();
            var attrRegistry = registryManager.get(net.minecraft.registry.RegistryKeys.ATTRIBUTE);
            var id = net.minecraft.util.Identifier.of(attributeId);
            var attr = attrRegistry.getEntry(id);
            if (attr.isPresent()) {
                EntityAttributeInstance inst = player.getAttributeInstance(attr.get());
                if (inst != null) return inst.getValue();
            }
        } catch (Exception ignored) {}

        return 0.0;
    }

    private String formatAttributeName(String id) {
        // e.g. "generic.max_health" -> "Max Health"
        String name = id.contains(".") ? id.substring(id.lastIndexOf('.') + 1) : id;
        String[] words = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                sb.append(w.substring(1).toLowerCase());
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}
