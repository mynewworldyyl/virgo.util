/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.util.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.io.ZipUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ZipUtilsTests {
    
    private static final int LONG_FILE_PATH_DEPTH = 10;

    private static final String A_LONG_FILE_PATH_DIRECTORY = "ALongFilePathDirectoryNameWhichGoesOnForeverAndEverNot";

    private final PathReference expectedZip = new PathReference("target/to-zip.zip");
    
    private final PathReference expectedDefaultUnzipped = new PathReference("target/to-zip");
    
    private final PathReference expectedSpecifiedUnzipped = new PathReference("target/zipped-to-here");
    
    private final PathReference expectedDefaultUnzippedFoo = new PathReference("target/to-zip/a/foo");
    
    private final PathReference expectedDefaultUnzippedBar = new PathReference("target/to-zip/a/b/bar");
    
    private final PathReference expectedDefaultPrefixedUnzippedFoo = new PathReference("target/to-zip/prefix/a/foo");
    
    private final PathReference expectedDefaultPrefixedUnzippedBar = new PathReference("target/to-zip/prefix/a/b/bar");
    
    private final PathReference expectedSpecifiedUnzippedFoo = new PathReference("target/zipped-to-here/a/foo");
    
    private final PathReference expectedSpecifiedUnzippedBar = new PathReference("target/zipped-to-here/a/b/bar");
    
    @Before
    public void before() {
        expectedZip.delete();
        expectedDefaultUnzipped.delete(true);
        expectedSpecifiedUnzipped.delete(true);
    }
    
    @Test
    public void zipToDirectory() throws IOException {
        PathReference toZip = new PathReference("src/test/resources/to-zip");
        PathReference destination = new PathReference("target");
        
        ZipUtils.zipTo(toZip, destination);
        
        Assert.assertTrue(expectedZip.exists());
        
        ZipUtils.unzipTo(expectedZip, destination);
        
        assertExistsAndContains(expectedDefaultUnzippedFoo, "Foo");
        assertExistsAndContains(expectedDefaultUnzippedBar, "Bar");
    }
    
    @Test
    public void zipWithPrefix() throws IOException {
        PathReference toZip = new PathReference("src/test/resources/to-zip");
        PathReference destination = new PathReference("target");
        
        ZipUtils.zipTo(toZip, destination, "prefix");
        
        Assert.assertTrue(expectedZip.exists());
        
        ZipUtils.unzipTo(expectedZip, destination);
        
        assertExistsAndContains(expectedDefaultPrefixedUnzippedFoo, "Foo");
        assertExistsAndContains(expectedDefaultPrefixedUnzippedBar, "Bar");
    }
    
    @Test
    public void zipToFile() throws IOException {
        PathReference toZip = new PathReference("src/test/resources/to-zip");
        PathReference destination = new PathReference("target/zipped-to-here.zip");
        destination.delete();
        
        ZipUtils.zipTo(toZip, destination);
        
        Assert.assertTrue(destination.exists());
        
        ZipUtils.unzipTo(destination, new PathReference("target"));
        
        assertExistsAndContains(expectedSpecifiedUnzippedFoo, "Foo");
        assertExistsAndContains(expectedSpecifiedUnzippedBar, "Bar");
    }
    
    @Test
    @Ignore("DMS-2397")
    public void unzipToLongFilePath() throws Exception {
        PathReference toZip = new PathReference("src/test/resources/to-zip");
        PathReference destination = new PathReference("target/zipped-to-here.zip");
        destination.delete();
        
        ZipUtils.zipTo(toZip, destination);
        
        Assert.assertTrue(destination.exists());
        
        PathReference longFileNameDir = new PathReference("target/longFilePathDir");
        longFileNameDir.delete(true);
        
        PathReference longFileNameTarget = longPathReference(longFileNameDir, LONG_FILE_PATH_DEPTH);
        longFileNameTarget.createDirectory();

        Assert.assertTrue("Deep target directory isn't created!", longFileNameTarget.exists());

        PathReference unzipFooPlace = longFileNameTarget.newChild("zipped-to-here").newChild("a").newChild("foo");
        PathReference unzipBarPlace = longFileNameTarget.newChild("zipped-to-here").newChild("a").newChild("b").newChild("bar");
        
        ZipUtils.unzipTo(destination, longFileNameTarget);

        assertExistsAndContains(unzipFooPlace, "Foo");
        assertExistsAndContains(unzipBarPlace, "Bar");

    }
    
    @Test
    @Ignore("DMS-2397")
    public void zipAndUnzipLongFilePath() throws Exception {
        PathReference filesToZip = new PathReference("src/test/resources/to-zip");

        PathReference deepDirsToZip = new PathReference("target/deepDirsToZip");
        deepDirsToZip.delete(true);
        
        //Create deep dirs to zip
        PathReference deepPath = longPathReference(deepDirsToZip, LONG_FILE_PATH_DEPTH);
        filesToZip.copy(deepPath.newChild("zipped-to-here"),true);
        
        PathReference destination = new PathReference("target/zipAndUnzipLongFilePath.zip");
        destination.delete();
        
        ZipUtils.zipTo(deepDirsToZip, destination);
        
        Assert.assertTrue(destination.exists());
        
        PathReference targetDir = new PathReference("target/zipAndUnzipLongFilePath");
        targetDir.delete(true);
        
        ZipUtils.unzipTo(destination, new PathReference("target"));

        PathReference longFileNameTarget = longPathReference(targetDir, LONG_FILE_PATH_DEPTH);
        PathReference unzipFooPlace = longFileNameTarget.newChild("zipped-to-here").newChild("a").newChild("foo");
        PathReference unzipBarPlace = longFileNameTarget.newChild("zipped-to-here").newChild("a").newChild("b").newChild("bar");
         
        assertExistsAndContains(unzipFooPlace, "Foo");
        assertExistsAndContains(unzipBarPlace, "Bar");
    }
    
    
    private static PathReference longPathReference(PathReference longRef, int depth) {
        for (int i=0; i<depth; ++i) {
           longRef = longRef.newChild(A_LONG_FILE_PATH_DIRECTORY); 
        }
        return longRef;
    }

    private static void assertExistsAndContains(PathReference file, String contents) throws IOException {
        Assert.assertTrue(file.exists());
        
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(file.toFile()));
            Assert.assertEquals(contents, reader.readLine());
            Assert.assertEquals(null, reader.readLine());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
