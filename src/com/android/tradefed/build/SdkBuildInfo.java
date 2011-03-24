/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.android.tradefed.build;

import com.android.tradefed.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of a {@link ISdkBuildInfo}
 */
public class SdkBuildInfo extends BuildInfo implements ISdkBuildInfo {

    private File mAdtDir = null;
    private File mSdkDir = null;

    /**
     * Creates a {@link SdkBuildInfo} using default attribute values.
     */
    public SdkBuildInfo() {
    }

    /**
     * Creates a {@link SdkBuildInfo}
     *
     * @param buildId the build id
     * @param testTarget the test target name
     * @param buildName the build name
     */
    public SdkBuildInfo(int buildId, String testTarget, String buildName) {
        super(buildId, testTarget, buildName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSdkDir() {
        return mSdkDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getAdtDir() {
        return mAdtDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdtDir(File adtDir) {
        mAdtDir  = adtDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSdkDir(File sdkDir) {
        mSdkDir = sdkDir;
    }

    @Override
    public void cleanUp() {
        if (mSdkDir != null) {
            FileUtil.recursiveDelete(mSdkDir);
        }
        if (mAdtDir != null) {
            FileUtil.recursiveDelete(mAdtDir);
        }
        mSdkDir = null;
        mAdtDir = null;
    }

    @Override
    public IBuildInfo clone() {
        SdkBuildInfo cloneBuild = new SdkBuildInfo(getBuildId(), getTestTarget(), getBuildName());
        cloneBuild.addAllBuildAttributes(getAttributesMultiMap());
        try {
            File cloneAdtDir = null;
            if (getAdtDir() != null) {
                cloneAdtDir = FileUtil.createTempDir("cloneAdt");
                FileUtil.recursiveCopy(getAdtDir(), cloneAdtDir);
                cloneBuild.setAdtDir(cloneAdtDir);
            }
            File cloneSdkDir = null;
            if (getSdkDir() != null) {
                cloneSdkDir = FileUtil.createTempDir("cloneSdk");
                FileUtil.recursiveCopy(getSdkDir(), cloneSdkDir);
                cloneBuild.setSdkDir(cloneSdkDir);
            }
            return cloneBuild;
        } catch (IOException e) {
            throw new RuntimeException("Could not clone sdk build", e);
        }
    }
}