package org.andstatus.todoagenda.task.astrid;

import android.net.Uri;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

interface AstridCloneTaskSource {

  AstridCloneTaskSource GOOGLE_TASKS = new AstridCloneTaskSource() {
    @Override
    public Uri getListUri() {
      return AstridCloneTasksProvider.GOOGLE_LISTS_URI;
    }

    @Override
    public String getListColumnId() {
      return "gtl_id";
    }

    @Override
    public String getListColumnTitle() {
      return "gtl_title";
    }

    @Override
    public String getListColumnListColor() {
      return "gtl_color";
    }

    @Override
    public String getListColumnAccount() {
      return "gtl_account";
    }
  };

  AstridCloneTaskSource ASTRID_TASKS = new AstridCloneTaskSource() {
    @Override
    public Uri getListUri() {
      return AstridCloneTasksProvider.TASKS_LISTS_URI;
    }

    @Override
    public String getListColumnId() {
      return "cdl_id";
    }

    @Override
    public String getListColumnTitle() {
      return "cdl_name";
    }

    @Override
    public String getListColumnListColor() {
      return "cdl_color";
    }

    @Override
    public String getListColumnAccount() {
      return "cda_name";
    }

    @Override
    public boolean isAllDay(Long dueMillisRaw) {
      return dueMillisRaw != null && dueMillisRaw % 60000 <= 0;
    }

    @Override
    public Long toDueMillis(Long dueMillisRaw, DateTimeZone zone) {
      if (!isAllDay(dueMillisRaw)) return dueMillisRaw;

      // Astrid tasks without due times are assigned a time of 12:00:00
      // see https://github.com/andstatus/todoagenda/issues/2#issuecomment-688866280
      return new DateTime(dueMillisRaw, zone).withTimeAtStartOfDay().getMillis();
    }
  };

  Uri getListUri();

  String getListColumnId();

  String getListColumnTitle();

  String getListColumnListColor();

  String getListColumnAccount();

  default boolean isAllDay(Long dueMillisRaw) {
    return false;
  }

  default Long toDueMillis(Long dueMillisRaw, DateTimeZone zone) {
    return dueMillisRaw;
  }
}
