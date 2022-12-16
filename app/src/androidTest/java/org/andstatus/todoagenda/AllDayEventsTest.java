package org.andstatus.todoagenda;

import org.andstatus.todoagenda.prefs.AllDayEventsPlacement;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.WidgetEntryPosition;
import org.junit.Test;

/**
 * @author yvolk@yurivolkov.com
 */
public class AllDayEventsTest extends BaseWidgetTest {

    @Test
    public void testAllDayEventsPlacement() {
        final String method = "testAllDayEventsPlacement";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.test.R.raw.all_day_tasks);
        provider.addResults(inputs);
        playResults(method);

        assertPosition(8, WidgetEntryPosition.END_OF_DAY);
        assertPosition(9, WidgetEntryPosition.END_OF_DAY);
        assertPosition(10, WidgetEntryPosition.DAY_HEADER);
        assertPosition(11, WidgetEntryPosition.END_OF_DAY);

        getSettings().setAllDayEventsPlacement(AllDayEventsPlacement.TOP_DAY);
        playResults(method);

        assertPosition(1, WidgetEntryPosition.START_OF_DAY);
        assertPosition(2, WidgetEntryPosition.START_OF_DAY);
        assertPosition(10, WidgetEntryPosition.DAY_HEADER);
        assertPosition(11, WidgetEntryPosition.START_OF_DAY);
    }
}
