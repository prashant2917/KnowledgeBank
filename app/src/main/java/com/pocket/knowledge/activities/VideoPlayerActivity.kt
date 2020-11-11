package com.pocket.knowledge.activities

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.pocket.knowledge.R
import kotlinx.android.synthetic.main.activity_video_player.*

class VideoPlayerActivity : AppCompatActivity() {
    private var videoUrl: String? = null
    private var player: SimpleExoPlayer? = null
    private var mediaDataSourceFactory: DataSource.Factory? = null
    private var mainHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_video_player)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        videoUrl = intent.getStringExtra("video_url")
        mediaDataSourceFactory = buildDataSourceFactory(true)
        mainHandler = Handler()
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val renderersFactory: RenderersFactory = DefaultRenderersFactory(this)
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val loadControl: LoadControl = DefaultLoadControl()
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl)

        exoPlayerView.player = player
        exoPlayerView.useController = true
        exoPlayerView.requestFocus()
        val uri = Uri.parse(videoUrl)
        val mediaSource = buildMediaSource(uri, null)
        player!!.prepare(mediaSource)
        player!!.playWhenReady = true
        player!!.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
                Log.d(TAG, "onTimelineChanged: ")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
                Log.d(TAG, "onTracksChanged: " + trackGroups.length)
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.d(TAG, "onLoadingChanged: $isLoading")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Log.d(TAG, "onPlayerStateChanged: $playWhenReady")
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException) {
                Log.e(TAG, "onPlayerError: ", error)
                player!!.stop()
                errorDialog()
            }

            override fun onPositionDiscontinuity(reason: Int) {
                Log.d(TAG, "onPositionDiscontinuity: true")
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })
        Log.d("INFO", "ActivityVideoPlayer")
    }

    private fun buildMediaSource(uri: Uri, overrideExtension: String?): MediaSource {
        return when (val type = if (TextUtils.isEmpty(overrideExtension)) Util.inferContentType(uri) else Util.inferContentType(".$overrideExtension")) {
            C.TYPE_SS -> SsMediaSource.Factory(DefaultSsChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(uri)
            C.TYPE_DASH -> DashMediaSource.Factory(DefaultDashChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri)
            C.TYPE_OTHER -> ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    private fun buildDataSourceFactory(useBandwidthMeter: Boolean): DataSource.Factory {
        return buildDataSourceFactory(if (useBandwidthMeter) BANDWIDTH_METER else null)
    }

    private fun buildDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
        return DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter))
    }

    private fun buildHttpDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayerDemo"), bandwidthMeter)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        player!!.stop()
    }

    fun errorDialog() {
        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Oops!")
                .setCancelable(false)
                .setMessage("Failed to load stream, probably the stream server currently down!")
                .setPositiveButton("Retry") { dialog, which -> retryLoad() }
                .setNegativeButton("No") { dialogInterface, i -> finish() }
                .show()
    }

    private fun retryLoad() {
        val uri = Uri.parse(videoUrl)
        val mediaSource = buildMediaSource(uri, null)
        player!!.prepare(mediaSource)
        player!!.playWhenReady = true
    }

    companion object {
        private const val TAG = "ActivityStreamPlayer"
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
    }
}