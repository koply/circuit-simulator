import java.util.StringTokenizer;

// contributed by Edward Calver

class PisoShiftElm extends ChipElm {
    short data = 0;//Lack of unsigned types sucks
    boolean clockstate = false;
    boolean modestate = false;

    public PisoShiftElm(int xx, int yy) {
        super(xx, yy);
    }
    public PisoShiftElm(int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    boolean hasReset() {
        return false;
    }

    String getChipName() {
        return "PISO shift register";
    }

    void setupPins() {
        sizeX = 10;
        sizeY = 3;
        pins = new Pin[getPostCount()];

        pins[0] = new Pin(1, SIDE_W, "L");
        pins[1] = new Pin(2, SIDE_W, "");
        pins[1].clock = true;

        pins[2] = new Pin(1, SIDE_N, "I7");
        pins[3] = new Pin(2, SIDE_N, "I6");
        pins[4] = new Pin(3, SIDE_N, "I5");
        pins[5] = new Pin(4, SIDE_N, "I4");
        pins[6] = new Pin(5, SIDE_N, "I3");
        pins[7] = new Pin(6, SIDE_N, "I2");
        pins[8] = new Pin(7, SIDE_N, "I1");
        pins[9] = new Pin(8, SIDE_N, "I0");

        pins[10] = new Pin(1, SIDE_E, "Q");
        pins[10].output = true;

    }

    int getPostCount() {
        return 11;
    }

    int getVoltageSourceCount() {
        return 1;
    }

    void execute() {
        if (pins[0].value && !modestate) {
            modestate = true;
            data = 0;
            if (pins[2].value) data += 128;
            if (pins[3].value) data += 64;
            if (pins[4].value) data += 32;
            if (pins[5].value) data += 16;
            if (pins[6].value) data += 8;
            if (pins[7].value) data += 4;
            if (pins[8].value) data += 2;
            if (pins[9].value) data += 1;
        } else if (pins[1].value && !clockstate) {
            clockstate = true;
            pins[10].value = (data & 1) != 0;
            data = (byte) (data >>> 1);
        }
        if (!pins[0].value) modestate = false;
        if (!pins[1].value) clockstate = false;
    }

    int getDumpType() {
        return 186;
    }

}
