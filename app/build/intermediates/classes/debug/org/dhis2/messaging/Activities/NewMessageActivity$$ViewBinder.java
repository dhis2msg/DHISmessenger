// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class NewMessageActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.NewMessageActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296354, "field 'subject'");
    target.subject = finder.castView(view, 2131296354, "field 'subject'");
    view = finder.findRequiredView(source, 2131296356, "field 'content'");
    target.content = finder.castView(view, 2131296356, "field 'content'");
    view = finder.findRequiredView(source, 2131296351, "field 'recipients'");
    target.recipients = finder.castView(view, 2131296351, "field 'recipients'");
    view = finder.findRequiredView(source, 2131296348, "field 'units'");
    target.units = finder.castView(view, 2131296348, "field 'units'");
  }

  @Override public void unbind(T target) {
    target.subject = null;
    target.content = null;
    target.recipients = null;
    target.units = null;
  }
}
