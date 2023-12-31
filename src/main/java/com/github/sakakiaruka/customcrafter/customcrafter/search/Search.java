package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;


public class Search {

    private static final String PASS_THROUGH_PATTERN = "^(?i)pass -> ([a-zA-Z_]+)$";
    private final Map<Coordinate, List<Coordinate>> AMORPHOUS_NULL_ANCHOR = new HashMap<Coordinate, List<Coordinate>>() {{
        put(Coordinate.NULL_ANCHOR, Collections.emptyList());
    }};

    public void massSearch(Player player,Inventory inventory, boolean isOneCraft){
        // mass (in batch)
        Recipe result = null;
        int massAmount = 0;
        Recipe input = toRecipe(inventory);
        List<ItemStack> interestedItems = getInterestedAreaItems(inventory);
        int itemContainedSlots = input.getContentsNoAir().size();
        if (itemContainedSlots == 0) return;
        if (ITEM_PLACED_SLOTS_RECIPE_MAP.get(itemContainedSlots) == null || ITEM_PLACED_SLOTS_RECIPE_MAP.get(itemContainedSlots).isEmpty()) {
            new VanillaSearch().main(player, inventory, isOneCraft);
            return;
        }

        int judge = 0;

        Top:for(Recipe recipe: ITEM_PLACED_SLOTS_RECIPE_MAP.get(itemContainedSlots)){

            if(recipe.hasPermission()){ // permission check
                RecipePermission source = recipe.getPermission();
                if(!new RecipePermissionUtil().containsPermission(player, source)) continue;
            }

            if(recipe.getTag().equals(Tag.NORMAL)){
                //normal
                if(getSquareSize(recipe.getCoordinateList()) != getSquareSize(input.getCoordinateList()))continue;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue;
                if(!isAllCandidateContains(recipe,input))continue;

                // check mass matter is one
                for(int i=0;i<recipe.getContentsNoAir().size();i++){
                    Matter recipeMatter = recipe.getContentsNoAir().get(i);
                    Matter inputMatter = input.getContentsNoAir().get(i);

                    if (!new ContainerUtil().isPass(interestedItems.get(i), recipeMatter)) continue Top;

                    //(amount one virtual test)
                    Matter recipeOne = recipeMatter.oneCopy();
                    Matter inputOne = inputMatter.oneCopy();

                    if(!isSameMatter(recipeOne,inputOne)) continue Top;
                    if(!(recipeOne.getClass().equals(Potions.class) && inputOne.getClass().equals(Potions.class))) continue;
                    if(!new PotionUtil().isSamePotion((Potions)recipeOne,(Potions) inputOne)) continue Top;

                    //end (amount one virtual test end)

                    if(recipe.getContentsNoAir().get(i).isMass()){
                        if(inputMatter.getAmount() != 1)continue Top;
                    }

                    if(inputMatter.getAmount() < recipeMatter.getAmount())continue Top;
                    if(!getEnchantWrapCongruence(recipeMatter,inputMatter))continue Top; // enchant check

                    judge += 1;
                }


            }else{

                //debug
                List<Map<Coordinate, List<Coordinate>>> temp = new ArrayList<>();
                Map<Coordinate, List<Coordinate>> enchant = new EnchantUtil().amorphous(recipe, input);
                Map<Coordinate, List<Coordinate>> container = new ContainerUtil().amorphous(recipe, input);
                Map<Coordinate,List<Coordinate>> candidate = new InventoryUtil().amorphous(recipe, input);
                Map<Coordinate, Map<String, Boolean>> rStatus = new InventoryUtil().getEachMatterStatus(recipe);
                Map<Coordinate, Map<String, Boolean>> iStatus = new InventoryUtil().getEachMatterStatus(input);

                Bukkit.getLogger().info("enchant map="+enchant);
                Bukkit.getLogger().info("container map="+container);
                Bukkit.getLogger().info("candidate map="+candidate);

                if (!isElementMatch(enchant, rStatus, "enchant")) {
                    Bukkit.getLogger().info("enchant not matched");
                    continue Top;
                }

                if (!isElementMatch(container, rStatus, "container")) {
                    Bukkit.getLogger().info("container not matched");
                    continue Top;
                }

                //TODO : have to impl Potion check


                //if (enchant.isEmpty()) continue;

                if (!enchant.isEmpty() && !enchant.equals(AMORPHOUS_NULL_ANCHOR)) temp.add(enchant);

                //if (container.isEmpty()) continue;
                if (!container.isEmpty() && !container.equals(AMORPHOUS_NULL_ANCHOR)) temp.add(container);


                for (Map<Coordinate, List<Coordinate>> element : temp) {
                    for (Map.Entry<Coordinate, List<Coordinate>> entry : element.entrySet()) {
                        Bukkit.getLogger().info("  source="+entry.getKey()+" / element="+entry.getValue());
                    }
                }

                for (Map.Entry<Coordinate, List<Coordinate>> element : candidate.entrySet()) {
                    Bukkit.getLogger().info("  source(candidate)="+element.getKey()+" / element="+element.getValue());
                }

                Bukkit.getLogger().info("recipe name="+recipe.getName());
                for (Map.Entry<Coordinate, Map<String, Boolean>> entry : rStatus.entrySet()) {
                    Bukkit.getLogger().info("coordinate="+entry.getKey()+" / status="+entry.getValue());
                }

                if (!enchant.isEmpty() && (!enchant.equals(AMORPHOUS_NULL_ANCHOR))) {
                    Bukkit.getLogger().info("enchant size congruence="+(enchant.size() == input.getEnchantedItemCoordinateList().size()));
                }

                if (!container.isEmpty() && !container.equals(AMORPHOUS_NULL_ANCHOR)) {
                    Bukkit.getLogger().info("container size congruence="+(container.size() == input.getHasContainerDataItemList().size()));
                }

                if (!candidate.isEmpty()) {
                    Bukkit.getLogger().info("candidate size congruence="+(candidate.size() == input.getContentsNoAir().size()));
                }

                temp.add(candidate);
                Map<Coordinate, Coordinate> relate;
                if ((relate = new InventoryUtil().combination(temp)).isEmpty()) continue;

                for (Map.Entry<Coordinate, Coordinate> entry : relate.entrySet()) {
                    Coordinate r = entry.getKey();
                    Coordinate i = entry.getValue();
                    if (!rStatus.get(r).equals(iStatus.get(i))) continue Top;
                }

                Bukkit.getLogger().info("temp map="+temp);

                judge += 1;
            }


            if (judge > 0) {
                result = recipe;
                massAmount  = getMinimalAmount(result,input);
            }

            break;
        }


        if(result != null){
            // custom recipe found
            new InventoryUtil().returnItems(result,inventory,massAmount,player);
            int quantity = (isOneCraft ? 1 : massAmount) * result.getResult().getAmount();
            setResultItem(inventory,result,input,player,quantity,isOneCraft);
        }else{
            // not found
            new VanillaSearch().main(player,inventory,isOneCraft);
        }
    }

    private boolean isElementMatch(Map<Coordinate, List<Coordinate>> map, Map<Coordinate, Map<String, Boolean>> status, String key) {
        Set<Coordinate> set = new HashSet<>();
        map.forEach((k, v) -> set.add(k));
        for (Map.Entry<Coordinate, Map<String, Boolean>> entry : status.entrySet()) {
            if (!entry.getValue().get(key)) continue;
            Coordinate needContained = entry.getKey();
            if (!set.contains(needContained)) return false;
        }
        return true;
    }




    private boolean isAllCandidateContains(Recipe recipe,Recipe input){
        for(int i=0;i<recipe.getContentsNoAir().size();i++){
            List<Material> matters = recipe.getContentsNoAir().get(i).getCandidate();
            Material material = input.getContentsNoAir().get(i).getCandidate().get(0);
            if(!matters.contains(material))return false;
        }
        return true;
    }

    private int getMinimalAmount(Recipe recipe,Recipe input){
        Set<Material> set = recipe.getMassMaterialSet();
        List<Integer> list = new ArrayList<>();
        for(Matter matter : input.getContentsNoAir()){
            if(set.contains(matter.getCandidate().get(0)))continue; // input matter is Mass
            list.add(matter.getAmount());
        }

        if(list.isEmpty())return -1;
        Collections.sort(list);

        return list.get(0);
    }


    private void setResultItem(Inventory inventory, Recipe recipe, Recipe input, Player player, int amount, boolean oneCraft){
        ItemStack item = null;
        if (ALL_MATERIALS.contains(recipe.getResult().getNameOrRegex())
        && recipe.getResult().getMatchPoint() == -1
        && !recipe.getResult().getNameOrRegex().contains("@")) {
            // result has defined material
            Material m = Material.valueOf(recipe.getResult().getNameOrRegex().toUpperCase());
            item = new ItemStack(m, amount);
            recipe.getResult().setMetaData(item);
            //setMetaData(item,recipe.getResult()); //set result itemStack's metadata
        }else if (recipe.getResult().getNameOrRegex().matches(PASS_THROUGH_PATTERN)) {
            // pass through mode
            // nameOrRegex: pass -> material name (there are only one in the inventory.)
            // example): nameOrRegex: pass -> cobblestone
            Material target;
            try {
                Matcher m = Pattern.compile(PASS_THROUGH_PATTERN).matcher(recipe.getResult().getNameOrRegex());
                if (!m.matches()) {
                    Bukkit.getLogger().warning("[CustomCrafter] pass-through mode failed. (Illegal Material name.)");
                    return;
                }
                target = Material.valueOf(m.group(1).toUpperCase());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] pass-through mode failed. (Illegal Material name.)");
                return;
            }
            List<ItemStack> items = new ArrayList<>();
            for (int i=0;i<inventory.getSize();i++) {
                if (inventory.getItem(i) == null) continue;
                if (inventory.getItem(i).getType().equals(target)) items.add(inventory.getItem(i));
            }
            if (items.size() != 1) {
                Bukkit.getLogger().warning("[CustomCrafter] pass-through mode failed. (Same material some where.) ");
                return;
            }

            item = items.get(0);
            recipe.getResult().setMetaData(item);

        }else {
            // not contains -> A result has written by regex pattern.
            List<String> list = Arrays.asList(recipe.getResult().getNameOrRegex().split("@"));

            String p = list.get(0);
            String replaced = list.get(1);
            Pattern pattern = Pattern.compile(p);
            List<String> materials = new ArrayList<>();
            for(Material m:getContainsMaterials(input)){
                String name = m.name();
                Matcher matcher = pattern.matcher(name);

                //if(!matcher.find())continue;
                int point = recipe.getResult().getMatchPoint();
                if(!matcher.find(0))continue;
                if(replaced.contains("{R}"))replaced = replaced.replace("{R}",matcher.group(point));
                materials.add(replaced);
            }
            Collections.sort(materials);

            Material material = Material.valueOf(materials.get(0).toUpperCase());
            item = new ItemStack(material,amount);
            recipe.getResult().setMetaData(item);
            //setMetaData(item,recipe.getResult());
        }

        if(item == null)return;
        if(item.getType().equals(Material.AIR))return;

        WHAT_MAKING.put(player.getUniqueId(),item.getType());

        new ContainerUtil().setRecipeDataContainerToResultItem(item, input, recipe);
        if (recipe.hasUsingContainerValuesMetadata()) new ContainerUtil().setRecipeUsingContainerValueMetadata(inventory, recipe, item);

        if(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) == null){
            // empty a result item's slot
            InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
        }else{
            if(item.getAmount() > item.getType().getMaxStackSize()){
                // over the max amount
                InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
            }else{
                // in the limit
                inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,item);
            }
        }

        new InventoryUtil().decrementMaterials(inventory,oneCraft ? 1 : getMinimalAmount(recipe,input));

    }


    private List<Material> getContainsMaterials(Recipe input){
        Set<Material> set = new HashSet<>();
        input.getContentsNoAir().forEach(s->{
            set.addAll(s.getCandidate());
        });
        List<Material> list = new ArrayList<>();
        set.forEach(s->list.add(s));

        return list;
    }


    public boolean isSameMatter(Matter recipe,Matter input){

        if(!recipe.getCandidate().containsAll(input.getCandidate()))return false;
        if(recipe.getAmount() != input.getAmount())return false;
        if(!getEnchantWrapCongruence(recipe,input))return false;
        return true;
    }



    public boolean getEnchantWrapCongruence(Matter recipe,Matter input){


        if(!input.hasWrap() && recipe.hasWrap()) return false;
        if(!recipe.hasWrap())return true; // no target

        if(recipe.getCandidate().get(0).equals(Material.ENCHANTED_BOOK)){
            if(!input.getCandidate().get(0).equals(Material.ENCHANTED_BOOK)) return false;

            for(EnchantWrap wrap : recipe.getWrap()){
                if(wrap.getStrict().equals(EnchantStrict.NOTSTRICT)) continue;
                if(!input.contains(wrap.getEnchant())) return false;
                if(wrap.getStrict().equals(EnchantStrict.ONLYENCHANT)) continue;
                if(wrap.getLevel() != input.getEnchantLevel(wrap.getEnchant())) return false;
            }
            return true;
        }


        for(EnchantWrap wrap : recipe.getWrap()){
            if(wrap.getStrict().equals(EnchantStrict.NOTSTRICT))continue; // not have to check

            Enchantment recipeEnchant = wrap.getEnchant();
            List<Enchantment> enchantList = new ArrayList<>();
            input.getWrap().forEach(s->enchantList.add(s.getEnchant()));
            if(!enchantList.contains(recipeEnchant))return false;

            if(wrap.getStrict().equals(EnchantStrict.ONLYENCHANT))continue; //enchant contains check OK

            int recipeLevel = wrap.getLevel();
            int inputLevel = input.getEnchantLevel(wrap.getEnchant());
            if(recipeLevel != inputLevel)return false; // level check failed
        }
        return true;
    }


    private List<EnchantWrap> getEnchantWrap(ItemStack item){
        List<EnchantWrap> list = new ArrayList<>();
        Map<Enchantment,Integer> map = item.getEnchantments();
        if(map.isEmpty())return null;
        EnchantStrict strict = EnchantStrict.INPUT;
        for(Map.Entry<Enchantment,Integer> entry:map.entrySet()){
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
            list.add(wrap);

        }
        return list;
    }


    private List<Coordinate> getCoordinateNoAir(Recipe recipe){
        List<Coordinate> list = new ArrayList<>();
        for(Map.Entry<Coordinate,Matter> entry:recipe.getCoordinate().entrySet()){
            if(entry.getValue().getCandidate().get(0).equals(Material.AIR))continue;
            list.add(entry.getKey());
        }
        return list;
    }



    public static int getSquareSize(List<Coordinate> list){
//        List<Coordinate> list = getCoordinateNoAir(recipe);
        if(list.isEmpty())return -1;
        if(list.get(0).getX() < 0 || list.get(0).getY() < 0)return -1;

        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        list.forEach(s->{
            x.add(s.getX());
            y.add(s.getY());
        });
        Collections.sort(x);
        Collections.sort(y);
        int width = Math.abs(x.get(0) - x.get(x.size()-1)) + 1;
        int height = Math.abs(y.get(0) - y.get(y.size()-1)) + 1;
        return Math.max(width,height);
    }

    private boolean isSameShape(List<Coordinate> models,List<Coordinate> reals){
        int xGap = models.get(0).getX() - reals.get(0).getX();
        int yGap = models.get(0).getY() - reals.get(0).getY();

        if(models.size() != reals.size())return false;
        int size = models.size();
        for(int i=1;i<size;i++){

            if(models.get(i).getX() - reals.get(i).getX() != xGap)return false;
            if(models.get(i).getY() - reals.get(i).getY() != yGap)return false;
        }
        return true;
    }

    private List<ItemStack> getInterestedAreaItems(Inventory inventory) {
        List<ItemStack> list = new ArrayList<>();
        for (int y = 0; y< CRAFTING_TABLE_SIZE; y++) {
            for (int x = 0; x< CRAFTING_TABLE_SIZE; x++) {
                int i = x+y*9;
                if (inventory.getItem(i) == null || inventory.getItem(i).getType().equals(Material.AIR)) continue;
                list.add(inventory.getItem(i));
            }
        }
        return list;
    }

    private Recipe toRecipe(Inventory inventory){
        Recipe recipe = new Recipe();
        for(int y = 0; y< CRAFTING_TABLE_SIZE; y++){
            for(int x = 0; x< CRAFTING_TABLE_SIZE; x++){
                int i = x+y*9;
                Matter matter = toMatter(inventory,i);
                if(inventory.getItem(i) == null)continue;
                if(inventory.getItem(i).getItemMeta().hasEnchants()) {
                    matter.setWrap(getEnchantWrap(inventory.getItem(i))); //set enchantments information
                }
                // enchanted_book pattern
                if(inventory.getItem(i).getType().equals(Material.ENCHANTED_BOOK)){
                    ItemStack item = inventory.getItem(i);
                    if(((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().isEmpty()) continue;
                    for(Map.Entry<Enchantment,Integer> entry : ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().entrySet()){
                        Enchantment enchant = entry.getKey();
                        int level = entry.getValue();
                        EnchantStrict strict = EnchantStrict.INPUT;
                        EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                        matter.addWrap(wrap);
                    }
                }
                new ContainerUtil().setContainerDataItemStackToMatter(inventory.getItem(i), matter);

                recipe.addCoordinate(x,y,matter);
            }
        }
        return recipe;
    }

    private Matter toMatter(Inventory inventory,int slot){
        Matter matter;
        if(inventory.getItem(slot) == null){
            matter = new Matter(Arrays.asList(Material.AIR),0);
        }else if(new PotionUtil().isPotion(inventory.getItem(slot).getType())){
            matter = new Potions(inventory.getItem(slot), PotionStrict.INPUT);
        }else{
            matter = new Matter(inventory.getItem(slot));
        }
        return matter;
    }
}
