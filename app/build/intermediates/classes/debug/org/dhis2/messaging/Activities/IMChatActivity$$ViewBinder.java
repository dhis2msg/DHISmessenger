// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class IMChatActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.IMChatActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296428, "field 'text'");
    target.text = finder.castView(view, 2131296428, "field 'text'");
    view = finder.findRequiredView(source, 2131296427, "field 'listView'");
    target.listView = finder.castView(view, 2131296427, "field 'listView'");
    view = finder.findRequiredView(source, 2131296362, "field 'send'");
    target.send = finder.castView(view, 2131296362, "field 'send'");
    view = finder.findRequiredView(source, 2131296364, "field 'pb'");
    target.pb = finder.castView(view, 2131296364, "field 'pb'");
    view = finder.findRequiredView(source, 2131296478, "method 'sendClicked'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.sendClicked();
        }
      });
  }

  @Override public void unbind(T target) {
    target.text = null;
    target.listView = null;
    target.send = null;
    target.pb = null;
  }
}
