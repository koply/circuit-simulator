import java.awt.Dialog;

// import netscape.javascript.*; // add plugin.jar to classpath during compilation

/*
import java.applet.Applet;	#unused imports
import java.awt.Frame;
*/

class ImportExportAppletDialog
        extends Dialog
        implements ImportExportDialog {
    /**
     *
     */
    private static final long serialVersionUID = 7610750057068682518L;
    Action type;
    CirSim cframe;
    String circuitDump;

    ImportExportAppletDialog(CirSim f, Action type)
            throws Exception {
        super(f, (type == Action.EXPORT) ? "Export" : "Import", false);
        this.type = type;
        cframe = f;
        if (cframe.applet == null)
            throw new Exception("Not running as an applet!");
    }

    public void setDump(String dump) {
        circuitDump = dump;
    }

    @Deprecated
    public void execute() {
        // Antique technology.

        /*
        try {
            JSObject window = JSObject.getWindow(cframe.applet);
            if (type == Action.EXPORT) {
                //cframe.setVisible(false);
                window.call("exportCircuit", circuitDump);
            } else {
                //cframe.setVisible(false);
                circuitDump = (String) window.eval("importCircuit()");
                cframe.readSetup(circuitDump);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
