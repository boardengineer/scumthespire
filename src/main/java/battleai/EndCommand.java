package battleai;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import communicationmod.CommunicationMod;

public class EndCommand implements Command {
    @Override
    public void execute() {
        AbstractDungeon.actionManager.callEndTurnEarlySequence();
//        AbstractDungeon.overlayMenu.endTurnButton.disable(true);
        CommunicationMod.readyForUpdate = true;
    }
}
