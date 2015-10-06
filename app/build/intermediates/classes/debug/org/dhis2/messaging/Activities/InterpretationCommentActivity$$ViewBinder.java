// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class InterpretationCommentActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.InterpretationCommentActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296358, "field 'listView'");
    target.listView = finder.castView(view, 2131296358, "field 'listView'");
    view = finder.findRequiredView(source, 2131296363, "field 'newMessage'");
    target.newMessage = finder.castView(view, 2131296363, "field 'newMessage'");
    view = finder.findRequiredView(source, 2131296362, "field 'sendBtn' and method 'sendClicked'");
    target.sendBtn = finder.castView(view, 2131296362, "field 'sendBtn'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.sendClicked();
        }
      });
    view = finder.findRequiredView(source, 2131296364, "field 'progressBar'");
    target.progressBar = finder.castView(view, 2131296364, "field 'progressBar'");
    view = finder.findRequiredView(source, 2131296360, "field 'contentLoader'");
    target.contentLoader = finder.castView(view, 2131296360, "field 'contentLoader'");
  }

  @Override public void unbind(T target) {
    target.listView = null;
    target.newMessage = null;
    target.sendBtn = null;
    target.progressBar = null;
    target.contentLoader = null;
  }
}
