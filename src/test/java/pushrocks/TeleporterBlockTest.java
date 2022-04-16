package pushrocks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

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
        BlockAbstract neutralValueCoordinates = new TeleporterBlock(x, y, null);
        assertEquals(x, neutralValueCoordinates.getX());
        assertEquals(x, neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(y, neutralValueCoordinates.getY());
        assertEquals(y, neutralValueCoordinates.getCoordinatesXY()[1]);
    }

    //Type checks are redundant as teleporter only have a single type: 't', which is thus set by default
    // @Test
    // public void testConstructorValidTypes() {
    //     for (char validType : this.validTypes) {
    //         BlockAbstract constructedValidType = new TeleporterBlock(0, 0, null);
    //         assertEquals(validType, constructedValidType.getType());
    //     }      
    // }
    // @Test
    // public void testConstructorInvalidTypes() {
    //     assertThrows(
    //         IllegalArgumentException.class,
    //         () -> new TeleporterBlock(0, 0, 'p', null),
    //         "IllegalArgument should be thrown if the constuctor is provided with an invalid type.");
    // }
    @Test
    @DisplayName("Test that blocks are constructed with the correct state value according to their type.")
    public void testConstructorState() {
        
        BlockAbstract contructedValidTypeWithoutConnection = new TeleporterBlock(0, 0, null);
        assertFalse(contructedValidTypeWithoutConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
        ObstacleBlock connection = new TeleporterBlock(0, 0, null);
        BlockAbstract contructedValidTypeWithConnection = new TeleporterBlock(0, 0, connection);
        assertTrue(contructedValidTypeWithConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
    
    }

    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract teleporterBlock = new TeleporterBlock(0, 0, null);
        assertTrue(teleporterBlock.hasCollision(), "Directed blocks, and thus by extension moveable blocks, do have collision, thus hasCollision() should always return true");
    }
    

    // //Tests for constructor and methods inherited from DirectedBlock
    // Teleporter blocks have only one valid direction (null), and thus rather than taking in a direction parameter,
    // it instead fills in null in the DirectedBlock parameter by default, thus there are no tests to be made specific
    // to the inherited DirectedBlock constructor aside from what gets tested in BlockAbstract section from which that class inherits.

    // @Test
    // public void testConstructorValidDirections() {
    //     for (String direction : validDirections) {
    //         DirectedBlock constructedValidDirection = new TeleporterBlock(0, 0, validTypes[0], null);
    //         assertEquals(direction, constructedValidDirection.getDirection());
    //         assertEquals(0, constructedValidDirection.getDirectionXY()[0]);
    //         assertEquals(0, constructedValidDirection.getDirectionXY()[1]);
    //     }
    // }

    // private void testConstructorInvalidDirections(String invalidDirection) {
    //     assertThrows(
    //         IllegalArgumentException.class,
    //         () -> new TeleporterBlock(0, 0, 't', null),
    //         "IllegalArgument should be thrown if the constuctor is provided with an invalid direction. Direction was: " + invalidDirection);
    // }
    // @Test
    // public void testConstructorInvalidDirections() {
    //     String[] invalidDirections = new String[]{"NorthToVabbi", "up", "down", "right", "left", ""};
    //     for (String invalidDirection : invalidDirections) {
    //         testConstructorInvalidDirections(invalidDirection);
    //     }
    // }

    //Tests for constructor and methods inherited from ObstacleBlock

    @Test
    public void testConstructorWithoutConnection() {
        TeleporterBlock tpWithoutConnection = new TeleporterBlock(0, 0, null);
        assertFalse(tpWithoutConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
        assertNull(tpWithoutConnection.getConnection(), "A teleporter constructed without a connection, should have their connection set to null.");
    }
    @Test
    public void testConstructorWithValidConnection() {
        ObstacleBlock connection = new TeleporterBlock(0, 0, null);
        BlockAbstract tpWithConnection = new TeleporterBlock(0, 0, connection);
        assertTrue(tpWithConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
        
    }
    @Test
    public void testConstructorWithInvalidConnection() {
        ObstacleBlock wall = new PortalWallBlock(0, 0, 'w', null, null);
        PortalWallBlock portal = new PortalWallBlock(0, 0, 'u', "left", null);
        assertThrows(IllegalArgumentException.class, 
        () -> new TeleporterBlock(0, 0, wall),
        "IllegalArgument should be thrown when attempting to construct a teleporter block with a non-teleporter connection.");
        assertThrows(IllegalArgumentException.class, 
        () -> new TeleporterBlock(0, 0, portal),
        "IllegalArgument should be thrown when attempting to construct a teleporter block with a non-teleporter connection.");
    }

    @Nested
    //The following connection tests are inspired by the partner excercise.
    class TestNestConnectionMethods {
        private TeleporterBlock teleporter1;
        private TeleporterBlock teleporter2;

        @BeforeEach
        private void setup() {
            teleporter1 = new TeleporterBlock(0, 0, null);
            teleporter2 = new TeleporterBlock(5, 5, null);
        }
        @Test
        @DisplayName("teleporter1 should be connected to teleporter2 and vice versa after a teleporter1.setConnection(teleporter2) method call, and their state should be set to true.")
        public void testSetConnectionValidInput() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            assertTrue(teleporter1.getState(), "teleporter1 should have it's state set to true once it is connected to another teleporter");
            assertTrue(teleporter2.getState(), "teleporter2 should have it's state set to true once it is connected to another teleporter");
        }
        @Test
        @DisplayName("Attempting to connect a teleporter to a non-teleporter object should throw IllegalArgumentException")
        public void testSetConnectionInvalidInput() {
            ObstacleBlock wall = new PortalWallBlock(0, 0, 'w', null, null);
            PortalWallBlock portal = new PortalWallBlock(0, 0, 'u', "left", null);
            assertThrows(IllegalArgumentException.class, 
            () -> teleporter1.setConnection(wall),
            "IllegalArgument should be thrown when attempting to connect a teleporter with a non-teleporter object");
            assertThrows(IllegalArgumentException.class, 
            () -> teleporter2.setConnection(portal),
            "IllegalArgument should be thrown when attempting to connect a teleporter with a non-teleporter object");
        }
        @Test
        @DisplayName("Setting a connected teleporter's connection to null should remove the connection of both that teleporter and its connection, and their state should be set to false.")
        public void testSetConnectionNull() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            teleporter1.setConnection(null);
            assertNull(teleporter1.getConnection(), "teleporter1 should no longer have a connection after the setConnection(null) was called on it.");
            assertNull(teleporter2.getConnection(), "teleporter2 should no longer have a connection after the setConnection(null) was called on its connection.");
            assertFalse(teleporter1.getState(), "teleporter1 should have its state set to false once its connection has been set to null.");
            assertFalse(teleporter2.getState(), "teleporter2 should have its state set to false once its connection's connection set to null.");
        }
        @Test
        @DisplayName("Removing a connected teleporter's connection should remove the connection of both that teleporter and its connection, and their state should be set to false.")
        public void testRemoveConnection() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            teleporter1.removeConnection();
            assertNull(teleporter1.getConnection(), "teleporter1 should no longer have a connection after the removeConnection() was called on it.");
            assertNull(teleporter2.getConnection(), "teleporter2 should no longer have a connection after the removeConnection() was called on its connection.");
            assertFalse(teleporter1.getState(), "teleporter1 should have its state set to false once its connection has been removed.");
            assertFalse(teleporter2.getState(), "teleporter2 should have its state set to false once its connection's connection has been removed.");
        }
        @Test
        @DisplayName("Removing a connected teleporter's connection should remove the connection of both that teleporter and its connection, and their state should be set to false.")
        public void testReplaceConnectionWithNewConnection() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            TeleporterBlock teleporter3 = new TeleporterBlock(0, 0, null);
            //teleporter3 replaces teleporter2 as teleporter1's connection
            teleporter1.setConnection(teleporter3);
            assertEquals(teleporter3, teleporter1.getConnection(), "teleporter1 should be connected to teleporter3");
            assertEquals(teleporter1, teleporter3.getConnection(), "teleporter3 should be connected to teleporter1");
            assertNull(teleporter2.getConnection(), "teleporter2 should no longer have a connection once it's been replaced by teleporter3 as teleporter1's connection.");
            assertTrue(teleporter1.getState(), "teleporter1 should have its state set to false once its connection has been removed.");
            assertTrue(teleporter3.getState(), "teleporter2 should have its state set to false once its connection's connection has been removed.");
            assertFalse(teleporter2.getState(), "teleporter2 should have its state set to false once its connection's connection has been removed.");
        }

        @Test
        @DisplayName("Check that a connected teleporter returns a list containing correct entry point coordinates")
        public void testGetEntryPointsConnectedTeleporter() {
            
        }
        @Test
        @DisplayName("Check that a disconnected does not return any entry point coordinates.")
        public void testGetEntryPointsDisconnectedTeleporter() {

        }
        @Test
        @DisplayName("Check that a connected teleporter returns a list containing correct entry point coordinates")
        public void testGetExitPointConnectedTeleporter() {
            
        }
        @Test
        @DisplayName("Check that a disconnected does not return any entry point coordinates.")
        public void testGetExitPointDisconnectedTeleporter() {

        }


        @Test
        @DisplayName("Check that a connected teleporter CAN be entered by a block that is standing at one of the teleporter's entry points.")
        public void testCanBlockAtEntryEnterConnectedTeleporter() {
            teleporter1.setConnection(teleporter2);
            BlockAbstract blockAtUpperEntry = new TeleporterBlock(0, 1, null);
            BlockAbstract blockAtLowerEntry = new TeleporterBlock(0, -1, null);
            BlockAbstract blockAtRightEntry = new TeleporterBlock(1, 0, null);
            BlockAbstract blockAtLeftEntry = new TeleporterBlock(-1, 0, null);
            assertTrue(teleporter1.canBlockEnter(blockAtUpperEntry));
            assertTrue(teleporter1.canBlockEnter(blockAtLowerEntry));
            assertTrue(teleporter1.canBlockEnter(blockAtRightEntry));
            assertTrue(teleporter1.canBlockEnter(blockAtLeftEntry));
        }
        @Test
        @DisplayName("Check that a connected teleporter CAN NOT be entered by a block that is NOT standing at one of the teleporter's entry points.")
        public void testCanBlockNotAtEntryEnterConnectedTeleporter() {
            teleporter1.setConnection(teleporter2);
            BlockAbstract blockNotAtEntry = new TeleporterBlock(0, 0, null);
            assertFalse(teleporter1.canBlockEnter(blockNotAtEntry));
            BlockAbstract blockAtTeleporterCoordinates = new TeleporterBlock(0, 0, null);
            assertFalse(teleporter1.canBlockEnter(blockAtTeleporterCoordinates));
            BlockAbstract blockNearEntry = new TeleporterBlock(1, 1, null);
            assertFalse(teleporter1.canBlockEnter(blockNearEntry ));
        }
        @Test
        @DisplayName("Check that disconnected teleporters can not be entered.")
        public void testCanBlockEnterDisconnectedTeleporter() {
            BlockAbstract block = new TeleporterBlock(1, 0, null);
            assertFalse(teleporter1.canBlockEnter(block), "A block should not be able to enter a disconnected teleporter, even while standing at an entry point.");
        }
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
        TeleporterBlock teleporter = new TeleporterBlock(0, 0, null);
        assertTrue(teleporter.isTeleporter());
    }
    
    
}
