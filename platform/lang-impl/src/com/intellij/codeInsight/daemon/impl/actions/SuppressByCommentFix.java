/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package com.intellij.codeInsight.daemon.impl.actions;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ven
 */
public class SuppressByCommentFix extends AbstractSuppressByNoInspectionCommentFix {
  protected Class<? extends PsiElement> mySuppressionHolderClass;

  public SuppressByCommentFix(@NotNull HighlightDisplayKey key, @NotNull Class<? extends PsiElement> suppressionHolderClass) {
    this(key.getID());
    mySuppressionHolderClass = suppressionHolderClass;
  }

  private SuppressByCommentFix(final String ID) {
    super(ID, false);
  }

  @Override
  @NotNull
  public String getText() {
    return InspectionsBundle.message("suppress.inspection.statement");
  }

  @Override
  @Nullable
  protected PsiElement getContainer(PsiElement context) {
    return PsiTreeUtil.getParentOfType(context, mySuppressionHolderClass);
  }
}
