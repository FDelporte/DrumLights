package be.webtechie.drumbooth.i2c;

import be.webtechie.drumbooth.i2c.definition.Board;
import be.webtechie.drumbooth.i2c.definition.Relay;
import be.webtechie.drumbooth.i2c.definition.State;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Controller to set the relay states on different boards.
 */
public class RelayController {

    private static Logger logger = Logger.getLogger(RelayController.class);

    /**
     * Hide constructor as all methods are static
     */
    private RelayController() {
        // NOP
    }

    /**
     * Set all relays off
     */
    public static void setAllOff() {
        setRelays(
                Arrays.asList(Board.BOARD_1, Board.BOARD_2),
                Arrays.asList(Relay.RELAY_1, Relay.RELAY_2, Relay.RELAY_3, Relay.RELAY_4),
                State.STATE_OFF
        );
    }

    /**
     * Set the state of the all the relays on the given boards.
     *
     * @param boards List of Enum Board
     * @param relays List of Enum Relay
     * @param state Enum State
     */
    public static void setRelays(List<Board> boards, List<Relay> relays, State state) {
        for (Board board : boards) {
            for (Relay relay : relays) {
                setRelay(board, relay, state);
            }
        }
    }

    /**
     * Set the state of the relay on the given board.
     *
     * @param board Enum Board
     * @param relay Enum Relay
     * @param state Enum State
     */
    public static void setRelay(Board board, Relay relay, State state) {
        String cmd = "i2cset -y 1"
                + " " + toHexString(board.getAddress())
                + " " + toHexString(relay.getChannel())
                + " " + toHexString(state.getValue());

        execute(cmd);

        logger.info(relay + " on " + board + " set to " + state
                + " with command: " + cmd);
    }

    /**
     * Convert the value to HEX byte string.
     *
     * @param value Numeric value
     * @return String value in HEX format
     */
    private static String toHexString(int value) {
        return String.format("0x%02X", value).substring(0, 4);
    }

    /**
     * Execute the given command, this is called by the public methods.
     *
     * @param cmd String command to be executed.
     */
    private static void execute(String cmd) {
        try {
            logger.info("Executing: " + cmd);

            // Get a process to be able to do native calls on the operating system.
            // You can compare this to opening a terminal window and running a command.
            Process p = Runtime.getRuntime().exec(cmd);

            // Get the error stream of the process and print it
            // so we will now if something goes wrong.
            InputStream error = p.getErrorStream();
            for (int i = 0; i < error.available(); i++) {
                logger.error("CMD error: " + error.read());
            }

            // Get the output stream of the process and print it
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                logger.info("CMD info: " + line);
            }
            input.close();

            // We don't need the process anymore.
            p.destroy();
        } catch (IOException ex) {
            logger.error("Error while sending relay I²C command: " + ex.getMessage());
        }
    }

    /*
    // I²C seems to be broken in Pi4J when used with after Java 8.
    // So below code doesn't work, but is added here as a reference.

    private I2CBus i2c;

    public RelayController() {
        try {
            this.i2c = I2CFactory.getInstance(I2CBus.BUS_1);
        } catch (UnsupportedBusNumberException ex) {
            logger.error("Bus number not supported: "
                    + ex.getMessage());
            this.i2c = null;
        } catch (IOException ex) {
            logger.error("Error while initializing the relay controller: "
                    + ex.getMessage());
            this.i2c = null;
        }
    }

    public void setRelay(Board board, Relay relay, State state) {
        logger.info("Setting relay on board "
                + String.format("0x%02X", board.getAddress())
                + ", relay " + String.format("0x%02X", relay.getChannel())
                + ", state " + String.format("0x%02X", state.getValue()));

        if (this.i2c == null) {
            logger.error("I²C not available");
            return;
        }

        try {
            I2CDevice device = i2c.getDevice(board.getAddress());
            device.write(relay.getChannel(), state.getValue());
        } catch (IOException ex) {
            logger.error("Error while setting relay: " + ex.getMessage());
        }
    }
    */
}
