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

#ifndef CONTROLLER_PAINT_APP_SRC_MAIN_JNI_APP_JNI_H_  // NOLINT
#define CONTROLLER_PAINT_APP_SRC_MAIN_JNI_APP_JNI_H_

#include <jni.h>
#include <stdint.h>

#define NATIVE_METHOD(return_type, method_name) \
  JNIEXPORT return_type JNICALL                 \
      Java_com_google_vr_ndk_samples_controllerpaint_MainActivity_##method_name

extern "C" {

NATIVE_METHOD(jlong, nativeOnCreate)
(JNIEnv* env, jobject obj, jobject asset_mgr, jlong gvrContextPtr);
NATIVE_METHOD(void, nativeOnResume)
(JNIEnv* env, jobject obj, jlong controller_paint_jptr);
NATIVE_METHOD(void, nativeOnPause)
(JNIEnv* env, jobject obj, jlong controller_paint_jptr);
NATIVE_METHOD(void, nativeOnSurfaceCreated)
(JNIEnv* env, jobject obj, jlong controller_paint_jptr);
NATIVE_METHOD(void, nativeOnSurfaceChanged)
(JNIEnv* env, jobject obj, jint width, jint height,
 jlong controller_paint_jptr);
NATIVE_METHOD(void, nativeOnDrawFrame)
(JNIEnv* env, jobject obj, jlong controller_paint_jptr);
NATIVE_METHOD(void, nativeOnDestroy)
(JNIEnv* env, jobject obj, jlong controller_paint_jptr);
}

#endif  // CONTROLLER_PAINT_APP_SRC_MAIN_JNI_APP_JNI_H_  // NOLINT
