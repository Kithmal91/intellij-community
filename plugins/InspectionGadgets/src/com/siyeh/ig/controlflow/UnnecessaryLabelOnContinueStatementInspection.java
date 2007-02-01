/*
 * Copyright 2003-2007 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.controlflow;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import org.jetbrains.annotations.NotNull;

public class UnnecessaryLabelOnContinueStatementInspection
        extends BaseInspection {

    public String getGroupDisplayName() {
        return GroupNames.CONTROL_FLOW_GROUP_NAME;
    }

    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "unnecessary.label.on.continue.statement.display.name");
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "unnecessary.label.on.continue.statement.problem.descriptor");
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return new UnnecessaryLabelOnContinueStatementFix();
    }

    private static class UnnecessaryLabelOnContinueStatementFix
            extends InspectionGadgetsFix {

        public String getName() {
            return InspectionGadgetsBundle.message(
                    "unnecessary.label.remove.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiElement continueKeywordElement =
                    descriptor.getPsiElement();
            final PsiContinueStatement continueStatement =
                    (PsiContinueStatement)continueKeywordElement.getParent();
            final PsiIdentifier labelIdentifier =
                    continueStatement.getLabelIdentifier();
            if (labelIdentifier == null) {
                return;
            }
            labelIdentifier.delete();
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new UnnecessaryLabelOnContinueStatementVisitor();
    }

    private static class UnnecessaryLabelOnContinueStatementVisitor
            extends BaseInspectionVisitor {

        public void visitContinueStatement(
                @NotNull PsiContinueStatement statement) {
            final PsiIdentifier labelIdentifier =
                    statement.getLabelIdentifier();
            if (labelIdentifier == null) {
                return;
            }
            final String labelText = labelIdentifier.getText();
            if (labelText == null || labelText.length() == 0) {
                return;
            }
            final PsiStatement exitedStatement =
                    statement.findContinuedStatement();
            if (exitedStatement == null) {
                return;
            }
            final PsiStatement labelEnabledParent =
                    PsiTreeUtil.getParentOfType(statement,
                            PsiForStatement.class, PsiDoWhileStatement.class,
                            PsiForeachStatement.class, PsiWhileStatement.class,
                            PsiSwitchStatement.class);
            if (labelEnabledParent == null) {
                return;
            }
            if (!exitedStatement.equals(labelEnabledParent)) {
                return;
            }
            registerStatementError(statement);
        }
    }
}