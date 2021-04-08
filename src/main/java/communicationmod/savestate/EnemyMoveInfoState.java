package communicationmod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;

public class EnemyMoveInfoState {
    public final byte nextMove;
    public final AbstractMonster.Intent intent;
    public final int baseDamage;
    public final int multiplier;
    public final boolean isMultiDamage;

    public EnemyMoveInfoState(EnemyMoveInfo enemyMoveInfo) {
        this.nextMove = enemyMoveInfo.nextMove;
        this.intent = enemyMoveInfo.intent;
        this.baseDamage = enemyMoveInfo.baseDamage;
        this.multiplier = enemyMoveInfo.multiplier;
        this.isMultiDamage = enemyMoveInfo.isMultiDamage;
    }

    public EnemyMoveInfoState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.nextMove = parsed.get("next_move").getAsByte();
        this.intent = AbstractMonster.Intent.valueOf(parsed.get("intent_name").getAsString());
        this.baseDamage = parsed.get("base_damage").getAsInt();
        this.multiplier = parsed.get("multiplier").getAsInt();
        this.isMultiDamage = parsed.get("is_multi_damage").getAsBoolean();
    }

    public String encode() {
        JsonObject enemyMoveInfoStateJson = new JsonObject();

        enemyMoveInfoStateJson.addProperty("next_move", nextMove);
        enemyMoveInfoStateJson.addProperty("intent_name", intent.name());
        enemyMoveInfoStateJson.addProperty("base_damage", baseDamage);
        enemyMoveInfoStateJson.addProperty("multiplier", multiplier);
        enemyMoveInfoStateJson.addProperty("is_multi_damage", isMultiDamage);

        return enemyMoveInfoStateJson.toString();
    }
}
