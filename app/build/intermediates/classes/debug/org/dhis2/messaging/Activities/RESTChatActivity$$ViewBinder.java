// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class RESTChatActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.RESTChatActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296358, "field 'listView'");
    target.listView = finder.castView(view, 2131296358, "field 'listView'");
    view = finder.findRequiredView(source, 2131296363, "field 'newMessage'");
    target.newMessage = finder.castView(view, 2131296363, "field 'newMessage'");
    view = finder.findRequiredView(source, 2131296364, "field 'sendLoader'");
    target.sendLoader = finder.castView(view, 2131296364, "field 'sendLoader'");
    view = finder.findRequiredView(source, 2131296360, "field 'receiveLoader'");
    target.receiveLoader = finder.castView(view, 2131296360, "field 'receiveLoader'");
    view = finder.findRequiredView(source, 2131296362, "field 'sendButton' and method 'clickedSend'");
    target.sendButton = finder.castView(view, 2131296362, "field 'sendButton'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.clickedSend();
        }
      });
  }

  @Override public void unbind(T target) {
    target.listView = null;
    target.newMessage = null;
    target.sendLoader = null;
    target.receiveLoader = null;
    target.sendButton = null;
  }
}
