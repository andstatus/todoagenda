package org.andstatus.todoagenda;

import static org.andstatus.todoagenda.prefs.InstanceSettings.EVENT_RANGE_TODAY;
import static org.andstatus.todoagenda.prefs.InstanceSettings.EVENT_RANGE_TODAY_AND_TOMORROW;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.WidgetEntryPosition;
import org.junit.Test;

/**
 * @author yvolk@yurivolkov.com
 * https://github.com/andstatus/todoagenda/issues/102
 */
public class TomorrowsTasksInTodaysFilterTest extends BaseWidgetTest {
    @Test
    public void testIssue102() {
        final String method = "testIssue102";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.tomorrows_tasks_one_week);
        provider.addResults(inputs);

        playResults(method);
        assertPosition(10, WidgetEntryPosition.LIST_FOOTER);

        getSettings().eventRange = EVENT_RANGE_TODAY;
        playResults(method);
        assertPosition(0, WidgetEntryPosition.LIST_FOOTER);

        getSettings().eventRange = EVENT_RANGE_TODAY_AND_TOMORROW;
        playResults(method);
        assertPosition(7, WidgetEntryPosition.LIST_FOOTER);
    }
}
