package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.command.ContainerModify;
import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeDataContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeDataContainerModifyType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.AttributeModifierUtil.USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_EQUIPMENT_SLOT_PATTERN;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.AttributeModifierUtil.USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_PATTERN;

public class ContainerUtil {
    public static Map<String, Map<Integer, ContainerWrapper>> containers = new HashMap<>();
    public static final String ALLOW_TAG = "allow_tag";
    public static final String ALLOW_VALUE = "allow_value";
    public static final String DENY_TAG = "deny_tag";
    public static final String DENY_VALUE = "deny_value";
    public static final String STORE_ONLY = "store_only";

    //---
    private static final String OPERATORS_PATTERN = "\\+|-|\\*|/|\\(|\\)";
    private static final String ORDER_NUMBER_PATTERN = "_\\d{1,2}";
    private static final String ARROW_RANGE_PATTERN = "^([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_]+)<-->([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_]+)$";
    private static final String LARGER_PATTERN = "^([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_]+)<$";
    private static final String SMALLER_PATTERN = "<([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_]+)";
    private static final String CONTAINER_OPERATION_PATTERN = "([\\+\\-/\\*\\^])";

    private static final String RECIPE_CONTAINER_ARROW_RANGE_PATTERN = "^([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_\\[\\]]+)<--\\[(maximum|minimum|median|mode|average|random)\\]-->([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_\\[\\]]+)$";
    private static final String RECIPE_CONTAINER_LARGER_PATTERN = "^([0-9a-zA-Z\\+\\-\\*/\\(\\)$_\\[\\]]+)<\\[(maximum|minimum|median|mode|average|random)\\]$";
    private static final String RECIPE_CONTAINER_SMALLER_PATTERN = "^\\[(maximum|minimum|median|mode|average|random)\\]<([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_\\[\\]]+)$";
    private static final String RECIPE_CONTAINER_EQUAL_PATTERN = "^\\[(maximum|minimum|median|mode|average|random)\\]==([0-9a-zA-Z\\+\\-\\*/\\(\\)\\$_\\[\\]]+)$";

    private static final String MULTI_VALUE_PATTERN = "^\\(multi\\)\\(types:(.+)\\)(.+)$";
    private static final String MULTI_VALUE_CLASS_PATTERN = "([\\w]+)\\*([\\d]+)";

    private static final String USING_CONTAINER_VALUES_LORE_PATTERN = "^using_container_values_lore -> (.+)$";
    private static final String USING_CONTAINER_VALUES_ENCHANTMENT_PATTERN = "^using_container_values_enchantment -> enchantment:([\\$a-zA-Z0-9\\-_]+)/level:(\\$[a-z0-9\\-_]+|[0-9]+)$";
    private static final String USING_CONTAINER_VALUES_POTION_COLOR_RGB_PATTERN = "^using_container_valeus_potion_color -> type:(?i)(rgb)/value:R->([$a-z0-9\\-_]+),G->([$a-z0-9\\-_]+),B->([$a-z0-9\\-_]+)$";
    private static final String USING_CONTAINER_VALUES_POTION_COLOR_RANDOM_PATTERN = "^using_container_values_potion_color -> type:(?i)(random)$";
    private static final String USING_CONTAINER_VALUES_TOOL_DURABILITY_ABSOLUTE_PATTERN = "^using_container_values_tool_durability -> type:absolute/value:([$a-z0-9\\-_]+)$";
    private static final String USING_CONTAINER_VALUES_TOOL_DURABILITY_PERCENTAGE_PATTERN = "^using_container_values_tool_durability -> type:percentage/value:([$a-z0-9\\-_]+)$";
    private static final String USING_CONTAINER_VALUES_TEXTURE_ID_PATTERN = "^using_container_values_texture_id -> ([a-z0-9\\-_]+)$";
    private static final String USING_CONTAINER_VALUES_ITEM_NAME_PATTERN = "^using_container_values_item_name -> (.+)$";
    private static final int ENCHANTMENT_MAX_LEVEL = 255;

    public PersistentDataType getDataType(String input) {
        if (input.equalsIgnoreCase("string")) return PersistentDataType.STRING;
        if (input.equalsIgnoreCase("int")) return PersistentDataType.INTEGER;
        if (input.equalsIgnoreCase("double")) return PersistentDataType.DOUBLE;
        return null;
    }

    public Class getClassFromType(PersistentDataType type) {
        if (type.equals(PersistentDataType.INTEGER)) return Integer.class;
        if (type.equals(PersistentDataType.STRING)) return String.class;
        if (type.equals(PersistentDataType.DOUBLE)) return Double.class;
        return null;
    }

    public Map<Integer, ContainerWrapper> mattersLoader(Path path) {
        Map<Integer, ContainerWrapper> map = new LinkedHashMap<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        int current = 0;
        while (true) {
            if (!config.contains("terms."+current)) break;
            String thumb =  "terms."+current+".";

            String tag = config.getString(thumb+"tag");
            int order = config.getInt(thumb+"order");
            NamespacedKey key = new NamespacedKey(getInstance(), config.getString(thumb+"key"));
            PersistentDataType type = getDataType(config.getString(thumb+"type"));
            String value = "";
            if (config.contains(thumb+"value")) {
                value = config.getString(thumb+"value");
            }

            ContainerWrapper wrapper = new ContainerWrapper(key, type, value,order,tag);

            if (!map.containsKey(order)) map.put(order, wrapper);
            current++;
        }

        String name = config.getString("name");

        containers.put(name, map);
        return map;
    }

    public boolean isPassTargetEmpty(Matter source) {
        // target = always empty
        if (!source.hasContainer()) return true;
        Map<Integer, ContainerWrapper> wrappers = source.getContainerWrappers();
        for (Map.Entry<Integer, ContainerWrapper> entry : wrappers.entrySet()) {
            String tag = entry.getValue().getTag();
            if (tag.equals(ALLOW_TAG) || tag.equals(ALLOW_VALUE)) return false;
        }
        return true;
    }


    public boolean amorphousContainerCongruence(Recipe r, Recipe i) {
        List<Matter> recipes = r.getContentsNoAir();
        List<Matter> inputs = i.getContentsNoAir();


        A:for (int j=0;j<recipes.size();j++) {
            Matter recipe = recipes.get(j);
            if (!recipe.hasContainer()) continue;

            B:for (int k=0;k<inputs.size();k++) {
                Matter input = inputs.get(k);
                List<ContainerWrapper> wrappers = new ArrayList<>(recipe.getContainerWrappers().values());
                List<ContainerWrapper> has = new ArrayList<>(input.getContainerWrappers().values());
                if (!getContainerListCongruence(wrappers, has)) continue B;
                continue A;
            }
            return false;
        }
        return true;
    }

    private boolean getContainerListCongruence(List<ContainerWrapper> recipe, List<ContainerWrapper> in) {
        ItemStack dummyItemStack = new ItemStack(Material.STONE);
        ItemMeta meta = dummyItemStack.getItemMeta();
        PersistentDataContainer dummyContainer = meta.getPersistentDataContainer();
        for (ContainerWrapper wrapper : in) {
            NamespacedKey key = wrapper.getKey();
            PersistentDataType type = wrapper.getType();
            String stringValue = wrapper.getValue();
            if (type.equals(PersistentDataType.STRING)) dummyContainer.set(key, type, stringValue);
            if (type.equals(PersistentDataType.INTEGER)) dummyContainer.set(key, type, Integer.valueOf(stringValue));
            if (type.equals(PersistentDataType.DOUBLE)) dummyContainer.set(key, type, Double.valueOf(stringValue));
        }
        dummyItemStack.setItemMeta(meta);

        Matter dummyMatter = new Matter(new ItemStack(Material.STONE));
        Map<Integer, ContainerWrapper> map = new HashMap<>();
        for (int i=0;i<recipe.size();i++) {
            map.put(i, recipe.get(i));
        }
        dummyMatter.setContainerWrappers(map);
        return isPass(dummyItemStack, dummyMatter);
    }

    public boolean isPass(ItemStack item, Matter matter) {
        // item -> target, matter -> source (recipe)
        // matter > item
        if (!matter.hasContainer()) return true;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.isEmpty() && !hasNeededElements(matter)) return true;
        if (container.isEmpty() && hasNeededElements(matter)) return false;

        for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
            String tag = entry.getValue().getTag();
            if (tag.equals(STORE_ONLY)) continue;

            ContainerWrapper wrapper = entry.getValue();
            String value = entry.getValue().getValue();
            NamespacedKey key = wrapper.getKey();
            PersistentDataType type = wrapper.getType();
            if (tag.equals(ALLOW_TAG)) {
                if (!value.contains(",") && !value.startsWith("(multi)")) {
                    if (!container.has(key, type)) return false;
                }
                if (!multiTagCongruence(wrapper, container)) return false;
                continue;
            }

            if (tag.equals(DENY_TAG)) {
                if (!value.contains(",") && !value.startsWith("(multi)")) {
                    if (container.has(key, type)) return false;
                }
                if (!multiTagCongruence(wrapper, container)) return false;
                continue;
            }

            if (tag.equals(ALLOW_VALUE)) if (!container.has(key, type)) return false;
            if (tag.equals(DENY_VALUE)) {

                if (!container.has(key, type)) continue;
            }


            if (value.matches(ARROW_RANGE_PATTERN)) {
                if (!arrowPatternOperation(value, container, wrapper)) return false;
                continue;
            } else if (value.matches(SMALLER_PATTERN)) {
                if (!smallerPatternOperation(value, container, wrapper)) return false;
                continue;
            } else if (value.matches(LARGER_PATTERN)) {
                if (!largerPatternOperation(value, container, wrapper)) return false;
                continue;
            }


            boolean isAllow = tag.equals(ALLOW_VALUE);
            // isAllow = true -> ALLOW_VALUE
            // isAllow = false -> DENY_VALUE
            if (!isAllow && container.getKeys().isEmpty()) continue;
            if (isAllow && container.getKeys().isEmpty()) return false;

            if (type.equals(PersistentDataType.STRING)) {

                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher(String.valueOf(container.get(key, type)));
                if (matcher.matches() == isAllow) continue;
                return false;

            } else if (type.equals(PersistentDataType.INTEGER) || type.equals(PersistentDataType.DOUBLE)){
                PersistentDataType valueType;
                if ((valueType = getSpecifiedKeyType(container, key)) == null) return false;
                if (!valueType.equals(type)) return false;
                String element = String.valueOf(container.get(key, type));
                if (!element.matches(ContainerModify.NUMBERS_PATTERN)) return false;

                double current = Double.valueOf(value);
                double containerHas = Double.valueOf(element);
                if ((current == containerHas) == isAllow) continue;
                return false;
            }
        }
        return true;
    }

    private boolean multiTagCongruence(ContainerWrapper wrapper, PersistentDataContainer container) {
        String tag = wrapper.getTag();
        String value = wrapper.getValue();
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        if (!value.contains(",") && !value.startsWith("(multi)")) {
            if (tag.equals(ALLOW_TAG)) return container.has(key, type);
            if (tag.equals(DENY_TAG)) return !container.has(key, type);
        }

        Matcher matcher = Pattern.compile(MULTI_VALUE_PATTERN).matcher(value.replace(" ",""));
        if (!matcher.matches()) return false;
        List<String> types = new ArrayList<>(Arrays.asList(matcher.group(1).split(",")));
        List<String> variables = new ArrayList<>(Arrays.asList(matcher.group(2).split(",")));
        List<String> keyTypes = new ArrayList<>();

        for (String s : types) {
            Matcher m = Pattern.compile(MULTI_VALUE_CLASS_PATTERN).matcher(s);
            if (!m.matches()) return false;

            String currentType = m.group(1);
            int times = Integer.valueOf(m.group(2));
            keyTypes.addAll(Collections.nCopies(times, currentType));
        }

        if (variables.size() != keyTypes.size()) return false;

        for (int i=0;i<variables.size();i++) {
            NamespacedKey temporaryKey = new NamespacedKey(getInstance(), variables.get(i));
            PersistentDataType temporaryType = getDataType(keyTypes.get(i));
            if (tag.equals(ALLOW_TAG) && !container.has(temporaryKey, temporaryType)) return false;
            if (tag.equals(DENY_TAG) && container.has(temporaryKey, temporaryType)) return false;
        }
        return true;
    }

    private boolean hasNeededElements(Matter matter) {
        if (!matter.hasContainer()) return false;
        for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
            String tag = entry.getValue().getTag();

            if (tag.equals(ALLOW_TAG) || tag.equals(ALLOW_VALUE)) return true;
        }
        return false;
    }



    private boolean arrowPatternOperation(String source, PersistentDataContainer container, ContainerWrapper wrapper) {
        if (container == null) return false;
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        Pattern pattern = Pattern.compile(ARROW_RANGE_PATTERN);
        Matcher matcher = pattern.matcher(source);
        String tag = wrapper.getTag();
        if (!matcher.matches()) return false;

        double element;
        try{
            element = Double.valueOf(String.valueOf(container.get(key,type)));
        }catch (Exception e) {
            return false;
        }
        /*
        * examples
        * $store_1+$store_2<-->10
        * $store_1<-->$store_3
        *
        * $[KeyName]+1<-->$[KeyName]+1
         */
        double start = getFormulaValue(matcher.group(1), container);
        double end = getFormulaValue(matcher.group(2), container);
        if (tag.equals(ALLOW_VALUE)) {
            return start < element && element < end;
        } else if (tag.equals(DENY_VALUE)) {
            return element < start || end < element;
        }
        return false;
    }

    private boolean smallerPatternOperation(String source, PersistentDataContainer container, ContainerWrapper wrapper) {
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        String formula = source.substring(1, source.length());
        String tag = wrapper.getTag();

        double value;
        try{
            if (type.equals(PersistentDataType.INTEGER)) value = Integer.valueOf(String.valueOf(container.get(key, type)));
            if (type.equals(PersistentDataType.DOUBLE)) value = Double.valueOf(String.valueOf(container.get(key, type)));
            else return false;

        }catch (Exception e) {
            return false;
        }
        double target = getFormulaValue(formula, container);
        if (tag.equals(ALLOW_VALUE)) {
            return value < target;
        } else if (tag.equals(DENY_VALUE)) {
            return target < value;
        }
        return false;
    }

    private boolean largerPatternOperation(String source, PersistentDataContainer container, ContainerWrapper wrapper) {
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        String formula = source.substring(0,source.length()-1);
        String tag = wrapper.getTag();

        int element;
        try{
            element = Integer.valueOf(String.valueOf(container.get(key, type)));
        }catch (Exception e) {
            return false;
        }
        double target = getFormulaValue(formula, container);
        if (tag.equals(ALLOW_VALUE)) {
            return target < element;
        } else if (tag.equals(DENY_VALUE)) {
            return element < target;
        }
        return false;
    }

    private double getFormulaValue(String input, PersistentDataContainer container) {
        List<String> list = new ArrayList<>();
        List<String> buffer = new ArrayList<>();
        input = input.replace(" ","");
        for (int i=0;i<input.length();i++) {
            String s = String.valueOf(input.charAt(i));
            if (i == input.length()-1 && !buffer.isEmpty()) {
                String variableName = String.join("",buffer).replace("$","") + (s.equals(")") ? "" : s);
                buffer.clear();
                NamespacedKey key = new NamespacedKey(getInstance(), variableName);
                PersistentDataType type = PersistentDataType.DOUBLE;
                String value = container.has(key, type) ? container.get(key, type).toString() : "0";
                list.add(value);
                if (s.equals(")")) list.add(s);
                break;
            }

            if (s.equals("$")) {
                buffer.add(s);
                continue;
            }

            if (!buffer.isEmpty() && s.matches(CONTAINER_OPERATION_PATTERN)) {
                String variableName = String.join("", buffer).replace("$","");
                buffer.clear();
                NamespacedKey key = new NamespacedKey(getInstance(), variableName);
                PersistentDataType type = PersistentDataType.DOUBLE;
                String value = container.has(key, type) ? container.get(key, type).toString() : "0";
                list.add(value);
                list.add(s);
                continue;
            }

            if (!buffer.isEmpty()) {
                buffer.add(s);
                continue;
            }

            list.add(s);
        }

        String formula = String.join("", list);
        return calc(formula);

    }


    private double calc(String input) {

        if (input.matches("^([\\d]*)(\\.?)(\\d)+$")) return Double.valueOf(input);

        List<String> outQueue = new ArrayList<>();
        List<String> stack = new ArrayList<>();

        String buffer = "";

        for (int i=0;i<input.length();i++) {
            String s = String.valueOf(input.charAt(i));
            if (s.matches("\\d") || s.equals("~") || s.equals(".")) buffer += s;
            if (s.equals("(")) {
                if (!buffer.isEmpty()) {
                    if (buffer.contains("~")) buffer = buffer.replace("~","-");
                    outQueue.add(buffer);
                    buffer = "";
                }

                stack.add(0,s);
            }
            if (s.equals(")")) {
                if (!buffer.isEmpty()) {
                    if (buffer.contains("~")) buffer = buffer.replace("~","-");
                    outQueue.add( buffer);
                    buffer = "";
                }
                int start = stack.indexOf("(");
                outQueue.addAll(stack.subList(0, start));
                while (start > -1) {
                    stack.remove(0);
                    start--;
                }
                continue;
            }
            if (s.matches("(\\+|\\-|\\*|/|\\^)")) {
                if (!buffer.isEmpty()) {
                    if (buffer.contains("~")) buffer = buffer.replace("~","-");
                    outQueue.add( buffer);
                    buffer = "";
                }

                if (stack.size() == 0) {
                    stack.add(0,s);
                    continue;
                }

                int _newPriority = getPriority(s);
                int _collectedPriority = getPriority(stack.get(0));
                if (_newPriority > _collectedPriority) {
                    stack.add(0, s);
                    continue;
                }

                if (!s.equals("^")) {
                    if (!buffer.isEmpty()) {
                        if (buffer.contains("~")) buffer = buffer.replace("~","-");
                        outQueue.add( buffer);
                        buffer = "";
                    }
                    outQueue.add(stack.get(0));
                    stack.remove(0);
                }
                stack.add(0, s);
            }
        }
        if (!buffer.isEmpty()) outQueue.add(buffer);
        outQueue.addAll(stack);
        return rpnCalc(outQueue);
    }

    private int getPriority(String s) {
        if (s.equals("^")) return 4;
        if (s.equals("*")) return 3;
        if (s.equals("/")) return 3;
        if (s.equals("+")) return 2;
        if (s.equals("-")) return 2;
        return -1;
    }

    private double rpnCalc(List<String> list) {
        double accumlator = 0;
        List<Double> stacks = new ArrayList<>();
        for (String s : list) {
            if (!s.matches("(\\+|\\-|\\*|/|^)")) {
                String t = s.contains("~") ? s.replace("~", "-") : s ;
                stacks.add(Double.valueOf(t));
                continue;
            }
            double element1 = stacks.get(stacks.size()-1);
            double element2 = stacks.get(stacks.size()-2);
            if (s.equals("+")) accumlator = element2 + element1;
            if (s.equals("-")) accumlator = element2 - element1;
            if (s.equals("*")) accumlator = element2 * element1;
            if (s.equals("/")) accumlator = element2 / element1;
            if (s.equals("^")) accumlator = Math.pow(element2, element1);

            stacks.remove(element1);
            stacks.remove(element2);
            stacks.add(accumlator);
        }
        return accumlator;
    }


    public void setContainerDataItemStackToMatter(ItemStack item, Matter matter) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Map<Integer, ContainerWrapper> wrappers = new LinkedHashMap<>();

        List<NamespacedKey> keys = new ArrayList<>(container.getKeys());
        for (int i=0;i<keys.size();i++) {
            NamespacedKey key = keys.get(i);
            PersistentDataType type;
            if ((type = getSpecifiedKeyType(container, key)) == null) return;
            // order: i
            // tag: STORE_ONLY
            // value: value
            String value = String.valueOf(container.get(key, type));
            ContainerWrapper wrapper = new ContainerWrapper(key, type, value, i, STORE_ONLY);
            wrappers.put(i, wrapper);
        }
        matter.setContainerWrappers(wrappers);
    }

    public PersistentDataType getSpecifiedKeyType(PersistentDataContainer container, NamespacedKey key) {
        for (PersistentDataType type : getDefaultDataTypes()) {
            try{
                container.get(key, type);
                return type;
            }catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private List<PersistentDataType> getDefaultDataTypes() {
        List<PersistentDataType> types = new ArrayList<>();
        types.add(PersistentDataType.STRING);
        types.add(PersistentDataType.INTEGER);
        types.add(PersistentDataType.DOUBLE);
        return types;
    }

    public String containerValues(PersistentDataContainer container) {
        StringBuilder builder = new StringBuilder();
        for (NamespacedKey key : container.getKeys()) {
            builder.append(bar+nl);
            builder.append("key: "+key.toString()+nl);
            PersistentDataType type = getSpecifiedKeyType(container, key);
            builder.append("type: "+type.getComplexType().getSimpleName()+nl);
            builder.append("value: "+container.get(key, type).toString()+nl);
            builder.append(bar+nl);
        }
        return builder.toString();
    }

    public void setRecipeDataContainerToResultItem(ItemStack item, Recipe input, Recipe recipe) {
        // String -> NK, List<String> -> each containers data
        Map<String, List<String>> stringTypeData = new HashMap<>();
        Map<String, List<Double>> numericTypeData = new HashMap<>();
        for (Matter matter : input.getContentsNoAir()) {
            if (!matter.hasContainer()) continue;
            for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
                String key = entry.getValue().getKey().toString();
                String value = entry.getValue().getValue();
                PersistentDataType type = entry.getValue().getType();
                if (type.equals(PersistentDataType.STRING)) {
                    if (!stringTypeData.containsKey(key)) stringTypeData.put(key, new ArrayList<>());
                    stringTypeData.get(key).add(value);
                } else if (type.equals(PersistentDataType.INTEGER) || type.equals(PersistentDataType.DOUBLE)) {
                    if (!numericTypeData.containsKey(key)) numericTypeData.put(key, new ArrayList<>());
                    numericTypeData.get(key).add(Double.valueOf(value));
                }
            }
        }
        //---
        Map<String, Map<String, Double>> derivedMap = getDerivedValues(numericTypeData);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (Map.Entry<NamespacedKey, List<RecipeDataContainer>> entry : recipe.getContainer().entrySet()) {
            NamespacedKey key = entry.getKey();
            for (RecipeDataContainer rdc : entry.getValue()) {
                PersistentDataType type = rdc.getDataType();
                RecipeDataContainerModifyType modifyType = rdc.getModifyType();
                String term = rdc.getTerm();
                String action = rdc.getAction();
                boolean isEnd = rdc.isEnd();
                boolean isNumeric = type.equals(PersistentDataType.INTEGER) || type.equals(PersistentDataType.DOUBLE);

                if (modifyType.equals(RecipeDataContainerModifyType.MAKE)) {
                    if (type.equals(PersistentDataType.STRING)) container.set(key, type, "");
                    else container.set(key, type, 0);
                }

                if (!isNumeric) {
                    Set<Boolean> resultSet = new HashSet<>();
                    boolean isAllMatch = term.equalsIgnoreCase("[all_match]");
                    stringTypeData.get(key.toString()).forEach(e-> resultSet.add(e.matches(term)));
                    if ((!isAllMatch && resultSet.contains(true) || (isAllMatch && !resultSet.contains(false)))) {
                        //success
                        container.set(key, type, action);
                    }
                }

                Matcher arrow = Pattern.compile(RECIPE_CONTAINER_ARROW_RANGE_PATTERN).matcher(term);
                if (isNumeric && arrow.matches()) {
                    double start = getValueFromString(arrow.group(1), derivedMap);
                    double value = derivedMap.get(key.toString()).get(arrow.group(2));
                    double end = getValueFromString(arrow.group(3), derivedMap);
                    if (start < value && value < end) {
                        setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);
                    }
                }

                Matcher larger = Pattern.compile(RECIPE_CONTAINER_LARGER_PATTERN).matcher(term);
                if (isNumeric && larger.matches()) {
                    double small = getValueFromString(larger.group(1), derivedMap);
                    double value = derivedMap.get(key.toString()).get(larger.group(2));
                    if (small < value) setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);

                }

                Matcher smaller = Pattern.compile(RECIPE_CONTAINER_SMALLER_PATTERN).matcher(term);
                if (isNumeric && smaller.matches()) {
                    double value = derivedMap.get(key.toString()).get(smaller.group(1));
                    double large = getValueFromString(smaller.group(2), derivedMap);
                    if (value < large) setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);

                }

                Matcher equal = Pattern.compile(RECIPE_CONTAINER_EQUAL_PATTERN).matcher(term);
                if (isNumeric && equal.matches()) {
                    double value = derivedMap.get(key.toString()).get(equal.group(1));
                    double comparison = getValueFromString(equal.group(2), derivedMap);
                    if (value == comparison) setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);
                }

                if (isEnd) {
                    item.setItemMeta(meta);
                    return;
                }
            }
        }
        item.setItemMeta(meta);
    }

    private void setValuesToContainer(NamespacedKey key, PersistentDataType type, double value, PersistentDataContainer container) {
        if (type.equals(PersistentDataType.INTEGER)) container.set(key, type, (int) value);
        if (type.equals(PersistentDataType.DOUBLE)) container.set(key, type, value);
    }
    private double getValueFromString(String formula, Map<String, Map<String, Double>> derived) {
        List<String> queue = new ArrayList<>();
        List<String> buffer = new ArrayList<>();
        for (int i=0; i<formula.length(); i++) {
            String s = String.valueOf(formula.charAt(i));
            if (s.equals("$")) {
                buffer.add(s);
                continue;
            }

            if (s.matches("(\\+|\\-|/|\\*|\\(|\\)|\\^|\\[|\\])")) {
                if (buffer.size() == 0) {
                    queue.add(s);
                    continue;
                }
                String joined = String.join("", buffer);
                Matcher matcher = Pattern.compile("^\\$([a-z0-9_\\.\\-]+)\\[(.+)\\]$").matcher(joined);
                if (!matcher.matches()) continue;
                String key = matcher.group(1);
                String type = matcher.group(2);
                double value = derived.get(key).get(type);
                queue.add(String.valueOf(value));

                buffer.clear();
                continue;
            }

            queue.add(s);
        }
        return calc(String.join("", queue));
    }

    private Map<String, Map<String, Double>> getDerivedValues(Map<String, List<Double>> source) {
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : source.entrySet()) {
            String key = entry.getKey();
            List<Double> doubleList = entry.getValue();
            if (!result.containsKey(key)) result.put(key, new HashMap<>());
            if (doubleList.size() == 1) {
                result.get(key).put("maximum", doubleList.get(0));
                result.get(key).put("minimum", doubleList.get(0));
                result.get(key).put("median", doubleList.get(0));
                result.get(key).put("mode", doubleList.get(0));
                result.get(key).put("average", doubleList.get(0));
                result.get(key).put("random", doubleList.get(0));
                continue;
            }

            Collections.sort(doubleList);
            result.get(key).put("maximum", Collections.max(doubleList));
            result.get(key).put("minimum", Collections.min(doubleList));
            double median;
            if (doubleList.size() % 2 == 0) {
                median = (doubleList.get(doubleList.size() / 2) + doubleList.get(doubleList.size() / 2) - 1) / 2;
            } else {
                median = doubleList.get(doubleList.size() / 2);
            }
            result.get(key).put("median", median);

            double mode = 0d;
            double sum = 0d;
            Map<Double, Integer> counter = new HashMap<>();
            for (double d : doubleList) {
                int current = counter.containsKey(d) ? counter.get(d) : 0;
                counter.put(d, current + 1);
                sum += d;
            }
            int biggest = Collections.max(counter.values());
            int times = 0;
            for (Map.Entry<Double, Integer> e : counter.entrySet()) {
                if (e.getValue().equals(biggest)) {
                    mode += e.getKey();
                    times++;
                }
            }
            mode /= times;
            result.get(key).put("mode", mode);

            double average = sum / doubleList.size();
            result.get(key).put("average", average);

            double random = doubleList.get(new Random().nextInt(doubleList.size()));
            result.get(key).put("random", random);

        }
        return result;
    }


    private PersistentDataType getSpecifiedDataType(PersistentDataContainer container, NamespacedKey key) {
        List<PersistentDataType> types = Arrays.asList(PersistentDataType.STRING, PersistentDataType.INTEGER, PersistentDataType.DOUBLE);
        for (PersistentDataType type : types) {
            try{
                if (!container.has(key, type)) continue;
                return type;
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private ItemStack getCorrespondenceItemStack(Inventory inventory, Matter matter) {
        for (ItemStack item : new InventoryUtil().getItemStackFromCraftingMenu(inventory)) {
            if (matter.getCandidate().contains(item.getType())) return item;
        }
        return null;
    }


    public void setRecipeUsingContainerValueMetadata(Inventory inventory, Recipe recipe, ItemStack item) {
        ItemMeta resultMeta = item.getItemMeta();

        if (!recipe.hasUsingContainerValuesMetadata()) return;
        for (Matter matter : recipe.getContentsNoAir()) {
            if (!recipe.getUsingContainerValuesMetadata().containsKey(matter)) continue;
            ItemStack relate;
            if ((relate = getCorrespondenceItemStack(inventory, matter)) == null) continue;
            List<String> orders = recipe.getUsingContainerValuesMetadata().get(matter);
            if (orders == null || orders.isEmpty()) continue;

            for (String order : orders) {
                Matcher lore = Pattern.compile(USING_CONTAINER_VALUES_LORE_PATTERN).matcher(order);
                Matcher enchant = Pattern.compile(USING_CONTAINER_VALUES_ENCHANTMENT_PATTERN).matcher(order);
                Matcher rgb = Pattern.compile(USING_CONTAINER_VALUES_POTION_COLOR_RGB_PATTERN).matcher(order);
                Matcher randomColor = Pattern.compile(USING_CONTAINER_VALUES_POTION_COLOR_RANDOM_PATTERN).matcher(order);
                Matcher absoluteDurability = Pattern.compile(USING_CONTAINER_VALUES_TOOL_DURABILITY_ABSOLUTE_PATTERN).matcher(order);
                Matcher percentageDurability = Pattern.compile(USING_CONTAINER_VALUES_TOOL_DURABILITY_PERCENTAGE_PATTERN).matcher(order);
                Matcher texture = Pattern.compile(USING_CONTAINER_VALUES_TEXTURE_ID_PATTERN).matcher(order);
                Matcher displayName = Pattern.compile(USING_CONTAINER_VALUES_ITEM_NAME_PATTERN).matcher(order);
                Matcher attributeModifierNormal = Pattern.compile(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_PATTERN).matcher(order);
                Matcher attributeModifierEquipment = Pattern.compile(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_EQUIPMENT_SLOT_PATTERN).matcher(order);

                PersistentDataContainer source = relate.getItemMeta().getPersistentDataContainer();
                if (lore.matches()) setUsingContainerValuesLore(resultMeta, source, order);
                else if (enchant.matches()) setUsingContainerValuesEnchantment(resultMeta, source, order);
                else if (rgb.matches()) setUsingContainerValuesPotionColor(resultMeta, source, order);
                else if (randomColor.matches()) setUsingContainerValuesPotionColor(resultMeta, source, order);
                else if (absoluteDurability.matches()) setUsingContainerValuesToolDurability(item.getType(), resultMeta, source, order);
                else if (percentageDurability.matches()) setUsingContainerValuesToolDurability(item.getType(), resultMeta, source, order);
                else if (texture.matches()) setUsingContainerValuesTextureId(resultMeta, source, order);
                else if (displayName.matches()) setUsingContainerValuesItemName(resultMeta, source, order);
                else if (attributeModifierNormal.matches()) new AttributeModifierUtil().setAttributeModifierToResult(resultMeta, source, order);
                else if (attributeModifierEquipment.matches()) new AttributeModifierUtil().setAttributeModifierToResult(resultMeta, source, order);
                else {
                    Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES Metadata failed. (Illegal configuration format found.)");
                    continue;
                }
            }
        }
        item.setItemMeta(resultMeta);
    }

    private String getOrderElement(PersistentDataContainer source, String order, String pattern, int locate) {
        String buffer = "";
        StringBuilder result = new StringBuilder();
        Matcher matcher = Pattern.compile(pattern).matcher(order);
        if (!matcher.matches()) return "None";
        String parsed = matcher.group(locate);
        for (int i=0; i<parsed.length(); i++) {
            String s = String.valueOf(parsed.charAt(i));
            if (s.equals("{") && (i ==0 || !buffer.equals("\\"))) {
                int start = i + 1;
                int end = parsed.substring(i, parsed.length()).indexOf("}");
                String formula = parsed.substring(start, i + end);
                i += formula.length() + 1;
                if (formula.matches("^\\$([a-z0-9\\-_]+)$")) {
                    result.append(getContent(source, formula));
                    continue;
                }
                double value = getFormulaValue(formula, source);
                result.append(String.valueOf(value));
                buffer = String.valueOf(parsed.charAt(i));
                continue;
            }

            if (s.equals("{") && i != 0 && buffer.equals("\\")) {
                // delete char
                result.deleteCharAt(result.length()-1);
            }
            result.append(s);
            buffer = s;
        }
        return result.toString();
    }

    private void setUsingContainerValuesLore(ItemMeta meta, PersistentDataContainer source, String order) {
        meta.setLore(Arrays.asList(getOrderElement(source, order, USING_CONTAINER_VALUES_LORE_PATTERN, 1)));
    }

    @Deprecated
    private void setUsingContainerValuesEnchantment(ItemMeta meta, PersistentDataContainer source, String order) {
        Matcher matcher = Pattern.compile(USING_CONTAINER_VALUES_ENCHANTMENT_PATTERN).matcher(order);
        if (!matcher.matches()) return;
        Enchantment enchant;
        try{
            String matched = matcher.group(1);
            enchant = Enchantment.getByName((matched.contains("$") ? getContent(source, matched) : matched).toUpperCase());
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ENCHANTMENT failed. (Illegal EnchantmentName)");
            return;
        }

        int level;
        try{
            double preLevel = Double.valueOf(matcher.group(2).contains("$") ? getContent(source, matcher.group(2)) : matcher.group(2));
            level = (int) Math.round(ENCHANTMENT_MAX_LEVEL < preLevel ? ENCHANTMENT_MAX_LEVEL : preLevel);
            if (level < 1) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ENCHANTMENT failed. (Illegal Level Range. x < 1)");
                return;
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ENCHANTMENT failed. (Illegal Integer pattern. Does not follow ([0-9]+))");
            return;
        }
        meta.addEnchant(enchant, level, true);

    }

    private void setUsingContainerValuesPotionColor(ItemMeta meta, PersistentDataContainer source, String order) {
        Matcher rgb = Pattern.compile(USING_CONTAINER_VALUES_POTION_COLOR_RGB_PATTERN).matcher(order);
        Matcher random = Pattern.compile(USING_CONTAINER_VALUES_POTION_COLOR_RANDOM_PATTERN).matcher(order);
        if (rgb.matches()) {
            // rgb
            String red = rgb.group(1);
            String green = rgb.group(2);
            String blue = rgb.group(3);
            int RED ,GREEN, BLUE;
            try {
                RED = (int) Math.round(Double.valueOf(red.contains("$") ? getContent(source, red) : red));
                GREEN = (int) Math.round(Double.valueOf(green.contains("$") ? getContent(source, green) : green));
                BLUE = (int) Math.round(Double.valueOf(blue.contains("$") ? getContent(source, blue) : blue));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (Returned None Value or String type-value.)");
                return;
            }

            if (RED < 0 || 255 < RED) RED = RED < 0 ? 0 : 255;
            if (GREEN < 0 || 255 < GREEN) GREEN = GREEN < 0 ? 0 : 255;
            if (BLUE < 0 || 255 < BLUE) BLUE = BLUE < 0 ? 0 : 255;
            try{
                PotionMeta potionMeta = (PotionMeta) meta;
                potionMeta.setColor(Color.fromRGB(RED, GREEN, BLUE));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (PotionMeta cast failed.)");
                return;
            }


        } else if (random.matches()) {
            // random
            Random dice = new Random();
            int RED = dice.nextInt(256);
            int GREEN = dice.nextInt(256);
            int BLUE = dice.nextInt(256);
            try{
                PotionMeta potionMeta = (PotionMeta) meta;
                potionMeta.setColor(Color.fromRGB(RED, GREEN, BLUE));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (PotionMeta cast failed.)");
                return;
            }

        } else {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (Illegal pattern.)");
            return;
        }
    }

    private void setUsingContainerValuesTextureId(ItemMeta meta, PersistentDataContainer source, String order) {
        int id;
        try{
            id = (int) Math.round(Double.valueOf(getContent(source, order)));
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TEXTURE_ID failed. (Illegal texture id.)");
            return;
        }
        meta.setCustomModelData(id);
    }

    private void setUsingContainerValuesToolDurability(Material material,ItemMeta meta, PersistentDataContainer source, String order) {
        Matcher absolute = Pattern.compile(USING_CONTAINER_VALUES_TOOL_DURABILITY_ABSOLUTE_PATTERN).matcher(order);
        Matcher percentage = Pattern.compile(USING_CONTAINER_VALUES_TOOL_DURABILITY_PERCENTAGE_PATTERN).matcher(order);
        if (absolute.matches()) {
            // absolute
            int LAST_ONE = material.getMaxDurability() - 1;
            String variable = absolute.group(1);
            double value;
            try {
                value = Double.valueOf(getContent(source, variable));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Returned None Value or String type-value found.)");
                return;
            }
            Damageable damageable;
            try{
                damageable = (Damageable) meta;
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Illegal item.(Un-Damageable))");
                return;
            }
            int durability = material.getMaxDurability() - (int) Math.round(value);
            if (durability < 1 || material.getMaxDurability() < durability) durability = durability < 0 ? LAST_ONE : 0;
            damageable.setDamage(durability);

        } else if (percentage.matches()) {
            // percentage
            int LAST_ONE = material.getMaxDurability() - 1;
            String variable = percentage.group(1);
            double value;
            try {
                value = Double.valueOf(getContent(source, variable));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Returned None Value or String type-value found.)");
                return;
            }
            Damageable damageable;
            try {
                damageable = (Damageable) meta;
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Illegal item.(Un-Damageable))");
                return;
            }
            // value -> (%)
            if (value < 1 || 100 < value) value = value < 1 ? 99 : 0;
            int durability = (int) Math.round((100 - value) * 0.01 * material.getMaxDurability());
            if (durability < 1) durability = LAST_ONE;
            damageable.setDamage(durability);

        } else {
            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Illegal pattern.)");
            return;
        }

    }

    private void setUsingContainerValuesItemName(ItemMeta meta, PersistentDataContainer source, String order) {
        // modify item-display-name
        meta.setDisplayName(getOrderElement(source, order, USING_CONTAINER_VALUES_ITEM_NAME_PATTERN, 1));
    }

    public String getContent(PersistentDataContainer container, String order) {
        if (!order.contains("$")) return order;
        order = order.replace("$","");
        NamespacedKey key = new NamespacedKey(getInstance(), order);
        PersistentDataType type;
        try{
            type = getSpecifiedDataType(container, key);
        } catch (Exception e) {
            return "None";
        }
        if (type == null) return "None";
        if (!container.has(key, type)) return "None";
        return String.valueOf(container.get(key, type));
    }
}