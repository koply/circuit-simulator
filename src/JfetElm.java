import java.awt.*;
import java.util.StringTokenizer;

class JfetElm extends MosfetElm {
    Polygon gatePoly;
    Polygon arrowPoly;
    Point gatePt;
    JfetElm(int xx, int yy, boolean pnpflag) {
        super(xx, yy, pnpflag);
        noDiagonal = true;
    }
    public JfetElm(int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        noDiagonal = true;
    }

    void draw(Graphics g) {
        setBbox(point1, point2, hs);
        setVoltageColor(g, volts[1]);
        drawThickLine(g, src[0], src[1]);
        drawThickLine(g, src[1], src[2]);
        setVoltageColor(g, volts[2]);
        drawThickLine(g, drn[0], drn[1]);
        drawThickLine(g, drn[1], drn[2]);
        setVoltageColor(g, volts[0]);
        drawThickLine(g, point1, gatePt);
        g.fillPolygon(arrowPoly);
        setPowerColor(g, true);
        g.fillPolygon(gatePoly);
        curcount = updateDotCount(-ids, curcount);
        if (curcount != 0) {
            drawDots(g, src[0], src[1], curcount);
            drawDots(g, src[1], src[2], curcount + 8);
            drawDots(g, drn[0], drn[1], -curcount);
            drawDots(g, drn[1], drn[2], -(curcount + 8));
        }
        drawPosts(g);
    }

    void setPoints() {
        super.setPoints();

        // find the coordinates of the various points we need to draw
        // the JFET.
        int hs2 = hs * dsign;
        src = newPointArray(3);
        drn = newPointArray(3);
        interpPoint2(point1, point2, src[0], drn[0], 1, hs2);
        interpPoint2(point1, point2, src[1], drn[1], 1, hs2 / 2);
        interpPoint2(point1, point2, src[2], drn[2], 1 - 10 / dn, hs2 / 2);

        gatePt = interpPoint(point1, point2, 1 - 14 / dn);

        Point[] ra = newPointArray(4);
        interpPoint2(point1, point2, ra[0], ra[1], 1 - 13 / dn, hs);
        interpPoint2(point1, point2, ra[2], ra[3], 1 - 10 / dn, hs);
        gatePoly = createPolygon(ra[0], ra[1], ra[3], ra[2]);
        if (pnp == -1) {
            Point x = interpPoint(gatePt, point1, 18 / dn);
            arrowPoly = calcArrow(gatePt, x, 8, 3);
        } else
            arrowPoly = calcArrow(point1, gatePt, 8, 3);
    }

    int getDumpType() {
        return 'j';
    }

    // these values are taken from Hayes+Horowitz p155
    double getDefaultThreshold() {
        return -4;
    }

    double getBeta() {
        return .00125;
    }

    void getInfo(String[] arr) {
        getFetInfo(arr, "JFET");
    }
}
