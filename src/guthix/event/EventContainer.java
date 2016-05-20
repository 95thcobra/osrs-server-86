package guthix.event;

import guthix.model.Entity;
import guthix.model.entity.Player;

/**
 * Created by Sky on 3-3-2016.
 */
public class EventContainer {

    private Entity entity;
    private boolean isRunning;
    private int tick;
    private Event event;
    private int cyclesPassed;

    public EventContainer(Entity entity, int ticks, Event event) {
        this.entity = entity;
        this.event = event;
        this.isRunning = true;
        this.cyclesPassed = 0;
        this.tick = tick;
    }

    public void execute() {
        event.execute(this);
    }

    public void stop() {
        isRunning = false;
        event.stop();
    }

    public boolean needsExecution() {
        if (++this.cyclesPassed >= tick) {
            this.cyclesPassed = 0;
            return true;
        }
        return false;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }
}
