package battleaimod.patches;

import FightPredictor.ml.ModelUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FightPredictorCompatibility {
    private static final List<String> VALID_RELICS = new ArrayList(Arrays
            .asList("Akabeko", "Anchor", "Ancient Tea Set", "Art of War", "Astrolabe", "Bag of Marbles", "Bag of Preparation", "Bird Faced Urn", "Black Blood", "Black Star", "Blood Vial", "Bloody Idol", "Blue Candle", "Boot", "Bottled Flame", "Bottled Lightning", "Bottled Tornado", "Brimstone", "Bronze Scales", "Burning Blood", "Busted Crown", "Cables", "Calipers", "Calling Bell", "CaptainsWheel", "Cauldron", "Centennial Puzzle", "CeramicFish", "Champion Belt", "Charon's Ashes", "Chemical X", "CloakClasp", "ClockworkSouvenir", "Coffee Dripper", "Cracked Core", "CultistMask", "Cursed Key", "Damaru", "Darkstone Periapt", "DataDisk", "Dead Branch", "Dodecahedron", "DollysMirror", "Dream Catcher", "Du-Vu Doll", "Ectoplasm", "Emotion Chip", "Empty Cage", "Enchiridion", "Eternal Feather", "FaceOfCleric", "FossilizedHelix", "Frozen Egg 2", "Frozen Eye", "FrozenCore", "Fusion Hammer", "Gambling Chip", "Ginger", "Girya", "Golden Idol", "GoldenEye", "Gremlin Horn", "GremlinMask", "HandDrill", "Happy Flower", "HolyWater", "HornCleat", "HoveringKite", "Ice Cream", "Incense Burner", "InkBottle", "Inserter", "Juzu Bracelet", "Kunai", "Lantern", "Lee's Waffle", "Letter Opener", "Lizard Tail", "Magic Flower", "Mango", "Mark of Pain", "Mark of the Bloom", "Matryoshka", "MawBank", "MealTicket", "Meat on the Bone", "Medical Kit", "Melange", "Membership Card", "Mercury Hourglass", "Molten Egg 2", "Mummified Hand", "MutagenicStrength", "Necronomicon", "NeowsBlessing", "Nilry's Codex", "Ninja Scroll", "Nloth's Gift", "NlothsMask", "Nuclear Battery", "Nunchaku", "Odd Mushroom", "Oddly Smooth Stone", "Old Coin", "Omamori", "OrangePellets", "Orichalcum", "Ornamental Fan", "Orrery", "Pandora's Box", "Pantograph", "Paper Crane", "Paper Frog", "Peace Pipe", "Pear", "Pen Nib", "Philosopher's Stone", "Pocketwatch", "Potion Belt", "Prayer Wheel", "PreservedInsect", "PrismaticShard", "PureWater", "Question Card", "Red Mask", "Red Skull", "Regal Pillow", "Ring of the Serpent", "Ring of the Snake", "Runic Capacitor", "Runic Cube", "Runic Dome", "Runic Pyramid", "SacredBark", "Self Forming Clay", "Shovel", "Shuriken", "Singing Bowl", "SlaversCollar", "Sling", "Smiling Mask", "Snake Skull", "Snecko Eye", "Sozu", "Spirit Poop", "SsserpentHead", "StoneCalendar", "Strange Spoon", "Strawberry", "StrikeDummy", "Sundial", "Symbiotic Virus", "TeardropLocket", "The Courier", "The Specimen", "TheAbacus", "Thread and Needle", "Tingsha", "Tiny Chest", "Tiny House", "Toolbox", "Torii", "Tough Bandages", "Toxic Egg 2", "Toy Ornithopter", "TungstenRod", "Turnip", "TwistedFunnel", "Unceasing Top", "Vajra", "Velvet Choker", "VioletLotus", "War Paint", "WarpedTongs", "Whetstone", "White Beast Statue", "WingedGreaves", "WristBlade", "Yang"));

    @SpirePatch(clz = ModelUtils.class, method = "getInputVector", optional = true, requiredModId = "FightPredictor")
    public static class FilterOutModRelicsPatch {
        static List<AbstractRelic> trueMasterRelics;

        @SpirePrefixPatch
        public static void removeModRelics(List<AbstractCard> masterDeck, List<AbstractRelic> masterRelics, String encounter, int maxHP, int enteringHP, int ascension, boolean potionUsed) {
            Iterator<AbstractRelic> masterRelicsIter = masterRelics.iterator();

            trueMasterRelics = new ArrayList<>(masterRelics);

            while (masterRelicsIter.hasNext()) {
                if (!VALID_RELICS.contains(masterRelicsIter.next().relicId)) {
                    masterRelicsIter.remove();
                }
            }
        }

        @SpirePostfixPatch
        public static void recoverRelics(List<AbstractCard> masterDeck, List<AbstractRelic> masterRelics, String encounter, int maxHP, int enteringHP, int ascension, boolean potionUsed) {
            masterRelics.clear();
            masterRelics.addAll(trueMasterRelics);
        }
    }
}
