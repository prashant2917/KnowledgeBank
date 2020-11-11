package com.pocket.knowledge.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener
import com.pocket.knowledge.R
import kotlinx.android.synthetic.main.activity_youtube.*

class YoutubePlayerActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    private var playerStateChangeListener: MyPlayerStateChangeListener? = null
    private var strVideoId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_youtube)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val intent = intent
        if (null != intent) {
            strVideoId = intent.getStringExtra("video_id")
        }

        //loadViewed();

        youtube_view.initialize(getString(R.string.youtube_api_key), this)
        playerStateChangeListener = MyPlayerStateChangeListener()
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider, player: YouTubePlayer, wasRestored: Boolean) {
        player.setPlayerStateChangeListener(playerStateChangeListener)
        if (!wasRestored) {
            player.loadVideo(strVideoId)
        }
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider, errorReason: YouTubeInitializationResult) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show()
        } else {
            Toast.makeText(this, resources.getString(R.string.error_player), Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubePlayerProvider!!.initialize(getString(R.string.youtube_api_key), this)
        }
    }

    private val youTubePlayerProvider: YouTubePlayer.Provider?
        private get() = youtube_view

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private inner class MyPlayerStateChangeListener : PlayerStateChangeListener {
        override fun onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        override fun onLoaded(s: String) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
        }

        override fun onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        override fun onVideoStarted() {
            // Called when playback of the video starts.
        }

        override fun onVideoEnded() {
            // Called when the video reaches its end.
            //showInterstitialAd();
        }

        override fun onError(errorReason: YouTubePlayer.ErrorReason) {
            // Called when an error occurs.
        }
    }

    companion object {
        private const val RECOVERY_REQUEST = 1
    }
}