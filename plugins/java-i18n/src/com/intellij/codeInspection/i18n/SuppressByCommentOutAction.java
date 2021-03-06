/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package com.intellij.codeInspection.i18n;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
* User: cdr
*/
class SuppressByCommentOutAction extends SuppressIntentionAction {
  private final String nonNlsCommentPattern;

  SuppressByCommentOutAction(String nonNlsCommentPattern) {
    this.nonNlsCommentPattern = nonNlsCommentPattern;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    if (!FileModificationService.getInstance().preparePsiElementForWrite(element)) return;
    element = findJavaCodeUpThere(element);
    PsiFile file = element.getContainingFile();
    editor = InjectedLanguageUtil.openEditorFor(file, project);
    int endOffset = element.getTextRange().getEndOffset();
    int line = editor.getDocument().getLineNumber(endOffset);
    int lineEndOffset = editor.getDocument().getLineEndOffset(line);

    PsiComment comment = PsiTreeUtil.findElementOfClassAtOffset(file, lineEndOffset-1, PsiComment.class, false);
    String prefix = "";
    boolean prefixFound = false;
    if (comment != null) {
      IElementType tokenType = comment.getTokenType();
      if (tokenType == JavaTokenType.END_OF_LINE_COMMENT) {
        prefix = StringUtil.trimStart(comment.getText(),"//") + " ";
        prefixFound = true;
      }
    }
    String commentText = "//" + prefix + nonNlsCommentPattern;
    if (prefixFound) {
      PsiComment newcom = JavaPsiFacade.getElementFactory(project).createCommentFromText(commentText, element);
      comment.replace(newcom);
    }
    else {
      editor.getDocument().insertString(lineEndOffset, " " + commentText);
    }
    DaemonCodeAnalyzer.getInstance(project).restart(); //comment replacement not necessarily rehighlights
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    if (!element.isValid()) {
      return false;
    }
    // find java code up there, going through injecttions if necessary
    return findJavaCodeUpThere(element) != null;
  }

  private static PsiElement findJavaCodeUpThere(PsiElement element) {
    while (element != null) {
      if (element.getLanguage() == StdLanguages.JAVA) return element;
      element = element.getContext();
    }
    return null;
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return InspectionsBundle.message("suppress.inspection.family");
  }

  @NotNull
  @Override
  public String getText() {
    return "Suppress with '" + nonNlsCommentPattern + "' comment";
  }
}
