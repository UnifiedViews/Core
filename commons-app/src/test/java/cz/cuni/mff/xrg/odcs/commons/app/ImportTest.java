package cz.cuni.mff.xrg.odcs.commons.app;


import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.*;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ImportTest {

    @Test
    public void ExportTest() throws IOException, ImportException, ExportException, URISyntaxException {
        ImportService importService = new ImportService();
        Path tmpPath = Files.createTempDirectory("dir");
        File tmpDir = tmpPath.toFile();

        URL zipResource = ImportTest.class.getResource(
                "/pipeline.zip");

        File zipFile = new File(zipResource.toURI());
        ZipCommons.unpack(zipFile, tmpDir);
        Pipeline pipeline = importService.loadPipeline(tmpDir);
        ExportService exportService = new ExportService();
        ExportService exportServiceMock = spy(exportService);
        TreeSet<DpuItem> usedDpus = exportService.getDpusInformation(pipeline);
        Assert.assertEquals(usedDpus.size(), 2);

        File tmpTarget = File.createTempFile("temp", ".tmp");
        ExportSetting setting = new ExportSetting(false, false, false);
        AuthenticationContext authMock = mock(AuthenticationContext.class);
        User userMock = new User();
        when(authMock.getUser()).thenReturn(userMock);
        exportServiceMock.exportPipeline(pipeline, tmpTarget, setting, authMock);


    }

    @Test
    public void ImportTest() throws IOException, ImportException, ExportException, URISyntaxException {
        ImportService importService = new ImportService();
        Path tmpPath = Files.createTempDirectory("dir");
        File tmpDir = tmpPath.toFile();

        URL zipResource = ImportTest.class.getResource(
                "/pipelineWithUsedDpus.zip");

        File zipFile = new File(zipResource.toURI());
        ZipCommons.unpack(zipFile, tmpDir);
        List<DpuItem> result = importService.loadUsedDpus(tmpDir);
        Assert.assertEquals(result.size(), 2);
    }

}

