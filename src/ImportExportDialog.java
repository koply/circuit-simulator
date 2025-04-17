
public interface ImportExportDialog {
    enum Action {IMPORT, EXPORT}

    void setDump(String dump);

    void execute();
}

