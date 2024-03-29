package com.muen.hitbricks.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.view.MotionEvent;

import com.muen.hitbricks.control.GameState;
import com.muen.hitbricks.util.TextResources;


/**
 * View object for the GL surface.  Wraps the renderer.
 */
public class GameSurfaceView extends GLSurfaceView {

    private GameSurfaceRenderer mRenderer;

    private final ConditionVariable syncObj = new ConditionVariable();

    /**
     * Prepares the OpenGL context and starts the Renderer thread.
     */
    public GameSurfaceView(Context context, GameState gameState,
            TextResources.Configuration textConfig) {

        super(context);

        setEGLContextClientVersion(2);      // Request OpenGL ES 2.0

        // Create our Renderer object, and tell the GLSurfaceView code about it.  This also
        // starts the renderer thread, which will be calling the various callback methods
        // in the GameSurfaceRenderer class.
        mRenderer = new GameSurfaceRenderer(gameState, this, textConfig);

        setRenderer(mRenderer);
    }

    @Override
    public void onPause() {
        /*
         * We call a "pause" function in our Renderer class, which tells it to save state and
         * go to sleep.  Because it's running in the Renderer thread, we call it through
         * queueEvent(), which doesn't wait for the code to actually execute.  In theory the
         * application could be killed shortly after we return from here, which would be bad if
         * it happened while the Renderer thread was still saving off important state.  We need
         * to wait for it to finish.
         */

        super.onPause();

        syncObj.close();

        queueEvent(() -> mRenderer.onViewPause(syncObj));

        syncObj.block();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        /*
         * Forward touch events to the game loop.  We don't want to call Renderer methods
         * directly, because they manipulate state that is "owned" by a different thread.  We
         * use the GLSurfaceView queueEvent() function to execute it there.
         *
         * This increases the latency of our touch response slightly, but it shouldn't be
         * noticeable.
         */
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                final float x, y;
                x = e.getX();
                y = e.getY();
                //Log.d(TAG, "GameSurfaceView onTouchEvent x=" + x + " y=" + y);
                queueEvent(() -> mRenderer.touchEvent(x, y));
                break;

            default:
                break;
        }

        return true;
    }
}
