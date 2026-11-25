package studio.cardia

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import studio.cardia.core.*

class CardiaState(app: Application) : AndroidViewModel(app) {
    private val prefs=app.getSharedPreferences("cardia",0)
    var welcomed by mutableStateOf(prefs.getBoolean("welcomed",false)); private set
    // 0 = system, 1 = light, 2 = dark
    var theme by mutableIntStateOf(prefs.getInt("theme",2)); private set
    var reducedMotion by mutableStateOf(prefs.getBoolean("reducedMotion",false)); private set
    var organ by mutableStateOf(Atlas.organ(prefs.getString("organ","heart") ?: "heart")); private set
    var progress by mutableStateOf(Progress.restore(prefs.getStringSet("completed",emptySet())!!.toSet(),prefs.getStringSet("bestScores",emptySet())!!.toSet(),prefs.getInt("best",0))); private set
    // Bookmarks are "organ:structure"; legacy heart-only ids are migrated.
    var bookmarks by mutableStateOf(prefs.getStringSet("bookmarks",emptySet())!!.map { if(':' in it) it else "heart:$it" }.toSet()); private set
    var notes by mutableStateOf(prefs.getString("notes","") ?: ""); private set
    fun welcome() { welcomed=true; prefs.edit().putBoolean("welcomed",true).apply() }
    fun setAppearance(value: Int) { theme=value; prefs.edit().putInt("theme",value).apply() }
    fun motion(value: Boolean) { reducedMotion=value;prefs.edit().putBoolean("reducedMotion",value).apply() }
    fun choose(value: Organ) { organ=value;prefs.edit().putString("organ",value.id).apply() }
    fun complete(organ: Organ,lesson: String) { progress=progress.complete(organ.id,lesson); prefs.edit().putStringSet("completed",progress.completed).apply() }
    fun score(organ: Organ,value: Int) { progress=progress.record(organ,value);prefs.edit().putStringSet("bestScores",progress.bestEntries()).remove("best").apply() }
    fun bookmark(organ: Organ,id: String) { val key="${organ.id}:$id";bookmarks=if(key in bookmarks) bookmarks-key else bookmarks+key;prefs.edit().putStringSet("bookmarks",bookmarks).apply() }
    fun note(value: String) { notes=value;prefs.edit().putString("notes",value).apply() }
    fun resetStudy() { progress=Progress();bookmarks=emptySet();notes="";prefs.edit().remove("completed").remove("best").remove("bestScores").remove("bookmarks").remove("notes").apply() }
}
