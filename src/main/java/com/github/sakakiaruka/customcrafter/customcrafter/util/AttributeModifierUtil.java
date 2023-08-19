package com.github.sakakiaruka.customcrafter.customcrafter.util;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeModifierUtil {
    public static final String USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_PATTERN = "^using_container_values_attribute_modifier -> type:([\\w_]+)/operation:(?i)(add|multiply|add_scalar)/value:([$a-z0-9\\-_.]+)$";
    public static final String USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_EQUIPMENT_SLOT_PATTERN = "^using_container_values_attribute_modifier -> type:([\\w_]+)/operation:(?i)(add|multiply|add_scalar)/value:([$a-z0-9\\-_.]+)/slot:([\\$a-zA-Z]+)$";

    private void setAttributeModifier(ItemMeta meta, PersistentDataContainer source, String order, boolean isNormal, Matcher matcher) {
        Attribute attribute;
        try {
            attribute = Attribute.valueOf(matcher.group(1).toUpperCase());
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER failed. (Attribute modifier not match.)");
            return;
        }

        double value;
        try {
            value = Double.valueOf(new ContainerUtil().getContent(source, matcher.group(3)));
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER failed. (Attribute modifier -> Not a Number.)");
            return;
        }

        EquipmentSlot slot = null;
        if (!isNormal) {
            try {
                slot = EquipmentSlot.valueOf(matcher.group(4).toUpperCase());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER failed. (Attribute modifier -> Illegal Equipment slot.)");
                return;
            }
        }

        AttributeModifier.Operation operation;
        if (matcher.group(2).equalsIgnoreCase("add")) {
            operation = AttributeModifier.Operation.ADD_NUMBER;
        } else if (matcher.group(2).equalsIgnoreCase("multiply")) {
            operation = AttributeModifier.Operation.MULTIPLY_SCALAR_1;
        } else if (matcher.group(2).equalsIgnoreCase("add_scalar")) {
            operation = AttributeModifier.Operation.ADD_SCALAR;
        } else {
            return;
        }

        UUID uuid = UUID.randomUUID();
        String name = matcher.group(1);
        AttributeModifier modifier;
        if (slot == null) {
            modifier = new AttributeModifier(uuid, name, value, operation);
        } else {
            modifier = new AttributeModifier(uuid, name, value, operation, slot);
        }

        try {
            meta.addAttributeModifier(attribute, modifier);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER failed. (Set Modifier Error.)");
        }
    }

    public void setAttributeModifierToResult(ItemMeta meta, PersistentDataContainer container, String order) {
        Matcher matcher;
        boolean isNormal = false;
        if (order.matches(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_PATTERN)) {
            matcher = Pattern.compile(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_PATTERN).matcher(order);
            isNormal = true;
        } else if (order.matches(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_EQUIPMENT_SLOT_PATTERN)) {
            matcher = Pattern.compile(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_EQUIPMENT_SLOT_PATTERN).matcher(order);
        } else {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER failed. (Not match AttributeModifier pattern.)");
            return;
        }

        if (!matcher.matches()) return;
        setAttributeModifier(meta, container, order, isNormal, matcher);
    }
}
