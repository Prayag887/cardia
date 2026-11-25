package studio.cardia

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import studio.cardia.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*

/**
 * Immutable renderer input. Structure ids refer to [AnatomyModel.organ]. [clip] is a plane
 * (normal xyz, offset w) in normalized model space; fragments with dot(p, n) > w are cut away.
 * [markers] are schematic points in the same space.
 */
data class SceneState(
    val model: AnatomyModel? = null,
    val selected: String = "",
    val focus: Set<String> = emptySet(),
    val isolate: Boolean = false,
    val hidden: Set<String> = emptySet(),
    val ghost: Set<String> = emptySet(),
    val peel: Int = 0,
    val clip: FloatArray? = null,
    val explode: Float = 0f,
    val markers: List<FloatArray> = emptyList(),
    val glow: Map<String, Float> = emptyMap(),
    val dark: Boolean = true
)

/** Native GLES 2 surface. Render-on-demand; Compose owns state, clock and lifecycle. */
class AnatomyView(context: Context) : GLSurfaceView(context) {
    val renderer = AnatomyRenderer()
    var onSelect: (String) -> Unit = {}
    private var px = 0f; private var py = 0f; private var travel = 0f; private var pinched = false
    private val scaler = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var fx = 0f; private var fy = 0f
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean { fx = detector.focusX; fy = detector.focusY; return true }
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            pinched = true
            val factor = detector.scaleFactor; val dx = detector.focusX - fx; val dy = detector.focusY - fy
            fx = detector.focusX; fy = detector.focusY
            queueEvent { renderer.zoom = (renderer.zoom * factor).coerceIn(.5f, 8f); renderer.panBy(dx, dy) }
            requestRender(); return true
        }
    })
    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        preserveEGLContextOnPause = true
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        contentDescription = "Interactive 3D anatomy. Drag to rotate, pinch to zoom, two fingers to pan. Use the structure chips for accessible selection."
    }
    fun update(state: SceneState) { renderer.state = state; requestRender() }
    /** Centres the camera on [bounds] (whole model when null) and applies organ or lesson angles. */
    fun frame(bounds: Bounds?, yaw: Float, pitch: Float) { queueEvent { renderer.frame(bounds, yaw, pitch) }; requestRender() }
    override fun performClick(): Boolean { super.performClick(); return true }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaler.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { px = event.x; py = event.y; travel = 0f; pinched = false; parent?.requestDisallowInterceptTouchEvent(true) }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - px; val dy = event.y - py; travel += abs(dx) + abs(dy)
                if (!scaler.isInProgress && !pinched && event.pointerCount == 1) queueEvent { renderer.yaw += dx * .4f; renderer.pitch = (renderer.pitch + dy * .3f).coerceIn(-89f, 89f) }
                px = event.x; py = event.y; requestRender()
            }
            MotionEvent.ACTION_UP -> {
                if (travel < 16 && !pinched) {
                    val x = event.x; val y = event.y
                    queueEvent { val id = renderer.pick(x, y); if (id != null) post { onSelect(id) } }; performClick()
                }
                parent?.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)
        }
        return true
    }
}

class AnatomyRenderer : GLSurfaceView.Renderer {
    @Volatile var state = SceneState()
    var yaw = 0f; var pitch = 0f; var zoom = 1f
    private var panX = 0f; private var panY = 0f
    private val target = FloatArray(3)
    private var width = 1; private var height = 1; private var program = 0; private var uintIndices = false
    private val projection = FloatArray(16); private val view = FloatArray(16); private val base = FloatArray(16)
    private val model = FloatArray(16); private val mv = FloatArray(16); private val mvp = FloatArray(16)
    private var aPosition = 0; private var aNormal = 0
    private var uMvp = 0; private var uModel = 0; private var uColor = 0; private var uGlow = 0; private var uAlpha = 0; private var uClip = 0; private var uMarker = 0

    private class GpuPart(val part: ScenePart, val vbo: Int, val ibo: Int, val count: Int, val type: Int)
    private var uploaded: AnatomyModel? = null
    private val gpu = mutableListOf<GpuPart>()
    private val sphere = Primitives.sphere()
    private val sphereBuffer = ByteBuffer.allocateDirect(sphere.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(sphere); position(0) }
    private val pickable = mutableListOf<Pair<ScenePart, FloatArray>>()
    private var pickClip: FloatArray? = null

    private var fitX = 1f; private var fitY = 1f
    /** Fits [bounds] to the viewport. Horizontal extent uses max(x, z) so any yaw still fits. */
    fun frame(bounds: Bounds?, yaw: Float, pitch: Float) {
        val b = bounds ?: Bounds(floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, 1f, 1f))
        b.center.copyInto(target)
        fitX = max(b.max[0] - b.min[0], b.max[2] - b.min[2]).coerceAtLeast(.02f) / 2
        fitY = (b.max[1] - b.min[1]).coerceAtLeast(.02f) / 2
        this.yaw = yaw; this.pitch = pitch; zoom = 1f; panX = 0f; panY = 0f
    }
    fun panBy(dx: Float, dy: Float) { val h = halfHeight(); panX += dx / height * 2 * h; panY -= dy / height * 2 * h }
    private fun halfHeight() = 1.12f * max(fitY, fitX * height / width) / zoom

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        uploaded = null; gpu.clear()
        uintIndices = glGetString(GL_EXTENSIONS)?.contains("GL_OES_element_index_uint") == true
        fun shader(type: Int, source: String): Int {
            val s = glCreateShader(type); glShaderSource(s, source); glCompileShader(s)
            val ok = IntArray(1); glGetShaderiv(s, GL_COMPILE_STATUS, ok, 0)
            check(ok[0] != 0) { glGetShaderInfoLog(s) }; return s
        }
        val vs = shader(GL_VERTEX_SHADER, """
            uniform mat4 uMvp; uniform mat4 uModel;
            attribute vec3 aPosition; attribute vec3 aNormal;
            varying vec3 vNormal; varying vec3 vLocal;
            void main() { vNormal=(uModel*vec4(aNormal,0.0)).xyz; vLocal=aPosition; gl_Position=uMvp*vec4(aPosition,1.0); }
        """.trimIndent())
        val fs = shader(GL_FRAGMENT_SHADER, """
            #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
            #else
            precision mediump float;
            #endif
            uniform vec3 uColor; uniform float uGlow; uniform float uAlpha; uniform vec4 uClip; uniform float uMarker;
            varying vec3 vNormal; varying vec3 vLocal;
            void main() {
                if(dot(vLocal,uClip.xyz)>uClip.w) discard;
                // Seen through a section, the inside of a shell stands in for the cut surface: flat, slightly darker tissue colour.
                if(!gl_FrontFacing && uMarker<0.5) { gl_FragColor=vec4(uColor*0.74+vec3(1.0,0.80,0.55)*uGlow*0.12,uAlpha); return; }
                vec3 n=normalize(vNormal); if(!gl_FrontFacing) n=-n; vec3 base=uColor;
                vec3 l=normalize(vec3(-0.35,0.55,0.75)); vec3 f=normalize(vec3(0.6,-0.25,0.45)); vec3 v=vec3(0.0,0.0,1.0);
                float diffuse=max(dot(n,l),0.0); float fill=max(dot(n,f),0.0);
                float rim=pow(1.0-max(dot(n,v),0.0),2.5); float spec=pow(max(dot(n,normalize(l+v)),0.0),40.0);
                vec3 c=base*(0.30+0.62*diffuse+0.16*fill)+vec3(1.0)*spec*0.16+vec3(0.75,0.85,1.0)*rim*0.16+vec3(1.0,0.80,0.55)*uGlow*0.30;
                gl_FragColor=vec4(c,uAlpha);
            }
        """.trimIndent())
        program = glCreateProgram(); glAttachShader(program, vs); glAttachShader(program, fs); glLinkProgram(program)
        val linked = IntArray(1); glGetProgramiv(program, GL_LINK_STATUS, linked, 0); check(linked[0] != 0) { glGetProgramInfoLog(program) }
        glDeleteShader(vs); glDeleteShader(fs)
        aPosition = glGetAttribLocation(program, "aPosition"); aNormal = glGetAttribLocation(program, "aNormal")
        uMvp = glGetUniformLocation(program, "uMvp"); uModel = glGetUniformLocation(program, "uModel")
        uColor = glGetUniformLocation(program, "uColor"); uGlow = glGetUniformLocation(program, "uGlow"); uAlpha = glGetUniformLocation(program, "uAlpha")
        uClip = glGetUniformLocation(program, "uClip"); uMarker = glGetUniformLocation(program, "uMarker")
        glEnable(GL_DEPTH_TEST)
    }
    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) { width = w.coerceAtLeast(1); height = h.coerceAtLeast(1); glViewport(0, 0, width, height) }

    private fun upload(m: AnatomyModel?) {
        val old = gpu.flatMap { listOf(it.vbo, it.ibo) }.filter { it != 0 }.toIntArray()
        if (old.isNotEmpty()) glDeleteBuffers(old.size, old, 0)
        gpu.clear(); uploaded = m
        if (m == null) return
        for (p in m.parts) {
            val vertices = p.positions.size / 3
            val ids = IntArray(2); glGenBuffers(2, ids, 0)
            if (vertices > 65535 && !uintIndices) {
                // Rare fallback: expand to a plain triangle list when 32-bit indices are unsupported.
                val flat = FloatArray(p.indices.size * 6)
                p.indices.forEachIndexed { i, v -> for (c in 0..2) { flat[i * 6 + c] = p.positions[v * 3 + c]; flat[i * 6 + 3 + c] = p.normals[v * 3 + c] } }
                bufferData(GL_ARRAY_BUFFER, ids[0], flat)
                glDeleteBuffers(1, ids, 1)
                gpu += GpuPart(p, ids[0], 0, p.indices.size, 0)
                continue
            }
            val interleaved = FloatArray(vertices * 6)
            for (v in 0 until vertices) for (c in 0..2) { interleaved[v * 6 + c] = p.positions[v * 3 + c]; interleaved[v * 6 + 3 + c] = p.normals[v * 3 + c] }
            bufferData(GL_ARRAY_BUFFER, ids[0], interleaved)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ids[1])
            if (vertices > 65535) {
                val b = ByteBuffer.allocateDirect(p.indices.size * 4).order(ByteOrder.nativeOrder()).asIntBuffer().apply { put(p.indices); position(0) }
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, p.indices.size * 4, b, GL_STATIC_DRAW)
                gpu += GpuPart(p, ids[0], ids[1], p.indices.size, GL_UNSIGNED_INT)
            } else {
                val b = ByteBuffer.allocateDirect(p.indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().apply { p.indices.forEach { put(it.toShort()) }; position(0) }
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, p.indices.size * 2, b, GL_STATIC_DRAW)
                gpu += GpuPart(p, ids[0], ids[1], p.indices.size, GL_UNSIGNED_SHORT)
            }
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0); glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }
    private fun bufferData(target: Int, id: Int, data: FloatArray) {
        glBindBuffer(target, id)
        val b = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(data); position(0) }
        glBufferData(target, data.size * 4, b, GL_STATIC_DRAW)
    }

    override fun onDrawFrame(gl: GL10?) {
        val s = state
        if (s.model !== uploaded) upload(s.model)
        if (s.dark) glClearColor(.063f, .071f, .094f, 1f) else glClearColor(.965f, .957f, .945f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT); glUseProgram(program)
        val aspect = width.toFloat() / height; val h = halfHeight() * (1f + s.explode)  // keep separated parts in view
        Matrix.orthoM(projection, 0, -h * aspect, h * aspect, -h, h, 1f, 12f)
        Matrix.setLookAtM(view, 0, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.setIdentityM(base, 0); Matrix.translateM(base, 0, panX, panY, 0f)
        Matrix.rotateM(base, 0, pitch, 1f, 0f, 0f); Matrix.rotateM(base, 0, yaw, 0f, 1f, 0f)
        Matrix.translateM(base, 0, -target[0], -target[1], -target[2])
        val clip = s.clip ?: floatArrayOf(0f, 0f, 0f, 1e4f)
        glUniform4f(uClip, clip[0], clip[1], clip[2], clip[3]); pickClip = s.clip
        glUniform1f(uMarker, 0f)
        pickable.clear()
        val organ = s.model?.organ
        fun visible(p: ScenePart): Boolean {
            if (p.structure in s.hidden) return false
            if (p.structure in s.ghost) return true
            if (s.isolate && s.selected.isNotEmpty()) return p.structure == s.selected
            return (organ?.structure(p.structure)?.depth ?: 0) >= s.peel
        }
        val emphasis = if (s.selected.isNotEmpty()) setOf(s.selected) else s.focus
        glEnable(GL_POLYGON_OFFSET_FILL)
        glDisable(GL_BLEND); glDepthMask(true); glDisable(GL_CULL_FACE)
        for (g in gpu) {
            val p = g.part
            if (!visible(p) || p.structure in s.ghost) continue
            val st = organ?.structure(p.structure)
            glPolygonOffset(0f, (st?.depth ?: 0) * 4f - (if (st?.decal == true) 3f else 0f))
            val emphasised = p.structure in emphasis
            val dim = if (emphasis.isNotEmpty() && !emphasised) (if (s.selected.isNotEmpty()) .45f else .58f) else 1f
            val glow = max(if (emphasised) (if (s.selected.isNotEmpty()) 1f else .55f) else 0f, s.glow[p.structure] ?: 0f)
            place(p, s.explode)
            drawPart(g, p.color[0] * dim, p.color[1] * dim, p.color[2] * dim, glow, 1f)
            pickable += p to mvp.copyOf()
        }
        glDisable(GL_POLYGON_OFFSET_FILL)
        // Ghosts: translucent context over the opaque scene, not pickable.
        if (s.ghost.isNotEmpty()) {
            glEnable(GL_BLEND); glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); glDepthMask(false)
            glEnable(GL_CULL_FACE); glCullFace(GL_BACK)
            for (g in gpu) {
                val p = g.part
                if (!visible(p) || p.structure !in s.ghost) continue
                place(p, s.explode); drawPart(g, p.color[0], p.color[1], p.color[2], 0f, .16f)
            }
            glDisable(GL_CULL_FACE); glDepthMask(true); glDisable(GL_BLEND)
        }
        // Schematic markers drawn over the anatomy so they stay visible inside ghosted tissue.
        if (s.markers.isNotEmpty()) {
            glDisable(GL_DEPTH_TEST); glUniform4f(uClip, 0f, 0f, 0f, 1e4f); glUniform1f(uMarker, 1f)
            glBindBuffer(GL_ARRAY_BUFFER, 0); glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
            sphereBuffer.position(0); glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 24, sphereBuffer); glEnableVertexAttribArray(aPosition)
            sphereBuffer.position(3); glVertexAttribPointer(aNormal, 3, GL_FLOAT, false, 24, sphereBuffer); glEnableVertexAttribArray(aNormal)
            val r = .045f * halfHeight()
            for (m in s.markers) {
                System.arraycopy(base, 0, model, 0, 16)
                Matrix.translateM(model, 0, m[0], m[1], m[2]); Matrix.scaleM(model, 0, r, r, r)
                matrices(); glUniform3f(uColor, 1f, .56f, .08f); glUniform1f(uGlow, .6f); glUniform1f(uAlpha, 1f)
                glDrawArrays(GL_TRIANGLES, 0, sphere.size / 6)
            }
            glEnable(GL_DEPTH_TEST)
        }
    }
    private fun place(p: ScenePart, explode: Float) {
        System.arraycopy(base, 0, model, 0, 16)
        if (explode > 0f) Matrix.translateM(model, 0, (p.centroid[0] - target[0]) * explode, (p.centroid[1] - target[1]) * explode, (p.centroid[2] - target[2]) * explode)
    }
    private fun matrices() {
        Matrix.multiplyMM(mv, 0, view, 0, model, 0); Matrix.multiplyMM(mvp, 0, projection, 0, mv, 0)
        glUniformMatrix4fv(uMvp, 1, false, mvp, 0); glUniformMatrix4fv(uModel, 1, false, model, 0)
    }
    private fun drawPart(g: GpuPart, r: Float, gr: Float, b: Float, glow: Float, alpha: Float) {
        matrices()
        glUniform3f(uColor, r, gr, b); glUniform1f(uGlow, glow); glUniform1f(uAlpha, alpha)
        glBindBuffer(GL_ARRAY_BUFFER, g.vbo)
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 24, 0); glEnableVertexAttribArray(aPosition)
        glVertexAttribPointer(aNormal, 3, GL_FLOAT, false, 24, 12); glEnableVertexAttribArray(aNormal)
        if (g.ibo == 0) glDrawArrays(GL_TRIANGLES, 0, g.count)
        else { glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, g.ibo); glDrawElements(GL_TRIANGLES, g.count, g.type, 0) }
    }

    /** Casts the tap through each drawn part's inverse MVP; the nearest surface not cut away wins. */
    fun pick(x: Float, y: Float): String? {
        val a = floatArrayOf(2 * x / width - 1, 1 - 2 * y / height, -1f, 1f); val b = a.copyOf().apply { this[2] = 1f }
        val near = FloatArray(4); val far = FloatArray(4); val inv = FloatArray(16); val clip = pickClip
        var nearest = Float.POSITIVE_INFINITY; var result: String? = null
        for ((part, matrix) in pickable) {
            if (!Matrix.invertM(inv, 0, matrix, 0)) continue
            Matrix.multiplyMV(near, 0, inv, 0, a, 0); Matrix.multiplyMV(far, 0, inv, 0, b, 0)
            for (i in 0..2) { near[i] /= near[3]; far[i] /= far[3] }
            val dir = floatArrayOf(far[0] - near[0], far[1] - near[1], far[2] - near[2])
            val t = Ray.nearest(part, near, dir, clip?.let { c -> { px, py, pz -> px * c[0] + py * c[1] + pz * c[2] <= c[3] + 1e-4f } })
            if (t < nearest) { nearest = t; result = part.structure.ifEmpty { null } }
        }
        return result
    }
}
