package org.andstatus.todoagenda

import java.util.concurrent.ConcurrentHashMap

/**
 * @author yvolk@yurivolkov.com
 */
class InstanceState private constructor(val updated: Long, val listReloaded: Long, val listRedrawn: Long) {
    companion object {
        private val instances = ConcurrentHashMap<Int, InstanceState>()
        val EMPTY = InstanceState(0, 0, 0)
        fun clearAll() {
            instances.clear()
        }

        fun clear(widgetId: Int) {
            instances.remove(widgetId)
        }

        fun updated(widgetId: Int) {
            instances.compute(
                widgetId
            ) { id: Int?, state: InstanceState? ->
                InstanceState(
                    if (state == null) 1 else state.updated + 1,
                    state?.listReloaded ?: 0,
                    state?.listRedrawn ?: 0
                )
            }
        }

        fun listReloaded(widgetId: Int) {
            instances.compute(
                widgetId
            ) { id: Int?, state: InstanceState? ->
                InstanceState(
                    state?.updated ?: 0,
                    if (state == null) 1 else state.listReloaded + 1,
                    state?.listRedrawn ?: 0
                )
            }
        }

        fun listRedrawn(widgetId: Int) {
            instances.compute(
                widgetId
            ) { id: Int?, state: InstanceState? ->
                InstanceState(
                    state?.updated ?: 0,
                    state?.listReloaded ?: 0,
                    if (state == null) 1 else state.listRedrawn + 1
                )
            }
        }

        operator fun get(widgetId: Int): InstanceState {
            return instances.getOrDefault(widgetId, EMPTY)
        }
    }
}
