/*
Copyright 2014 BarD Software s.r.o

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sourceforge.ganttproject.chart;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.CancelAction;
import net.sourceforge.ganttproject.action.OkAction;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.options.ProjectCalendarOptionPageProvider;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyException;

/**
 * This action shows a project calendar settings page.
 *
 * @author dbarashev (Dmitry Barashev)
 */
public class ProjectCalendarDialogAction extends AbstractAction {

  private final IGanttProject myProject;

  private final UIFacade myUIFacade;

  public ProjectCalendarDialogAction(IGanttProject project, UIFacade uiFacade) {
    super(GanttLanguage.getInstance().getCorrectedLabel("editPublicHolidays"));
    myProject = project;
    myUIFacade = uiFacade;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    final ProjectCalendarOptionPageProvider configPage = new ProjectCalendarOptionPageProvider();
    configPage.init(myProject, myUIFacade);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(configPage.buildPageComponent(), BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    myUIFacade.createDialog(panel, new Action[] { new OkAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        myUIFacade.getUndoManager().undoableEdit("Project calendar change", new Runnable() {
          @Override
          public void run() {
            onCalendarEditCommited(configPage);
          }
        });
      }
    }, CancelAction.EMPTY }, GanttLanguage.getInstance().getCorrectedLabel("editPublicHolidays")).show();
  }

  private void onCalendarEditCommited(ProjectCalendarOptionPageProvider configPage) {
    configPage.commit();
    myProject.setModified();
    try {
      myProject.getTaskManager().getAlgorithmCollection().getScheduler().run();
    } catch (TaskDependencyException e1) {
      GPLogger.getLogger(ProjectCalendarDialogAction.class).log(Level.SEVERE, "Exception after changing holidays", e1);
    }
    myUIFacade.getActiveChart().reset();
  }
}
