import java.awt.*;
import java.util.StringTokenizer;

class AnalogSwitchElm extends CircuitElm {
    final int FLAG_INVERT = 1;
    double resistance, r_on, r_off;

    public AnalogSwitchElm(int xx, int yy) {
        super(xx, yy);
        r_on = 20;
        r_off = 1e10;
    }

    public AnalogSwitchElm(int xa, int ya, int xb, int yb, int f,
                           StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        r_on = 20;
        r_off = 1e10;
        try {
            r_on = Double.parseDouble(st.nextToken());
            r_off = Double.parseDouble(st.nextToken());
        } catch (Exception e) {
        }

    }

    String dump() {
        return super.dump() + " " + r_on + " " + r_off;
    }

    int getDumpType() {
        return 159;
    }

    boolean open;

    Point ps, point3, lead3;

    void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps = new Point();
        int openhs = 16;
        point3 = interpPoint(point1, point2, .5, -openhs);
        lead3 = interpPoint(point1, point2, .5, -openhs / 2);
    }

    void draw(Graphics g) {
        int openhs = 16;
        int hs = (open) ? openhs : 0;
        setBbox(point1, point2, openhs);

        draw2Leads(g);

        g.setColor(lightGrayColor);
        interpPoint(lead1, lead2, ps, 1, hs);
        drawThickLine(g, lead1, ps);

        setVoltageColor(g, volts[2]);
        drawThickLine(g, point3, lead3);

        if (!open)
            doDots(g);
        drawPosts(g);
    }

    void calculateCurrent() {
        current = (volts[0] - volts[1]) / resistance;
    }

    // we need this to be able to change the matrix for each step
    boolean nonLinear() {
        return true;
    }

    void stamp() {
        sim.stampNonLinear(nodes[0]);
        sim.stampNonLinear(nodes[1]);
    }

    void doStep() {
        open = (volts[2] < 2.5);
        if ((flags & FLAG_INVERT) != 0)
            open = !open;
        resistance = (open) ? r_off : r_on;
        sim.stampResistor(nodes[0], nodes[1], resistance);
    }

    void drag(int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        if (abs(x - xx) < abs(y - yy))
            xx = x;
        else
            yy = y;
        int q1 = abs(x - xx) + abs(y - yy);
        int q2 = (q1 / 2) % sim.gridSize;
        if (q2 != 0)
            return;
        x2 = xx;
        y2 = yy;
        setPoints();
    }

    int getPostCount() {
        return 3;
    }

    Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : point3;
    }

    void getInfo(String[] arr) {
        arr[0] = "analog switch";
        arr[1] = open ? "open" : "closed";
        arr[2] = "Vd = " + getVoltageDText(getVoltageDiff());
        arr[3] = "I = " + getCurrentDText(getCurrent());
        arr[4] = "Vc = " + getVoltageText(volts[2]);
    }

    // we have to just assume current will flow either way, even though that
    // might cause singular matrix errors
    boolean getConnection(int n1, int n2) {
        return n1 != 2 && n2 != 2;
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Normally closed",
                    (flags & FLAG_INVERT) != 0);
            return ei;
        }
        if (n == 1)
            return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
        if (n == 2)
            return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            flags = (ei.checkbox.getState()) ?
                    (flags | FLAG_INVERT) :
                    (flags & ~FLAG_INVERT);
        if (n == 1 && ei.value > 0)
            r_on = ei.value;
        if (n == 2 && ei.value > 0)
            r_off = ei.value;
    }
}

