/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns.inject;

import static com.google.errorprone.BugPattern.Category.INJECT;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.InjectMatchers.IS_APPLICATION_OF_JAVAX_INJECT;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import static javax.lang.model.element.Modifier.FINAL;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(
  name = "JavaxInjectOnFinalField",
  summary = "@javax.inject.Inject cannot be put on a final field.",
  explanation =
      "According to the JSR-330 spec, the @javax.inject.Inject annotation "
          + "cannot go on final fields.)",
  category = INJECT,
  severity = ERROR,
  maturity = EXPERIMENTAL
)
public class JavaxInjectOnFinalField extends BugChecker implements AnnotationTreeMatcher {

  private static final Matcher<Tree> FINAL_FIELD_MATCHER =
      new Matcher<Tree>() {
        @Override
        public boolean matches(Tree t, VisitorState state) {
          return isField(t, state) && ((VariableTree) t).getModifiers().getFlags().contains(FINAL);
        }
      };

  @Override
  public Description matchAnnotation(AnnotationTree annotationTree, VisitorState state) {
    if (IS_APPLICATION_OF_JAVAX_INJECT.matches(annotationTree, state)
        && FINAL_FIELD_MATCHER.matches(getAnnotatedNode(state), state)) {
      return describeMatch(annotationTree, SuggestedFix.delete(annotationTree));
    }
    return Description.NO_MATCH;
  }

  private static Tree getAnnotatedNode(VisitorState state) {
    return state.getPath().getParentPath().getParentPath().getLeaf();
  }

  private static boolean isField(Tree tree, VisitorState state) {
    return tree.getKind().equals(VARIABLE)
        && ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class)
            .getMembers()
            .contains(tree);
  }
}
