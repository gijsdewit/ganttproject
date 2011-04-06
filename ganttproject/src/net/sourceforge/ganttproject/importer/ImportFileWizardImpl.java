/*
 * Created on 30.04.2005
 */
package net.sourceforge.ganttproject.importer;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.sourceforge.ganttproject.GanttOptions;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.Mediator;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.projectwizard.WizardImpl;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.plugins.PluginManager;

/**
 * @author bard
 */
public class ImportFileWizardImpl extends WizardImpl {
    private final State myState;

    private static List<Importer> ourImporters;

    public ImportFileWizardImpl(UIFacade uiFacade, GanttProject project, GanttOptions options) {
        super(uiFacade, ImportFileWizardImpl.i18n("importWizard.dialog.title"));
        myState = new State();
        if (ourImporters == null) {
            ourImporters = getImporters();
        }
        for (Importer importer : ourImporters) {
            importer.setContext((IGanttProject)project, uiFacade, options.getPluginPreferences());
        }
        addPage(new ImporterChooserPage(ourImporters, myState));
        addPage(new FileChooserPage(
                this,
                options.getPluginPreferences().node("/instance/net.sourceforge.ganttproject/import"),
                myState));
    }

    private static List<Importer> getImporters() {
        return PluginManager.getExtensions(Importer.EXTENSION_POINT_ID, Importer.class);
    }

    protected void onOkPressed() {
        super.onOkPressed();
        if ("file".equals(myState.getUrl().getProtocol())) {
            myState.myImporter.run(new File(myState.getUrl().getPath()));
        }
        else {
            getUIFacade().showErrorDialog(new Exception("You are not supposed to see this. Please report this bug."));
        }
    }

    protected boolean canFinish() {
        return myState.myImporter != null
            && myState.getUrl() != null
            && "file".equals(myState.getUrl().getProtocol());
    }

    private static String i18n(String key) {
        return GanttLanguage.getInstance().getText(key);
    }

    static class State {
        Importer myImporter;

        private URL myUrl;

        public void setUrl(URL url) {
            myUrl = url;
        }

        public URL getUrl() {
            return myUrl;
        }
    }
}
