import java.awt.*;
import java.util.StringTokenizer;

class SweepElm extends CircuitElm {
    final int FLAG_LOG = 1;
    final int FLAG_BIDIR = 2;
    final int circleSize = 17;
    double maxV, maxF, minF, sweepTime, frequency;
    double fadd, fmul, freqTime, savedTimeStep;
    int dir = 1;
    double v;

    public SweepElm(int xx, int yy) {
        super(xx, yy);
        minF = 20;
        maxF = 4000;
        maxV = 5;
        sweepTime = .1;
        flags = FLAG_BIDIR;
        reset();
    }

    public SweepElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        minF = Double.parseDouble(st.nextToken());
        maxF = Double.parseDouble(st.nextToken());
        maxV = Double.parseDouble(st.nextToken());
        sweepTime = Double.parseDouble(st.nextToken());
        reset();
    }

    int getDumpType() {
        return 170;
    }

    int getPostCount() {
        return 1;
    }

    String dump() {
        return super.dump() + " " + minF + " " + maxF + " " + maxV + " " +
                sweepTime;
    }

    void setPoints() {
        super.setPoints();
        lead1 = interpPoint(point1, point2, 1 - circleSize / dn);
    }

    void draw(Graphics g) {
        setBbox(point1, point2, circleSize);
        setVoltageColor(g, volts[0]);
        drawThickLine(g, point1, lead1);
        g.setColor(needsHighlight() ? selectColor : Color.gray);
        setPowerColor(g, false);
        int xc = point2.x;
        int yc = point2.y;
        drawThickCircle(g, xc, yc, circleSize);
        int wl = 8;
        adjustBbox(xc - circleSize, yc - circleSize,
                xc + circleSize, yc + circleSize);
        int i;
        int xl = 10;
        int ox = -1, oy = -1;
        long tm = System.currentTimeMillis();
        //double w = (this == mouseElm ? 3 : 2);
        tm %= 2000;
        if (tm > 1000)
            tm = 2000 - tm;
        double w = 1 + tm * .002;
        if (!sim.stoppedCheck.getState())
            w = 1 + 2 * (frequency - minF) / (maxF - minF);
        for (i = -xl; i <= xl; i++) {
            int yy = yc + (int) (.95 * Math.sin(i * pi * w / xl) * wl);
            if (ox != -1)
                drawThickLine(g, ox, oy, xc + i, yy);
            ox = xc + i;
            oy = yy;
        }
        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(frequency, "Hz");
            if (dx == 0 || dy == 0)
                drawValues(g, s, circleSize);
        }

        drawPosts(g);
        curcount = updateDotCount(-current, curcount);
        if (sim.dragElm != this)
            drawDots(g, point1, lead1, curcount);
    }

    void stamp() {
        sim.stampVoltageSource(0, nodes[0], voltSource);
    }

    void setParams() {
        if (frequency < minF || frequency > maxF) {
            frequency = minF;
            freqTime = 0;
            dir = 1;
        }
        if ((flags & FLAG_LOG) == 0) {
            fadd = dir * sim.timeStep * (maxF - minF) / sweepTime;
            fmul = 1;
        } else {
            fadd = 0;
            fmul = Math.pow(maxF / minF, dir * sim.timeStep / sweepTime);
        }
        savedTimeStep = sim.timeStep;
    }

    void reset() {
        frequency = minF;
        freqTime = 0;
        dir = 1;
        setParams();
    }

    void startIteration() {
        // has timestep been changed?
        if (sim.timeStep != savedTimeStep)
            setParams();
        v = Math.sin(freqTime) * maxV;
        freqTime += frequency * 2 * pi * sim.timeStep;
        frequency = frequency * fmul + fadd;
        if (frequency >= maxF && dir == 1) {
            if ((flags & FLAG_BIDIR) != 0) {
                fadd = -fadd;
                fmul = 1 / fmul;
                dir = -1;
            } else
                frequency = minF;
        }
        if (frequency <= minF && dir == -1) {
            fadd = -fadd;
            fmul = 1 / fmul;
            dir = 1;
        }
    }

    void doStep() {
        sim.updateVoltageSource(0, nodes[0], voltSource, v);
    }

    double getVoltageDiff() {
        return volts[0];
    }

    int getVoltageSourceCount() {
        return 1;
    }

    boolean hasGroundConnection(int n1) {
        return true;
    }

    void getInfo(String[] arr) {
        arr[0] = "sweep " + (((flags & FLAG_LOG) == 0) ? "(linear)" : "(log)");
        arr[1] = "I = " + getCurrentDText(getCurrent());
        arr[2] = "V = " + getVoltageText(volts[0]);
        arr[3] = "f = " + getUnitText(frequency, "Hz");
        arr[4] = "range = " + getUnitText(minF, "Hz") + " .. " +
                getUnitText(maxF, "Hz");
        arr[5] = "time = " + getUnitText(sweepTime, "s");
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Min Frequency (Hz)", minF, 0, 0);
        if (n == 1)
            return new EditInfo("Max Frequency (Hz)", maxF, 0, 0);
        if (n == 2)
            return new EditInfo("Sweep Time (s)", sweepTime, 0, 0);
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Logarithmic", (flags & FLAG_LOG) != 0);
            return ei;
        }
        if (n == 4)
            return new EditInfo("Max Voltage", maxV, 0, 0);
        if (n == 5) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Bidirectional", (flags & FLAG_BIDIR) != 0);
            return ei;
        }
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        double maxfreq = 1 / (8 * sim.timeStep);
        if (n == 0) {
            minF = ei.value;
            if (minF > maxfreq)
                minF = maxfreq;
        }
        if (n == 1) {
            maxF = ei.value;
            if (maxF > maxfreq)
                maxF = maxfreq;
        }
        if (n == 2)
            sweepTime = ei.value;
        if (n == 3) {
            flags &= ~FLAG_LOG;
            if (ei.checkbox.getState())
                flags |= FLAG_LOG;
        }
        if (n == 4)
            maxV = ei.value;
        if (n == 5) {
            flags &= ~FLAG_BIDIR;
            if (ei.checkbox.getState())
                flags |= FLAG_BIDIR;
        }
        setParams();
    }
}
    
