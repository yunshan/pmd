/*
 * Created on 14 avr. 2005
 * 
 * Copyright (c) 2005, PMD for Eclipse Development Team All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. * The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgement: "This product includes software developed in part by
 * support from the Defense Advanced Research Project Agency (DARPA)" *
 * Neither the name of "PMD for Eclipse Development Team" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.InputStream;

import name.herlin.command.CommandException;
import name.herlin.command.UnsetInputPropertiesException;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.renderers.HTMLRenderer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the report rendering
 * 
 * @author Philippe Herlin
 * 
 */
@Ignore("doesn't work - workspace is closed")
public class RenderReportCmdTest {
  private IProject testProject;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Before
  public void setUp() throws Exception {

    // 1. Create a Java project
    this.testProject = EclipseUtils.createJavaProject("PMDTestProject");
    Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
        this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

    // 2. Create a test source file inside that project
    EclipseUtils.createTestSourceFile(this.testProject);
    final InputStream is = EclipseUtils.getResourceStream(this.testProject, "/Test.java");
    Assert.assertNotNull("Cannot find the test source file", is);
    is.close();

  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @After
  public void tearDown() throws Exception {
    if (this.testProject != null) {
      if (this.testProject.exists() && this.testProject.isAccessible()) {
        EclipseUtils.removePMDNature(this.testProject);
        // this.testProject.refreshLocal(IResource.DEPTH_INFINITE,
        // null);
        // Thread.sleep(500);
        // this.testProject.delete(true, true, null);
        // this.testProject = null;
      }
    }
  }

  /**
   * Test the basic usage of the report rendering command
   * 
   */
  @Test
  public void testRenderReportCmdBasic() throws CommandException, CoreException {
    final ReviewCodeCmd reviewCmd = new ReviewCodeCmd();
    reviewCmd.addResource(this.testProject);
    reviewCmd.performExecute();

    final RenderReportsCmd cmd = new RenderReportsCmd();
    cmd.setProject(this.testProject);
    cmd.registerRenderer(new HTMLRenderer(), PMDRuntimeConstants.HTML_REPORT_NAME);
    cmd.performExecute();
    cmd.join();

    final IFolder reportFolder = this.testProject.getFolder(PMDRuntimeConstants.REPORT_FOLDER);
    Assert.assertTrue(reportFolder.exists());

    final IFile reportFile = reportFolder.getFile(PMDRuntimeConstants.HTML_REPORT_NAME);
    Assert.assertTrue(reportFile.exists());

    this.testProject.deleteMarkers(PMDRuntimeConstants.PMD_MARKER, true, IResource.DEPTH_INFINITE);

    if (reportFile.exists()) {
      reportFile.delete(true, false, null);
    }

    if (reportFolder.exists()) {
      reportFolder.delete(true, false, null);
    }
  }

  /**
   * Test robustness #1
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg1() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(null);
      cmd.registerRenderer(new HTMLRenderer(), PMDRuntimeConstants.HTML_REPORT_NAME);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }

  /**
   * Test robustness #2
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg2() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(this.testProject);
      cmd.registerRenderer(null, PMDRuntimeConstants.HTML_REPORT_NAME);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }

  /**
   * Test robustness #3
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg3() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(this.testProject);
      cmd.registerRenderer(new HTMLRenderer(), null);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }

  /**
   * Test robustness #4
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg4() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(null);
      cmd.registerRenderer(null, PMDRuntimeConstants.HTML_REPORT_NAME);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }

  /**
   * Test robustness #5
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg5() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(null);
      cmd.registerRenderer(new HTMLRenderer(), null);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }

  /**
   * Test robustness #6
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg6() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(this.testProject);
      cmd.registerRenderer(null, null);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }

  /**
   * Test robustness #7
   * 
   * @throws CommandException
   */
  @Test
  public void testRenderReportCmdNullArg7() throws CommandException {
    try {
      final RenderReportsCmd cmd = new RenderReportsCmd();
      cmd.setProject(null);
      cmd.registerRenderer(null, null);
      cmd.performExecute();
      Assert.fail();
    }
    catch (final UnsetInputPropertiesException e) {
      // yes cool
    }
  }
}
