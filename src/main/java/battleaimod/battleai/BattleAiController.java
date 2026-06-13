package battleaimod.battleai;


import battleaimod.ValueFunctions;
import battleaimod.battleai.data.CardAction;
import battleaimod.battleai.data.CardSequence;
import battleaimod.battleai.data.dummycommands.*;
import battleaimod.battleai.evolution.utils.ValueFunctionManager;
import battleaimod.battleai.evolution.utils.fitness.AbstractFitness;
import battleaimod.battleai.evolution.utils.fitness.CompatExpression;
import battleaimod.battleai.evolution.utils.fitness.WeightedSumFitness;
import battleaimod.utils.FileLogger;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import ludicrousspeed.Controller;
import ludicrousspeed.simulator.commands.*;
import savestate.CardState;
import savestate.SaveState;
import savestate.SaveStateMod;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BattleAiController implements Controller {
    private final int maxTurnLoads;

    public int targetTurn;
    public int targetTurnJump;

    public PriorityQueue<TurnNode> turns = new PriorityQueue<>();

    // The best winning result unless the AI gave up in which case it will contain the chosen death
    // path
    public StateNode bestEnd;

    // If it doesn't work out just send back a path to kill the players so the game doesn't get
    // stuck.
    public StateNode deathNode = null;

    // The state the AI is currently processing from
    public TurnNode committedTurn = null;

    // The target turn that will be loaded if/when the max turn loads is hit
    public TurnNode bestTurn = null;
    public TurnNode backupTurn = null;

    public int startingHealth;
    public boolean isDone = false;
    public boolean fitnessFailed = false;
    public final SaveState startingState;
    public int expectedDamage = 0;
    private boolean initialized;

    //evolution stuff
    private Queue<CardSequence> sequences;
    private Deque<DummyCommand> dummyCommandQueue;
    private CardSequence currentCardSeq;
    private List<CardSequence> finalSequences;
    private StateNode currentState;
    private StateNode startStateNode;
    private List<AbstractCard> startingHand;
    private List<AbstractCard> previousHand;
    private int previousDrawPileSize;
    private boolean lastCmdNull = false;
    private boolean lastCmdEnd = false;
    private boolean currSequenceValid = true;
    private int cardsPlayed = 0;
    private AbstractFitness currentFitness;

    private int currentGeneration = 0;
    private final int GENERATIONS = 15;
    private final int POPULATIONSIZE = 80;
    private final int MUTATIONSIZE = 55;
    private final int PARENTSIZE = 15;
    private final int ELITENUM = 5;

    // EXPERIMENTAL
    public static final boolean SHOULD_SHOW_TREE = false;



    public BattleAiController(SaveState state, int maxTurnLoads) {
        SaveStateMod.runTimes = new HashMap<>();
        targetTurn = 8;
        targetTurnJump = 6;

        bestEnd = null;
        startingState = state;
        initialized = false;

        System.err.println("loading state from constructor");
        startingState.loadState();

        this.maxTurnLoads = maxTurnLoads;
    }
    private Queue<CardSequence> generateInitPop(int populationSize) {
        Queue<CardSequence> population = new ArrayDeque<>();

        if (AbstractDungeon.player == null || AbstractDungeon.player.hand == null) {
            return population;
        }

        startingHand = new ArrayList<>(AbstractDungeon.player.hand.group);

//        FileLogger.log("starting hand: ");
//        for (AbstractCard c : AbstractDungeon.player.hand.group){
//            FileLogger.log("   card id: " + c.getMetricID());
//        }

        for (int p = 0; p < populationSize; p++) {

            List<CardAction> cardActionList = new ArrayList<>();
            Set<AbstractCard> usedCards = new HashSet<>();

            AbstractDungeon.player.hand.refreshHandLayout();
            int energy = EnergyPanel.totalCount;

            // Copy + shuffle hand
            List<AbstractCard> cards = new ArrayList<>(AbstractDungeon.player.hand.group);
            Collections.shuffle(cards);

            for (AbstractCard card : cards) {

                if (card == null) continue;

                int cost = card.costForTurn;


                // Skip unplayable
                if (cost == -2) {
                    continue;
                }

                if(AbstractDungeon.player.hasPower("Entangled") && card.type == AbstractCard.CardType.ATTACK){
                    //entangled edge case, makes attacks unplayable -> skip
                    continue;
                }

                // X-cost card
                if (cost == -1) {
                    CardAction action = CardAction.createCardAction(card);
                    if (action != null) {
                        cardActionList.add(action);
                        usedCards.add(card);
                    }
                    break; // consumes all energy
                }

                // Normal cost
                if (cost <= energy) {
                    CardAction action = CardAction.createCardAction(card);
                    if (action != null) {
                        cardActionList.add(action);
                        usedCards.add(card);
                        energy -= cost;
                    }
                }

                if (energy <= 0) {
                    break;
                }
            }

            // --- Compute leftover cards ---
            List<AbstractCard> leftoverCards = new ArrayList<>();
            for (AbstractCard card : cards) {
                if (card != null && !usedCards.contains(card)) {
                    leftoverCards.add(card);
                }
            }

            CardSequence sequence = new CardSequence(cardActionList, leftoverCards);
            population.add(sequence);
        }

        return population;
    }




    private void printMetrics(StateNode start, StateNode end, CardSequence finalSequence) {
        //log to file since console is not visible/freezes
        FileLogger.log("SIMULATION RESULTS:");
        FileLogger.log("Player HP: " + end.saveState.getPlayerHealth());
        FileLogger.log("Damage taken: " + StateNode.getPlayerDamage(end));
        FileLogger.log("Damage Dealt: " + ValueFunctions.getTotalDamageDealt(start.saveState, end.saveState));
        FileLogger.log("Monster HP: " + ValueFunctions.getTotalMonsterHealth(end.saveState));
        FileLogger.log("Score: " + getFitness(start, end, finalSequence.getCardsAsAbstractCard(), new ArrayList<>(AbstractDungeon.player.hand.group)));
        FileLogger.log("Final Cards: ");
        for(CardAction a : finalSequence.getCards()){
            FileLogger.log("   "+a.getMainCard().toString());
        }
        FileLogger.log("Starting Hand: ");
        for(AbstractCard c : startingHand){
            FileLogger.log("   "+c);
        }
        FileLogger.log("==========================");
    }

    private void loadFitness() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("CurrentFitnessValues.txt"))) {

            String typeLine = reader.readLine();

            if (typeLine == null || typeLine.trim().isEmpty()) {
                return;
            }

            String dataLine = reader.readLine();
            if (dataLine == null || dataLine.trim().isEmpty()) {
                return;
            }

            String type = typeLine.trim();
            String data = dataLine.trim();

            switch (type) {
                case "WEIGHTED_SUM":
                    currentFitness = new WeightedSumFitness(data);
                    break;
                case "EXPRESSION_TREE":
                    currentFitness = new CompatExpression(data);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fitness type: " + type);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load fitness file", e);
        }
    }

    private double getFitness(StateNode start, StateNode end, List<AbstractCard> cardsPlayed, List<AbstractCard> endHand) {
        ValueFunctionManager.initFuncValues(start.saveState, end.saveState, cardsPlayed, endHand);

        double result = 0;
        try{
            result = currentFitness.evaluate();
        }catch (Exception e){
            isDone = true;
            bestEnd = end;
            fitnessFailed = true;
            FileLogger.logError("fitness failed: " + e.getMessage());
        }
        return result;
    }


    private Deque<DummyCommand> actionsToCommands(List<CardAction> cardActionList){
        Deque<DummyCommand> commands = new ArrayDeque<>();
        for(CardAction action: cardActionList){
            commands.addAll(action.getDummyCommands());
        }
        commands.add(new GeneralDummyCommand(new EndCommand()));
        return commands;
    }


    public void step() {
        try{
            if (isDone) {
                return;
            }

            if (!initialized) {

                if(GameActionManager.turn >= 50){
                    FileLogger.log("Too Slow! Cancelling Early");
                    isDone = true;
                    bestEnd = currentState;
                    fitnessFailed = true;
                    return;
                }


                //FileLogger.log("Init!");
                initialized = true;
                isDone = false;
                bestEnd = null;
                fitnessFailed = false;
                //shouldRunEndCommand = false;
                finalSequences = new ArrayList<>();

                loadFitness();

                // Match A* init
                SaveStateMod.runTimes = new HashMap<>();
                CardState.resetFreeCards();

                // 1. Load starting state
                SaveState startState = new SaveState();
                startState.loadState();

                startStateNode = new StateNode(null, null, this);
                startStateNode.saveState = startingState;
                currentState = startStateNode;

                startingHealth = startState.getPlayerHealth();

                sequences = generateInitPop(POPULATIONSIZE);

                resetLoopVars();

            }
            else{
                if(!currSequenceValid){
                    //kill this sim and discard it
                    FileLogger.logError("Invalid sequence detected, discarding sim...");
                    dummyCommandQueue = null;
                    currentCardSeq = null;
                }

                if(currentState.saveState == null){
                    //IMPORTANT: SaveState MUST come in the next step after running a command to ensure effects propagate!
                    currentState.saveState = new SaveState();
                }

                if(!lastCmdNull && !lastCmdEnd){ //don't check new cards drawn if last command failed or was end
                    addNewCardsInHand(currentCardSeq);
                }

                //regardless if we added new cards or not, keep these vars updated
                List<AbstractCard> newHand = new ArrayList<>(AbstractDungeon.player.hand.group);
                previousHand = newHand;
                previousDrawPileSize = AbstractDungeon.player.drawPile.size();
                lastCmdNull = false;
                lastCmdEnd = false;

                //FileLogger.log("current energy: " + EnergyPanel.totalCount);


                if(dummyCommandQueue == null || dummyCommandQueue.isEmpty()){
                    if(dummyCommandQueue != null){ //check if we have any UI to clear
                        if(checkUICommands(dummyCommandQueue, currentCardSeq)){
                            return; //keep looping
                        }
                    }

                    if (currentCardSeq != null) { //not null check to skip first loop
                        //eval score, add to final sorting list
                        FileLogger.log("==Finished single sim==");
                        //FileLogger.log("last command: " +currentState.lastCommand);
                        //FileLogger.log("size of node list: "+ stateNodesToGetToNode(currentState).size());

                        double turnFitness = getFitness(startStateNode, currentState, currentCardSeq.getCardsAsAbstractCard(), new ArrayList<>(AbstractDungeon.player.hand.group));
                        FileLogger.log("fitness: "+turnFitness);

                        currentCardSeq.setFitness(turnFitness);
                        currentCardSeq.setEndState(currentState);
                        finalSequences.add(currentCardSeq);
                    }


                    if(sequences.isEmpty()){
                        //finished simulating all

                        if(finalSequences.isEmpty()){
                            FileLogger.logError("No final sequences, maybe all invalid, fitnessFailed");
                            fitnessFailed = true;
                            bestEnd = currentState;
                            isDone = true;
                            return;
                        }

                        //sort, get best end
                        Collections.sort(finalSequences);

                        FileLogger.log("FINISHED GEN "+currentGeneration +" SIMULATIONS");

                        currentGeneration++;
                        if(currentGeneration == GENERATIONS){
                            FileLogger.log("====Finished all simulations====");
                            bestEnd = finalSequences.get(0).getEndState();

                            //FileLogger.log("size of node list: "+ stateNodesToGetToNode(bestEnd).size());

                            printMetrics(startStateNode, bestEnd, finalSequences.get(0));
                            isDone = true;
                            initialized = false;
                        }
                        else{
                            //restart with new population

                            currentState = startStateNode;
                            startStateNode.saveState.loadState();

                            List<CardSequence> parents = selectParents(finalSequences);
                            sequences = nextGeneration(parents);
                            finalSequences = new ArrayList<>();
                            currentCardSeq = null;

                            resetLoopVars();

                        }
                    }
                    else{
                        //init next sequence
                        FileLogger.log("==new sequence init==");
                        currentCardSeq = sequences.poll(); //just so we can save it and evolve later
                        dummyCommandQueue = actionsToCommands(currentCardSeq.getCards());
                        //get queue of commands to run

                        resetLoopVars();

                        currentState = startStateNode;
                        startStateNode.saveState.loadState(); //reset sim to start
                    }
                    return; //return here to ensure states properly loaded

                }

                //This code will run if dummyCommandQueue has commands to run still:
                Command cmd = dummyCommandQueue.peek().getRealCommand();

                if (cmd instanceof CardCommand || cmd instanceof EndCommand){
                    //FileLogger.log("checking for UI command");
                    checkUICommands(dummyCommandQueue, currentCardSeq); //check if there is a UI menu: this func will add commands if needed
                    //FileLogger.log("done checking for UI command");

                    if(cmd instanceof EndCommand){
                        checkExtraEnergy(dummyCommandQueue, currentCardSeq);
                    }
                }

                DummyCommand dCmd = dummyCommandQueue.poll();
                boolean playingCard = dCmd instanceof DummyCardCommand;

                cmd = dCmd.getRealCommand(); //overwrite old command with new one

                if(cmd == null){
                    FileLogger.log("cmd was null, skipping cmd");
                    lastCmdNull = true;
                    //TODO: if needed, print DummyCommand and implement toString()

                    if(playingCard){

                        boolean valid = removeCardAction(currentCardSeq, ((DummyCardCommand)dCmd).canRevalidate());
                        if(!valid){
                            currSequenceValid = false;
                            return;
                        }
                    }
                }
                else{
                    StateNode next = new StateNode(currentState, cmd, this);
                    //FileLogger.log("Playing command: " + cmd);

                    if(cmd instanceof EndCommand){
                        lastCmdEnd = true;
                    }
                    cmd.execute();
                    currentState = next;

                    if(playingCard){
                        //FileLogger.log("Cards played: " + cardsPlayed);
                        cardsPlayed++;
                    }
                }
            }
        }catch (Exception e){
            FileLogger.logError("error");
            FileLogger.logError("message: " + e.getMessage());
            FileLogger.logError("cause: " + e.getCause());
            FileLogger.logError("stack trace: " + Arrays.toString(e.getStackTrace()));

            currSequenceValid = false;
            //throw new RuntimeException(e);
        }


    }


    public static List<StateNode> stateNodesToGetToNode(StateNode endNode) {
        ArrayList<StateNode> result = new ArrayList<>();
        StateNode iterator = endNode;
        while (iterator != null) {
            result.add(0, iterator);
            iterator = iterator.parent;
        }

        return result;
    }


    public boolean isDone() {
        return isDone;
    }

    public TurnNode committedTurn() {
        return committedTurn;
    }

    public int turnsLoaded() {
        return 0;
    }

    public int maxTurnLoads() {
        return maxTurnLoads;
    }


    private boolean checkUICommands(Deque<DummyCommand> dummyCommandQueue, CardSequence cardSequence){

        //FileLogger.log("checking UI commands");
        if (isInHandSelect()) {
            //FileLogger.log("getting card");
            AbstractCard card = cardSequence.getNextHandSelectCard();
            //FileLogger.log("finished get");
            if(card != null){
                //FileLogger.log("putting commands in");
                dummyCommandQueue.offerFirst(new GeneralDummyCommand(HandSelectConfirmCommand.INSTANCE));
                dummyCommandQueue.offerFirst(new DummyHandSelectCommand(card));
            }
            else{
                FileLogger.logError("Ran out of all cards to select!");
                currSequenceValid = false;

            }
            return true;
        }

        if (isInGridSelect()) {
            //includes Scry, Seek, Headbutt etc.

            dummyCommandQueue.offerFirst(new GeneralDummyCommand(GridSelectConfrimCommand.INSTANCE));
            //this one is harder since we don't know the set of cards we are going to see,
            //and we don't know where the card is going (discard, draw pile, hand)

            if (cardSequence.hasGridSelectChoices()){
                //try to use the saved choice
                AbstractCard selectedCard = cardSequence.getNextGridSelectChoice();
                dummyCommandQueue.offerFirst(new DummyGridSelectCommand(selectedCard));

                //TODO: if choice does not exist, need to remove from buffer and handle it
            }
            else{
                //random
                List<AbstractCard> group = AbstractDungeon.gridSelectScreen.targetGroup.group;
                int randIndex = new Random().nextInt(group.size());
                AbstractCard randomCard = group.get(randIndex);

                dummyCommandQueue.offerFirst(new DummyGridSelectCommand(randomCard));
                cardSequence.addGridSelectChoiceToBuffer(randomCard); //setup for next time
            }
            return true;
        }

        if (isInCardRewardSelect()) { //literally just for Discover, just pick 0 every time, change later if needed
            dummyCommandQueue.offerFirst(new GeneralDummyCommand(new CardRewardSelectCommand(0)));
//            for(int i = 0; i < AbstractDungeon.cardRewardScreen.rewardGroup.size(); ++i) {
//
//            }
            return true;
        }
        return false;

    }

    private void checkExtraEnergy(Deque<DummyCommand> dummyCommandQueue, CardSequence cardSequence){
        CardAction a = cardSequence.getNextPlayableCard(EnergyPanel.totalCount);
        if(a == null){
            return; //no cards, stop here
        }

        FileLogger.log("has playable cards! energy = " + EnergyPanel.totalCount);
        FileLogger.log("extra card: " + a.getMainCard());

        List<DummyCommand> cmds = a.getDummyCommands();
        Collections.reverse(cmds);

        for(DummyCommand cmd : cmds) {
            dummyCommandQueue.offerFirst(cmd);
        }
    }

    private static boolean isInGridSelect() {
        //FileLogger.log("checking grid select menu open");
        return isInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.isScreenUp && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID;
    }


    private static boolean isInHandSelect() {
        //FileLogger.log("checking hand select menu open");
        return isInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.isScreenUp && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT;
    }

    private static boolean isInCardRewardSelect() {
        //FileLogger.log("checking card reward menu open");
        return isInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.isScreenUp && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD;
    }

    private static boolean isInDungeon() {
        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY && AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.currMapNode != null;
    }


    private void addNewCardsInHand(CardSequence currentCardSeq) {
        if(currentCardSeq == null){
            FileLogger.logError("currentCardSeq null in addNewCardsInHand, skipping");
            return;
        }

        List<AbstractCard> newHand = new ArrayList<>(AbstractDungeon.player.hand.group);

        int newDrawPileSize = AbstractDungeon.player.drawPile.size();

        if(previousHand == null){
            FileLogger.log("starting energy: " + EnergyPanel.totalCount);
            previousHand = newHand; //init first previous hand
            FileLogger.log("starting hand: ");
            for(AbstractCard c : previousHand){
                FileLogger.log("   "+c);
            }
            currentCardSeq.logCardActions();
            return; //first loop, no point checking anything
        }

        if (previousHand.size() > newHand.size() || newHand.isEmpty()) {
            // strictly larger before -> no cards were created
            //OR newHand is empty, no cards created
            return;
        }
        if (previousHand.size() == newHand.size()){
            if(newDrawPileSize == previousDrawPileSize){ //check if we actually drew anything
                //there's a chance we generated 1 card, so check if its equal
                AbstractCard potentialNewCard = newHand.get(newHand.size()-1);
                AbstractCard oldCard = previousHand.get(previousHand.size()-1);

                if(potentialNewCard.name.equals(oldCard.name)){
                    //just check name since card generating itself is impossible
                    return; //nothing was drawn
                }
            }
        }



        FileLogger.log("new cards detected from new hand: ");
        for(AbstractCard c : newHand){
            FileLogger.log("   "+c);
        }
        FileLogger.log("old hand: ");
        for(AbstractCard c : previousHand){
            FileLogger.log("   "+c);
        }

        if(previousHand.isEmpty()){
            //then all the cards in newHand are new
            FileLogger.log("old hand is empty, adding all newHand cards");
            currentCardSeq.addCardsCreated(newHand);
            return;
        }

        List<AbstractCard> createdCards = new ArrayList<>();
        for (int i = previousHand.size()-1; i < newHand.size(); i++) {
            FileLogger.log("new card: " + newHand.get(i));
            createdCards.add(newHand.get(i));
        }

        currentCardSeq.addCardsCreated(createdCards);
    }

    private List<CardSequence> selectParents(List<CardSequence> sortedSequences) {
        List<CardSequence> selected = new ArrayList<>();
        int n = sortedSequences.size();
        Random rand = new Random();

        // top 3 elites
        int eliteCount = Math.min(ELITENUM, n);
        for (int i = 0; i < eliteCount; i++) {
            selected.add(sortedSequences.get(i));
        }

        // If we already filled parent pool, return early
        if (selected.size() >= PARENTSIZE) {
            return selected.subList(0, PARENTSIZE);
        }

        // Build weights
        double[] weights = new double[n];
        double totalWeight = 0;

        for (int i = 0; i < n; i++) {
            weights[i] = n - i; // linear bias
            totalWeight += weights[i];
        }

        // Fill remaining slots using weighted sampling
        int remaining = PARENTSIZE - selected.size();

        for (int p = 0; p < remaining; p++) {
            double r = rand.nextDouble() * totalWeight;

            double cumulative = 0;
            for (int i = 0; i < n; i++) {
                cumulative += weights[i];
                if (r <= cumulative) {
                    selected.add(sortedSequences.get(i));
                    break;
                }
            }
        }

        return selected;
    }

    private Queue<CardSequence> nextGeneration(List<CardSequence> parents) {
        int parentSize = parents.size();
        Queue<CardSequence> nextGen = new ArrayDeque<>(parentSize);
        Random rand = new Random();

        // 1. Elitism: keep top N unchanged
        for (int i = 0; i < ELITENUM; i++) {
            nextGen.add(new CardSequence(parents.get(i)));
        }

//        // 2. Crossover + mutation-after-crossover
//        for (int i = 0; i < crossoverNum; i++) {
//            CardSequence parent1 = parents.get(rand.nextInt(parentSize));
//            CardSequence parent2 = parents.get(rand.nextInt(parentSize));
//            CardSequence child = crossover(parent1, parent2);
//
//            // Mutation-after-crossover (common GA practice)
//            if (rand.nextDouble() < mutationRate) {
//                child = mutate(child);
//            }
//
//            nextGen.add(child);
//        }

        // 3. Mutation-only offspring (mutated clones from single parents)
        for (int i = 0; i < MUTATIONSIZE; i++) {
            CardSequence parent = parents.get(rand.nextInt(parentSize));
            CardSequence mutant = mutate(parent);
            //FileLogger.log("mutant cards.size(): " + mutant.getCards().size());
            nextGen.add(mutant);
        }

        // 4. If the nextGen is short, fill with random parents
        while (nextGen.size() < POPULATIONSIZE) {
            int idx = ELITENUM + rand.nextInt(parentSize - ELITENUM);
            nextGen.add(new CardSequence(parents.get(idx)));
        }

        return nextGen;
    }

//    private CardSequence crossover(CardSequence parent1, CardSequence parent2){
//        return new CardSequence(parent1,parent2);
//    }

    private CardSequence mutate(CardSequence parent){
        CardSequence child = new CardSequence(parent);
        child.mutate();

        return child;
    }

    private void resetLoopVars(){
        //new loop reset vars
        lastCmdNull = false;
        lastCmdEnd = false;
        previousDrawPileSize = AbstractDungeon.player.drawPile.size();
        previousHand = null;

        currSequenceValid = true;
        cardsPlayed = 0;
    }

    private boolean removeCardAction(CardSequence currentCardSeq, boolean canValidate){
        //returns TRUE is sequence is still valid
        //returns FALSE if letting this stay in population could be bad, therefore invalid
        FileLogger.log("removing card, full sequence:");
        currentCardSeq.logCardActions();

        if(cardsPlayed == currentCardSeq.getCards().size()){
            FileLogger.log("removeCardAction out of bounds, probably discarded/exhausted:");
            return true;
        }
        else if (cardsPlayed > currentCardSeq.getCards().size()){
            FileLogger.log("removeCardAction out of bounds, maybe could not find card of specified type");
            return false;
        }
        FileLogger.log("removing card: " + currentCardSeq.getCards().get(cardsPlayed).getMainCard());
        currentCardSeq.removeCard(cardsPlayed, canValidate);
        return true;
    }
}
