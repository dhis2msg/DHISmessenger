// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ProfileActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.ProfileActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296381, "field 'list'");
    target.list = finder.castView(view, 2131296381, "field 'list'");
    view = finder.findRequiredView(source, 2131296364, "field 'loader'");
    target.loader = finder.castView(view, 2131296364, "field 'loader'");
  }

  @Override public void unbind(T target) {
    target.list = null;
    target.loader = null;
  }
}
