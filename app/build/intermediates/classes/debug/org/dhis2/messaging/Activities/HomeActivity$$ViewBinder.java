// Generated code from Butter Knife. Do not modify!
package org.dhis2.messaging.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class HomeActivity$$ViewBinder<T extends org.dhis2.messaging.Activities.HomeActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296342, "field 'drawerLayout'");
    target.drawerLayout = finder.castView(view, 2131296342, "field 'drawerLayout'");
    view = finder.findRequiredView(source, 2131296345, "field 'drawerListView'");
    target.drawerListView = finder.castView(view, 2131296345, "field 'drawerListView'");
  }

  @Override public void unbind(T target) {
    target.drawerLayout = null;
    target.drawerListView = null;
  }
}
