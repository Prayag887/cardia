@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package studio.cardia

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import studio.cardia.core.*
import kotlin.math.*

private val Rose=Color(0xFFB84A63)
private val Amber=Color(0xFFC97A1F)
private val DarkColors=darkColorScheme(
    primary=Color(0xFFE2A6B4),onPrimary=Color(0xFF40101F),primaryContainer=Color(0xFF3A222A),onPrimaryContainer=Color(0xFFEFCDD5),
    secondary=Color(0xFFADC6EA),background=Color(0xFF15130F),surface=Color(0xFF15130F),
    surfaceVariant=Color(0xFF1E1C17),onSurface=Color(0xFFEDE8E0),onSurfaceVariant=Color(0xFFA39B8F),outlineVariant=Color(0xFF302C24)
)
private val LightColors=lightColorScheme(
    primary=Rose,onPrimary=Color.White,primaryContainer=Color(0xFFF1E1E3),onPrimaryContainer=Color(0xFF5C1D31),
    secondary=Color(0xFF42688A),background=Color(0xFFFAF7F1),surface=Color(0xFFFAF7F1),
    surfaceVariant=Color(0xFFF0EBE1),onSurface=Color(0xFF241F18),onSurfaceVariant=Color(0xFF6E6759),outlineVariant=Color(0xFFE1DACB)
)
private val Shapes=Shapes(small=RoundedCornerShape(6.dp),medium=RoundedCornerShape(10.dp),large=RoundedCornerShape(14.dp))
private val Serif=FontFamily.Serif
private val Type=Typography(
    displayLarge=androidx.compose.ui.text.TextStyle(fontFamily=Serif,fontSize=44.sp,lineHeight=48.sp,fontWeight=FontWeight.Normal,letterSpacing=0.sp),
    headlineLarge=androidx.compose.ui.text.TextStyle(fontFamily=Serif,fontSize=30.sp,lineHeight=36.sp,fontWeight=FontWeight.Normal,letterSpacing=0.sp),
    headlineMedium=androidx.compose.ui.text.TextStyle(fontFamily=Serif,fontSize=25.sp,lineHeight=31.sp,fontWeight=FontWeight.Normal,letterSpacing=0.sp),
    titleLarge=androidx.compose.ui.text.TextStyle(fontFamily=Serif,fontSize=20.sp,lineHeight=27.sp,fontWeight=FontWeight.Medium),
    bodyLarge=androidx.compose.ui.text.TextStyle(fontSize=16.sp,lineHeight=26.sp),
    bodyMedium=androidx.compose.ui.text.TextStyle(fontSize=14.sp,lineHeight=22.sp),
    labelSmall=androidx.compose.ui.text.TextStyle(fontSize=11.5.sp,lineHeight=15.sp,letterSpacing=.8.sp,fontWeight=FontWeight.Medium)
)

private fun organIcon(id: String): ImageVector = when(id) {
    "heart"->Icons.Rounded.Favorite;"kidney"->Icons.Rounded.WaterDrop;"liver"->Icons.Rounded.Spa
    "lungs"->Icons.Rounded.Air;"pancreas"->Icons.Rounded.Grain;else->Icons.Rounded.Shield
}

@Composable fun CardiaApp(vm: CardiaState = viewModel()) {
    val dark=when(vm.theme) { 1->false;2->true;else->isSystemInDarkTheme() }
    var page by rememberSaveable { mutableStateOf("study") }
    var lessonId by rememberSaveable { mutableStateOf("") }
    var settings by rememberSaveable { mutableStateOf(false) }
    val full=page in listOf("explore","lesson","quiz")
    MaterialTheme(colorScheme=if(dark) DarkColors else LightColors,typography=Type,shapes=Shapes) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            if(!vm.welcomed) Welcome { vm.welcome() }
            else {
                BackHandler(page!="study") { page="study" }
                Scaffold(
                    containerColor=MaterialTheme.colorScheme.background,
                    bottomBar={ if(!full) NavigationBar(containerColor=MaterialTheme.colorScheme.background,tonalElevation=0.dp) {
                        listOf(Triple("study","Study",Icons.AutoMirrored.Rounded.MenuBook),Triple("explore","Explore",Icons.Rounded.ViewInAr),Triple("progress","Your space",Icons.Rounded.Insights)).forEach { (route,label,icon) ->
                            NavigationBarItem(selected=page==route,onClick={page=route},icon={Icon(icon,null)},label={Text(label)})
                        }
                    } }
                ) { padding ->
                    AnimatedContent(targetState=page,modifier=Modifier.padding(padding),transitionSpec={fadeIn(tween(if(vm.reducedMotion) 0 else 200)) togetherWith fadeOut(tween(if(vm.reducedMotion) 0 else 120))},label="Screen") { route ->
                        when(route) {
                            "study" -> Study(vm,{page="explore"},{lessonId=it;page="lesson"},{page="quiz"},{settings=true})
                            "explore" -> Explorer(vm,dark,{page="study"})
                            "lesson" -> LessonScreen(vm,dark,lessonId,{page="study"},{page="quiz"})
                            "quiz" -> QuizScreen(vm) {page="study"}
                            else -> ProgressScreen(vm,{settings=true},{page="explore"})
                        }
                    }
                }
                if(settings) Settings(vm) {settings=false}
            }
        }
    }
}

@Composable private fun Eyebrow(text: String) { Text(text.uppercase(),style=MaterialTheme.typography.labelSmall,letterSpacing=1.1.sp,color=MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun Muted(text: String,modifier: Modifier=Modifier) { Text(text,modifier,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun ActionIcon(icon: ImageVector,label: String,onClick: ()->Unit) { IconButton(onClick=onClick,modifier=Modifier.size(48.dp)) {Icon(icon,label)} }
@Composable private fun Pill(text: String,dot: Color?=null) { Surface(shape=CircleShape,color=MaterialTheme.colorScheme.surfaceVariant) {Row(Modifier.padding(horizontal=12.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically) {if(dot!=null) {Box(Modifier.size(8.dp).clip(CircleShape).background(dot));Spacer(Modifier.width(6.dp))};Text(text,style=MaterialTheme.typography.labelMedium)}} }
@Composable private fun SectionTitle(title: String,sub: String?=null) { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) { Text(title,style=MaterialTheme.typography.titleLarge);if(sub!=null) Muted(sub) } }
@Composable private fun Panel(modifier: Modifier=Modifier,content: @Composable ColumnScope.()->Unit) {
    Surface(modifier,shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.55f)) {
        Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)
    }
}
@Composable private fun Rule() { HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.7f)) }

@Composable private fun HeartMark(modifier: Modifier=Modifier) {
    val color=MaterialTheme.colorScheme.primary
    Canvas(modifier.semantics { contentDescription="Cardia heart emblem" }) {
        val w=size.width; val h=size.height
        val p=Path().apply {
            moveTo(w*.52f,h*.90f);cubicTo(w*.40f,h*.78f,w*.04f,h*.53f,w*.09f,h*.29f)
            cubicTo(w*.13f,h*.04f,w*.40f,h*.02f,w*.51f,h*.27f)
            cubicTo(w*.69f,h*.03f,w*.94f,h*.14f,w*.93f,h*.36f)
            cubicTo(w*.92f,h*.61f,w*.68f,h*.80f,w*.52f,h*.90f);close()
        }
        drawPath(p,color)
        val line=Path().apply {moveTo(w*.12f,h*.49f);lineTo(w*.35f,h*.49f);lineTo(w*.43f,h*.35f);lineTo(w*.54f,h*.67f);lineTo(w*.64f,h*.43f);lineTo(w*.72f,h*.49f);lineTo(w*.88f,h*.49f)}
        drawPath(line,Color.White.copy(alpha=.85f),style=Stroke(w*.017f,cap=StrokeCap.Round,join=StrokeJoin.Round))
    }
}

@Composable private fun Welcome(start: ()->Unit) {
    BoxWithConstraints {
        val compact=maxWidth<400.dp
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal=if(compact) 22.dp else 28.dp,vertical=28.dp),verticalArrangement=Arrangement.spacedBy(30.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {HeartMark(Modifier.size(28.dp));Spacer(Modifier.width(10.dp));Text("cardia",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.weight(1f));Pill("Offline first")}
            Box(Modifier.fillMaxWidth().height(if(compact) 180.dp else 220.dp),contentAlignment=Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) { repeat(3) { drawCircle(Color(0xFF8990A6).copy(alpha=.12f),radius=58.dp.toPx()+it*26.dp.toPx(),style=Stroke(1.dp.toPx())) } }
                HeartMark(Modifier.size(if(compact) 138.dp else 160.dp))
            }
            Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Eyebrow("A little curiosity. A deeper understanding.")
                Text("Know the body.\nBy heart.",style=if(compact) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayLarge)
            }
            Muted("An interactive anatomy studio built on real reference anatomy. Section the kidney, peel the liver into its segments, trace the airways — then turn what you see into what you know.")
            Muted("Six organ systems · dissection tools · guided lessons · recall practice")
            Button(onClick=start,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(12.dp)) {Text("Begin exploring");Spacer(Modifier.width(12.dp));Icon(Icons.AutoMirrored.Rounded.ArrowForward,null)}
            Muted(Atlas.disclaimer)
        }
    }
}

@Composable private fun OrganPicker(vm: CardiaState) {
    LazyRow(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        items(Atlas.organs) { o ->
            val chosen=o.id==vm.organ.id
            Surface(onClick={vm.choose(o)},shape=RoundedCornerShape(12.dp),color=if(chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.5f),border=if(chosen) BorderStroke(1.5.dp,MaterialTheme.colorScheme.primary) else null,modifier=Modifier.semantics {selected=chosen}) {
                Column(Modifier.width(134.dp).height(132.dp).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Icon(organIcon(o.id),null,tint=MaterialTheme.colorScheme.primary)
                    Text(o.name,fontWeight=FontWeight.Medium,maxLines=2)
                    Text(o.system,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable private fun Study(vm: CardiaState,explore: ()->Unit,lesson: (String)->Unit,quiz: ()->Unit,settings: ()->Unit) {
    val organ=vm.organ
    BoxWithConstraints {
        val compact=maxWidth<400.dp
        val edge=if(compact) 20.dp else 24.dp
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=edge,vertical=26.dp),verticalArrangement=Arrangement.spacedBy(30.dp)) {
        item {Row(verticalAlignment=Alignment.CenterVertically) {Column(Modifier.weight(1f)) {Eyebrow("Your anatomy studio");Text("cardia.",style=MaterialTheme.typography.headlineLarge)};ActionIcon(Icons.Rounded.Tune,"Settings",settings)}}
        item {Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {SectionTitle("Choose a system","Real reference anatomy, one organ at a time.");OrganPicker(vm)}}
        item {
            Surface(onClick=explore,shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.primaryContainer,modifier=Modifier.fillMaxWidth()) {
                Box(Modifier.padding(24.dp)) {
                    Icon(organIcon(organ.id),null,Modifier.align(Alignment.CenterEnd).size(96.dp).graphicsLayer {alpha=.25f},tint=MaterialTheme.colorScheme.primary)
                    Column(Modifier.fillMaxWidth(.72f),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        Eyebrow("The interactive atlas")
                        Text(organ.name,style=MaterialTheme.typography.headlineLarge)
                        Text("${organ.structures.size} structures · ${organ.tagline}",style=MaterialTheme.typography.bodySmall)
                        FilledTonalButton(onClick=explore) {Text("Open 3D studio");Spacer(Modifier.width(8.dp));Icon(Icons.AutoMirrored.Rounded.ArrowForward,null,Modifier.size(18.dp))}
                    }
                }
            }
        }
        item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            Panel(Modifier.weight(1f)) {Eyebrow("Your journey");Text("${vm.progress.completedIn(organ)} / ${organ.lessons.size}",style=MaterialTheme.typography.headlineMedium);Muted("lessons complete")}
            Panel(Modifier.weight(1f)) {Eyebrow("Personal best");Text("${vm.progress.bestFor(organ.id)} / ${organ.questions.size}",style=MaterialTheme.typography.headlineMedium);Muted("recall questions")}
        }}
        item {SectionTitle("Learn with a little guidance","${organ.lessons.size} ${if(organ.lessons.size==1) "lesson" else "lessons"} · ${organ.lessons.sumOf {it.steps.size}} chapters")}
        items(organ.lessons,key={"${organ.id}/${it.id}"}) { l ->
            Surface(onClick={lesson(l.id)},shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.5f)) {
                Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    Surface(shape=RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.primaryContainer) {Box(Modifier.size(48.dp),contentAlignment=Alignment.Center) {if(vm.progress.isComplete(organ.id,l.id)) Icon(Icons.Rounded.Check,null,tint=MaterialTheme.colorScheme.primary) else Text("0${organ.lessons.indexOf(l)+1}",fontFamily=FontFamily.Monospace)}}
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {Text(l.title,fontWeight=FontWeight.Medium);Muted("${l.minutes} min · ${l.steps.size} chapters · ${l.subtitle}")}
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward,null,Modifier.size(19.dp))
                }
            }
        }
        item {Panel {Row(verticalAlignment=Alignment.CenterVertically) {Icon(Icons.Rounded.Psychology,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(12.dp));Text("Make it stick",style=MaterialTheme.typography.titleLarge)};Muted("${organ.questions.size} questions with explanations. Learn from every answer.");OutlinedButton(onClick=quiz,modifier=Modifier.fillMaxWidth()) {Text("Start active recall")}}}
        item {Muted(organ.note)}
        item {Muted(Atlas.disclaimer)}
        }
    }
}

private class ModelLoad(val model: AnatomyModel?,val error: String?)

@Composable private fun rememberModel(organ: Organ): ModelLoad {
    val context=LocalContext.current.applicationContext
    val state=produceState(ModelLoad(ModelRepository.cached(organ),null),organ.id) {
        value=try {ModelLoad(ModelRepository.load(context,organ),null)} catch(e: Exception) {ModelLoad(null,e.message ?: "Could not load the model")}
    }
    return state.value
}

/** Camera request: a new [key] reframes onto [targets] (structure ids or node names; whole model when empty). */
private data class Shot(val key: String,val targets: List<String>,val yaw: Float,val pitch: Float)

@Composable private fun NativeAnatomy(state: SceneState,shot: Shot,modifier: Modifier=Modifier,onSelect: (String)->Unit) {
    val context=LocalContext.current
    val view=remember(context) { AnatomyView(context) }
    val owner=LocalLifecycleOwner.current
    val select by rememberUpdatedState(onSelect)
    DisposableEffect(view,owner) {
        val observer=LifecycleEventObserver { _,event -> when(event) {Lifecycle.Event.ON_RESUME->view.onResume();Lifecycle.Event.ON_PAUSE->view.onPause();else->Unit} }
        owner.lifecycle.addObserver(observer)
        if(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) view.onResume()
        onDispose {owner.lifecycle.removeObserver(observer);view.onPause()}
    }
    val model=state.model
    LaunchedEffect(shot,model) { if(model!=null) view.frame(model.bounds(shot.targets),shot.yaw,shot.pitch) }
    AndroidView(factory={view},modifier=modifier,update={it.onSelect={id->select(id)};it.update(state)})
}

@Composable private fun Viewport(load: ModelLoad,state: SceneState,shot: Shot,modifier: Modifier,onSelect: (String)->Unit={},overlay: @Composable BoxScope.()->Unit={}) {
    Box(modifier) {
        NativeAnatomy(state,shot,Modifier.fillMaxSize(),onSelect)
        if(load.model==null) Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
            if(load.error==null) {CircularProgressIndicator();Muted("Loading reference model…")} else Muted("Model unavailable: ${load.error}")
        }
        overlay()
    }
}

@Composable private fun Explorer(vm: CardiaState,dark: Boolean,back: ()->Unit) {
    val organ=vm.organ
    val load=rememberModel(organ)
    val model=load.model
    var selected by rememberSaveable(organ.id) {mutableStateOf("")}
    var exploded by rememberSaveable(organ.id) {mutableStateOf(false)}
    var isolate by rememberSaveable(organ.id) {mutableStateOf(false)}
    var expanded by rememberSaveable {mutableStateOf(false)}
    var axis by rememberSaveable(organ.id) {mutableIntStateOf(-1)}
    var shift by rememberSaveable(organ.id) {mutableFloatStateOf(0f)}
    var peel by rememberSaveable(organ.id) {mutableIntStateOf(0)}
    var search by rememberSaveable(organ.id) {mutableStateOf("")}
    var shots by remember {mutableIntStateOf(0)}
    var shot by remember(organ.id) {mutableStateOf(Shot("${organ.id}/home",emptyList(),organ.yaw,organ.pitch))}
    val heart=organ.id=="heart"
    var playing by rememberSaveable {mutableStateOf(false)}
    var phase by rememberSaveable {mutableFloatStateOf(0f)}
    var bpm by rememberSaveable {mutableFloatStateOf(72f)}
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val active=heart && playing && !vm.reducedMotion && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val explosion by animateFloatAsState(if(exploded) 1f else 0f,tween(if(vm.reducedMotion) 0 else 650),label="Explode")
    LaunchedEffect(active,bpm) {
        if(active) {var last=0L;while(true) {withFrameNanos {now -> if(last!=0L) phase=Cycle.phaseAfter(phase,((now-last)/1e9).toFloat().coerceAtMost(.1f),bpm);last=now}}}
    }
    val clip=if(axis>=0 && model!=null) model.plane(Cut(Axis.entries[axis],shift=shift)) else null
    val scene=SceneState(model,selected,isolate=isolate,peel=peel,clip=clip,explode=explosion*.35f,glow=if(heart && (playing || phase>0f)) Cycle.active(phase) else emptyMap(),dark=dark)
    fun select(id: String) { selected=if(selected==id) "" else id;if(selected.isEmpty()) isolate=false }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
            ActionIcon(Icons.AutoMirrored.Rounded.ArrowBack,"Back",back)
            Column(Modifier.weight(1f)) {Text("${organ.name} studio",style=MaterialTheme.typography.titleLarge,maxLines=1);Muted("HRA reference model · anterior start view")}
            ActionIcon(Icons.Rounded.RestartAlt,"Reset camera") {shot=Shot("${organ.id}/home/${++shots}",emptyList(),organ.yaw,organ.pitch)}
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Atlas.organs.forEach {o->FilterChip(o.id==organ.id,{vm.choose(o)},label={Text(o.name)},leadingIcon={Icon(organIcon(o.id),null,Modifier.size(16.dp))})}
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            val wide=maxWidth>=760.dp
            @Composable fun viewport(modifier: Modifier) {
                Viewport(load,scene,shot,modifier,onSelect={selected=it;expanded=true}) {
                    Row(Modifier.align(Alignment.TopStart).padding(18.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {Pill("3D · offline");if(selected.isNotEmpty()) organ.structure(selected)?.let {Pill(it.name)}}
                    Text("Drag to rotate  ·  Pinch to zoom  ·  Two fingers to pan  ·  Tap to inspect",Modifier.align(Alignment.BottomCenter).padding(12.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            @Composable fun controls(modifier: Modifier) {
                Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        FilterChip(exploded,{exploded=!exploded},label={Text("Separate")},leadingIcon={Icon(Icons.Rounded.Layers,null,Modifier.size(16.dp))})
                        FilterChip(isolate,{if(selected.isNotEmpty()) isolate=!isolate},enabled=selected.isNotEmpty(),label={Text("Isolate")})
                        AssistChip({shot=Shot("${organ.id}/focus/${++shots}",listOf(selected),organ.yaw,organ.pitch)},enabled=selected.isNotEmpty(),label={Text("Focus")},leadingIcon={Icon(Icons.Rounded.CenterFocusStrong,null,Modifier.size(16.dp))})
                        FilterChip(expanded,{expanded=!expanded},label={Text("Inspect")})
                    }
                    Eyebrow("Dissect")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        FilterChip(axis<0,{axis=-1},label={Text("No section")})
                        Axis.entries.forEachIndexed {i,a->FilterChip(axis==i,{axis=i;shift=0f},label={Text(a.label)},leadingIcon={Icon(Icons.Rounded.ContentCut,null,Modifier.size(16.dp))})}
                    }
                    if(axis>=0) {
                        Muted(when(Axis.entries[axis]) {Axis.Coronal->"Removes tissue in front of the plane.";Axis.Sagittal->"Removes the patient's right side of the plane.";Axis.Transverse->"Removes tissue above the plane."})
                        Slider(shift,{shift=it},valueRange=-1f..1f,modifier=Modifier.semantics {contentDescription="Section plane position"})
                    }
                    if(organ.layers.size>1) {
                        Row(verticalAlignment=Alignment.CenterVertically) {Muted("Layer",Modifier.weight(1f));Text(organ.layers[peel],style=MaterialTheme.typography.labelMedium)}
                        Slider(peel.toFloat(),{peel=it.roundToInt()},valueRange=0f..(organ.layers.size-1).toFloat(),steps=(organ.layers.size-2).coerceAtLeast(0),modifier=Modifier.semantics {contentDescription="Peel dissection layers"})
                    }
                    if(heart) {
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            FilledIconButton(onClick={playing=!playing},enabled=!vm.reducedMotion) {Icon(if(playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,if(playing) "Pause cycle" else "Play cycle")}
                            Column(Modifier.weight(1f)) {Eyebrow(Cycle.label(phase));Ecg(phase,Modifier.fillMaxWidth().height(34.dp))}
                            Text("${bpm.toInt()}\nBPM",style=MaterialTheme.typography.labelMedium,fontFamily=FontFamily.Monospace)
                        }
                        Slider(value=phase,onValueChange={phase=it;playing=false},modifier=Modifier.semantics {contentDescription="Cardiac cycle timeline"})
                    }
                    AnimatedVisibility(expanded || wide) {
                        Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                            if(heart) {
                                Row(verticalAlignment=Alignment.CenterVertically) {Muted("Cycle rate",Modifier.weight(1f));Text("${bpm.toInt()} BPM",style=MaterialTheme.typography.labelMedium)}
                                Slider(bpm,{bpm=it},valueRange=40f..140f,modifier=Modifier.semantics {contentDescription="Cycle beats per minute"})
                            }
                            OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Find a structure")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(10.dp))
                            val structures=organ.structures.filter {it.name.contains(search,true) || it.subtitle.contains(search,true)}
                            if(structures.isEmpty()) Muted("No structures match.")
                            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                structures.forEach {s->FilterChip(selected==s.id,{select(s.id)},label={Text(s.name)})}
                            }
                            val current=organ.structure(selected)
                            if(current!=null) StructureDetails(organ,current,vm)
                            else Muted("Tap the model or choose a structure. Anatomical right is on your left in the starting view.")
                            Muted(organ.note)
                            if(heart) Muted("${if(vm.reducedMotion) "Reduced motion is on; use the timeline to scrub. " else ""}The cycle highlights contracting chambers and open valves. Timing and ECG are schematic.")
                        }
                    }
                }
            }
            if(wide) Row(Modifier.fillMaxSize()) {viewport(Modifier.weight(1f).fillMaxHeight());controls(Modifier.width(360.dp).fillMaxHeight())}
            else Column(Modifier.fillMaxSize()) {viewport(Modifier.weight(1f).fillMaxWidth());controls(Modifier.fillMaxWidth().heightIn(max=if(expanded) 420.dp else 280.dp))}
        }
    }
}

@Composable private fun StructureDetails(organ: Organ,s: Structure,vm: CardiaState) {
    Panel {
        Row(verticalAlignment=Alignment.CenterVertically) {Column(Modifier.weight(1f)) {Eyebrow(s.subtitle);Text(s.name,style=MaterialTheme.typography.titleLarge)};ActionIcon(if("${organ.id}:${s.id}" in vm.bookmarks) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,"Toggle bookmark") {vm.bookmark(organ,s.id)}}
        Text(s.description,style=MaterialTheme.typography.bodyMedium)
        Rule()
        Muted(s.fact)
        Text(s.tag.ifEmpty {"${organ.name} · ${organ.layers.getOrElse(s.depth) {""}} layer"},style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
    }
}

@Composable private fun Ecg(phase: Float,modifier: Modifier) {
    val color=MaterialTheme.colorScheme.primary;val grid=MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.semantics {contentDescription="Schematic ECG waveform, not a clinical recording"}) {
        drawLine(grid,Offset(0f,size.height*.70f),Offset(size.width,size.height*.70f),1f)
        val path=Path()
        for(i in 0..200) {val x=i/200f;val y=size.height*(.72f-Cycle.ecg(x)*.62f);if(i==0) path.moveTo(0f,y) else path.lineTo(size.width*x,y)}
        drawPath(path,color.copy(alpha=.65f),style=Stroke(2.dp.toPx(),cap=StrokeCap.Round))
        drawCircle(color,3.dp.toPx(),Offset(phase*size.width,size.height*(.72f-Cycle.ecg(phase)*.62f)))
    }
}

@Composable private fun LessonScreen(vm: CardiaState,dark: Boolean,id: String,back: ()->Unit,quiz: ()->Unit) {
    val organ=vm.organ
    val lesson=organ.lessons.firstOrNull {it.id==id} ?: organ.lessons.first()
    val load=rememberModel(organ)
    val model=load.model
    var step by rememberSaveable(organ.id,lesson.id) {mutableIntStateOf(0)}
    var finished by rememberSaveable(organ.id,lesson.id) {mutableStateOf(false)}
    var phase by remember {mutableFloatStateOf(.35f)}
    val current=lesson.steps[step.coerceIn(lesson.steps.indices)]
    if(finished) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
            ActionIcon(Icons.AutoMirrored.Rounded.ArrowBack,"Back to study",back)
            Icon(Icons.Rounded.TaskAlt,null,Modifier.size(70.dp),tint=MaterialTheme.colorScheme.primary)
            Eyebrow("LESSON COMPLETE");Text("One more piece\nconnected.",style=MaterialTheme.typography.headlineLarge)
            Muted("You finished “${lesson.title}”. Your progress is saved on this device.")
            Panel {Text("Before you move on",style=MaterialTheme.typography.titleLarge);Text("Explain the pathway aloud without looking. Retrieving an idea helps you discover what still needs practice.")}
            Button(quiz,Modifier.fillMaxWidth().height(54.dp)) {Text("Try active recall")}
            OutlinedButton(back,Modifier.fillMaxWidth()) {Text("Return to your lessons")}
        };return
    }
    val v=current.view
    val cycle=organ.id=="heart" && lesson.id=="cycle"
    val scene=SceneState(model,focus=v.focus.toSet(),hidden=v.hide.toSet(),ghost=v.ghost.toSet(),peel=v.peel,clip=v.cut?.let {model?.plane(it)},explode=v.explode,
        markers=v.markers.mapNotNull {model?.anchor(it.anchor)},glow=if(cycle) Cycle.active(phase) else emptyMap(),dark=dark)
    val shot=Shot("${organ.id}/${lesson.id}/$step",v.frame,v.yaw ?: organ.yaw,v.pitch ?: organ.pitch)
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {ActionIcon(Icons.AutoMirrored.Rounded.ArrowBack,"Back",back);Column(Modifier.weight(1f)) {Text(lesson.title,fontWeight=FontWeight.Medium);Muted("Chapter ${step+1} of ${lesson.steps.size}")};Pill("${lesson.minutes} MIN")}
        LinearProgressIndicator(progress={(step+1f)/lesson.steps.size},modifier=Modifier.fillMaxWidth())
        BoxWithConstraints(Modifier.weight(1f)) {
            val wide=maxWidth>=760.dp
            @Composable fun visual(modifier: Modifier) {
                Viewport(load,scene,shot,modifier) {
                    FlowRow(Modifier.align(Alignment.TopStart).padding(16.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        v.focus.mapNotNull {organ.structure(it)}.take(4).forEach {Pill(it.name)}
                        v.markers.forEach {Pill(it.label,Amber)}
                    }
                }
            }
            @Composable fun text(modifier: Modifier) {
                Column(modifier.verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    Eyebrow("CHAPTER ${(step+1).toString().padStart(2,'0')}");Text(current.title,style=MaterialTheme.typography.headlineMedium)
                    Text(current.body,style=MaterialTheme.typography.bodyLarge)
                    Panel {Eyebrow("LOOK A LITTLE CLOSER");Text(current.prompt,style=MaterialTheme.typography.bodyMedium)}
                    if(cycle) {Muted(Cycle.label(phase));Slider(phase,{phase=it},modifier=Modifier.semantics {contentDescription="Lesson cardiac cycle timeline"})}
                    if(v.markers.isNotEmpty()) Muted("Amber markers are schematic, placed at points computed from the reference meshes. They are not part of the anatomical model.")
                    Muted("Drag, pinch and tap work here too.")
                }
            }
            if(wide) Row {visual(Modifier.weight(1f).fillMaxHeight());text(Modifier.width(390.dp).fillMaxHeight())}
            else Column {visual(Modifier.fillMaxWidth().weight(1f));text(Modifier.fillMaxWidth().weight(1f))}
        }
        Row(Modifier.padding(20.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick={step--},enabled=step>0,modifier=Modifier.height(54.dp)) {Text("Previous")}
            Button(onClick={if(step<lesson.steps.lastIndex) step++ else {vm.complete(organ,lesson.id);finished=true}},modifier=Modifier.weight(1f).height(54.dp)) {Text(if(step==lesson.steps.lastIndex) "Complete lesson" else "Next chapter");Spacer(Modifier.width(8.dp));Icon(Icons.AutoMirrored.Rounded.ArrowForward,null)}
        }
    }
}

@Composable private fun QuizScreen(vm: CardiaState,exit: ()->Unit) {
    val organ=vm.organ;val questions=organ.questions
    var index by rememberSaveable(organ.id) {mutableIntStateOf(0)}
    var answer by rememberSaveable(organ.id) {mutableIntStateOf(-1)}
    var checked by rememberSaveable(organ.id) {mutableStateOf(false)}
    var score by rememberSaveable(organ.id) {mutableIntStateOf(0)}
    var results by rememberSaveable(organ.id) {mutableStateOf(false)}
    var wrong by rememberSaveable(organ.id) {mutableStateOf("")}
    var confirmExit by remember {mutableStateOf(false)}
    BackHandler(!results) {confirmExit=true}
    if(confirmExit) AlertDialog(onDismissRequest={confirmExit=false},title={Text("Leave this quiz?")},text={Text("Answers from this unfinished attempt will not be saved.")},confirmButton={TextButton(onClick=exit) {Text("Leave")}},dismissButton={TextButton(onClick={confirmExit=false}) {Text("Keep studying")}})
    if(results) {
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(26.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
            item {Eyebrow("YOUR RECALL SESSION · ${organ.name}");Text("Keep connecting\nthe dots.",style=MaterialTheme.typography.headlineLarge)}
            item {Panel {Text("$score / ${questions.size}",style=MaterialTheme.typography.displayLarge);Text(if(score>=questions.size*3/4) "A strong foundation." else "Every attempt builds understanding.",style=MaterialTheme.typography.titleLarge);Muted("Personal best: ${vm.progress.bestFor(organ.id)} / ${questions.size}. This is practice feedback, not a competence assessment.")}}
            val missed=wrong.split(",").mapNotNull {it.toIntOrNull()}
            if(missed.isNotEmpty()) item {SectionTitle("A few ideas to revisit")}
            items(missed) {i->Panel {Text(questions[i].prompt,fontWeight=FontWeight.SemiBold);Muted(questions[i].explanation)}}
            item {Button(onClick={index=0;answer=-1;checked=false;score=0;results=false;wrong=""},modifier=Modifier.fillMaxWidth().height(54.dp)) {Text("Practice again")};TextButton(exit,Modifier.fillMaxWidth()) {Text("Back to study")}}
        };return
    }
    val q=questions[index.coerceIn(questions.indices)]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {ActionIcon(Icons.Rounded.Close,"Exit quiz") {confirmExit=true};Spacer(Modifier.weight(1f));Pill("${organ.name} · ${index+1} / ${questions.size}")}
        LinearProgressIndicator(progress={index.toFloat()/questions.size},modifier=Modifier.fillMaxWidth())
        Eyebrow("ACTIVE RECALL");Text(q.prompt,style=MaterialTheme.typography.headlineMedium)
        q.options.forEachIndexed {i,option ->
            val correct=checked && i==q.correct; val chosen=answer==i
            Surface(onClick={if(!checked) answer=i},shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth(),color=if(chosen || correct) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.6f),border=BorderStroke(if(chosen || correct) 2.dp else 1.dp,if(chosen || correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {Text(('A'+i).toString(),fontFamily=FontFamily.Monospace,color=MaterialTheme.colorScheme.primary);Text(option,Modifier.weight(1f));if(correct) Icon(Icons.Rounded.Check,"Correct answer") else if(checked && chosen) Icon(Icons.Rounded.Close,"Incorrect answer")}
            }
        }
        AnimatedVisibility(checked) {Panel {Text(if(answer==q.correct) "Exactly right." else "A useful one to revisit.",fontWeight=FontWeight.SemiBold);Text(q.explanation,style=MaterialTheme.typography.bodyMedium)}}
        Button(onClick={
            if(!checked) {checked=true;if(answer==q.correct) score++ else wrong+="$index,"}
            else if(index==questions.lastIndex) {vm.score(organ,score);results=true}
            else {index++;answer=-1;checked=false}
        },enabled=answer>=0,modifier=Modifier.fillMaxWidth().height(56.dp)) {Text(if(!checked) "Check answer" else if(index==questions.lastIndex) "See my results" else "Next question")}
    }
}

@Composable private fun ProgressScreen(vm: CardiaState,settings: ()->Unit,explore: ()->Unit) {
    val saved=vm.bookmarks.mapNotNull {key->key.split(':').takeIf {it.size==2}?.let {(o,s)->Atlas.organs.firstOrNull {it.id==o}?.let {organ->organ.structure(s)?.let {organ to it}}}}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(24.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
        item {Row(verticalAlignment=Alignment.CenterVertically) {Column(Modifier.weight(1f)) {Eyebrow("YOUR SPACE");Text("A growing\nunderstanding.",style=MaterialTheme.typography.headlineLarge)};ActionIcon(Icons.Rounded.Settings,"Settings",settings)}}
        item {Panel {
            Muted("Everything is saved locally. No account required.")
            Atlas.organs.forEach {o->
                val done=vm.progress.completedIn(o)
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Icon(organIcon(o.id),null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {Text(o.name,fontWeight=FontWeight.Medium);LinearProgressIndicator(progress={done/o.lessons.size.toFloat()},modifier=Modifier.fillMaxWidth().padding(top=6.dp))}
                    Text("$done/${o.lessons.size} · ${vm.progress.bestFor(o.id)}/${o.questions.size}",style=MaterialTheme.typography.labelMedium,fontFamily=FontFamily.Monospace)
                }
            }
            Muted("Lessons complete · best recall score")
        }}
        item {SectionTitle("Saved structures","Keep your tricky concepts close.")}
        if(saved.isEmpty()) item {Panel {Icon(Icons.Rounded.BookmarkBorder,null,tint=MaterialTheme.colorScheme.primary);Text("Your collection starts with a tap.");Muted("Open a structure in the studio and use its bookmark button.");TextButton(explore) {Text("Open the studio")}}}
        items(saved) {(o,s)->StructureDetails(o,s,vm)}
        item {SectionTitle("Your study notes");Spacer(Modifier.height(12.dp));OutlinedTextField(vm.notes,{vm.note(it)},modifier=Modifier.fillMaxWidth(),minLines=5,label={Text("What clicked today?")},supportingText={Text("Saved automatically on this device")},shape=RoundedCornerShape(20.dp))}
        item {Panel {
            Eyebrow("REFERENCES & SCOPE");Muted(Atlas.disclaimer);val uri=LocalUriHandler.current
            Atlas.organs.flatMap {it.references}.distinct().forEach {(label,url)->TextButton(onClick={uri.openUri(url)}) {Text(label)}}
            Eyebrow("3D MODEL CREDIT");Muted(Atlas.credit)
            TextButton(onClick={uri.openUri("https://humanatlas.io/3d-reference-library")}) {Text("Human Reference Atlas · 3D reference library")}
            TextButton(onClick={uri.openUri("https://creativecommons.org/licenses/by/4.0/")}) {Text("Creative Commons Attribution 4.0")}
        }}
    }
}

@Composable private fun Settings(vm: CardiaState,dismiss: ()->Unit) {
    var confirmReset by remember {mutableStateOf(false)}
    ModalBottomSheet(onDismissRequest=dismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start=24.dp,end=24.dp,bottom=32.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Text("Make yourself at home",style=MaterialTheme.typography.headlineMedium)
            Eyebrow("APPEARANCE")
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("System","Light","Dark").forEachIndexed {i,label->FilterChip(vm.theme==i,{vm.setAppearance(i)},label={Text(label)})}}
            Row(verticalAlignment=Alignment.CenterVertically) {Column(Modifier.weight(1f)) {Text("Reduced motion",fontWeight=FontWeight.Medium);Muted("Pause automatic motion; keep manual scrubbing.")};Switch(vm.reducedMotion,{vm.motion(it)})}
            Rule()
            Muted("Cardia 0.2 · Android-first Kotlin Multiplatform\nWorks offline. No analytics, permissions, advertisements, or accounts.")
            Muted(Atlas.credit)
            Muted("A study companion, not a clinical reference. Medical review is required before use in formal teaching.")
            TextButton(onClick={confirmReset=true}) {Text("Reset study progress and notes",color=MaterialTheme.colorScheme.error)}
        }
    }
    if(confirmReset) AlertDialog(onDismissRequest={confirmReset=false},title={Text("Reset your study data?")},text={Text("This deletes local progress, quiz best scores, bookmarks and notes. It cannot be undone.")},confirmButton={TextButton(onClick={vm.resetStudy();confirmReset=false}) {Text("Reset")}},dismissButton={TextButton(onClick={confirmReset=false}) {Text("Cancel")}})
}
