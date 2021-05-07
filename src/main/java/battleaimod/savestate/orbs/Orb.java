package battleaimod.savestate.orbs;

import com.megacrit.cardcrawl.orbs.*;

import java.util.function.Function;

public enum Orb {
    DARK(Dark.class, orb -> new DarkOrbState(orb), json -> new DarkOrbState(json)),
    EMPTY(EmptyOrbSlot.class, orb -> new EmptyOrbSlotState(orb), json -> new EmptyOrbSlotState(json)),
    FROST(Frost.class, orb -> new FrostOrbState(orb), json -> new FrostOrbState(json)),
    LIGHTNING(Lightning.class, orb -> new LightningOrbState(orb), json -> new LightningOrbState(json)),
    PLASMA(Plasma.class, orb -> new PlasmaOrbState(orb), json -> new PlasmaOrbState(json));

    public Class<? extends AbstractOrb> orbClass;
    public Function<AbstractOrb, OrbState> factory;
    public Function<String, OrbState> jsonFactory;

    Orb() {
    }

    Orb(Class<? extends AbstractOrb> orbClass, Function<AbstractOrb, OrbState> factory, Function<String, OrbState> jsonFactory) {
        this.orbClass = orbClass;
        this.factory = factory;
        this.jsonFactory = jsonFactory;
    }
}
