// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class LoginActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.LoginActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296338, "field 'username'");
    target.username = finder.castView(view, 2131296338, "field 'username'");
    view = finder.findRequiredView(source, 2131296340, "field 'password'");
    target.password = finder.castView(view, 2131296340, "field 'password'");
    view = finder.findRequiredView(source, 2131296339, "field 'signin'");
    target.signin = finder.castView(view, 2131296339, "field 'signin'");
    view = finder.findRequiredView(source, 2131296341, "field 'about' and method 'clickedAbout'");
    target.about = finder.castView(view, 2131296341, "field 'about'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.clickedAbout();
        }
      });
    view = finder.findRequiredView(source, 2131296337, "field 'server'");
    target.server = finder.castView(view, 2131296337, "field 'server'");
  }

  @Override public void unbind(T target) {
    target.username = null;
    target.password = null;
    target.signin = null;
    target.about = null;
    target.server = null;
  }
}
