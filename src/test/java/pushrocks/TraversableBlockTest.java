package pushrocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pushrocks.model.BlockAbstract;
import pushrocks.model.TraversableBlock;

public class TraversableBlockTest {
    //Tests of constructor and methods inherited by BlockAbstract
    @Test
    @DisplayName("Test ")
    public void testConstructorCoordinates() {
        BlockAbstract neutralValueCoordinates = new TraversableBlock(0, 0, ' ', true);
        assertEquals(0, neutralValueCoordinates.getX());
        assertEquals(0, neutralValueCoordinates.getY());
        assertEquals(new int[]{0,0}[0], neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(new int[]{0,0}[1], neutralValueCoordinates.getCoordinatesXY()[1]);
        BlockAbstract positiveValueCoordinates = new TraversableBlock(11, 12, ' ', true);
        assertEquals(11, positiveValueCoordinates.getX());
        assertEquals(12, positiveValueCoordinates.getY());
        assertEquals(new int[]{11,12}[0], positiveValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(new int[]{11,12}[1], positiveValueCoordinates.getCoordinatesXY()[1]);
        BlockAbstract negativeValueCoordinates = new TraversableBlock(-11, -12, ' ', true);
        assertEquals(-11, negativeValueCoordinates.getX());
        assertEquals(-12, negativeValueCoordinates.getY());
        assertEquals(new int[]{-11,-12}[0], negativeValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(new int[]{-11,-12}[1], negativeValueCoordinates.getCoordinatesXY()[1]);
    }
    @Test
    public void testConstructorValidTypes() {
        BlockAbstract constructedAir = new TraversableBlock(0, 0, ' ', true);
        assertEquals(' ', constructedAir.getType());
        BlockAbstract constructedPressurePlate = new TraversableBlock(0, 0, 'd', true);
        assertEquals('d', constructedPressurePlate.getType());
    }
    @Test
    public void testConstructorInvalidTypes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TraversableBlock(0, 0, 'w', true),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid type.");
    }
    @Test
    @DisplayName("Test that blocks are constructed with the correct state value according to their type.")
    public void testConstructorStateStartValues() {
        BlockAbstract contructedAir = new TraversableBlock(0, 0, ' ', true);
        assertFalse(contructedAir.getState(), "Air blocks should have their state set to false once constructed.");
        BlockAbstract constructedPressurePlate = new TraversableBlock(0, 0, 'd', true);
        assertFalse(constructedPressurePlate.getState(), "Pressure plate blocks should have their state set to false once constructed.");
    }
    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract traversableBlock = new TraversableBlock(0, 0, ' ', true);
        assertFalse(traversableBlock.hasCollision(), "Traversable blocks do not have collision, thus hasCollision() should always return false");
    }
    

    //Tests of constructor-parameters and class methods specific to TraversableBlock
    @Test 
    @DisplayName("Test that blocks are constructed with a bird view (view angle) value matching the given constructor input.")
    public void testConstructorBirdView() {
        TraversableBlock constructedWithBirdViewEnabled = new TraversableBlock(0, 0, ' ', true) ;
        assertTrue(constructedWithBirdViewEnabled.isBirdView());
        TraversableBlock constructedWithBirdViewDisabled = new TraversableBlock(0, 0, ' ', false) ;
        assertFalse(constructedWithBirdViewDisabled.isBirdView());
    }
    @Test
    @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    public void testTypeChecks() {
        TraversableBlock constructedAir = new TraversableBlock(0, 0, ' ', true);
        assertEquals(' ', constructedAir.getType());
        assertTrue(constructedAir.isAir());
        assertFalse(constructedAir.isPressurePlate());
        TraversableBlock constructedPressurePlate = new TraversableBlock(0, 0, 'd', true);
        assertTrue(constructedPressurePlate.isPressurePlate());
        assertFalse(constructedPressurePlate.isAir());
    }
}
