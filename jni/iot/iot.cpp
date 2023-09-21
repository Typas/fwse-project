/*
 * Copyright (C) 2008 The Android Open Source Project
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

#include <assert.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <jni.h>
extern "C" {
#include "bt_adapter.h"
}

// ----------------------------------------------------------------------------
/* TODO: fix class name */
static const char *const kClassName = "fwse/group/liao/Iot";

static jboolean iot_init(JNIEnv *env, jobject thiz) {
    if (bt_open() < 0)
        return JNI_FALSE;
    else
        return JNI_TRUE;
}

static jboolean iot_exit(JNIEnv *env, jobject thiz) {
    if (bt_close() < 0)
        return JNI_FALSE;
    else
        return JNI_TRUE;
}

static jboolean iot_join(JNIEnv *env, jobject thiz) {
    if (bt_join() < 0)
        return JNI_FALSE;
    else
        return JNI_TRUE;
}

static jboolean iot_leave(JNIEnv *env, jobject thiz) {
    if (bt_leave() < 0)
        return JNI_FALSE;
    else
        return JNI_TRUE;
}

static jboolean iot_recv(JNIEnv *env, jobject thiz, jbyteArray jbuffer) {
    ssize_t read_bytes = 0;
    jbyte *jbuf = env->GetByteArrayElements(jbuffer, NULL);
    char *buf = (char *)jbuf; /* XXX: need a proper way to modify buffer from java */
    size_t len = env->GetArrayLength(jbuffer);

    read_bytes = bt_read(buf, len);
    env->ReleaseByteArrayElements(jbuffer, jbuf, 0);

    if (read_bytes > 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static jboolean iot_send(JNIEnv *env, jobject thiz, jbyteArray jbuffer) {
    ssize_t written_bytes = 0;
    jbyte *jbuf = env->GetByteArrayElements(jbuffer, NULL);
    char *buf = (char *)jbuf;
    size_t len = env->GetArrayLength(jbuffer);
    written_bytes = bt_write(buf, len);
    env->ReleaseByteArrayElements(jbuffer, jbuf, JNI_ABORT);

    if (written_bytes > 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

// ----------------------------------------------------------------------------

/*
 * Array of methods.
 *
 * Each entry has three fields: the name of the method, the method
 * signature, and a pointer to the native implementation.
 */
static const JNINativeMethod gMethods[] = {
    {"_init", "()Z", (void *)iot_init},   {"_exit", "()Z", (void *)iot_exit},
    {"_join", "()Z", (void *)iot_join},   {"_leave", "()Z", (void *)iot_leave},
    {"_recv", "([B)Z", (void *)iot_recv}, {"_send", "([B)Z", (void *)iot_send},
};

static int registerMethods(JNIEnv *env) {
    jclass clazz;

    /* look up the class */
    clazz = env->FindClass(kClassName);
    if (clazz == NULL) {
        return -1;
    }

    /* register all the methods */
    if (env->RegisterNatives(clazz, gMethods,
                             sizeof(gMethods) / sizeof(gMethods[0])) !=
        JNI_OK) {
        return -1;
    }

    /* fill out the rest of the ID cache */
    return 0;
}

// ----------------------------------------------------------------------------

/*
 * This is caliot by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    assert(env != NULL);

    if (registerMethods(env) != 0) {
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}
