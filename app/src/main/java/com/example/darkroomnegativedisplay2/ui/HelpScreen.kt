package com.example.darkroomnegativedisplay2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Art {
    SETUP, SLIDE, FLIP, CAPTURE, FILES,
    PAPER, MIX, CLOCK, TONEGATIVE, TABLET_FRAME, PROJECTOR, WATER, DRYING,
    SAFELIGHT, SWITCHES, TIMELINE, STRIPS, MULTI, TRAYS
}

private data class HelpStep(val title: String, val text: String, val art: Art?)
private data class HelpGroup(val title: String?, val steps: List<HelpStep>)
private data class HelpGuide(
    val tab: String,
    val title: String,
    val intro: String,
    val groups: List<HelpGroup>,
    val tips: List<String>
)

private val filmGuide = HelpGuide(
    tab = "FILM",
    title = "FILM NEGATIVE VIEWER",
    intro = "Turn your phone into a negative-to-positive scanner. Browse developed film " +
        "and snap the frames you like.",
    groups = listOf(
        HelpGroup(
            null, listOf(
                HelpStep(
                    "Get a slide viewer",
                    "A cheap slide viewer (about 20 USD on Amazon) is a tiny lightbox with a magnifier. " +
                        "Your film strip slides through it, lit from behind. That's your light table.",
                    Art.SLIDE
                ),
                HelpStep(
                    "Park your phone",
                    "Put the phone in a holder and point its back camera at the viewer's lens. " +
                        "Get close, hold steady, and keep the room dim so nothing reflects.",
                    Art.SETUP
                ),
                HelpStep(
                    "Open Film Negative Viewer",
                    "Tap it on the home screen and allow camera access the first time. " +
                        "The screen goes full-bright and shows a live feed.",
                    null
                ),
                HelpStep(
                    "Let the magic flip",
                    "The app inverts the live image in real time, so dark becomes light. " +
                        "Your negatives turn into positives. Slide the film along and browse your frames.",
                    Art.FLIP
                ),
                HelpStep(
                    "Tune the brightness",
                    "Use the BRIGHTNESS slider at the bottom left if the picture looks too dim or too hot.",
                    null
                ),
                HelpStep(
                    "Found a good one? Snap it!",
                    "Tap TAKE PHOTO at the bottom right. The converted image is saved, " +
                        "so you can keep the frames you like for later darkroom work.",
                    Art.CAPTURE
                ),
                HelpStep(
                    "Collect your shots",
                    "Photos land in Pictures/ContactDisplay on your phone. " +
                        "Connect it to your computer (or sync your gallery) and review them on a big screen.",
                    Art.FILES
                ),
                HelpStep(
                    "Done?",
                    "Tap anywhere on the image to close the viewer and return to the menu.",
                    null
                )
            )
        )
    ),
    tips = listOf(
        "Clean the slide viewer's lens: dust looks huge.",
        "Dim room + bright viewer = best contrast.",
        "No need to tap to focus: just adjust the distance between phone and viewer."
    )
)

private val gumGuide = HelpGuide(
    tab = "GUM",
    title = "GUM BICHROMATE",
    intro = "Print your photo by hand with light-sensitive paint. The app turns your tablet " +
        "or projector into the light source that exposes the paper.",
    groups = listOf(
        HelpGroup(
            "PART 1 · BEFORE THE APP", listOf(
                HelpStep(
                    "Pick your paper",
                    "Use watercolor paper. It has to survive a long soak in water.",
                    Art.PAPER
                ),
                HelpStep(
                    "Mix the emulsion",
                    "Prepare a gum arabic solution and mix it with watercolor paint. " +
                        "The paint decides the colour of your print.",
                    Art.MIX
                ),
                HelpStep(
                    "Add potassium bichromate",
                    "From this moment the mix is sensitive to light. No safelight is needed, " +
                        "just keep the room slightly dark.",
                    null
                ),
                HelpStep(
                    "Paint and dry",
                    "Brush the mix onto the paper and let it dry for about 2 hours. " +
                        "Optionally add a second layer and dry it again.",
                    Art.DRYING
                )
            )
        ),
        HelpGroup(
            "PART 2 · IN THE APP", listOf(
                HelpStep(
                    "Load your photo",
                    "Open Gum Bichromate and load an image from your device or Google Drive. " +
                        "Got a colour image? Convert it to black & white.",
                    null
                ),
                HelpStep(
                    "Make it a negative",
                    "Convert the black & white image to a negative. " +
                        "Light passes through the dark parts of the print, so the image must be inverted.",
                    Art.TONEGATIVE
                ),
                HelpStep(
                    "Set the exposure",
                    "Choose the exposure time, usually 1 to 2 hours, in 15-minute steps.",
                    Art.CLOCK
                ),
                HelpStep(
                    "Set the black-screen timer",
                    "Busy and might not be back on time? Choose how many minutes the screen stays black " +
                        "after the exposure ends, again in 15-minute steps (0 = off).",
                    null
                ),
                HelpStep(
                    "Projector? Resize the image",
                    "Using a projector instead of a tablet? Use the size slider to fit the picture " +
                        "to your paper before you start.",
                    Art.PROJECTOR
                ),
                HelpStep(
                    "Expose!",
                    "Put the dry paper in a clip frame in a dark place. Tap Display Negative and lay the " +
                        "tablet on the frame. With a projector, aim it at the frame and set the sharpness first.",
                    Art.TABLET_FRAME
                ),
                HelpStep(
                    "Leave the display",
                    "Tap the screen 5 times to exit the picture or the black screen.",
                    null
                )
            )
        ),
        HelpGroup(
            "PART 3 · AFTER THE EXPOSURE", listOf(
                HelpStep(
                    "Into the water",
                    "Put the paper in water, image side down, for about 20 minutes.",
                    Art.WATER
                ),
                HelpStep(
                    "Check, wait, repeat",
                    "Look at the paper every now and then. Is the image visible yet? " +
                        "The time is hard to predict, so give it more water if it needs it.",
                    null
                ),
                HelpStep(
                    "Dry it",
                    "When the image looks right, take the paper out and leave it to dry.",
                    Art.DRYING
                )
            )
        )
    ),
    tips = listOf(
        "Potassium bichromate is toxic. Wear gloves, ventilate the room and dispose of it properly.",
        "The black-screen timer keeps the paper safe if you come back late.",
        "Image not visible yet? More time in the water is fine."
    )
)

private val paperGuide = HelpGuide(
    tab = "PAPER",
    title = "PHOTO PAPER",
    intro = "Expose classic photosensitive paper with your tablet or projector, under a red safelight. " +
        "A digital enlarger with a built-in test-print maker.",
    groups = listOf(
        HelpGroup(
            "PART 1 · BEFORE THE APP", listOf(
                HelpStep(
                    "Get photosensitive paper",
                    "Standard black & white darkroom paper. Keep it sealed and away from white light.",
                    Art.PAPER
                ),
                HelpStep(
                    "Prepare three baths",
                    "You need developer, stop bath and fixer. All are standard and sold in photo shops. " +
                        "Pour each into its own tray.",
                    Art.TRAYS
                ),
                HelpStep(
                    "Switch on the red safelight",
                    "Red light is safe for photo paper. Work under it and keep any other light off.",
                    Art.SAFELIGHT
                )
            )
        ),
        HelpGroup(
            "PART 2 · IN THE APP", listOf(
                HelpStep(
                    "Load and prepare the photo",
                    "Tap Load Photos. Got a colour image? Tap Convert to Black & White. " +
                        "Then tap Convert to Negative.",
                    Art.TONEGATIVE
                ),
                HelpStep(
                    "Projector? Resize the image",
                    "Use the Image size slider, just below Load Photos, to fit the picture to your paper.",
                    Art.PROJECTOR
                ),
                HelpStep(
                    "Safety Mode switch",
                    "Turn on Red Interface (Safety Mode). The interface turns dark red, which is safe " +
                        "for the paper. The negative will not display unless this is on.",
                    Art.SWITCHES
                ),
                HelpStep(
                    "Use Device Brightness switch",
                    "Use it only before or after exposure, to load photos and set times. " +
                        "Keep it OFF while exposing: the paper is very sensitive and needs minimal light.",
                    Art.SWITCHES
                ),
                HelpStep(
                    "Set the display timings",
                    "Pre-display black: seconds of black screen before the image, so you can put the " +
                        "tablet on the paper.\n" +
                        "Display duration: seconds the negative is shown. Decide it by testing.\n" +
                        "Post-display black: seconds of black screen after the image, so you can " +
                        "take the tablet away and store it safely.",
                    Art.TIMELINE
                ),
                HelpStep(
                    "Test exposure parts",
                    "Set how many strips or test images the test modes will use.",
                    Art.CLOCK
                ),
                HelpStep(
                    "Test Strips",
                    "Like the classic darkroom trick of revealing the paper step by step: each strip " +
                        "gets more exposure than the last. Compare them after processing.",
                    Art.STRIPS
                ),
                HelpStep(
                    "Multi-Copy Test",
                    "Shows several small copies of the full image on one sheet, each exposed for a " +
                        "different time. Better than strips for judging a whole photo.",
                    Art.MULTI
                ),
                HelpStep(
                    "Pick your exposure time",
                    "Process the test sheet and find the best-looking part. If the one exposed for " +
                        "8 seconds is best, set Display duration to 8.",
                    null
                ),
                HelpStep(
                    "Display Negative",
                    "Put the paper under the tablet or projector and tap Display Negative. " +
                        "The pre-black, image and post-black run in order.",
                    Art.TABLET_FRAME
                ),
                HelpStep(
                    "Leave the display",
                    "Tap the screen 5 times to exit the picture or the black screen.",
                    null
                )
            )
        ),
        HelpGroup(
            "PART 3 · AFTER THE EXPOSURE", listOf(
                HelpStep(
                    "Developer",
                    "Slide the paper into the developer. Use the same time as for your test sheet.",
                    Art.TRAYS
                ),
                HelpStep(
                    "Stop bath, then fixer",
                    "Move the paper to the stop bath, then to the fixer. Keep these times the same " +
                        "as for the test, or the exposure result will not match.",
                    Art.WATER
                ),
                HelpStep(
                    "Dry it",
                    "Leave the finished print to dry.",
                    Art.DRYING
                )
            )
        )
    ),
    tips = listOf(
        "Keep the process identical between test and final print: same developer, stop bath and fixer times.",
        "Keep Use Device Brightness off during exposure.",
        "Run a new test for every new paper or photo.",
        "Wear gloves and ventilate the room when working with chemicals."
    )
)

private val guides = listOf(filmGuide, gumGuide, paperGuide)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val guide = guides[selected]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
        Text(
            "HOW TO USE",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )

        // Tabs
        Row(Modifier.fillMaxWidth()) {
            guides.forEachIndexed { i, g ->
                val on = i == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (on) Color.White else Color.Black)
                        .border(BorderStroke(2.dp, Color.White))
                        .clickable { selected = i }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        g.tab,
                        color = if (on) Color.Black else Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            guide.title,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(guide.intro, color = Color.LightGray, fontSize = 15.sp)

        var n = 0
        guide.groups.forEach { group ->
            group.title?.let {
                Text(
                    it,
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp)
                )
            }
            group.steps.forEach { step ->
                n++
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(2.dp, Color.White))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "$n. ${step.title}",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    step.art?.let {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) { drawArt(it) }
                    }
                    Text(step.text, color = Color.LightGray, fontSize = 15.sp, lineHeight = 21.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Gray))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("TIPS", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            guide.tips.forEach { Text("• $it", color = Color.LightGray, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Pixel-art style illustrations: white on black, like a Game of Life grid. */
private fun DrawScope.drawArt(art: Art) {
    val w = size.width
    val h = size.height
    val white = Color.White
    val stroke = Stroke(width = 4f)
    fun box(x: Float, y: Float, bw: Float, bh: Float, fill: Boolean = false, c: Color = white) {
        if (fill) drawRect(c, Offset(x, y), Size(bw, bh))
        else drawRect(c, Offset(x, y), Size(bw, bh), style = stroke)
    }
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, sw: Float = 4f) =
        drawLine(white, Offset(x1, y1), Offset(x2, y2), sw)

    fun gliderGrid(x0: Float, inverted: Boolean) {
        val cell = minOf(w * 0.4f / 5f, h / 5f)
        val glider = listOf(1 to 0, 2 to 1, 0 to 2, 1 to 2, 2 to 2)
        val y0 = (h - cell * 5) / 2
        box(x0 - 4, y0 - 4, cell * 3 + 8, cell * 3 + 8)
        for (gx in 0 until 3) for (gy in 0 until 3) {
            val on = glider.contains(gx to gy) != inverted
            if (on) box(x0 + gx * cell, y0 + gy * cell, cell - 3, cell - 3, fill = true)
        }
    }

    when (art) {
        Art.SLIDE -> {
            box(w * 0.3f, h * 0.15f, w * 0.4f, h * 0.7f)
            box(w * 0.4f, h * 0.3f, w * 0.2f, h * 0.4f, fill = true, c = Color.Gray)
            val fy = h * 0.42f
            val fh = h * 0.16f
            box(w * 0.05f, fy, w * 0.9f, fh)
            for (i in 0 until 12) box(w * 0.07f + i * w * 0.073f, fy + 5f, 8f, 8f, fill = true)
            for (i in 0 until 12) box(w * 0.07f + i * w * 0.073f, fy + fh - 13f, 8f, 8f, fill = true)
        }
        Art.SETUP -> {
            box(w * 0.1f, h * 0.1f, w * 0.14f, h * 0.8f)
            box(w * 0.13f, h * 0.15f, 12f, 12f, fill = true)
            line(w * 0.17f, h * 0.9f, w * 0.17f, h, 6f)
            line(w * 0.24f, h * 0.18f, w * 0.62f, h * 0.38f, 3f)
            line(w * 0.24f, h * 0.18f, w * 0.62f, h * 0.62f, 3f)
            box(w * 0.62f, h * 0.3f, w * 0.28f, h * 0.4f)
            box(w * 0.68f, h * 0.4f, w * 0.16f, h * 0.2f, fill = true, c = Color.Gray)
        }
        Art.FLIP -> {
            gliderGrid(w * 0.1f, true)
            line(w * 0.45f, h * 0.5f, w * 0.55f, h * 0.5f, 6f)
            line(w * 0.55f, h * 0.5f, w * 0.52f, h * 0.42f, 6f)
            line(w * 0.55f, h * 0.5f, w * 0.52f, h * 0.58f, 6f)
            gliderGrid(w * 0.65f, false)
        }
        Art.CAPTURE -> {
            box(w * 0.2f, h * 0.3f, w * 0.6f, h * 0.4f)
            drawCircle(white, h * 0.1f, Offset(w * 0.5f, h * 0.5f), style = stroke)
            box(w * 0.42f, h * 0.2f, w * 0.16f, h * 0.1f, fill = true)
            box(w * 0.85f, h * 0.2f, 10f, 10f, fill = true)
        }
        Art.FILES -> {
            box(w * 0.08f, h * 0.2f, w * 0.12f, h * 0.6f)
            line(w * 0.2f, h * 0.5f, w * 0.55f, h * 0.5f)
            box(w * 0.55f, h * 0.2f, w * 0.35f, h * 0.45f)
            box(w * 0.68f, h * 0.65f, w * 0.1f, h * 0.1f, fill = true)
            box(w * 0.6f, h * 0.75f, w * 0.26f, 6f, fill = true)
            box(w * 0.65f, h * 0.3f, 12f, 12f, fill = true)
            box(w * 0.75f, h * 0.42f, 12f, 12f, fill = true)
        }
        Art.PAPER -> {
            box(w * 0.2f, h * 0.1f, w * 0.4f, h * 0.8f)
            for (i in 0 until 5) line(w * 0.24f, h * (0.25f + i * 0.13f), w * 0.56f, h * (0.25f + i * 0.13f), 2f)
            line(w * 0.85f, h * 0.1f, w * 0.65f, h * 0.55f, 8f)
            box(w * 0.6f, h * 0.55f, w * 0.08f, h * 0.15f, fill = true)
        }
        Art.MIX -> {
            box(w * 0.15f, h * 0.35f, w * 0.14f, h * 0.55f)
            box(w * 0.19f, h * 0.15f, w * 0.06f, h * 0.2f)
            drawCircle(white, 8f, Offset(w * 0.45f, h * 0.3f))
            drawCircle(white, 8f, Offset(w * 0.5f, h * 0.45f))
            drawArc(white, 0f, 180f, false, Offset(w * 0.55f, h * 0.35f), Size(w * 0.3f, h * 0.5f), style = stroke)
            line(w * 0.55f, h * 0.6f, w * 0.85f, h * 0.6f)
        }
        Art.CLOCK -> {
            val c = Offset(w * 0.5f, h * 0.5f)
            val r = h * 0.4f
            drawCircle(white, r, c, style = stroke)
            line(c.x, c.y, c.x, c.y - r * 0.7f, 6f)
            line(c.x, c.y, c.x + r * 0.5f, c.y, 6f)
            for (i in 0 until 12) {
                val a = Math.toRadians(i * 30.0)
                val x = c.x + (r * 0.88f) * Math.sin(a).toFloat()
                val y = c.y - (r * 0.88f) * Math.cos(a).toFloat()
                box(x - 3, y - 3, 6f, 6f, fill = true)
            }
        }
        Art.TONEGATIVE -> {
            gliderGrid(w * 0.1f, false)
            line(w * 0.45f, h * 0.5f, w * 0.55f, h * 0.5f, 6f)
            line(w * 0.55f, h * 0.5f, w * 0.52f, h * 0.42f, 6f)
            line(w * 0.55f, h * 0.5f, w * 0.52f, h * 0.58f, 6f)
            gliderGrid(w * 0.65f, true)
        }
        Art.TABLET_FRAME -> {
            box(w * 0.2f, h * 0.35f, w * 0.6f, h * 0.55f)
            box(w * 0.25f, h * 0.45f, w * 0.5f, h * 0.4f, fill = true, c = Color.DarkGray)
            box(w * 0.15f, h * 0.1f, w * 0.7f, h * 0.22f)
            box(w * 0.18f, h * 0.13f, w * 0.64f, h * 0.16f, fill = true, c = Color.Gray)
            for (i in 0 until 5) line(w * (0.3f + i * 0.1f), h * 0.33f, w * (0.3f + i * 0.1f), h * 0.43f, 2f)
        }
        Art.PROJECTOR -> {
            box(w * 0.08f, h * 0.35f, w * 0.2f, h * 0.3f)
            drawCircle(white, h * 0.08f, Offset(w * 0.28f, h * 0.5f), style = stroke)
            line(w * 0.3f, h * 0.45f, w * 0.7f, h * 0.2f, 3f)
            line(w * 0.3f, h * 0.55f, w * 0.7f, h * 0.8f, 3f)
            box(w * 0.7f, h * 0.2f, w * 0.2f, h * 0.6f)
            line(w * 0.93f, h * 0.2f, w * 0.93f, h * 0.8f, 3f)
        }
        Art.WATER -> {
            box(w * 0.15f, h * 0.4f, w * 0.7f, h * 0.45f)
            box(w * 0.25f, h * 0.55f, w * 0.5f, h * 0.08f, fill = true)
            for (i in 0 until 4) {
                val x = w * (0.2f + i * 0.17f)
                drawArc(white, 180f, 180f, false, Offset(x, h * 0.4f), Size(w * 0.085f, h * 0.1f), style = Stroke(3f))
            }
            box(w * 0.46f, h * 0.1f, 10f, 10f, fill = true)
        }
        Art.SAFELIGHT -> {
            line(w * 0.5f, 0f, w * 0.5f, h * 0.2f, 4f)
            box(w * 0.4f, h * 0.2f, w * 0.2f, h * 0.2f)
            box(w * 0.43f, h * 0.24f, w * 0.14f, h * 0.12f, fill = true, c = Color(0xFFB00000))
            for (i in 0 until 5) {
                val x = w * (0.3f + i * 0.1f)
                line(w * 0.5f, h * 0.4f, x, h * 0.9f, 2f)
            }
        }
        Art.SWITCHES -> {
            for (i in 0 until 2) {
                val y = h * (0.15f + i * 0.4f)
                val on = i == 0
                box(w * 0.3f, y, w * 0.4f, h * 0.25f)
                val tx = if (on) w * 0.5f else w * 0.32f
                box(tx, y + 4f, w * 0.18f, h * 0.25f - 8f, fill = on)
            }
        }
        Art.TIMELINE -> {
            box(w * 0.05f, h * 0.35f, w * 0.25f, h * 0.3f, fill = true, c = Color.DarkGray)
            box(w * 0.30f, h * 0.35f, w * 0.4f, h * 0.3f, fill = true)
            box(w * 0.70f, h * 0.35f, w * 0.25f, h * 0.3f, fill = true, c = Color.DarkGray)
            line(w * 0.05f, h * 0.8f, w * 0.95f, h * 0.8f, 3f)
            for (x in listOf(0.05f, 0.3f, 0.7f, 0.95f)) line(w * x, h * 0.72f, w * x, h * 0.88f, 3f)
        }
        Art.STRIPS -> {
            box(w * 0.2f, h * 0.1f, w * 0.6f, h * 0.8f)
            for (i in 0 until 6) {
                val shade = 0.2f + i * 0.15f
                box(w * 0.2f + i * w * 0.1f, h * 0.1f, w * 0.1f, h * 0.8f, fill = true, c = Color(shade, shade, shade))
            }
        }
        Art.MULTI -> {
            box(w * 0.2f, h * 0.05f, w * 0.6f, h * 0.9f)
            for (r in 0 until 2) for (c in 0 until 3) {
                val shade = 0.25f + (r * 3 + c) * 0.12f
                box(w * (0.24f + c * 0.19f), h * (0.12f + r * 0.42f), w * 0.15f, h * 0.35f, fill = true, c = Color(shade, shade, shade))
            }
        }
        Art.TRAYS -> {
            for (i in 0 until 3) {
                val x = w * (0.06f + i * 0.31f)
                box(x, h * 0.45f, w * 0.27f, h * 0.4f)
                box(x + 6f, h * 0.55f, w * 0.27f - 12f, h * 0.06f, fill = true)
                box(x + w * 0.11f, h * 0.2f, 10f, 10f, fill = true)
            }
        }
        Art.DRYING -> {
            line(w * 0.1f, h * 0.15f, w * 0.9f, h * 0.15f, 4f)
            for (i in 0 until 3) {
                val x = w * (0.2f + i * 0.25f)
                box(x, h * 0.15f, w * 0.15f, h * 0.65f)
                box(x + w * 0.06f, h * 0.1f, 8f, 18f, fill = true)
            }
        }
    }
}
