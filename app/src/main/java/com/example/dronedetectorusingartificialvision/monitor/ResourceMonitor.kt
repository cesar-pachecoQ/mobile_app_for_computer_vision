package com.example.dronedetectorusingartificialvision.monitor

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.dronedetectorusingartificialvision.model.ResourceSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Monitorea recursos del dispositivo usando APIs nativas de Android.
 *
 * CPU: usa /proc/self/stat (proceso propio) vs /proc/stat (total sistema)
 *      para obtener el % real que consume la app.
 * RAM: ActivityManager.MemoryInfo
 * Batería: ACTION_BATTERY_CHANGED broadcast
 */
class ResourceMonitor(
    private val context: Context,
    private val pollIntervalMs: Long = 500L
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val pid = android.os.Process.myPid()

    // Delta de CPU (entre lecturas)
    private var prevProcessCpu  = 0L   // utime + stime del proceso
    private var prevSystemTotal = 0L   // suma de todos los ticks del sistema

    // Stats de inferencia: actualizadas externamente
    @Volatile private var currentFps   = 0f
    @Volatile private var currentLatMs = 0L

    /** Actualiza FPS y latencia desde el motor de inferencia. */
    fun updateInferenceStats(fps: Float, inferenceMs: Long) {
        currentFps   = fps
        currentLatMs = inferenceMs
    }

    /**
     * Flow que emite snapshots de recursos cada [pollIntervalMs] ms.
     * Colectar en Dispatchers.IO.
     */
    val resourceFlow: Flow<ResourceSnapshot> = flow {
        // Primera lectura para inicializar deltas (descartamos el valor)
        readCpuPercent()
        delay(pollIntervalMs)

        while (true) {
            emit(
                ResourceSnapshot(
                    cpuPercent     = readCpuPercent(),
                    ramUsedMb      = readRamUsedMb(),
                    ramTotalMb     = readRamTotalMb(),
                    batteryPercent = readBatteryPercent(),
                    fps            = currentFps,
                    inferenceMs    = currentLatMs
                )
            )
            delay(pollIntervalMs)
        }
    }

    // ─── CPU (/proc/self/stat y /proc/stat) ──────────────────────────────────
    //
    // Fórmula:
    //   cpuPercent = (Δprocess_cpu / Δsystem_total) × 100
    //
    // /proc/self/stat campos 14 (utime) y 15 (stime) son ticks de CPU del proceso
    // /proc/stat primera línea: cpu user nice system idle iowait irq softirq ...

    private fun readCpuPercent(): Float {
        return try {
            // Ticks del proceso actual
            val selfStat = File("/proc/$pid/stat").readText().split(" ")
            val utime = selfStat[13].toLong()
            val stime = selfStat[14].toLong()
            val processCpu = utime + stime

            // Total de ticks del sistema
            val systemLine = File("/proc/stat").readLines()[0]
                .trim().split("\\s+".toRegex())
            val systemTotal = systemLine.drop(1).sumOf { it.toLongOrNull() ?: 0L }

            val deltaProcess = processCpu  - prevProcessCpu
            val deltaSystem  = systemTotal - prevSystemTotal

            prevProcessCpu  = processCpu
            prevSystemTotal = systemTotal

            if (deltaSystem <= 0L) 0f
            else (deltaProcess.toFloat() * 100f / deltaSystem.toFloat()).coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        }
    }

    // ─── RAM ─────────────────────────────────────────────────────────────────

    private fun readRamUsedMb(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.totalMem - memInfo.availMem) / (1024L * 1024L)
    }

    private fun readRamTotalMb(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024L * 1024L)
    }

    // ─── Batería ─────────────────────────────────────────────────────────────

    private fun readBatteryPercent(): Int {
        return try {
            val intent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        } catch (e: Exception) {
            0
        }
    }
}
