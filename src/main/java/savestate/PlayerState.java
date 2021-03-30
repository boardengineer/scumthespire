package savestate;

import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.stances.AbstractStance;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PlayerState extends CreatureState {
    private final AbstractPlayer.PlayerClass chosenClass;
    private final int gameHandSize;
    private final int masterHandSize;
    private final int startingMaxHP;
    private final ArrayList<CardState> masterDeck;
    private final ArrayList<CardState> drawPile;
    private final ArrayList<CardState> hand;
    private final ArrayList<CardState> discardPile;
    private final ArrayList<CardState> exhaustPile;
    private final ArrayList<CardState> limbo;
    private final ArrayList<RelicState> relics;
    private final ArrayList<AbstractBlight> blights;
    private final int potionSlots;
    private final ArrayList<AbstractPotion> potions;

    private final int energyManagerEnergy;
    private final int energyManagerMaxMaster;
    private final int energyPanelTotalEnergy;

    private final boolean isEndingTurn;
    private final boolean viewingRelics;
    private final boolean inspectMode;
    private final Hitbox inspectHb;
    private final int damagedThisCombat;
    private final String title;
    private final ArrayList<AbstractOrb> orbs;
    private final int masterMaxOrbs;
    private final int maxOrbs;
    private final AbstractStance stance;

    private final AbstractPlayer player;

    public PlayerState(AbstractPlayer player) {
        super(player);

        this.chosenClass = player.chosenClass;
        this.gameHandSize = player.gameHandSize;
        this.masterHandSize = player.masterHandSize;
        this.startingMaxHP = player.startingMaxHP;

        this.masterDeck = player.masterDeck.group.stream().map(CardState::new)
                                                 .collect(Collectors.toCollection(ArrayList::new));
        this.drawPile = player.drawPile.group.stream().map(CardState::new)
                                             .collect(Collectors.toCollection(ArrayList::new));
        this.hand = player.hand.group.stream().map(CardState::new)
                                     .collect(Collectors.toCollection(ArrayList::new));
        this.discardPile = player.discardPile.group.stream().map(CardState::new)
                                                   .collect(Collectors
                                                           .toCollection(ArrayList::new));
        this.exhaustPile = player.exhaustPile.group.stream().map(CardState::new)
                                                   .collect(Collectors
                                                           .toCollection(ArrayList::new));
        this.limbo = player.limbo.group.stream().map(CardState::new)
                                       .collect(Collectors.toCollection(ArrayList::new));

        this.relics = player.relics.stream().map(RelicState::new)
                                   .collect(Collectors.toCollection(ArrayList::new));
        this.blights = (ArrayList<AbstractBlight>) player.blights.clone();
        this.potionSlots = player.potionSlots;
        this.potions = (ArrayList<AbstractPotion>) player.potions.clone();

        this.energyManagerEnergy = player.energy.energy;
        this.energyPanelTotalEnergy = EnergyPanel.totalCount;

        this.energyManagerMaxMaster = player.energy.energyMaster;

        this.isEndingTurn = player.isEndingTurn;
        this.viewingRelics = player.viewingRelics;
        this.inspectMode = player.inspectMode;
        this.inspectHb = player.inspectHb;
        this.damagedThisCombat = player.damagedThisCombat;

        this.title = player.title;
        this.orbs = (ArrayList<AbstractOrb>) player.orbs.clone();
        this.masterMaxOrbs = player.masterMaxOrbs;
        this.maxOrbs = player.maxOrbs;
        this.stance = player.stance;

        this.player = player;
    }

    public AbstractPlayer loadPlayer() {
        super.loadCreature(player);

        player.chosenClass = this.chosenClass;
        player.gameHandSize = this.gameHandSize;
        player.masterHandSize = this.masterHandSize;
        player.startingMaxHP = this.startingMaxHP;

        player.masterDeck.group = this.masterDeck.stream().map(CardState::loadCard)
                                                 .collect(Collectors.toCollection(ArrayList::new));
        player.drawPile.group = this.drawPile.stream().map(CardState::loadCard)
                                             .collect(Collectors.toCollection(ArrayList::new));
        player.hand.group = this.hand.stream().map(CardState::loadCard)
                                     .collect(Collectors.toCollection(ArrayList::new));
        player.discardPile.group = this.discardPile.stream().map(CardState::loadCard)
                                                   .collect(Collectors
                                                           .toCollection(ArrayList::new));
        player.exhaustPile.group = this.exhaustPile.stream().map(CardState::loadCard)
                                                   .collect(Collectors
                                                           .toCollection(ArrayList::new));
        player.limbo.group = this.limbo.stream().map(CardState::loadCard)
                                       .collect(Collectors.toCollection(ArrayList::new));

        player.hand.refreshHandLayout();
        player.drawPile.refreshHandLayout();
        player.discardPile.refreshHandLayout();
        player.exhaustPile.refreshHandLayout();
        player.limbo.refreshHandLayout();

        player.relics = this.relics.stream().map(RelicState::loadRelic)
                                   .collect(Collectors.toCollection(ArrayList::new));
        player.blights = (ArrayList<AbstractBlight>) this.blights.clone();
        player.potionSlots = this.potionSlots;
        player.potions = (ArrayList<AbstractPotion>) this.potions.clone();

        player.energy.energy = this.energyManagerEnergy;
        player.energy.energyMaster = this.energyManagerMaxMaster;
        EnergyPanel.setEnergy(this.energyManagerEnergy);
        EnergyPanel.totalCount = energyPanelTotalEnergy;

        player.isEndingTurn = this.isEndingTurn;
        player.viewingRelics = this.viewingRelics;
        player.inspectMode = this.inspectMode;
        player.inspectHb = this.inspectHb;
        player.damagedThisCombat = this.damagedThisCombat;
        player.title = this.title;
        player.orbs = (ArrayList<AbstractOrb>) this.orbs.clone();
        player.masterMaxOrbs = this.masterMaxOrbs;
        player.maxOrbs = this.maxOrbs;
        player.stance = this.stance;

        return player;
    }

    public int getDamagedThisCombat() {
        return damagedThisCombat;
    }

    public String getHandString() {
        return String.format("hand:%s discard:%s", hand.stream().map(CardState::getName).sorted()
                                                       .collect(Collectors.joining(" ")),
                discardPile.stream().map(CardState::getName).collect(Collectors.joining(" ")));
    }
}
