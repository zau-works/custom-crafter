package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;


public class SettingsLoad {
    private static CustomCrafter cc;
    private static FileConfiguration config;
    public static List<OriginalRecipe> recipes = new ArrayList<>();
    public static Material baseBlock;

    private Map<String,ItemStack> recipeResults = new HashMap<>();
    private Map<String,ItemStack> recipeMaterials = new HashMap<>();
    public void set(){
        cc = CustomCrafter.getInstance();
        config = cc.getConfig();
        getRecipeResults();
        getRecipeMaterials();
        getOriginalRecipeList();
        getBaseBlockMaterial();
    }

    @Deprecated
    private void getRecipeResults(){
        List<String> names = config.getStringList("result-items");
        for(String s:names){
            String name = s;
            String path = "result-item."+name+".";
            ItemStack item = new ItemStack(Material.valueOf(config.getString(path+"material").toUpperCase()));
            item.setAmount(config.getInt(path+"amount"));
            ItemMeta meta = item.getItemMeta();

            if(config.contains(path+"lore")){
                //set lore
                meta.setLore(config.getStringList(path+"lore"));
            }
            if(config.contains(path+"enchants")){
                //set enchantments
                List<String> raw = config.getStringList(path+"enchants");
                for(String t:raw){
                    Enchantment enchant = Enchantment.getByName(Arrays.asList(t.split(",")).get(0).toUpperCase());
                    item.addUnsafeEnchantment(enchant,Integer.valueOf(Arrays.asList(t.split(",")).get(1)));
                }
            }
            if(config.contains(path+"flags")){
                //set ItemFlags
                List<String> raw = config.getStringList(path+"flags");
                for(String t:raw){
                    meta.addItemFlags(ItemFlag.valueOf(t.toUpperCase()));
                }
            }

            item.setItemMeta(meta);
            recipeResults.put(name,item);
        }
    }

    private void getRecipeMaterials(){
        List<String> materials = config.getStringList("recipe-materials-list");
        for(String s:materials){
            String name = s;
            String path = "recipe-materials."+name+".";
            ItemStack item = new ItemStack(Material.valueOf(config.getString(path+"material").toUpperCase()));
            item.setAmount(config.getInt(path+"amount"));
            recipeMaterials.put(name,item);
        }
    }

    private void getOriginalRecipeList(){
        List<String> originals = config.getStringList("recipe-list");
        for(String s:originals){
            String path = "recipes."+s+".";
            String name = s;
            int size = config.getInt(path+"size");
            ItemStack result = recipeResults.get(config.getString(path+"result"));

            List<String> itemArr = config.getStringList(path+"recipe");
            RecipeMaterial rp = new RecipeMaterial();
            for(int i=0;i<size;i++){
                List<String> list = Arrays.asList(itemArr.get(i).split(","));
                for(int j=0;j<size;j++){
                    int x = j;
                    int y = i;
                    MultiKeys key = new MultiKeys(x,y);
                    ItemStack material;
                    if(list.get(j).equalsIgnoreCase("null")){
                        material = new ItemStack(Material.AIR);
                    }else if(recipeMaterials.containsKey(list.get(j))){
                        material = recipeMaterials.get(list.get(j));
                    }else{
                        Bukkit.getLogger().info("cc config load error.");
                        return;
                    }
                    rp.put(key,material);
                }
            }

            int total = 0;
            for(Map.Entry<MultiKeys,ItemStack> entry:rp.getRecipeMaterial().entrySet()){
                if(entry.getValue().getType().equals(Material.AIR))continue;
                total++;
            }
            OriginalRecipe originalRecipe = new OriginalRecipe(result,size,total,rp,name);
            recipes.add(originalRecipe);
        }
    }

    private void getBaseBlockMaterial(){
        String s = config.getString("base").toUpperCase();
        Material m = Material.getMaterial(s);
        baseBlock = m;
    }

}
