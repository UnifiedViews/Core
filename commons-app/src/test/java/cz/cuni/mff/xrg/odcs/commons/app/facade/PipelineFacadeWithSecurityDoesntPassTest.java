package cz.cuni.mff.xrg.odcs.commons.app.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUType;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbOpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.OpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Test suite for pipeline facade interface.
 * Each test is run in own transaction, which is rolled back in the end.
 * 
 * @author Jan Vojt
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context-security.xml" })
public class PipelineFacadeWithSecurityDoesntPassTest extends PipelineFacadeDoesntPassTest {
    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authManager;

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ScheduleFacade schedulerFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private DbOpenEvent openEventDao;

    @Before
    public void before() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userFacade.getUserByUsername("jdoe");
            TestingAuthenticationToken token = new TestingAuthenticationToken(user, user.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(token));
        }
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testGetPipelinesUsingDPU2() {
        super.testGetPipelinesUsingDPU2();
    }

    @Test
    @Transactional
    public void testGetPipelinesUsingDPU3() {
        DPUTemplateRecord dpu = new DPUTemplateRecord();
        dpu.setId(1L);
        dpu.setOwner(authCtx.getUser());

        List<Pipeline> pipes = pipelineFacade.getPipelinesUsingDPU(dpu);

        assertNotNull(pipes);
        assertEquals(1, pipes.size());
        assertEquals("Test 1", pipes.get(0).getName());
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testDelete_Pipeline() {
        super.testDelete_Pipeline();
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testDelete_Pipeline2() {
        System.out.println("delete");

        Pipeline pipeline = pipelineFacade.createPipeline();
        pipeline.setDescription("testDescription");
        pipeline.setGraph(new PipelineGraph());
        pipeline.setLastChange(new Date());
        pipeline.setName("testName");
        pipeline.setUser(userFacade.getUserByUsername("jdoe"));
        pipeline.setShareType(ShareType.PUBLIC_RO);
        pipeline.getConflicts().add(pipeline);
        pipeline.setUser(authCtx.getUser());

        pipelineFacade.save(pipeline);
        assertNotNull(pipeline.getId());
        Long id = pipeline.getId();

        em.flush();
        em.clear();

        pipelineFacade.delete(pipeline);

        // pipeline no longer exist, so we expect AccessDeniedException
        pipelineFacade.getPipeline(id);
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testCopyPipeline() {
        super.testCopyPipeline();
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testGetPipelinesUsingUnusedDPU() {
        super.testGetPipelinesUsingUnusedDPU();
    }

    @Test
    @Transactional
    public void testGetPipelinesUsingUnusedDPU2() {
        DPUTemplateRecord dpu = new DPUTemplateRecord();
        dpu.setId(2L);
        dpu.setOwner(authCtx.getUser());

        List<Pipeline> pipes = pipelineFacade.getPipelinesUsingDPU(dpu);

        assertNotNull(pipes);
        assertEquals(0, pipes.size());
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testDeepDeletePipeline() {
        super.testDeepDeletePipeline();
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testDuplicateCopyPipeline() {
        super.testDuplicateCopyPipeline();
    }

    @Test
    @Transactional
    public void testDuplicateCopyPipeline2() {

        Pipeline ppl = new Pipeline();
        ppl.setName("pplName");
        ppl.setDescription("pplDesc");
        ppl.setUser(authCtx.getUser());

        Pipeline nPpl = pipelineFacade.copyPipeline(ppl);

        // test copying for the first time
        String newName = "Copy #1 of " + ppl.getName();
        assertNotSame(ppl, nPpl);
        assertEquals(newName, nPpl.getName());
        assertEquals(ppl.getDescription(), nPpl.getDescription());

        Pipeline nPpl1 = pipelineFacade.copyPipeline(ppl);

        // test copying for the second time
        String newName1 = "Copy #2 of " + ppl.getName();
        assertNotSame(ppl, nPpl1);
        assertEquals(newName1, nPpl1.getName());
        assertEquals(ppl.getDescription(), nPpl1.getDescription());
    }

    @Override
    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testDeletePipeline() {
        super.testDeletePipeline();
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    public void testDeletePipeline2() {

        Pipeline[] pipes = new Pipeline[3];
        for (int i = 0; i < 3; i++) {
            pipes[i] = pipelineFacade.createPipeline();
            pipes[i].setUser(authCtx.getUser());
            pipelineFacade.save(pipes[i]);
        }

        pipelineFacade.delete(pipes[1]);

        em.flush();

        assertEquals(pipes[0], pipelineFacade.getPipeline(pipes[0].getId()));
        assertEquals(pipes[2], pipelineFacade.getPipeline(pipes[2].getId()));
        // pipeline no longer exist, so we expect AccessDeniedException
        assertNull(pipelineFacade.getPipeline(pipes[1].getId()));
    }

    /**
     * Test of createOpenEvent method, of class PipelineFacade.
     */
    @Test
    @Transactional
    public void testCreateOpenEvent() {
        System.out.println("createOpenEvent");

        DPUTemplateRecord parentTemplateRecord = dpuFacade.createTemplate(
                "testParent", DPUType.EXTRACTOR);
        parentTemplateRecord.setDescription("parentTestDescription");
        parentTemplateRecord.setJarDescription("parenttestJarDescription");
        parentTemplateRecord.setJarDirectory("parenttestJarDirectory");
        parentTemplateRecord.setJarName("parenttestJarName");

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName",
                DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");
        templateRecord.setShareType(ShareType.PRIVATE);
        templateRecord.setParent(parentTemplateRecord);
        dpuFacade.save(parentTemplateRecord);
        dpuFacade.save(templateRecord);

        DPUTemplateRecord templateRecord2 = dpuFacade.createTemplate("testName2",
                DPUType.EXTRACTOR);
        templateRecord2.setDescription("testDescription2");
        templateRecord2.setJarDescription("testJarDescription2");
        templateRecord2.setJarDirectory("testJarDirectory2");
        templateRecord2.setJarName("testJarName2");
        templateRecord2.setShareType(ShareType.PUBLIC_RW);
        templateRecord2.setParent(parentTemplateRecord);
        dpuFacade.save(templateRecord2);

        Pipeline pipeline = pipelineFacade.createPipeline();
        pipeline.setDescription("testDescription");
        PipelineGraph pipelineGraph = new PipelineGraph();
        DPUInstanceRecord dpuInstanceRecord = dpuFacade
                .createInstanceFromTemplate(templateRecord);
        DPUInstanceRecord dpuInstanceRecord2 = dpuFacade
                .createInstanceFromTemplate(templateRecord2);
        pipelineGraph.addDpuInstance(dpuInstanceRecord);
        pipelineGraph.addDpuInstance(dpuInstanceRecord2);
        pipeline.setGraph(pipelineGraph);
        pipeline.setLastChange(new Date());
        pipeline.setName("testName");
        pipeline.setUser(userFacade.getUserByUsername("jdoe"));
        pipeline.setShareType(ShareType.PUBLIC_RO);
        pipeline.getConflicts().add(pipeline);
        dpuFacade.save(dpuInstanceRecord);

        pipelineFacade.createOpenEvent(pipeline);
        if (authCtx != null) {
            assertNull(openEventDao.getOpenEvent(pipeline, authCtx.getUser()));
        }

        pipelineFacade.save(pipeline);
        if (authCtx != null) {
            pipelineFacade.createOpenEvent(pipeline);
            OpenEvent openEvent = openEventDao.getOpenEvent(pipeline, authCtx.getUser());
            assertNotNull(openEvent);
            assertEquals(authCtx.getUser(), openEvent.getUser());
            assertEquals(pipeline, openEvent.getPipeline());
            Date timestamp = openEvent.getTimestamp();

            pipelineFacade.createOpenEvent(pipeline);
            OpenEvent openEvent2 = openEventDao.getOpenEvent(pipeline, authCtx.getUser());
            assertNotNull(openEvent2);
            assertEquals(authCtx.getUser(), openEvent2.getUser());
            assertEquals(pipeline, openEvent2.getPipeline());

            // test that we gate two same events
// TODO: it dosn't work why?
//			assertEquals(timestamp, openEvent.getTimestamp());

            assertEquals(openEvent.getTimestamp(), openEvent2.getTimestamp());
            assertEquals(openEvent.getId(), openEvent2.getId());
            assertEquals(openEvent, openEvent2);
        } else {
            // drop it, it has to test for null
            pipelineFacade.createOpenEvent(pipeline);
        }
    }

}
