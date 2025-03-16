package org.andstatus.todoagenda

import org.andstatus.todoagenda.layout.EventEntryLayout
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.TaskScheduling
import org.andstatus.todoagenda.prefs.TasksWithoutDates
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringStartsWith
import org.junit.Assert
import org.junit.Test
import java.util.function.UnaryOperator

/**
 * See https://github.com/andstatus/todoagenda/issues/4
 * @author yvolk@yurivolkov.com
 */
class TasksFilteringAndOrderingTest : BaseWidgetTest() {
    @Test
    fun dateDueNoFilters() {
        val method = "dateDueNoFilters"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ",
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "a Today's event at midnight",
                "Today's event at 4AM",
                "Today's event at 8:05",
                "task3 ",
                "Today's event later at 9:05PM",
                "task1 ",
                "task10 ",
                "task9 ",
                "",
                "task6 ",
                "task7 ",
                "task12 ",
                "",
                "Test event that",
                "",
                "task17 ",
                "",
                "task16 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ",
                "task8 ",
                "task4 ",
                "task2 ",
                "task15 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskScheduling = TaskScheduling.DATE_DUE,
                    taskWithoutDates = TasksWithoutDates.END_OF_TODAY,
                    filterModeIn = FilterMode.NO_FILTERING,
                )
            }
        oneCase(method, setter, names)
    }

    /** T1 at https://github.com/andstatus/todoagenda/issues/4#issue-551945909  */
    @Test
    fun dateDueEndOfList() {
        val method = "dateDueNoFilters"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ",
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "a Today's event at midnight",
                "Today's event at 4AM",
                "Today's event at 8:05",
                "task3 ",
                "Today's event later at 9:05PM",
                "task1 ",
                "",
                "task6 ",
                "task7 ",
                "task12 ",
                "",
                "Test event that",
                "",
                "task17 ",
                "",
                "task16 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ",
                "task8 ",
                "task4 ",
                "task2 ",
                "task15 ",
                "task10 ",
                "task9 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskWithoutDates = TasksWithoutDates.END_OF_LIST,
                    filterModeIn = FilterMode.NO_FILTERING,
                )
            }
        oneCase(method, setter, names)
    }

    @Test
    fun dateDueFiltered() {
        val method = "testDateDueFiltered"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "a Today's event at midnight",
                "Today's event at 4AM",
                "Today's event at 8:05",
                "task3 ",
                "Today's event later at 9:05PM",
                "task1 ",
                "task10 ",
                "",
                "task12 ",
                "",
                "Test event that",
                "",
                "task17 ",
                "",
                "task16 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task8 ",
                "task2 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskWithoutDates = TasksWithoutDates.END_OF_TODAY,
                    filterModeIn = FilterMode.DEBUG_FILTER,
                )
            }
        oneCase(method, setter, names)
    }

    @Test
    fun dateDueFilteredHideNoDates() {
        val method = "dateDueFilteredHideNoDates"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "a Today's event at midnight",
                "Today's event at 4AM",
                "Today's event at 8:05",
                "task3 ",
                "Today's event later at 9:05PM",
                "task1 ",
                "",
                "task12 ",
                "",
                "Test event that",
                "",
                "task17 ",
                "",
                "task16 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task8 ",
                "task2 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskScheduling = TaskScheduling.DATE_DUE,
                    taskWithoutDates = TasksWithoutDates.HIDE,
                    filterModeIn = FilterMode.DEBUG_FILTER,
                )
            }
        oneCase(method, setter, names)
    }

    @Test
    fun dateStartedNoFilters() {
        val method = "dateStartedNoFilters"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ",
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "task3 ",
                "task1 ",
                "task6 ",
                "task16 ",
                "task4 ",
                "task2 ",
                "a Today's event at midnight",
                "Today's event at 4AM",
                "task12 ",
                "task7 ",
                "Today's event at 8:05",
                "Today's event later at 9:05PM",
                "task10 ",
                "task9 ",
                "",
                "Test event that",
                "",
                "task8 ",
                "",
                "task17 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ",
                "task15 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskScheduling = TaskScheduling.DATE_STARTED,
                    taskWithoutDates = TasksWithoutDates.END_OF_TODAY,
                    filterModeIn = FilterMode.NO_FILTERING,
                )
            }
        oneCase(method, setter, names)
    }

    /** T2 at https://github.com/andstatus/todoagenda/issues/4#issue-551945909  */
    @Test
    fun dateStartedEndOfList() {
        val method = "dateStartedEndOfList"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ",
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "task3 ",
                "task1 ",
                "task6 ",
                "task16 ",
                "task4 ",
                "task2 ",
                "a Today's event at midnight",
                "Today's event at 4AM",
                "task12 ",
                "task7 ",
                "Today's event at 8:05",
                "Today's event later at 9:05PM",
                "",
                "Test event that",
                "",
                "task8 ",
                "",
                "task17 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ",
                "task15 ",
                "task10 ",
                "task9 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskScheduling = TaskScheduling.DATE_STARTED,
                    taskWithoutDates = TasksWithoutDates.END_OF_LIST,
                    filterModeIn = FilterMode.NO_FILTERING,
                )
            }
        oneCase(method, setter, names)
    }

    @Test
    fun dateStartedHideNoDates() {
        val method = "dateStartedHideNoDates"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ",
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "task3 ",
                "task1 ",
                "task6 ",
                "task16 ",
                "task4 ",
                "task2 ",
                "a Today's event at midnight",
                "Today's event at 4AM",
                "task12 ",
                "task7 ",
                "Today's event at 8:05",
                "Today's event later at 9:05PM",
                "",
                "Test event that",
                "",
                "task8 ",
                "",
                "task17 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ",
                "task15 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskScheduling = TaskScheduling.DATE_STARTED,
                    taskWithoutDates = TasksWithoutDates.HIDE,
                    filterModeIn = FilterMode.NO_FILTERING,
                )
            }
        oneCase(method, setter, names)
    }

    @Test
    fun dateStartedFiltered() {
        val method = "dateStartedFiltered"
        val names =
            listOf(
                WidgetEntryPosition.DAY_HEADER.value,
                "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "task3 ",
                "task1 ",
                "task16 ",
                "task4 ",
                "task2 ",
                "a Today's event at midnight",
                "Today's event at 4AM",
                "task12 ",
                "Today's event at 8:05",
                "Today's event later at 9:05PM",
                "task10 ",
                "",
                "Test event that",
                "",
                "task8 ",
                "",
                "task17 ",
            )
        val setter =
            UnaryOperator { settings: InstanceSettings ->
                settings.copy(
                    taskScheduling = TaskScheduling.DATE_STARTED,
                    taskWithoutDates = TasksWithoutDates.END_OF_TODAY,
                    filterModeIn = FilterMode.DEBUG_FILTER,
                )
            }
        oneCase(method, setter, names)
    }

    private fun oneCase(
        method: String,
        setter: UnaryOperator<InstanceSettings>,
        names: List<String>,
    ) {
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.filter_tasks_308_no_filters)
        oneCaseSettings(method, setter, names)
        oneCaseSettings(
            method,
            { settings: InstanceSettings ->
                setter.apply(settings).copy(
                    eventEntryLayout = (EventEntryLayout.ONE_LINE),
                )
            },
            names,
        )
    }

    private fun oneCaseSettings(
        method: String,
        setter: UnaryOperator<InstanceSettings>,
        names: List<String>,
    ) {
        provider.settings =
            setter
                .apply(settings)
                .copy(showCurrentTimeLine = false)
        playResults(method)
        val widgetEntries = factory.widgetEntries
        for (ind in names.indices) {
            when (val entryPosition = WidgetEntryPosition.fromValue(names[ind])) {
                WidgetEntryPosition.UNKNOWN ->
                    MatcherAssert.assertThat(
                        "ind=$ind",
                        widgetEntries[ind].title,
                        StringStartsWith.startsWith(names[ind]),
                    )

                else -> Assert.assertEquals("ind=$ind", entryPosition, widgetEntries[ind].entryPosition)
            }
        }
    }
}
