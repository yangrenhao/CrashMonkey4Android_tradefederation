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

package com.android.tradefed.targetprep;

import com.android.ddmlib.FileListingService;
import com.android.tradefed.build.DeviceBuildInfo;
import com.android.tradefed.build.IDeviceBuildInfo;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.device.ITestDevice.RecoveryMode;
import com.android.tradefed.device.MockFileUtil;

import junit.framework.TestCase;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;

public class DefaultTestsZipInstallerTest extends TestCase {
    private static final String SKIP_THIS = "skipThis";

    private static final String TEST_STRING = "foo";

    private static final File SOME_PATH_1 = new File("/some/path/1");

    private static final File SOME_PATH_2 = new File("/some/path/2");

    private ITestDevice mMockDevice;
    private IDeviceBuildInfo mDeviceBuild;
    private DefaultTestsZipInstaller mZipInstaller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mZipInstaller = new DefaultTestsZipInstaller(SKIP_THIS) {
            @Override
            File[] getTestsZipDataFiles(File hostDir) {
                return new File[] { new File("foo") };
            }

            @Override
            Set<File> findDirs(File hostDir, File deviceRootPath) {
                Set<File> files = new HashSet<File>(2);
                files.add(SOME_PATH_1);
                files.add(SOME_PATH_2);
                return files;
            };
        };

        mMockDevice = EasyMock.createMock(ITestDevice.class);
        EasyMock.expect(mMockDevice.getSerialNumber()).andStubReturn(TEST_STRING);
        EasyMock.expect(mMockDevice.getProductType()).andStubReturn(TEST_STRING);
        EasyMock.expect(mMockDevice.getBuildId()).andStubReturn("1");
        mDeviceBuild = new DeviceBuildInfo("1", TEST_STRING, TEST_STRING);
    }

    /**
     * Exercise the core logic on a successful scenario.
     */
    public void testPushTestsZipOntoData() throws DeviceNotAvailableException, TargetSetupError {
        // mock a filesystem with these contents:
        // /data/app
        // /data/$SKIP_THIS
        MockFileUtil.setMockDirContents(
                mMockDevice, FileListingService.DIRECTORY_DATA, "app", SKIP_THIS);

        // expect initial android stop
        EasyMock.expect(mMockDevice.getSerialNumber()).andReturn("serial_number_stub").anyTimes();
        EasyMock.expect(mMockDevice.getRecoveryMode()).andReturn(RecoveryMode.AVAILABLE);
        mMockDevice.setRecoveryMode(RecoveryMode.ONLINE);
        EasyMock.expect(mMockDevice.executeShellCommand("stop")).andReturn("");

        // expect 'rm app' but not 'rm $SKIP_THIS'
        EasyMock.expect(mMockDevice.executeShellCommand(EasyMock.contains("rm -r data/app")))
                .andReturn("");

        mMockDevice.setRecoveryMode(RecoveryMode.AVAILABLE);

        EasyMock.expect(mMockDevice.syncFiles((File) EasyMock.anyObject(),
                EasyMock.contains(FileListingService.DIRECTORY_DATA)))
                .andReturn(Boolean.TRUE);

        EasyMock.expect(
                mMockDevice.executeShellCommand(EasyMock.startsWith("chown system.system "
                        + SOME_PATH_1.getPath()))).andReturn("");
        EasyMock.expect(
                mMockDevice.executeShellCommand(EasyMock.startsWith("chown system.system "
                        + SOME_PATH_2.getPath()))).andReturn("");

        EasyMock.replay(mMockDevice);
        mZipInstaller.pushTestsZipOntoData(mMockDevice, mDeviceBuild);
        EasyMock.verify(mMockDevice);
    }
}
