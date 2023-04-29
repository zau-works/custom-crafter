package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.matters;

public class PotionCheck {
    public void checker(Player player){
        if(player.getInventory().getItemInMainHand()==null)return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if(
                !item.getType().equals(Material.POTION) &&
                !item.getType().equals(Material.SPLASH_POTION) &&
                !item.getType().equals(Material.LINGERING_POTION))return;

        if(!item.hasItemMeta())return;
        ItemMeta meta = item.getItemMeta();
        PotionMeta pMeta = (PotionMeta)meta;
        Bukkit.getLogger().info(pMeta.toString());
        if(pMeta.hasCustomEffects()){
            Bukkit.getLogger().info("This potion has Custom Effects.");
            for(PotionEffect effect : pMeta.getCustomEffects()){
                Bukkit.getLogger().info(String.format("effect : %s",effect.toString()));
            }
        }

        Bukkit.getLogger().info(String.format("potion type : %s",pMeta.getBasePotionData().getType()));
        System.out.println(String.format("potion : %s",new Potions(item,PotionStrict.INPUT).PotionInfo()));
    }
}
