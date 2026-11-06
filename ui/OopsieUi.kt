package com.anonymousbeta.oopsie.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.anonymousbeta.oopsie.core.Hunter
import com.anonymousbeta.oopsie.core.Kind
import com.anonymousbeta.oopsie.core.Perm
import com.anonymousbeta.oopsie.core.WatchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun App() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var report by remember { mutableStateOf<Hunter.Report?>(null) }
    var busy by remember { mutableStateOf(false) }
    var granted by remember { mutableStateOf(Perm.hasAllFiles(ctx)) }
    var watching by remember { mutableStateOf(WatchService.running) }
    var showDiag by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = Perm.hasAllFiles(ctx)
                watching = WatchService.running
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scan: () -> Unit = {
        if (!Perm.hasAllFiles(ctx)) {
            Perm.requestAllFiles(ctx)
        } else if (!busy) {
            scope.launch {
                busy = true
                val r = withContext(Dispatchers.IO) { Hunter.sweep(copy = true) }
                report = r
                busy = false
            }
        }
    }

    val toggleWatch: (Boolean) -> Unit = { on ->
        watching = on
        val intent = Intent(ctx, WatchService::class.java)
        if (on) ctx.startForegroundService(intent) else ctx.stopService(intent)
    }

    val hits = report?.hits.orEmpty()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().background(Cream),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Oopsie",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Text(
                    "by Anonymous-beta",
                    fontSize = 12.sp,
                    color = Ink.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(14.dp))
                Mascot()
                Spacer(Modifier.height(14.dp))
            }
        }

        item(span = { GridItemSpan(3) }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (report == null) "nothing grabbed yet"
                    else "${hits.size} caught · ${report!!.copied} saved",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                if (report != null) {
                    Text(
                        "scanned ${report!!.dirsScanned} folders · ${report!!.millis} ms",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.45f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = scan,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    shape = RoundedCornerShape(31.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Bubblegum)
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            if (granted) "Grab 'em!" else "Give me access first",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(3) }) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Live watch", fontWeight = FontWeight.Bold, color = Ink, fontSize = 15.sp)
                    Text(
                        "grabs new hits the moment they land",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.45f)
                    )
                }
                Switch(
                    checked = watching,
                    onCheckedChange = toggleWatch,
                    colors = SwitchDefaults.colors(checkedTrackColor = Mint)
                )
            }
        }

        if (report != null) {
            item(span = { GridItemSpan(3) }) {
                Text(
                    "caught",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }
            items(hits.size) { index ->
                HitCell(hits[index])
            }
        }

        item(span = { GridItemSpan(3) }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                TextButton(onClick = { showDiag = !showDiag }) {
                    Text(
                        if (showDiag) "hide diagnostics" else "diagnostics",
                        color = Ink.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
                if (showDiag && report != null) {
                    val r = report!!
                    DiagLine("roots", r.roots.joinToString("\n").ifEmpty { "none found" })
                    DiagLine("folders scanned", r.dirsScanned.toString())
                    DiagLine("files seen", r.filesSeen.toString())
                    DiagLine(
                        "by kind",
                        r.hits.groupingBy { it.kind.name }.eachCount()
                            .entries.joinToString(" · ") { "${it.key.lowercase()}=${it.value}" }
                    )
                    DiagLine("output", Hunter.outputDir().absolutePath)
                    if (r.errors.isNotEmpty()) {
                        DiagLine("errors", r.errors.take(6).joinToString("\n"))
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Text(
                    "Made by Anonymous-beta · works on your own device, on your own chats.",
                    fontSize = 10.sp,
                    color = Ink.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun DiagLine(label: String, value: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.5f))
        Text(value, fontSize = 11.sp, color = Ink.copy(alpha = 0.75f))
    }
}

@Composable
private fun HitCell(hit: Hunter.Hit) {
    val path = hit.copiedTo ?: hit.sourcePath
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(Soft),
        contentAlignment = Alignment.Center
    ) {
        if (hit.kind == Kind.IMAGE) {
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = when (hit.kind) {
                        Kind.VIDEO -> Icons.Rounded.PlayArrow
                        Kind.AUDIO -> Icons.Rounded.GraphicEq
                        Kind.LOCKED -> Icons.Rounded.Lock
                        else -> Icons.Rounded.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = Ink.copy(alpha = 0.55f)
                )
                Text(
                    hit.kind.name.lowercase(),
                    fontSize = 10.sp,
                    color = Ink.copy(alpha = 0.45f)
                )
            }
        }
    }
}
