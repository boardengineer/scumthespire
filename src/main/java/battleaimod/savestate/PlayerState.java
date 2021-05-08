package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.savestate.orbs.OrbState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class PlayerState extends CreatureState {
    private static final String CARD_DELIMETER = ";;;";
    private static final String RELIC_DELIMETER = "!;!";

    private final AbstractPlayer.PlayerClass chosenClass;
    private final int gameHandSize;
    private final int masterHandSize;
    private final int startingMaxHP;

    private final int energyManagerEnergy;
    private final int energyManagerMaxMaster;
    private final int energyPanelTotalEnergy;

    private final boolean isEndingTurn;
    private final boolean viewingRelics;
    private final boolean inspectMode;
    private final HitboxState inspectHb;
    private final int damagedThisCombat;
    private final String title;

    private final boolean isDead;
    private final boolean renderCorpse;

    public final ArrayList<CardState> masterDeck;
    public final ArrayList<CardState> drawPile;
    public final ArrayList<CardState> hand;
    public final ArrayList<CardState> discardPile;
    public final ArrayList<CardState> exhaustPile;
    public final ArrayList<CardState> limbo;

    public int maxOrbs;
    public ArrayList<OrbState> orbs;

    public final ArrayList<PotionState> potions;

    private final ArrayList<RelicState> relics;

    public PlayerState(AbstractPlayer player) {
        super(player);

        long startPlayerLoad = System.currentTimeMillis();

        this.chosenClass = player.chosenClass;
        this.gameHandSize = player.gameHandSize;
        this.masterHandSize = player.masterHandSize;
        this.startingMaxHP = player.startingMaxHP;

        this.masterDeck = toCardStateArray(player.masterDeck.group);
        this.drawPile = toCardStateArray(player.drawPile.group);
        this.hand = toCardStateArray(player.hand.group);
        this.discardPile = toCardStateArray(player.discardPile.group);
        this.exhaustPile = toCardStateArray(player.exhaustPile.group);
        this.limbo = toCardStateArray(player.limbo.group);

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Save Time Save Player Decks", System
                    .currentTimeMillis() - startPlayerLoad);
        }

        long relicSaveStart = System.currentTimeMillis();

        this.relics = player.relics.stream().map(RelicState::new)
                                   .collect(Collectors.toCollection(ArrayList::new));

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Save Time Save Player Relics", System
                    .currentTimeMillis() - relicSaveStart);
        }


        this.orbs = player.orbs.stream()
                               .map(orb -> BattleAiMod.orbByClassMap.get(orb.getClass()).factory
                                       .apply(orb))
                               .collect(Collectors.toCollection(ArrayList::new));

        this.potions = player.potions.stream().map(PotionState::new)
                                     .collect(Collectors.toCollection(ArrayList::new));

        this.energyManagerEnergy = player.energy.energy;
        this.energyPanelTotalEnergy = EnergyPanel.totalCount;

        this.energyManagerMaxMaster = player.energy.energyMaster;

        this.isEndingTurn = player.isEndingTurn;
        this.viewingRelics = player.viewingRelics;
        this.inspectMode = player.inspectMode;

        this.isDead = player.isDead;
        this.renderCorpse = ReflectionHacks
                .getPrivate(player, AbstractPlayer.class, "renderCorpse");

        this.inspectHb = player.inspectHb == null ? null : new HitboxState(player.inspectHb);
        this.damagedThisCombat = player.damagedThisCombat;
        this.maxOrbs = player.maxOrbs;


        this.title = player.title;
    }

    public PlayerState(String jsonString) {
        super(new JsonParser().parse(jsonString).getAsJsonObject().get("creature").getAsString());

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.chosenClass = AbstractPlayer.PlayerClass
                .valueOf(parsed.get("chosen_class_name").getAsString());
        this.gameHandSize = parsed.get("game_hand_size").getAsInt();
        this.masterHandSize = parsed.get("master_hand_size").getAsInt();
        this.startingMaxHP = parsed.get("starting_max_hp").getAsInt();

        this.energyManagerEnergy = parsed.get("energy_manager_energy").getAsInt();
        this.energyPanelTotalEnergy = parsed.get("energy_panel_total_energy").getAsInt();
        this.energyManagerMaxMaster = parsed.get("energy_manager_max_master").getAsInt();

        this.isEndingTurn = parsed.get("is_ending_turn").getAsBoolean();
        this.viewingRelics = parsed.get("viewing_relics").getAsBoolean();
        this.inspectMode = parsed.get("inspect_mode").getAsBoolean();
        this.inspectHb = parsed.get("inspect_hb").isJsonNull() ? null : new HitboxState(parsed
                .get("inspect_hb").getAsString());
        this.damagedThisCombat = parsed.get("damaged_this_combat").getAsInt();

        this.title = parsed.get("title").getAsString();

        this.masterDeck = decodeCardList(parsed.get("master_deck").getAsString());
        this.drawPile = decodeCardList(parsed.get("draw_pile").getAsString());
        this.hand = decodeCardList(parsed.get("hand").getAsString());
        this.discardPile = decodeCardList(parsed.get("discard_pile").getAsString());
        this.exhaustPile = decodeCardList(parsed.get("exhaust_pile").getAsString());
        this.limbo = decodeCardList(parsed.get("limbo").getAsString());

        this.relics = Stream.of(parsed.get("relics").getAsString().split(RELIC_DELIMETER))
                            .filter(s -> !s.isEmpty()).map(RelicState::new)
                            .collect(Collectors.toCollection(ArrayList::new));

        this.potions = new ArrayList<>();
        parsed.get("potions").getAsJsonArray().forEach(potionElement -> this.potions
                .add(new PotionState(potionElement.getAsString())));

        this.maxOrbs = parsed.get("max_orbs").getAsInt();
        this.orbs = new ArrayList<>();
        parsed.get("orbs").getAsJsonArray().forEach(orbElement -> this.orbs
                .add(OrbState.forJsonString(orbElement.getAsString())));

        //TODO
        this.isDead = false;
        this.renderCorpse = false;
    }

    public AbstractPlayer loadPlayer() {
        AbstractPlayer player = CardCrawlGame.characterManager.getCharacter(chosenClass);
        super.loadCreature(player);

        player.chosenClass = this.chosenClass;
        player.gameHandSize = this.gameHandSize;
        player.masterHandSize = this.masterHandSize;
        player.startingMaxHP = this.startingMaxHP;

        long freeCardStart = System.currentTimeMillis();

        CardState.freeCardList(player.masterDeck.group);
        CardState.freeCardList(player.drawPile.group);
        CardState.freeCardList(player.hand.group);
        CardState.freeCardList(player.discardPile.group);
        CardState.freeCardList(player.exhaustPile.group);
        CardState.freeCardList(player.limbo.group);

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Load Time Player Free Card", System
                            .currentTimeMillis() - freeCardStart);
        }

        long loadDecksStart = System.currentTimeMillis();

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

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Load Time Player Decks Populated", System
                            .currentTimeMillis() - loadDecksStart);
        }

        long relicStartTime = System.currentTimeMillis();

        player.relics = this.relics.stream().map(RelicState::loadRelic)
                                   .collect(Collectors.toCollection(ArrayList::new));

        if (!shouldGoFast()) {
            AbstractDungeon.topPanel.adjustRelicHbs();
            for (int i = 0; i < player.relics.size(); i++) {
                player.relics.get(i).instantObtain(player, i, false);
            }
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Load Time Player Relics", System
                            .currentTimeMillis() - relicStartTime);
        }

        player.potions = this.potions.stream().map(PotionState::loadPotion)
                                     .collect(Collectors.toCollection(ArrayList::new));


        for (int i = 0; i < player.potions.size(); i++) {
            player.potions.get(i).setAsObtained(i);
        }

        player.orbs = this.orbs.stream().map(OrbState::loadOrb)
                               .collect(Collectors.toCollection(ArrayList::new));

        player.energy.energy = this.energyManagerEnergy;
        player.energy.energyMaster = this.energyManagerMaxMaster;
        EnergyPanel.setEnergy(this.energyManagerEnergy);
        EnergyPanel.totalCount = energyPanelTotalEnergy;

        player.isEndingTurn = this.isEndingTurn;
        player.viewingRelics = this.viewingRelics;
        player.inspectMode = this.inspectMode;
        player.inspectHb = this.inspectHb == null ? null : this.inspectHb.loadHitbox();
        player.damagedThisCombat = this.damagedThisCombat;
        player.title = this.title;
        player.maxOrbs = this.maxOrbs;

        ReflectionHacks
                .setPrivate(player, AbstractPlayer.class, "renderCorpse", this.renderCorpse);


        if (!shouldGoFast()) {
            for(int i = 0; i < player.orbs.size(); i++) {
                player.orbs.get(i).setSlot(i, player.maxOrbs);
            }
            player.update();
        }

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

    public int getNumSlimes() {
        return getNumInstance("Slimed");
    }

    public int getNumBurns() {
        return getNumInstance("Burn");
    }


    public int getNumInstance(String cardId) {
        long numInstances = discardPile.stream().filter(card -> card.getName().equals(cardId))
                                       .count();

        numInstances += hand.stream().filter(card -> card.getName().equals(cardId))
                            .count();

        numInstances += drawPile.stream().filter(card -> card.getName().equals(cardId))
                                .count();

        return (int) numInstances;
    }

    public String encode() {
        JsonObject playerStateJson = new JsonObject();

        playerStateJson.addProperty("creature", super.encode());

        playerStateJson.addProperty("chosen_class_name", chosenClass.name());
        playerStateJson.addProperty("game_hand_size", gameHandSize);
        playerStateJson.addProperty("master_hand_size", masterHandSize);
        playerStateJson.addProperty("starting_max_hp", startingMaxHP);
        playerStateJson.addProperty("energy_manager_energy", energyManagerEnergy);
        playerStateJson.addProperty("energy_manager_max_master", energyManagerMaxMaster);
        playerStateJson.addProperty("energy_panel_total_energy", energyPanelTotalEnergy);
        playerStateJson.addProperty("is_ending_turn", isEndingTurn);
        playerStateJson.addProperty("viewing_relics", viewingRelics);
        playerStateJson.addProperty("inspect_mode", inspectMode);
        playerStateJson.addProperty("inspect_hb", inspectHb == null ? null : inspectHb.encode());
        playerStateJson.addProperty("damaged_this_combat", damagedThisCombat);
        playerStateJson.addProperty("title", title);

        playerStateJson.addProperty("master_deck", encodeCardList(masterDeck));
        playerStateJson.addProperty("draw_pile", encodeCardList(drawPile));
        playerStateJson.addProperty("hand", encodeCardList(hand));
        playerStateJson.addProperty("discard_pile", encodeCardList(discardPile));
        playerStateJson.addProperty("exhaust_pile", encodeCardList(exhaustPile));
        playerStateJson.addProperty("limbo", encodeCardList(limbo));

        playerStateJson.addProperty("relics", relics.stream().map(RelicState::encode)
                                                    .collect(Collectors.joining(RELIC_DELIMETER)));
        playerStateJson.addProperty("max_orbs", maxOrbs);

        JsonArray potionArray = new JsonArray();
        for (PotionState potion : potions) {
            potionArray.add(potion.encode());
        }
        playerStateJson.add("potions", potionArray);

        JsonArray orbArray = new JsonArray();
        for (OrbState orb : orbs) {
            orbArray.add(orb.encode());
        }
        playerStateJson.add("orbs", orbArray);

        return playerStateJson.toString();
    }

    public static String encodeCardList(ArrayList<CardState> cardList) {
        return cardList.stream().map(CardState::encode).collect(Collectors.joining(CARD_DELIMETER));
    }

    private static ArrayList<CardState> decodeCardList(String cardListString) {
        return Stream.of(cardListString.split(CARD_DELIMETER)).filter(s -> !s.isEmpty())
                     .map(CardState::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<CardState> toCardStateArray(ArrayList<AbstractCard> cards) {
        ArrayList<CardState> result = new ArrayList<>();

        for (AbstractCard card : cards) {
            result.add(new CardState(card));
        }

        return result;
    }

}
