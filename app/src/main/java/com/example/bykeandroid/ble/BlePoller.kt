package com.example.bykeandroid.ble

import kotlinx.coroutines.*

class BlePoller(
    private val bleService: BleService,
) {
    fun startRepeatingJob(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                bleService.read()
                //repeate Task Here
                delay(timeInterval)
            }
        }
    }
}