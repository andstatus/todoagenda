package org.andstatus.todoagenda;

import static org.junit.Assert.assertEquals;

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
            org.andstatus.todoagenda.test.R.raw.tomorrows_tasks_today);
        provider.addResults(inputs);

        playResults(method);
        assertEquals("Number of entries, " + getFactory().getWidgetEntries(), 1, getFactory().getWidgetEntries().size());
        assertEquals("No events", WidgetEntryPosition.LIST_FOOTER, getFactory().getWidgetEntries().get(0).entryPosition);
    }
}
