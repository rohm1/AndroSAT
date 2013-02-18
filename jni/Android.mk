LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cc
LOCAL_CFLAGS    := -fexceptions
#~ LOCAL_CFLAGS    := -fexceptions -Wwrite-strings -Wno-psabi

LOCAL_MODULE    := minisat
LOCAL_SRC_FILES := core/Main.cc \
		core/Launch.cc \
		core/Solver.cc \
		utils/Options.cc \
		utils/System.cc

LOCAL_LDLIBS    := -lm

include $(BUILD_SHARED_LIBRARY)
