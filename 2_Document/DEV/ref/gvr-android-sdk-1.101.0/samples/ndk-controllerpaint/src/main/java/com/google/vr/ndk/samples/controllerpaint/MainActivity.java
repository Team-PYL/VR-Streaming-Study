/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vr.ndk.samples.controllerpaint;

import android.app.Activity;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.ndk.base.GvrLayout;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A Google VR NDK sample application.
 *
 * <p>This app is a "paint program" that allows the user to paint in virtual space using the
 * controller. A cursor shows where the controller is pointing at. Touching or clicking the touchpad
 * begins drawing. Then, as the user moves their hand, lines are drawn. The user can switch the
 * drawing color by swiping to the right or left on the touchpad. The user can also change the
 * drawing stroke width by moving their finger up and down on the touchpad.
 *
 * <p>This is the main Activity for the sample application. It initializes a GLSurfaceView to allow
 * rendering, a GvrLayout for GVR API access, and forwards relevant events to the native demo app
 * instance where rendering and interaction are handled.
 */
public class MainActivity extends Activity {
  private static final String TAG = "MainActivity";

  static {
    // Load our JNI code.
    System.loadLibrary("controllerpaint_jni");
  }

  // Opaque native pointer to the DemoApp C++ object.
  // This object is owned by the MainActivity instance and passed to the native methods.
  private long nativeControllerPaint;

  private GvrLayout gvrLayout;
  private GLSurfaceView surfaceView;
  private AssetManager assetManager;

  // Note that pause and resume signals to the native app are performed on the GL thread, ensuring
  // thread-safety.
  private final Runnable pauseNativeRunnable =
      new Runnable() {
        @Override
        public void run() {
          nativeOnPause(nativeControllerPaint);
        }
      };

  private final Runnable resumeNativeRunnable =
      new Runnable() {
        @Override
        public void run() {
          nativeOnResume(nativeControllerPaint);
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setImmersiveSticky();
    getWindow()
        .getDecorView()
        .setOnSystemUiVisibilityChangeListener(
            new View.OnSystemUiVisibilityChangeListener() {
              @Override
              public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                  setImmersiveSticky();
                }
              }
            });

    // Enable VR mode, if the device supports it.
    AndroidCompat.setVrModeEnabled(this, true);

    // Get the GvrLayout.
    gvrLayout = new GvrLayout(this);

    // Enable async reprojection, if possible.
    if (gvrLayout.setAsyncReprojectionEnabled(true)) {
      Log.d(TAG, "Successfully enabled async reprojection.");
      // Async reprojection decouples the app framerate from the display framerate,
      // allowing immersive interaction even at the throttled clockrates set by
      // sustained performance mode.
      AndroidCompat.setSustainedPerformanceMode(this, true);
    } else {
      Log.w(TAG, "Failed to enable async reprojection.");
    }

    // Configure the GLSurfaceView.
    surfaceView = new GLSurfaceView(this);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 0, 0, 0);
    surfaceView.setRenderer(renderer);

    // Note that we are not setting setPreserveEGLContextOnPause(true) here,
    // even though it is recommended.  This is done so that we have at least
    // one demo that provides some testing coverage for no-preserve contexts.

    // Set the GLSurfaceView as the GvrLayout's presentation view.
    gvrLayout.setPresentationView(surfaceView);

    // Add the GvrLayout to the View hierarchy.
    setContentView(gvrLayout);

    assetManager = getResources().getAssets();

    nativeControllerPaint =
        nativeOnCreate(assetManager, gvrLayout.getGvrApi().getNativeGvrContext());

    // Prevent screen from dimming/locking.
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Destruction order is important; shutting down the GvrLayout will detach
    // the GLSurfaceView and stop the GL thread, allowing safe shutdown of
    // native resources from the UI thread.
    gvrLayout.shutdown();
    nativeOnDestroy(nativeControllerPaint);
    nativeControllerPaint = 0;
  }

  @Override
  protected void onPause() {
    surfaceView.queueEvent(pauseNativeRunnable);
    surfaceView.onPause();
    gvrLayout.onPause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    gvrLayout.onResume();
    surfaceView.onResume();
    surfaceView.queueEvent(resumeNativeRunnable);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    gvrLayout.onBackPressed();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      setImmersiveSticky();
    }
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Avoid accidental volume key presses while the phone is in the VR headset.
    if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
        || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  private void setImmersiveSticky() {
    getWindow()
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }

  private final GLSurfaceView.Renderer renderer =
      new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
          nativeOnSurfaceCreated(nativeControllerPaint);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
          nativeOnSurfaceChanged(width, height, nativeControllerPaint);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
          nativeOnDrawFrame(nativeControllerPaint);
        }
      };

  private native long nativeOnCreate(AssetManager assetManager, long gvrContextPtr);
  private native void nativeOnDestroy(long controllerPaintJptr);
  private native void nativeOnResume(long controllerPaintJptr);
  private native void nativeOnPause(long controllerPaintJptr);
  private native void nativeOnSurfaceCreated(long controllerPaintJptr);
  private native void nativeOnSurfaceChanged(int width, int height, long controllerPaintJptr);
  private native void nativeOnDrawFrame(long controllerPaintJptr);
}
