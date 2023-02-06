package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RecipeMaterial {

    private Map<MultiKeys,ItemStack> map = new LinkedHashMap<>();
    public RecipeMaterial(int key1, int key2, ItemStack material){
        MultiKeys mk = new MultiKeys(key1,key2);
        map = new HashMap<MultiKeys,ItemStack>(){{
            put(mk,material);
        }};
    }

    public String recipeMaterialInfo(){
        StringBuilder sb = new StringBuilder();
        map.entrySet().forEach(s->sb.append(String.format("key:%s | Item:%s\n",s.getKey().getKeys(),s.getValue())));
        return sb.toString();
    }

    public RecipeMaterial(Map<MultiKeys,ItemStack> in){
        map = in;
    }

    public RecipeMaterial(MultiKeys key,ItemStack item){
        map = new HashMap<MultiKeys,ItemStack>(){{
            put(key,item);
        }};
    }

    public RecipeMaterial(){
    }

    public Map<MultiKeys,ItemStack> getRecipeMaterial(){
        return map;
    }

    public void put(MultiKeys multiKeys,ItemStack itemStack){
        map.put(multiKeys,itemStack);
    }

    public ItemStack getItemStack(MultiKeys keys){
        for(Map.Entry<MultiKeys,ItemStack> entry: map.entrySet()){
            if(entry.getKey().same(keys))return entry.getValue();
        }
        return new ItemStack(Material.AIR);
    }

    public int getMapSize(){
        Map<MultiKeys,ItemStack> map = this.map;
        return map.size();
    }

    public boolean isEmpty(){
        for (Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            if(entry.getValue()!=null){
                if(!entry.getValue().getType().equals(Material.AIR))return false;
            }
        }
        return true;
    }



    public RecipeMaterial recipeMaterialClone(){
        RecipeMaterial child = new RecipeMaterial();
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            child.put(entry.getKey(),entry.getValue());
        }
        return child;
    }

    public void setAllAmount(int amount){
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            if(entry.getValue().getType()==Material.AIR)continue;
            entry.getValue().setAmount(amount);
        }
    }

    public List<MultiKeys> getMultiKeysList(){
        List<MultiKeys> list = new ArrayList<>();
        map.entrySet().forEach(s->list.add(s.getKey()));
        return list;
    }

    public List<ItemStack> getItemStackListNoAir(){
        // the list that returns does not contain AIR and null
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<MultiKeys,ItemStack> entry: map.entrySet()){
            if(entry.getValue()==null)continue;
            if(entry.getValue().getType().equals(Material.AIR))continue;
            list.add(entry.getValue());
        }
        return list;
    }

    public List<ItemStack> getItemStackList(){
        // the list that returns that contains AIR
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<MultiKeys, ItemStack> entry:map.entrySet()){
            ItemStack item;
            if(entry.getValue()==null){
                item = new ItemStack(Material.AIR);
            }else{
                item = entry.getValue();
            }
            list.add(item);
        }
        return list;
    }

    public RecipeMaterial copy(){
        RecipeMaterial copied = new RecipeMaterial(map);
        return copied;
    }



}
