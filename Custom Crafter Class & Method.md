Custom Crafter Class & Method

# Data Class
## Matter 
### Constructor
- Matter(String name, List \<Material> candidate, List \<EnchantWrap> wrap, int amount, boolean mass)

- Matter(List \<Material> candidate, int amount)

### Methods
- getter(name, candidate, wrap, amount, mass)
- setter(name, candidate, wrap, amount, mass)
---
- addCandidate(List \<Material> add) | void : Candidate のリストに Material を追加する
- hasWrap() | boolean : EnchantWrap のリストの中身が1つ以上あるか返す
- addWrap(EnchantWrap wrap) | void : EnchantWrap のリストに引数で与えられた EnchantWrap を加える
- addAllWrap(List \<EnchantWrap> add) | void : 渡されたリストの中身すべてを EnchantWrap のリストに加える
- isMass() | boolean : この Matter が，mass = true かどうかを返す
- getEnchantLevel(Enchantment enchant) | int : 引数に与えられたエンチャントが EnchantWrap のリストに含まれる場合はそのレベルを，含まれていない場合は -1 を返す
- getAllWrapInfo() | String : EnchantWrap のリストの情報を文字列にして返す．EnchantWrap のリストが isEmpty である場合は，空文字列を返す
- info() | String : この Matter に関する情報すべてを文字列にして返す．EnchantWrap のリストの中身が isEmpty である場合は当該箇所に "Null" が代入される

---

## Result
### Constructor
- Result(String name, Map \<Enchantment, Integer> enchantsInfo, int amount, Map \<MetadataType, List \<String>> metadata, String nameOrRegex, int matchPoint)

### Methods
- getter(name, enchantsInfo, amount, metadata, nameOrRegex, matchPoint)
- setter(name, enchantsInfo, amount, metadata, nameOrRegex, matchPoint)

---

## Recipe
### Constructor
- Recipe(String name, String tag, Map \<Coordinate, Matter> coordinate, Map \<Material, ItemStack> returnItems, Result result)

- Recipe()

### Methods
- getter(name, tag, coordinate, returnItems, result)
- setter(name, tag, coordinate, returnItems, result)
---
- addCoordinate(int x, int y, Matter matter) | void : 引数の x, y からCoordinate を生成し，Map \<Coordinate, Matter> に加える
- getContentsNoAir() | List \<Matter> : candidate が Material.AIR のみである Matter を覗いた coordinate の中身を返す
- getCoordinateList() | List \<Coordinate> : Map \<Coordinate, Matter> の鍵だけを返す
- getContentsNoDuplicate() | List \<Matter> : Map \<Coordinate, Matter> から重複なしの Matter を入れたリストを返す.
- getContentsNoDuplicateRelateAmount() | Map \<Matter, Integer> : getContentsNoDuplicate() で返されるリストの要素それぞれに個数を関係づけた Map を返す.
- getMassMaterialSet() | Set \<Material> : mass = true である Matter の candidate を Set に入れて返す．mass = true である Matter が存在しない場合は 初期状態の Set を返す
- getMatterFromCoordinate(Coordinate c) | Matter : Map \<Coordinate, Matter> の中で引数として与えられた Coordinate に関連付けられた Matter を返す．引数に関連付けられたMatter が存在しない場合は Null を返す

## EnchantWrap
### Constructor
- EnchantWrap(int level, Enchantment enchant, EnchantStrict strict)

### Methods
- getter(level, enchant, strict)
- setter(level, enchant, strict)

---

- info() | String : EnchantWrap の情報を文字列化して返す．EnchantWrap が Null である場合，エンチャント名は "Null", レベルは -1, Strict は "Null(Strict)" として表示される

---

## Coordinate
### Constructor
- Coordinate(int x, int y)

### Methods
- getter(x, y)
- setter(x, y)

---

- isSame(Coordinate coordinate) | boolean : 引数で与えられた Coordinate と等しいかどうかを返す


---


# Enum
## EnchantStrict
- Input : カスタムクラフターのクラフト画面から入力されたアイテムのエンチャントに付与される
- NotStrict : この EnchantWrap は同一性のチェック時に参照されない
- OnlyEnchant : この EnchantWrap は同一性のチェック時に，Enchantment の同一性のみを参照される
- Strict : この EnchantWrap は同一性のチェック時に，Enchantment の同一性とレベルが等しいかどうかを参照される

---

## Tag
- Normal : この Recipe が定型レシピであることを示す
- Amorphous : この Recipe が不定形レシピであることを示す

---

## MetadataType
- Lore : この値をキーに持つメタデータは，アイテムの説明文であることを示す
- DisplayName : この値をキーに持つメタデータは，アイテムの表示名であることを示す
- Enchantment : この値をキーに持つメタデータは，アイテムに付与するエンチャントに関する情報であることを示す
- ItemFlag : この値をキーに持つメタデータは，アイテムに付与する ItemFlag であることを示す
- Unbreakable : この値をキーに持つメタデータは，対象のアイテムの耐久を無限にするかどうかを示す
- CustomModelData : この値をキーに持つメタデータは，対象のアイテムに設定するカスタムモデルデータであることを示す

---

# Search Class
- Search -> Custom Recipe Seacher
- VanillaSearch -> Vanilla Recipe Seacher

## Search (Methods)

### main(Player player, Inventory inventory) | void
コンフィグファイルから作成したすべてのカスタムレシピと，カスタムクラフターの画面から取得したアイテムと配置を比較し，すべての要素で一致した最初のレシピを得る．そのレシピに紐づけられた成果物を，プレイヤーが開いているカスタムクラフターの画面，もしくはプレイヤーの座標にドロップする.  
  
全てのカスタムレシピと比較した結果，一致するものが存在しなかった場合には VanillaSearch に処理が引き渡される.  

### massSearch(Player player, Inventory inventory) | void
複数個のカスタムアイテムを一度に作成するために，カスタムクラフターの作成ボタンを右クリックした場合はこのメソッドが呼び出される.   
個数を調整する処理を除き， main とさして差はない.  

### isAllCandidateContains(Recipe recipe, Recipe input) | boolean
recipe に含まれるすべての Matter が持つ candidate に input が持つすべての Matter に含まれる candidate が含まれているかどうかを返す.  
  
-> 素材の整合性を取得する.  

### getMinimalAmount(Recipe recipe, Recipe input) | int
input に含まれる Matter のうち，mass = true でないもののうち最も少ないアイテム個数を返す.  
input に含まれる全ての Matter がmass = true である場合は -1 を返す.  

### getAllCandidateNoDuplicate(Recipe recipe) | Set \<Material>
recipe が持つ Matter の Candidate を重複なしの Set にして返す.  
全ての Matter が空気である Recipe を引数に与えた場合は，空の Set \<Material> を返す.  

### setResultItem(Inventory inventory, Recipe recipe, Recipe input, Player player, int amount) | void
recipe に含まれた成果物作成し inventory の成果物スロット，もしくは player の座標にドロップする.  

### setMetaData(ItemStack item, Result result) | void
result に含まれる成果物のメタデータを item に付与する.  

### getContainsMaterials(Recipe input) | List \<Material>
input に含まれる全ての Matter が持つ Candidate を重複なしのリストにして返す.  
空の Recipe を引数に取った場合，空の List \<Material> を返す.

### isSameMatter(Matter recipe, Matter input) | boolean
input が持つ情報と recipe が持つ情報が等しいものであるかを返す.  
getEnchantWrapCongruence を呼び出す.  

### getEnchantWrapCongruenceAmorphousWrap(Recipe recipe, Recipe input) | boolean
recipe と input のすべての Matter が持つエンチャント情報が等しいものであるかを返す.  
EnchantUtil#containsFromDoubleListを呼び出す.

### getEnchantWrapCongruence(Matter recipe, Matter input) | boolean
recipe と input が持つエンチャント情報が等しいものであるかどうかを返す.

### getEnchantmentList(List \<EnchantWrap> wrap) | List \<Enchantment> 
wrap に含まれる全ての Enchantment をリストにして返す.

### getEnchantWrap(ItemStack item) | List \<EnchantWrap>
item が持つエンチャント情報を EnchantWrap のリストにして返す.  
item がエンチャントを含まない場合は Null を返す.

### getCoordinateNoAir(Recipe recipe) | List \<Coordinate>
recipe が持つすべての配置の情報をリストにして返す.

### getTotal(Recipe recipe) | int
recipe に含まれる全ての Matter の個数を合計を返す.  
なお， Candidate に Material#AIR のみを持つ Matter は個数の合計に加算しない.

### getSquareSize(Recipe recipe) | int
アイテムが配置されている範囲を正方形で取得した際の一辺の長さを返す.

### isSameShape(List \<Coordinate> models, List \<Coordinate> reals) | boolean
reals を平行移動した場合 models に重なるかどうかを返す.

### toRecipe(Inventory inventory) | Recipe
inventory から Recipe を作成する.  
アイテムが配置されていないスロットは Candidate に Material＃AIR のみを持ち個数が0である Matter に変換される.  

--- 

## VanillaSearch (Methods)
### main(Player player, Inventory inventory, boolean batchBool) | void
inventory に含まれるアイテムを長さ9の ItemStack の配列に入れ，それを Bukkit#craftItem へ渡し成果物を得て， player が開いているカスタムクラフターの成果物スロット，もしくは player の座標にドロップする.    

inventory のアイテム配置を正方形として扱ったときの一辺の長さが 3 を超えた場合はその場で処理を終了する.

### getMinimalAmount(ItemStack[] items) | int
items に含まれる ItemStack の中で最も小さい個数を返す.  
items に空気以外のアイテムが含まれていない場合は -1 を返す.  

### getItemStacks(Inventory inventory, Coordinate start) | ItemStack[]
inventory の中から start が持つ座標を始点とする 3*3 の正方形のスロットのアイテムを，長さ9 の ItemStack の配列に入れる.  
start を始点とする 3*3 の範囲がカスタムクラフターのクラフト画面の範囲を飛び出る場合は，対象のスロットのアイテムを `ItemStack item = new Itemstack(Material.AIR);` とする

### getCoordinateList(Inventory inventory) | List \<Coordinate>
inventory に含まれる空気以外のクラフティングスロットを Coordinate に変換し リストに入れて返す.  
全てのクラフティングスロットにアイテムが配置されていない場合は空の List \<Coordinate> を返す

### getSquareSize(List \<Coordinate> list) | int
list から正方サイズを取得する  

---

# Listener  
## CloseCraftingTable
### onInventoryClose(InventoryCloseEvent event) | void
プレイヤーがカスタムクラフターの画面を閉じた際に呼び出される  
カスタムクラフターの画面を開いているプレイヤーのリストである `opening` から，対象のプレイヤーを削除する.  

### close(Player player, Inventory inventory) | void
カスタムクラフターの画面にアイテムが残っている場合，プレイヤーの座標にそれらをドロップする.  

---

## ModifyCraftingInventory
### onInventoryClick(InventoryClickEvent event) | void
カスタムクラフターの画面を開いているプレイヤーが，インベントリ内のアイテムを移動させた時に呼び出される.  
左右クリック，シフト押しながらの左右クリック以外の操作をした場合はイベントをキャンセルして処理を終了する.  
  
- 作成スロット(スロットNo.35)をクリックしたとき，成果物スロット(スロットNo.44)のアイテムをプレイヤーの座標にドロップした後にクリックタイプ別の処理を行う.
  - 左クリックをしたとき，Search#main を呼び出す  
  - 右クリックをしたとき，Search#massSearch を呼び出す

- 成果物スロットをクリックしたとき，そのスロットにアイテムが存在している場合はそのアイテムをプレイヤーの座標にドロップする.  
  
### getMinimalAmount(Inventory inventory) | int
渡された inventory のクラフティングスロットに存在するアイテムのうち，最も少ないアイテムの個数を返す.  
クラフティングスロットにアイテムが配置されていない場合, -1 を返す.  

---

## OpenCraftingTable
### onPlayerInteract(PlayerInteractEvent event) | void
プレイヤーが作業台をクリックした際に呼び出される.  
作業台の1マス下, 作業台を中心として 3*3 の範囲に存在するブロック全てがコンフィグの `baseBlock` で指定されたブロックと一致する場合，2 tick 後にカスタムクラフターの画面をプレイヤーに表示する.  
### setCraftingInventory() | void
クラフティングスロット外であることを示す黒ガラス，作成スロットに配置される金床をカスタムクラフターの画面に配置する.  
作成した画面は onPlayerInteract で呼び出される.  
SettingsLoad から呼び出される.  

---

# Data Loader
## SettingsLoad
### load() | void
デフォルトコンフィグを取得して main() を呼び出す.  

### getAllMaterialsName() | void
Material の全アイテムのアイテム名を文字列にしてリストに入れる.  

### main() | void
`baseBlock`, `results`, `matters`, `recipes` からそれぞれの設定ファイルを保存しているディレクトリを取得し，Matter, Result, Recipe を取得するメソッドを呼び出す.  

### configFileFirectoryCheck(Path path) | void
引数で与えられた path がディレクトリであるかを調べる．  
path が存在しない場合はディレクトリを作成する.  
path がファイルである場合は警告文をコンソールに出力して，プラグインをアンロードする．  

### getFiles(Path path) | List \<Path>
path に含まれる全てのファイルを取得する

### getBaseBlock(List \<Path> paths) | void
paths から作業台の下に設置しなくてはいけないブロックの名前を取得する.

### getResult(List \<Path> paths) | void
paths から情報を読み取り， Result を作成する.  

### getMatter(List \<Path> paths) | void
paths から情報を読み取り， Matter を作成する.  

### getCandidateFromRegex(String regexpattern) | List \<Material>
regexPattern にマッチする Material をリストに入れて返す.  

### getRecipe(List \<Path> paths) | void
paths から Recipe を作成する.  

---

# Util
## InventoryUtil
### getTableSlots(int size) | List \<Integer>
入力された大きさを一辺の大きさとするカスタムクラフターのクラフティングスロットの番号をリストにして返す.  

### getBlankCoordinates(int size) | List \<Integer>
カスタムクラフターのクラフト画面における黒ガラス板のスロット番号をリストにして返す.  

### decrementMaterials(Inventory inventory, Player player, int amount) | void
inventory のクラフト画面から amount で指定された量だけアイテムを減らす.

### decrementResult(Inventory inventory, Player player) | void
inventory の成果物スロットから指定された量だけアイテムを減らす.  

### returnItems(Recipe recipe, Inventory inventory, int removeAmount) | void
recipe に returnItems が設定されている場合、inventory から減らした量だけ対応するアイテムを player の座標にドロップする.  

---

## EnchantUtil
### getTargetMaterialMatters(Recipe recipe, Material target) | List \<Matter>
target を Candidate に含む Matter を　recipe から取得しリストにして返す.  
recipe に target を含む Matter が存在しない場合には空のリストを返す.  

### getTargetEnchantMatters(Recipe recipe, Enchantment target) | List \<Matter>
recipe が持つ Matter から target で指定されたエンチャントを EnchantWrap に持つものをリストに入れて返す.

### containsFromDoubleList(List \<List\<EnchantWrap>> list, Matter matter) | boolean
ダミーデータを作成して Search#getEnchantWrapCongruence に処理を渡し、 list と matter が持つ EnchantWrap の整合性を取得する.  
Search#main, Search#massSearch の amorphous レシピの処理において呼び出される.