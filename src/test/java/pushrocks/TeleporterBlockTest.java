package pushrocks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

import pushrocks.model.BlockAbstract;
import pushrocks.model.DirectedBlock;
import pushrocks.model.ObstacleBlock;
import pushrocks.model.PortalWallBlock;
import pushrocks.model.TeleporterBlock;

public class TeleporterBlockTest {
    private char[] validTypes = new char[] {'t'};
    private String[] validDirections = new String[] {null};

    //Tests for constructor and methods inherited from BlockAbstract
    @Test
    @DisplayName("Test positive, zero, and positive integers for constructor coordinates.")
    public void testConstructorCoordinates() {
        int[] coordinateValues = new int[]{2, 0, -3};
        for (int value: coordinateValues) {
            testConstructorCoordinates(value, value);
            testConstructorCoordinates(0, value);
            testConstructorCoordinates(value, 0);
        }
    }
    public void testConstructorCoordinates(int x, int y) {
        BlockAbstract neutralValueCoordinates = new TeleporterBlock(x, y, validTypes[0], validDirections[0], null);
        assertEquals(x, neutralValueCoordinates.getX());
        assertEquals(x, neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(y, neutralValueCoordinates.getY());
        assertEquals(y, neutralValueCoordinates.getCoordinatesXY()[1]);
    }

    @Test
    public void testConstructorValidTypes() {
        for (char validType : this.validTypes) {
            BlockAbstract constructedValidType = new TeleporterBlock(0, 0, validType, validDirections[0], null);
            assertEquals(validType, constructedValidType.getType());
        }      
    }
    @Test
    public void testConstructorInvalidTypes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TeleporterBlock(0, 0, 'p', validDirections[0], null),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid type.");
    }
    @Test
    @DisplayName("Test that blocks are constructed with the correct state value according to their type.")
    public void testConstructorState() {
        for (char validType : this.validTypes) {
            BlockAbstract contructedValidTypeWithoutConnection = new TeleporterBlock(0, 0, validType, validDirections[0], null);
            assertFalse(contructedValidTypeWithoutConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
            ObstacleBlock connection = new TeleporterBlock(0, 0, 't', validDirections[0], null);
            BlockAbstract contructedValidTypeWithConnection = new TeleporterBlock(0, 0, validType, validDirections[0], connection);
            assertTrue(contructedValidTypeWithConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
        }
    }

    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract teleporterBlock = new TeleporterBlock(0, 0, validTypes[0], validDirections[0], null);
        assertTrue(teleporterBlock.hasCollision(), "Directed blocks, and thus by extension moveable blocks, do have collision, thus hasCollision() should always return true");
    }
    

    //Tests for constructor and methods inherited from DirectedBlock
    @Test
    public void testConstructorValidDirections() {
        for (String direction : validDirections) {
            DirectedBlock constructedValidDirection = new TeleporterBlock(0, 0, validTypes[0], direction, null);
            assertEquals(direction, constructedValidDirection.getDirection());
            assertEquals(0, constructedValidDirection.getDirectionXY()[0]);
            assertEquals(0, constructedValidDirection.getDirectionXY()[1]);
        }
    }

    private void testConstructorInvalidDirections(String invalidDirection) {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TeleporterBlock(0, 0, 't', invalidDirection, null),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction. Direction was: " + invalidDirection);
    }
    @Test
    public void testConstructorInvalidDirections() {
        String[] invalidDirections = new String[]{"NorthToVabbi", "up", "down", "right", "left", ""};
        for (String invalidDirection : invalidDirections) {
            testConstructorInvalidDirections(invalidDirection);
        }
    }

    //Tests for constructor and methods inherited from ObstacleBlock

    @Test
    public void testConstructorWithoutConnection() {
        TeleporterBlock tpWithoutConnection = new TeleporterBlock(0, 0, validTypes[0], validDirections[0], null);
        assertFalse(tpWithoutConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
        assertNull(tpWithoutConnection.getConnection(), "A teleporter constructed without a connection, should have their connection set to null.");
    }
    @Test
    public void testConstructorWithConnection() {
        ObstacleBlock connection = new TeleporterBlock(0, 0, validTypes[0], validDirections[0], null);
        BlockAbstract tpWithConnection = new TeleporterBlock(0, 0, validTypes[0], validDirections[0], connection);
        assertTrue(tpWithConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
    }


    @Test
    public void testSetConnection() {
        PortalWallBlock portal = new PortalWallBlock(0, 0, 'u', "left", null);
        TeleporterBlock teleporter = new TeleporterBlock(0, 0, 't', null, null);
        teleporter.setConnection(portal);
        assertNull(teleporter.getConnection());
    }
    @Test
    public void testRemoveConnection() {

    }
    @Test
    public void testCanBlockEnter() {

    }




///////////COPY Start
    // private Partner p1;
	// private Partner p2;

	// @BeforeEach
	// public void setup() {
	// 	p1 = new Partner("1");
	// 	p2 = new Partner("2");
	// }

	// @Test
	// @DisplayName("Sjekk konstruktøren ikke oppretter noen partnere")
	// public void testConstructor() {
	// 	assertNull(p1.getPartner());
	// 	assertNull(p2.getPartner());
	// }

	// @Test
	// @DisplayName("Sjekk at p1 og p2 er partnere etter p1.setPartner(s2)")
	// public void simplePartnerShip() {
	// 	// Enkelt partnerskap
	// 	assertNull(p1.getPartner());
	// 	assertNull(p2.getPartner());
	// 	p1.setPartner(p2);
	// 	assertEquals(p1.getPartner(), p2, "P1 skulle vært partneren til p2");
	// 	assertEquals(p2.getPartner(), p1, "P2 skulle vært partneren til p1");
	// }

	// @Test
	// @DisplayName("Sjekk at man kan oppløse partnerskap")
	// public void partnershipWithDivorce() {
	// 	// Partnerskap med etterfølgende brudd
	// 	p1.setPartner(p2);
	// 	assertEquals(p1.getPartner(), p2, "P1 skulle vært partneren til p2");
	// 	assertEquals(p2.getPartner(), p1, "P2 skulle vært partneren til p1");
	// 	p1.setPartner(null);
	// 	assertNull(p1.getPartner());
	// 	assertNull(p2.getPartner());
	// }

	// @Test
	// @DisplayName("Sjekk at kombinert brudd med påfølgende opprettelse av nytt partnerskap fungerer")
	// void swinger() {
	// 	// "Partnerskap med etterfølgende kombinert brudd og nytt partnerskap"
	// 	Partner p3 = new Partner("3");
	// 	Partner p4 = new Partner("4");
	// 	// Partnerskap inngås
	// 	p1.setPartner(p2);
	// 	p3.setPartner(p4);
	// 	assertEquals(p1.getPartner(), p2, "P1 skulle vært partneren til p2");
	// 	assertEquals(p2.getPartner(), p1, "P2 skulle vært partneren til p1");
	// 	assertEquals(p3.getPartner(), p4, "P3 skulle vært partneren til p4");
	// 	assertEquals(p4.getPartner(), p3, "P4 skulle vært partneren til p3");
	// 	// Kombinert brudd og nytt partnerskap
	// 	p1.setPartner(p4);
	// 	assertEquals(p1.getPartner(), p4, "P4 skulle vært partneren til p1");
	// 	assertEquals(p4.getPartner(), p1, "P1 skulle vært partneren til p4");
	// 	assertNull(p2.getPartner());
	// 	assertNull(p3.getPartner());
	// }
    ///////////COPY END
















    //Tests constructor and class methods specific to TeleporterBlock
    @Test
    @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    public void testTypeChecks() {
        TeleporterBlock teleporter = new TeleporterBlock(0, 0, 't', validDirections[0], null);
        assertTrue(teleporter.isTeleporter());
    }
    
    
}
