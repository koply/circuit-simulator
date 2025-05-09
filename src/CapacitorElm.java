import java.awt.*;
import java.util.StringTokenizer;

class CapacitorElm extends CircuitElm {
    double capacitance;
    double compResistance, voltdiff;
    Point[] plate1, plate2;
    public static final int FLAG_BACK_EULER = 2;

    public CapacitorElm(int xx, int yy) {
        super(xx, yy);
        capacitance = 1e-5;
    }

    public CapacitorElm(int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        capacitance = Double.parseDouble(st.nextToken());
        voltdiff = Double.parseDouble(st.nextToken());
    }

    boolean isTrapezoidal() {
        return (flags & FLAG_BACK_EULER) == 0;
    }

    void setNodeVoltage(int n, double c) {
        super.setNodeVoltage(n, c);
        voltdiff = volts[0] - volts[1];
    }

    void reset() {
        current = curcount = 0;
        // put small charge on caps when reset to start oscillators
        voltdiff = 1e-3;
    }

    int getDumpType() {
        return 'c';
    }

    String dump() {
        return super.dump() + " " + capacitance + " " + voltdiff;
    }

    void setPoints() {
        super.setPoints();
        double f = (dn / 2 - 4) / dn;
        // calc leads
        lead1 = interpPoint(point1, point2, f);
        lead2 = interpPoint(point1, point2, 1 - f);
        // calc plates
        plate1 = newPointArray(2);
        plate2 = newPointArray(2);
        interpPoint2(point1, point2, plate1[0], plate1[1], f, 12);
        interpPoint2(point1, point2, plate2[0], plate2[1], 1 - f, 12);
    }

    void draw(Graphics g) {
        int hs = 12;
        setBbox(point1, point2, hs);

        // draw first lead and plate
        setVoltageColor(g, volts[0]);
        drawThickLine(g, point1, lead1);
        setPowerColor(g, false);
        drawThickLine(g, plate1[0], plate1[1]);
        if (sim.powerCheckItem.getState())
            g.setColor(Color.gray);

        // draw second lead and plate
        setVoltageColor(g, volts[1]);
        drawThickLine(g, point2, lead2);
        setPowerColor(g, false);
        drawThickLine(g, plate2[0], plate2[1]);

        updateDotCount();
        if (sim.dragElm != this) {
            drawDots(g, point1, lead1, curcount);
            drawDots(g, point2, lead2, -curcount);
        }
        drawPosts(g);
        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(capacitance, "F");
            drawValues(g, s, hs);
        }
    }

    void stamp() {
        // capacitor companion model using trapezoidal approximation
        // (Norton equivalent) consists of a current source in
        // parallel with a resistor.  Trapezoidal is more accurate
        // than backward euler but can cause oscillatory behavior
        // if RC is small relative to the timestep.
        if (isTrapezoidal())
            compResistance = sim.timeStep / (2 * capacitance);
        else
            compResistance = sim.timeStep / capacitance;
        sim.stampResistor(nodes[0], nodes[1], compResistance);
        sim.stampRightSide(nodes[0]);
        sim.stampRightSide(nodes[1]);
    }

    void startIteration() {
        if (isTrapezoidal())
            curSourceValue = -voltdiff / compResistance - current;
        else
            curSourceValue = -voltdiff / compResistance;
        //System.out.println("cap " + compResistance + " " + curSourceValue + " " + current + " " + voltdiff);
    }

    void calculateCurrent() {
        double voltdiff = volts[0] - volts[1];
        // we check compResistance because this might get called
        // before stamp(), which sets compResistance, causing
        // infinite current
        if (compResistance > 0)
            current = voltdiff / compResistance + curSourceValue;
    }

    double curSourceValue;

    void doStep() {
        sim.stampCurrentSource(nodes[0], nodes[1], curSourceValue);
    }

    void getInfo(String[] arr) {
        arr[0] = "capacitor";
        getBasicInfo(arr);
        arr[3] = "C = " + getUnitText(capacitance, "F");
        arr[4] = "P = " + getUnitText(getPower(), "W");
        //double v = getVoltageDiff();
        //arr[4] = "U = " + getUnitText(.5*capacitance*v*v, "J");
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Capacitance (F)", capacitance, 0, 0);
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Trapezoidal Approximation", isTrapezoidal());
            return ei;
        }
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.value > 0)
            capacitance = ei.value;
        if (n == 1) {
            if (ei.checkbox.getState())
                flags &= ~FLAG_BACK_EULER;
            else
                flags |= FLAG_BACK_EULER;
        }
    }

    int getShortcut() {
        return 'c';
    }
}
