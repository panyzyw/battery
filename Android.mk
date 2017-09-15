# Copyright (C) 2010 iFlytek
# 整个工程

LOCAL_PATH:= $(call my-dir)

#Build .so  
#include $(CLEAR_VARS)
#LOCAL_MODULE := libmsc
#ifeq ($(TARGET_ARCH),arm)
#	LOCAL_SRC_FILES_32 := libs/armeabi/libmsc.so
#	LOCAL_MULTILIB := 32
#else ifeq ($(TARGET_ARCH),arm64)
#	LOCAL_SRC_FILES_64 := libs/arm64-v8a/libmsc.so
#	LOCAL_MULTILIB := 64
#endif
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)


#Build apk
include $(CLEAR_VARS)
LOCAL_STATIC_JAVA_LIBRARIES:=android-support-v4 battery_jar1 battery_jar2 battery_jar3  battery_jar4
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files) 
LOCAL_PACKAGE_NAME := YYDRobotVoiceBattery
LOCAL_CERTIFICATE := platform

LOCAL_JNI_SHARED_LIBRARIES += libmsc

ifeq ($(TARGET_ARCH),arm64)
	LOCAL_MULTILIB := 64
else ifeq ($(TARGET_ARCH),arm)
	LOCAL_MULTILIB := 32
endif

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := battery_jar1:libs/Sunflower.jar battery_jar2:libs/Msc.jar battery_jar3:libs/gson-2.2.4.jar battery_jar4:libs/Speach_3.jar

include $(BUILD_MULTI_PREBUILT)

