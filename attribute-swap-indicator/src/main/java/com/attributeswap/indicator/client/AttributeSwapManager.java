package com.attributeswap.indicator.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class AttributeSwapManager {

    private static final double CHANGE_THRESHOLD = 0.001;
    private final Map<String, Double> previousValues = new HashMap<>();

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

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            previousValues.clear();
            return;
        }

        PlayerEntity player = client.player;

        for (String[] pair : SWAP_PAIRS) {
            String attrA = pair[0];
            String attrB = pair[1];

            double currentA = getAttributeValue(player, attrA);
            double currentB = getAttributeValue(player, attrB);
            double prevA = previousValues.getOrDefault(attrA, currentA);
            double prevB = previousValues.getOrDefault(attrB, currentB);

            double deltaA = currentA - prevA;
            double deltaB = currentB - prevB;

            if (deltaA < -CHANGE_THRESHOLD && deltaB > CHANGE_THRESHOLD) {
                renderer.triggerSwap(formatAttributeName(attrA), formatAttributeName(attrB), prevA, currentB);
            } else if (deltaB < -CHANGE_THRESHOLD && deltaA > CHANGE_THRESHOLD) {
                renderer.triggerSwap(formatAttributeName(attrB), formatAttributeName(attrA), prevB, currentA);
            }

            previousValues.put(attrA, currentA);
            previousValues.put(attrB, currentB);
        }
    }

    public void triggerManual(String from, String to, double fromVal, double toVal) {
        renderer.triggerSwap(from, to, fromVal, toVal);
    }

    private double getAttributeValue(PlayerEntity player, String attributeId) {
        try {
            Identifier id = Identifier.of(attributeId);
            var attr = Registries.ATTRIBUTE.getEntry(id);
            if (attr.isPresent()) {
                EntityAttributeInstance inst = player.getAttributeInstance(attr.get());
                if (inst != null) return inst.getValue();
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    private String formatAttributeName(String id) {
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
