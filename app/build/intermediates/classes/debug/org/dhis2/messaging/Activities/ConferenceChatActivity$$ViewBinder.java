// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ConferenceChatActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.ConferenceChatActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296428, "field 'text'");
    target.text = finder.castView(view, 2131296428, "field 'text'");
    view = finder.findRequiredView(source, 2131296427, "field 'list'");
    target.list = finder.castView(view, 2131296427, "field 'list'");
    view = finder.findRequiredView(source, 2131296478, "field 'send' and method 'sendClicked'");
    target.send = finder.castView(view, 2131296478, "field 'send'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.sendClicked();
        }
      });
    view = finder.findRequiredView(source, 2131296364, "field 'pb'");
    target.pb = finder.castView(view, 2131296364, "field 'pb'");
    view = finder.findRequiredView(source, 2131296360, "field 'contentLoader'");
    target.contentLoader = finder.castView(view, 2131296360, "field 'contentLoader'");
  }

  @Override public void unbind(T target) {
    target.text = null;
    target.list = null;
    target.send = null;
    target.pb = null;
    target.contentLoader = null;
  }
}
