package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final ArrayList<CardState> masterDeck;
    private final ArrayList<CardState> drawPile;
    private final ArrayList<CardState> hand;
    private final ArrayList<CardState> discardPile;
    private final ArrayList<CardState> exhaustPile;
    private final ArrayList<CardState> limbo;

    private final ArrayList<RelicState> relics;

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

        this.energyManagerEnergy = player.energy.energy;
        this.energyPanelTotalEnergy = EnergyPanel.totalCount;

        this.energyManagerMaxMaster = player.energy.energyMaster;

        this.isEndingTurn = player.isEndingTurn;
        this.viewingRelics = player.viewingRelics;
        this.inspectMode = player.inspectMode;
        this.inspectHb = player.inspectHb == null ? null : new HitboxState(player.inspectHb);
        this.damagedThisCombat = player.damagedThisCombat;

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
    }

    public AbstractPlayer loadPlayer() {
        AbstractPlayer player = CardCrawlGame.characterManager.getCharacter(chosenClass);
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
        player.relics = this.relics.stream().map(RelicState::loadRelic)
                                   .collect(Collectors.toCollection(ArrayList::new));
        AbstractDungeon.topPanel.adjustRelicHbs();
        for (int i = 0; i < player.relics.size(); i++) {
            player.relics.get(i).instantObtain(player, i, false);
        }

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
        long numSlimes = discardPile.stream().filter(card -> card.getName().equals("Slimed"))
                                    .count();

        numSlimes += hand.stream().filter(card -> card.getName().equals("Slimed"))
                         .count();

        numSlimes += drawPile.stream().filter(card -> card.getName().equals("Slimed"))
                             .count();

        return (int) numSlimes;
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

        return playerStateJson.toString();
    }

    private static String encodeCardList(ArrayList<CardState> cardList) {
        return cardList.stream().map(CardState::encode).collect(Collectors.joining(CARD_DELIMETER));
    }

    private static ArrayList<CardState> decodeCardList(String cardListString) {
        return Stream.of(cardListString.split(CARD_DELIMETER)).filter(s -> !s.isEmpty())
                     .map(CardState::new).collect(Collectors.toCollection(ArrayList::new));
    }

}
