package org.andstatus.todoagenda;

import android.util.Log;

import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.util.LazyVal;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryPosition;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class BaseWidgetTest {
    final static String TAG = BaseWidgetTest.class.getSimpleName();
    private static final int MAX_MILLIS_TO_WAIT_FOR_LAUNCHER = 2000;
    private static final int MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION = 40000;

    protected FakeCalendarContentProvider provider = null;
    protected LazyVal<RemoteViewsFactory> factory = LazyVal.of(
            () -> new RemoteViewsFactory(provider.getContext(), provider.getWidgetId(), false));

    @Before
    public void setUp() throws Exception {
        provider = FakeCalendarContentProvider.getContentProvider();
    }

    @After
    public void tearDown() throws Exception {
        FakeCalendarContentProvider.tearDown();
        factory.reset();
    }

    DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth) {
        return dateTime(year, monthOfYear, dayOfMonth, 0, 0);
    }

    DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth,
            int hourOfDay,
            int minuteOfHour) {
        return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0,
                provider.getSettings().clock().getZone());
    }

    protected void playResults(String tag) {
        Log.d(tag, provider.getWidgetId() + " playResults started");
        provider.updateAppSettings(tag);

        if (provider.usesActualWidget) {
            InstanceState.clear(provider.getWidgetId());
            EnvironmentChangedReceiver.updateWidget(provider.getContext(), provider.getWidgetId());
            if (!RemoteViewsFactory.factories.containsKey(provider.getWidgetId())) {
                waitForRemoteViewsFactoryCreation();
            }
            waitTillWidgetIsUpdated(tag);
            waitTillWidgetIsReloaded(tag);
            waitTillWidgetIsRedrawn(tag);
            EnvironmentChangedReceiver.sleep(1000);
            if (InstanceState.get(provider.getWidgetId()).listReloaded == 0) {
                Log.d(tag, provider.getWidgetId() + " was not reloaded by a Launcher");
                getFactory().onDataSetChanged();
            }
        } else {
            getFactory().onDataSetChanged();
        }
        getFactory().logWidgetEntries(tag);
        Log.d(tag, provider.getWidgetId() + " playResults ended");
    }

    private void waitForRemoteViewsFactoryCreation() {
        long start = System.currentTimeMillis();
        while (RemoteViewsFactory.factories.get(getSettings().getWidgetId()) == null &&
                Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION){
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    private void waitTillWidgetIsUpdated(String tag) {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState.get(provider.getWidgetId()).updated > 0) {
                Log.d(tag, provider.getWidgetId() + " updated");
                break;
            }
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    private void waitTillWidgetIsReloaded(String tag) {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER){
            if (InstanceState.get(provider.getWidgetId()).listReloaded > 0) {
                Log.d(tag, provider.getWidgetId() + " reloaded");
                break;
            }
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    private void waitTillWidgetIsRedrawn(String tag) {
        long start = System.currentTimeMillis();
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER){
            if (InstanceState.get(provider.getWidgetId()).listRedrawn > 0) {
                Log.d(tag, provider.getWidgetId() + " redrawn");
                break;
            }
            EnvironmentChangedReceiver.sleep(20);
        }
    }

    protected InstanceSettings getSettings() {
        return provider.getSettings();
    }

    public RemoteViewsFactory getFactory() {
        RemoteViewsFactory existingFactory = RemoteViewsFactory.factories.get(provider.getWidgetId());
        return existingFactory == null ? factory.get() : existingFactory;
    }

    protected void ensureNonEmptyResults() {
        QueryResultsStorage inputs = provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.birthday);
        InstanceSettings settings = getSettings();
        settings.setFilterMode(FilterMode.NO_FILTERING);
        provider.addResults(inputs);
    }

    protected void assertPosition(int ind, WidgetEntryPosition position) {
        List<? extends WidgetEntry> widgetEntries = getFactory().getWidgetEntries();
        assertEquals(widgetEntries.get(ind).toString(), position, widgetEntries.get(ind).entryPosition);
    }
}
