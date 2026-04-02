package com.attributeswap.indicator.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;

public class AttributeSwapManager {

    private static final double CHANGE_THRESHOLD = 0.001;
    private final Map<String, Double> previousValues = new HashMap<>();
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
        checkSwap(player, "max_health", "armor",
                getVal(player, "max_health"), getVal(player, "armor"));
        checkSwap(player, "movement_speed", "attack_speed",
                getVal(player, "movement_speed"), getVal(player, "attack_speed"));
        checkSwap(player, "attack_damage", "knockback_resistance",
                getVal(player, "attack_damage"), getVal(player, "knockback_resistance"));
    }

    private void checkSwap(PlayerEntity player, String nameA, String nameB, double currentA, double currentB) {
        double prevA = previousValues.getOrDefault(nameA, currentA);
        double prevB = previousValues.getOrDefault(nameB, currentB);
        double deltaA = currentA - prevA;
        double deltaB = currentB - prevB;
        if (deltaA < -CHANGE_THRESHOLD && deltaB > CHANGE_THRESHOLD) {
            renderer.triggerSwap(formatName(nameA), formatName(nameB), prevA, currentB);
        } else if (deltaB < -CHANGE_THRESHOLD && deltaA > CHANGE_THRESHOLD) {
            renderer.triggerSwap(formatName(nameB), formatName(nameA), prevB, currentA);
        }
        previousValues.put(nameA, currentA);
        previousValues.put(nameB, currentB);
    }

    private double getVal(PlayerEntity player, String name) {
        try {
            EntityAttributeInstance inst = switch (name) {
                case "max_health" -> player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                case "armor" -> player.getAttributeInstance(EntityAttributes.ARMOR);
                case "movement_speed" -> player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
                case "attack_speed" -> player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
                case "attack_damage" -> player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
                case "knockback_resistance" -> player.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE);
                default -> null;
            };
            return inst != null ? inst.getValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public void triggerManual(String from, String to, double fromVal, double toVal) {
        renderer.triggerSwap(from, to, fromVal, toVal);
    }

    private String formatName(String id) {
        String[] words = id.split("_");
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
