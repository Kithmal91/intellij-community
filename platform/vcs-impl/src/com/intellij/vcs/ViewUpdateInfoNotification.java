/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.vcs;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.ex.ProjectLevelVcsManagerEx;
import com.intellij.openapi.vcs.update.UpdateInfoTree;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ContentUtilEx;
import org.jetbrains.annotations.NotNull;

public class ViewUpdateInfoNotification extends NotificationAction {
  @NotNull private final Project myProject;
  @NotNull private final NullableComputable<UpdateInfoTree> myUpdateInfoTabCreator;

  @NotNull private UpdateInfoTree myTree;

  public ViewUpdateInfoNotification(@NotNull Project project,
                                    @NotNull UpdateInfoTree updateInfoTree,
                                    @NotNull String actionName,
                                    @NotNull NullableComputable<UpdateInfoTree> updateInfoTabCreator) {
    super(actionName);
    myProject = project;
    myTree = updateInfoTree;
    myUpdateInfoTabCreator = updateInfoTabCreator;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    boolean found = focusUpdateInfoTree(myProject, myTree);
    if (!found) {
      UpdateInfoTree tree = myUpdateInfoTabCreator.compute();
      if (tree != null) {
        myTree = tree;
        focusUpdateInfoTree(myProject, myTree);
      }
      else {
        notification.expire();
      }
    }
  }

  public static boolean focusUpdateInfoTree(@NotNull Project project, @NotNull UpdateInfoTree updateInfoTree) {
    Ref<Boolean> found = Ref.create(false);
    ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.VCS).activate(() -> {
      ContentManager contentManager = ProjectLevelVcsManagerEx.getInstanceEx(project).getContentManager();
      if (contentManager != null) {
        found.set(ContentUtilEx.selectContent(contentManager, updateInfoTree, true));
      }
    }, true, true);
    return found.get();
  }
}
