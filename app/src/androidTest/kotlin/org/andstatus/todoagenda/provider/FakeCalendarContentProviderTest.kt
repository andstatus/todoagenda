package org.andstatus.todoagenda.provider

import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.andstatus.todoagenda.BaseWidgetTest
import org.andstatus.todoagenda.calendar.CalendarEventProvider
import org.andstatus.todoagenda.util.PermissionsUtil
import org.json.JSONException
import java.util.function.Function

/**
 * Tests of the Testing framework itself
 *
 * @author yvolk@yurivolkov.com
 */
class FakeCalendarContentProviderTest : BaseWidgetTest() {
    private val projection = CalendarEventProvider.projection
    private val sortOrder = CalendarEventProvider.EVENT_SORT_ORDER
    private var eventId: Long = 0

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @After
    @Throws(Exception::class)
    override fun tearDown() {
        QueryResultsStorage.setNeedToStoreResults(false, 0)
        super.tearDown()
    }

    @Test
    fun testTestMode() {
        withClue("isTestMode should be true") {
            PermissionsUtil.isTestMode shouldBe true
        }
    }

    @Test
    fun testTwoEventsToday() {
        val results = QueryResultsStorage()
        val input1 = newResult("")
        results.addResult(input1)
        val input2 = newResult("SOMETHING=1")
        results.addResult(input2)
        provider.addResults(results)
        provider.updateAppSettings(TAG)
        QueryResultsStorage.setNeedToStoreResults(true, provider.widgetId)
        val resolver = MyContentResolver(EventProviderType.CALENDAR, provider.context, provider.widgetId)
        val result1 = queryList(resolver, input1.uri, input1.selection)
        val stored1 = QueryResultsStorage.storage!!.getResults(EventProviderType.CALENDAR, provider.widgetId)
        result1 shouldBe input1
        input1 shouldBe result1
        withClue("Results 1 size\n$stored1") {
            stored1.size shouldBe 1
        }
        stored1[0] shouldBe input1
        val result2 = queryList(resolver, input2.uri, input2.selection)
        val stored2 = QueryResultsStorage.storage!!.getResults(EventProviderType.CALENDAR, provider.widgetId)
        result2 shouldBe input2
        input2 shouldBe result2
        withClue("Results 2 size\n$stored2") {
            stored2.size shouldBe 2
        }
        stored2[1] shouldBe input2
        result2 shouldNotBeSameInstanceAs result1
        result1.rows[1].setTitle("Changed title")
        result1 shouldNotBeSameInstanceAs input1
        input1 shouldNotBeSameInstanceAs result1
    }

    private fun newResult(selection: String): QueryResult {
        val input = QueryResult(
            EventProviderType.CALENDAR, settings,
            CalendarContract.Instances.CONTENT_URI, projection, selection, null, sortOrder
        )
        val today = settings.clock.now().withTimeAtStartOfDay()
        input.addRow(
            QueryRow().setEventId(++eventId)
                .setTitle("First Event today").setBegin(today.plusHours(8).millis)
        )
        input.addRow(
            QueryRow()
                .setEventId(++eventId)
                .setTitle("Event with all known attributes")
                .setBegin(today.plusHours(12).millis)
                .setEnd(today.plusHours(13).millis)
                .setDisplayColor(0xFF00FF)
                .setAllDay(false)
                .setEventLocation("somewhere")
                .setHasAlarm(true)
                .setRRule("what's this?")
        )
        input.uri shouldBe CalendarContract.Instances.CONTENT_URI
        input.selection shouldBe selection
        return input
    }

    private fun queryList(resolver: MyContentResolver, uri: Uri, selection: String): QueryResult {
        var result = QueryResult(
            EventProviderType.CALENDAR, settings,
            uri, projection, selection, null, sortOrder
        )
        result = resolver.foldEvents(uri, projection, selection, null, sortOrder, result) { r: QueryResult ->
            Function { cursor: Cursor? ->
                r.addRow(cursor)
                r
            }
        }
        result.dropNullColumns()
        return result
    }

    @Test
    @Throws(JSONException::class)
    fun testJsonToAndFrom() {
        val inputs1 = provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.birthday)
        val jsonOutput = inputs1.toJson(provider.context, provider.widgetId, true)
        val inputs2 = QueryResultsStorage.fromJson(provider.widgetId, jsonOutput)
        inputs2 shouldBe inputs1
    }

    companion object {
        private val TAG = FakeCalendarContentProviderTest::class.java.simpleName
    }
}
