package studio.cardia

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import studio.cardia.core.AnatomyModel
import studio.cardia.core.Glb
import studio.cardia.core.Organ

/** Parses bundled GLB assets off the main thread and keeps the two most recent organs in memory. */
object ModelRepository {
    private val lock = Mutex()
    private val cache = object : LinkedHashMap<String, AnatomyModel>(4, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AnatomyModel>?) = size > 2
    }
    fun cached(organ: Organ): AnatomyModel? = synchronized(cache) { cache[organ.id] }
    suspend fun load(context: Context, organ: Organ): AnatomyModel = lock.withLock {
        cached(organ) ?: withContext(Dispatchers.Default) {
            AnatomyModel.build(organ, organ.assets.map { name -> Glb.parse(context.assets.open("models/$name").use { it.readBytes() }) })
        }.also { synchronized(cache) { cache[organ.id] = it } }
    }
}
