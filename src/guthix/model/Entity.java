package guthix.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.fs.NpcDefinition;
import guthix.model.entity.*;
import guthix.model.entity.player.NpcSyncInfo;
import guthix.model.entity.player.PlayerSyncInfo;
import guthix.model.map.*;
import guthix.net.message.game.AddMessage;
import guthix.script.TimerKey;
import guthix.script.TimerRepository;
import guthix.util.CombatStyle;
import guthix.util.Varbit;

import java.util.*;

/**
 * Created by Bart Pelle on 8/22/2014.
 */
public abstract class Entity implements HitOrigin {

    private static final Logger logger = LogManager.getLogger(Entity.class);

    protected Tile tile;
    protected World world;
    protected int index;
    protected PathQueue pathQueue;
    protected Map<AttributeKey, Object> attribs = new EnumMap<>(AttributeKey.class);
    protected Queue<Hit> hits = new LinkedList<>();
    protected TimerRepository timers = new TimerRepository();
    private Map<Entity, Integer> damagers = new HashMap<>();

    private LockType lock = LockType.NONE;

    /**
     * Information on our synchronization
     */
    protected SyncInfo sync;

    public Entity() {
        this.tile = new Tile(0, 0, 0);
        this.pathQueue = new PathQueue(this);
    }

    public Entity(World world, Tile tile) {
        this.world = world;
        this.tile = new Tile(tile);
        this.pathQueue = new PathQueue(this);
    }

    public int index() {
        return index;
    }

    public void index(int i) {
        index = i;
    }

    public World world() {
        return world;
    }

    public void world(World w) {
        world = w;
    }

    public Tile tile() {
        return tile;
    }

    public void tile(Tile tile) {
        this.tile = tile;
    }

    public PathQueue pathQueue() {
        return pathQueue;
    }

    public TimerRepository timers() {
        return timers;
    }

    public void teleport(Tile tile) {
        teleport(tile.x, tile.z, tile.level);
    }

    public void teleport(int x, int z) {
        teleport(x, z, 0);
    }

    public void teleport(int x, int z, int level) {
        tile = new Tile(x, z, level);
        sync.teleported(true);
        pathQueue.clear();
    }

    public SyncInfo sync() {
        return sync;
    }

    public void tryAnimate(int id) {
        if (!sync.hasFlag(isPlayer() ? PlayerSyncInfo.Flag.ANIMATION.value : NpcSyncInfo.Flag.ANIMATION.value))
            animate(id); // TODO all the block animations QQ
    }

    public void animate(int id) {
        sync.animation(id, 0);
    }

    public void animate(int id, int delay) {
        sync.animation(id, delay);
    }

    public void graphic(int id) {
        sync.graphic(id, 0, 0);
    }

    public void freeze(int time) {
        if (!timers.has(TimerKey.FROZEN)) {
            timers.extendOrRegister(TimerKey.FROZEN, time);
            pathQueue.clear();
            if (isPlayer())
                ((Player) this).message("You have been frozen!");
        }
    }

    public void stun(int time) {
        if (!timers.has(TimerKey.STUNNED)) {
            timers.extendOrRegister(TimerKey.STUNNED, time);
            pathQueue.clear();
            if (isPlayer())
                ((Player) this).message("You have been stunned!");
        }
    }

    public void graphic(int id, int height, int delay) {
        sync.graphic(id, height, delay);
    }

    public <T> T attrib(AttributeKey key) {
        return (T) attribs.get(key);
    }

    public <T> T attrib(AttributeKey key, Object defaultValue) {
        return (T) attribs.getOrDefault(key, defaultValue);
    }

    public void clearattrib(AttributeKey key) {
        attribs.remove(key);
    }

    public void putattrib(AttributeKey key, Object v) {
        attribs.put(key, v);
    }

    public void walkTo(Tile tile, PathQueue.StepType mode) {
        walkTo(tile.x, tile.z, mode);
    }

    public void walkTo(int x, int z, PathQueue.StepType mode) {
        pathQueue.clear();

        // Are we frozen?
        if (frozen()) {
            message("A magical force stops you from moving.");
            return;
        }

        if (stunned()) {
            message("You're stunned!");
            return;
        }

        FixedTileStrategy target = new FixedTileStrategy(x, z);
        int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, size(), target, true, false);
        int[] bufferX = WalkRouteFinder.getLastPathBufferX();
        int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

        for (int i = steps - 1; i >= 0; i--) {
            pathQueue.interpolate(bufferX[i], bufferZ[i], mode);
        }
    }

    public void walkToNpc(Npc npc) {
        Tile npcTile = npc.tile();

        pathQueue.clear();

        // Are we frozen?
        if (frozen()) {
            message("A magical force stops you from moving.");
            return;
        }

        if (stunned()) {
            message("You're stunned!");
            return;
        }

        FixedTileStrategy target = new FixedTileStrategy(npcTile.x, npcTile.z);
        int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, 2, target, true, false);
        int[] bufferX = WalkRouteFinder.getLastPathBufferX();
        int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

        for (int i = steps - 1; i >= 0; i--) {
            pathQueue.interpolate(bufferX[i], bufferZ[i], PathQueue.StepType.REGULAR);
        }
    }

    public int size() {
        return 1;
    }

    public boolean walkTo(MapObj obj, PathQueue.StepType mode) {
        pathQueue.clear();

        // Are we frozen?
        if (frozen()) {
            message("A magical force stops you from moving.");
            return false;
        }

        if (stunned()) {
            message("You're stunned!");
            return false;
        }

        ObjectStrategy target = new ObjectStrategy(world, obj);
        int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, 1, target, true, false);
        int[] bufferX = WalkRouteFinder.getLastPathBufferX();
        int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

        for (int i = steps - 1; i >= 0; i--) {
            pathQueue.interpolate(bufferX[i], bufferZ[i], mode);
        }

        return !WalkRouteFinder.isAlternative;
    }

    public boolean frozen() {
        return timers().has(TimerKey.FROZEN);
    }

    public boolean stunned() {
        return timers().has(TimerKey.STUNNED);
    }

    public Tile stepTowards(Entity e, int maxSteps) {
        return stepTowards(e, e.tile, maxSteps);
    }

    public Tile stepTowards(Entity e) {
        return stepTowards(e, e.tile, 20);
    }

    public Tile stepTowards(Entity e, Tile t, int maxSteps) {
        if (e == null)
            return tile;

        EntityStrategy target = new EntityStrategy(t);
        int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, 1, target, true, false);
        int[] bufferX = WalkRouteFinder.getLastPathBufferX();
        int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

        Tile last = tile;
        for (int i = steps - 1; i >= 0; i--) {
            maxSteps -= pathQueue.interpolate(bufferX[i], bufferZ[i], PathQueue.StepType.REGULAR, maxSteps);

            last = new Tile(bufferX[i], bufferZ[i], tile.level);
            if (maxSteps <= 0)
                break;
        }

        return last;
    }

    public boolean touches(Entity e) {
        return touches(e, tile);
    }

    public boolean touches(Entity e, Tile from) {
        EntityStrategy target = new EntityStrategy(e);
        int[][] clipAround = world.clipAround(e.tile(), 5); // TODO better algo for determining the size we need..
        return target.canExit(from.x, from.z, 1, clipAround, e.tile.x - 5, e.tile.z - 5);
    }

    public boolean locked() {
        return lock == LockType.FULL;
    }

    public boolean moveLocked() {
        return lock == LockType.MOVEMENT;
    }

    public void lock() {
        lock = LockType.FULL;
    }

    public void lockMovement() {
        lock = LockType.MOVEMENT;
    }

    public void unlock() {
        lock = LockType.NONE;
    }

    public abstract void hp(int hp, int exceed);

    public abstract int hp();

    public abstract int maxHp();

    public void message(String format, Object... params) {
        // Stub to ease player-specific messaging
    }

    public void heal(int amount) {
        heal(amount, 0);
    }

    public void heal(int amount, int exceed) {
        hp(hp() + amount, exceed);
    }

    public Hit hit(HitOrigin origin, int hit) {
        return hit(origin, hit, 0);
    }

    public Hit hit(HitOrigin origin, int hit, int delay) {
        return hit(origin, hit, delay, null);
    }

    public Hit hit(HitOrigin origin, int hit, int delay, Hit.Type type) {
        Hit h = new Hit(hit, type != null ? type : hit > 0 ? Hit.Type.REGULAR : Hit.Type.MISS, delay).origin(origin);
        hits.add(h);

        if (origin instanceof Player) {
            damagers.compute(((Player) origin), (key, value) -> value == null ? hit : value + hit);
        }

        if ((boolean) attrib(AttributeKey.VENGEANCE_ACTIVE, false)) {
            if (isPlayer() && origin instanceof Entity) {
                clearattrib(AttributeKey.VENGEANCE_ACTIVE);
                ((Entity) origin).hit(this, (int) (hit * 0.75), delay).block(false);
                //TODO Taste vengeance
            }
        }

        return h;
    }

    public Hit hit(HitOrigin origin, int hit, Hit.Type type) {
        return hit(origin, hit, 0, type);
    }

    public void blockHit() {
        animate(424);
    }

    public Map<Entity, Integer> damagers() {
        return damagers;
    }

    public Entity killer() {
        if (damagers.isEmpty())
            return null;

        Comparator<Map.Entry<Entity, Integer>> valueComparator = (e1, e2) -> e1.getValue().compareTo(e2.getValue());
        return damagers.entrySet().stream().sorted(valueComparator).findFirst().orElse(null).getKey();
    }

    public boolean dead() {
        //int queuedDamage = hits.stream().mapToInt(Hit::damage).sum();
        return hp()/* - queuedDamage*/ < 1;
    }

    public void stopActions(boolean cancelMoving) {
        world.getEventHandler().stopEvents(this);
        //world.server().scriptExecutor().interruptFor(this);
        sync.faceEntity(null);
        animate(-1);
        graphic(-1);
        if (cancelMoving)
            pathQueue.clear();
    }

    public void face(Entity e) {
        sync.faceEntity(e);
    }

    public void faceObj(MapObj obj) {
        int x = obj.tile().x;
        int z = obj.tile().z;

        // Do some trickery to face properly
        if (tile.x == x && tile.z == z && (obj.type() == 0 || obj.type() == 5)) {
            if (obj.rot() == 0) {
                x--;
            } else if (obj.rot() == 1) {
                z++;
            } else if (obj.rot() == 2) {
                x++;
            } else if (obj.rot() == 3) {
                z--;
            }
        }

        int sx = obj.definition(world).sizeX;
        int sz = obj.definition(world).sizeY;

        sync.facetile(new Tile((int) (x * 2) + sx, (int) (z * 2) + sz));
    }

    public void faceTile(Tile tile) {
        sync.facetile(new Tile(tile.x * 2 + 1, tile.z * 2 + 1));
    }

    public void faceTile(double x, double z) {
        sync.facetile(new Tile((int) (x * 2) + 1, (int) (z * 2) + 1));
    }

    public void cycle() {
        timers.cycle();

        // Only process hits if not locked!
        if (!locked() && hp() > 0) {
            for (Iterator<Hit> it = hits.iterator(); it.hasNext() && hp() > 0; ) {
                Hit hit = it.next();

                // TODO decrease delay
                if (hit.delay() <= 0) {
                    int damage = hit.damage();

                    // Protection prayers :)
                    if (isPlayer()) {
                        Player us = (Player) this;
                        if (us.varps().varbit(Varbit.PROTECT_FROM_MELEE) == 1 && hit.style() == CombatStyle.MELEE) {
                            damage -= damage * 0.4;
                        } else if (us.varps().varbit(Varbit.PROTECT_FROM_MAGIC) == 1 && hit.style() == CombatStyle.MAGIC) {
                            damage -= damage * 0.4;
                        } else if (us.varps().varbit(Varbit.PROTECT_FROM_MISSILES) == 1 && hit.style() == CombatStyle.RANGE) {
                            damage -= damage * 0.4;
                        }
                    }

                    if (damage > hp())
                        damage = hp();

                    hp(hp() - damage, 0);
                    sync.hit(hit.type().ordinal(), damage);

                    if (hit.graphic() >= 0)
                        graphic(hit.graphic());

                    if (hit.block())
                        blockHit();

                    it.remove();
                } else {
                    hit.delay(hit.delay() - 1);
                }
            }
        }

        if (hp() < 1 && !locked()) { // Avoid dieing while doing something critical!
            hits.clear();
            die();
        }
    }

    public abstract boolean isPlayer();

    public abstract boolean isNpc();

    protected abstract void die();

}
