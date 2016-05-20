package guthix.script;

/**
 * Created by Bart on 7/9/2015.
 * <p>
 * The man of the triggers.
 */
public class ScriptRepository {

    // this is where everything is categorized and handled
    /*private static final Logger logger = LogManager.getLogger(ScriptRepository.class);

    private List<Function1<Script, Unit>> loginTriggers = new LinkedList<>();
    private Map<Integer, Function1<Script, Unit>> buttonTriggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> npcSpawnTriggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> objectTriggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> spellOnPlayerTriggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> itemOnItemTriggers = new HashMap<>();

    private Map<Integer, Function1<Script, Unit>> regionEnterTriggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> regionExitTriggers = new HashMap<>();
    private Map<TimerKey, Function1<Script, Unit>> timerTriggers = new EnumMap<>(TimerKey.class);

    private Map<Integer, Function1<Script, Unit>> item1Triggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> item4Triggers = new HashMap<>();

    private Map<Integer, Function1<Script, Unit>> equipment1Triggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> equipment2Triggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> equipment3Triggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> equipment4Triggers = new HashMap<>();
    private Map<Integer, Function1<Script, Unit>> equipment5Triggers = new HashMap<>();

    private ScriptExecutor executor;

    public ScriptRepository(ScriptExecutor executor) {
        this.executor = executor;
    }

    public void load() {
        // Clear all existing script containers
        loginTriggers.clear();
        buttonTriggers.clear();
        npcSpawnTriggers.clear();
        objectTriggers.clear();
        spellOnPlayerTriggers.clear();
        itemOnItemTriggers.clear();;
        regionEnterTriggers.clear();
        regionExitTriggers.clear();
        timerTriggers.clear();
        item1Triggers.clear();
        item4Triggers.clear();
        equipment1Triggers.clear();
        equipment2Triggers.clear();
        equipment3Triggers.clear();
        equipment4Triggers.clear();
        equipment5Triggers.clear();

        Set<Method> methods = new Reflections("server.content", new SubTypesScanner(false), new MethodAnnotationsScanner()).getMethodsAnnotatedWith(ScriptMain.class);
        methods.forEach(m -> {
            if (!m.getDeclaringClass().getName().contains("$") && !m.getDeclaringClass().getName().endsWith("Package")) {
                try {
                    m.invoke(null, this);
                } catch (Exception e) {
                    logger.error("Error loading script {}. Could not invoke method.", m.getDeclaringClass().getSimpleName(), e);
                }
            }
        });
    }

    public ScriptExecutor executor() {
        return executor;
    }

    public void onLogin(Function1<Script, Unit> script) {
        loginTriggers.add(script);
    }

    public void onButton(int i, int c, Function1<Script, Unit> script) {
        buttonTriggers.put((i << 16) | c, script);
    }

    public void onObject(int id, Function1<Script, Unit> script) {
        objectTriggers.put(id, script);
    }

    public void onSpellOnPlayer(int i, int c, Function1<Script, Unit> script) {
        spellOnPlayerTriggers.put((i << 16) | c, script);
    }

    public void onItemOnItem(int itemUsed, int itemUsedWith, Function1<Script, Unit> script) {
        itemOnItemTriggers.put((itemUsed << 16) | itemUsedWith, script);
    }

    public void onNpcSpawn(int i, Function1<Script, Unit> script) {
        npcSpawnTriggers.put(i, script);
    }

    public void onRegionEnter(int region, Function1<Script, Unit> script) {
        regionEnterTriggers.put(region, script);
    }

    public void onItemOption1(int item, Function1<Script, Unit> script) {
        item1Triggers.put(item, script);
    }

    public void onItemOption4(int item, Function1<Script, Unit> script) {
        item4Triggers.put(item, script);
    }

    public void onEquipmentOption(int opt, int item, Function1<Script, Unit> script) {
        switch (opt) {
            case 1:
                equipment1Triggers.put(item, script);
                break;
            case 2:
                equipment2Triggers.put(item, script);
                break;
            case 3:
                equipment3Triggers.put(item, script);
                break;
            case 4:
                equipment4Triggers.put(item, script);
                break;
            case 5:
                equipment5Triggers.put(item, script);
                break;
        }
    }

    public void onTimer(TimerKey timer, Function1<Script, Unit> script) {
        timerTriggers.put(timer, script);
    }

    public void triggerLogin(Player player) {
        loginTriggers.forEach(t -> executor.executeScript(player, t));
    }

    public void triggerButton(Player player, int i, int c, int s, int action) {
        player.putattrib(AttributeKey.BUTTON_SLOT, s);
        player.putattrib(AttributeKey.BUTTON_ACTION, action);
        if (buttonTriggers.containsKey((i << 16) | c)) {
            executor.executeScript(player, buttonTriggers.get((i << 16) | c));
        }
    }

    public void triggerNpcSpawn(Npc n) {
        if (npcSpawnTriggers.containsKey(n.id())) {
            executor.executeScript(n, npcSpawnTriggers.get(n.id()));
        }
    }

    public boolean triggerObject(Player player, MapObj obj, int action) {
        player.putattrib(AttributeKey.INTERACTION_OBJECT, obj);
        player.putattrib(AttributeKey.INTERACTION_OPTION, action);
        if (objectTriggers.containsKey(obj.id())) {
            executor.executeScript(player, objectTriggers.get(obj.id()));
            return true;
        } else {
            return false;
        }
    }

    public void triggerSpellOnPlayer(Player player, int i, int c) {
        if (spellOnPlayerTriggers.containsKey((i << 16) | c)) {
            executor.executeScript(player, spellOnPlayerTriggers.get((i << 16) | c));
        }
    }

    public void triggerItemOnItem(Player player, int itemUsed, int itemUsedWith) {
        if (itemOnItemTriggers.containsKey((itemUsed << 16) | itemUsedWith)) {
            executor.executeScript(player, itemOnItemTriggers.get((itemUsed << 16) | itemUsedWith));
        }
    }

    public void triggerRegionEnter(Player player, int region) {
        if (regionEnterTriggers.containsKey(region)) {
            executor.executeScript(player, regionEnterTriggers.get(region));
        }
    }

    public void triggerTimer(Player player, TimerKey timer) {
        if (timerTriggers.containsKey(timer)) {
            executor.executeScript(player, timerTriggers.get(timer));
        }
    }

    public void triggerItemOption1(Player player, int item, int slot) {
        if (item1Triggers.containsKey(item)) {
            player.putattrib(AttributeKey.ITEM_SLOT, slot);
            executor.executeScript(player, item1Triggers.get(item));
        }
    }

    public void triggerItemOption4(Player player, int item, int slot) {
        if (item4Triggers.containsKey(item)) {
            player.putattrib(AttributeKey.ITEM_SLOT, slot);
            executor.executeScript(player, item4Triggers.get(item));
        }
    }

    public void triggerEquipmentOption(Player player, int item, int slot, int opt) {
        Function1<Script, Unit> fnc = null;
        switch (opt) {
            case 1:
                fnc = equipment1Triggers.get(item);
                break;
            case 2:
                fnc = equipment2Triggers.get(item);
                break;
            case 3:
                fnc = equipment3Triggers.get(item);
                break;
            case 4:
                fnc = equipment4Triggers.get(item);
                break;
            case 5:
                fnc = equipment5Triggers.get(item);
                break;
        }

        if (fnc != null) {
            player.putattrib(AttributeKey.ITEM_SLOT, slot);
            executor.executeScript(player, fnc);
        }
    }
*/
}
