package battleaimod.battleai.commands;

import battleaimod.fastobjects.ActionSimulator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;

import java.util.stream.Collectors;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class PotionCommand implements Command {
    private final int potionIndex;
    private final int monsterIndex;

    public PotionCommand(int potionIndex, int monsterIndex) {
        this.potionIndex = potionIndex;
        this.monsterIndex = monsterIndex;
    }

    public PotionCommand(int potionIndex) {
        this(potionIndex, -1);
    }

    public PotionCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.potionIndex = parsed.get("potion_index").getAsInt();
        this.monsterIndex = parsed.get("monster_index").getAsInt();
    }

    @Override
    public void execute() {
        AbstractPotion potion = AbstractDungeon.player.potions.get(potionIndex);
        AbstractCreature target = AbstractDungeon.player;

        if (monsterIndex != -1) {
            target = AbstractDungeon.getMonsters().monsters.get(monsterIndex);
            if (!shouldGoFast()) {
                String allMonsters = AbstractDungeon.getMonsters().monsters.stream().map(m -> String
                        .format("hp:%s\t", m.currentHealth)).collect(Collectors.joining());
            }
        }

        potion.use(target);
        AbstractDungeon.topPanel.destroyPotion(potionIndex);

        if (!shouldGoFast()) {
            AbstractDungeon.actionManager.addToBottom(new WaitAction(.2F));
        } else {
            ActionSimulator.ActionManageUpdate();
        }
    }

    @Override
    public String toString() {
        return "Potion " + potionIndex + " " + monsterIndex;
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "POTION");

        cardCommandJson.addProperty("potion_index", potionIndex);
        cardCommandJson.addProperty("monster_index", monsterIndex);

        return cardCommandJson.toString();
    }
}
