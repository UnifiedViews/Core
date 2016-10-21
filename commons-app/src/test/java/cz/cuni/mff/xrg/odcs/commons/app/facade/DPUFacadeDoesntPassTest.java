package cz.cuni.mff.xrg.odcs.commons.app.facade;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUType;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Test suite for DPU facade interface. Each test is run in own transaction,
 * which is rolled back in the end.
 * 
 * @author michal.klempa@eea.sk
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DPUFacadeDoesntPassTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private DPUFacade dpuFacade;

    public DPUFacadeDoesntPassTest() {
    }

    /**
     * Test of createTemplate method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testCreateTemplate() {
        System.out.println("createTemplate");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        assertNotNull(templateRecord);
        assertEquals(DPUType.EXTRACTOR, templateRecord.getType());
        assertEquals("testName", templateRecord.getName());
    }

    /**
     * Test of createCopy method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testCreateCopy() {
        System.out.println("createCopy");
        DPUTemplateRecord parentTemplateRecord = dpuFacade.createTemplate("testParent", DPUType.EXTRACTOR);
        parentTemplateRecord.setDescription("testDescription");
        parentTemplateRecord.setId(-54L);
        parentTemplateRecord.setJarDescription("testJarDescription");
        parentTemplateRecord.setJarDirectory("testJarDirectory");
        parentTemplateRecord.setJarName("testJarName");

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        // set description as only jarproperties are inherit from parent
        templateRecord.setDescription("secondTestDescription");
        templateRecord.setParent(parentTemplateRecord);

        DPUTemplateRecord copyTemplateRecord = dpuFacade.createCopy(templateRecord);
        assertNotNull(copyTemplateRecord);
        assertEquals(DPUType.EXTRACTOR, copyTemplateRecord.getType());
        assertEquals("secondTestDescription", copyTemplateRecord.getDescription());
        assertEquals(null, copyTemplateRecord.getId());
        assertEquals("testName", copyTemplateRecord.getName());
        assertEquals("testJarDescription", copyTemplateRecord.getJarDescription());
        assertEquals("testJarDirectory", copyTemplateRecord.getJarDirectory());
        assertEquals("testJarName", copyTemplateRecord.getJarName());
        assertEquals(ShareType.PRIVATE, copyTemplateRecord.getShareType());

        templateRecord.setParent(null);
        DPUTemplateRecord copy1TemplateRecord = dpuFacade.createCopy(templateRecord);
        assertNotNull(copy1TemplateRecord);
        assertEquals(DPUType.EXTRACTOR, copy1TemplateRecord.getType());
        assertEquals("secondTestDescription", copy1TemplateRecord.getDescription());
        assertEquals(null, copy1TemplateRecord.getId());
        assertEquals("testName", copy1TemplateRecord.getName());
        assertEquals("testJarDescription", copy1TemplateRecord.getJarDescription());
        assertEquals("testJarDirectory", copy1TemplateRecord.getJarDirectory());
        assertEquals("testJarName", copy1TemplateRecord.getJarName());
        assertEquals(ShareType.PRIVATE, copy1TemplateRecord.getShareType());
    }

    /**
     * Test of createTemplateFromInstance method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testCreateTemplateFromInstance() {
        System.out.println("createTemplateFromInstance");
        DPUInstanceRecord instanceRecord = new DPUInstanceRecord();
        DPUTemplateRecord templateRecord = new DPUTemplateRecord();
        DPUTemplateRecord parentTemplateRecord = dpuFacade.createTemplate("testParent", DPUType.EXTRACTOR);
        String rawConfig = "<xml><a>value</a></xml";

        instanceRecord.setName("testname");
        instanceRecord.setDescription("testdescription");
        instanceRecord.setRawConf(rawConfig);
        instanceRecord.setTemplate(templateRecord);

        DPUTemplateRecord copyTemplateRecord = dpuFacade.createTemplateFromInstance(instanceRecord);

        assertNotNull(copyTemplateRecord);
        assertEquals(instanceRecord.getName(), copyTemplateRecord.getName());
        assertEquals(instanceRecord.getDescription(), copyTemplateRecord.getDescription());
        assertEquals(instanceRecord.getJarPath(), copyTemplateRecord.getJarPath());
        assertNotSame(instanceRecord.getRawConf(), copyTemplateRecord.getRawConf());

        templateRecord.setParent(parentTemplateRecord);
        DPUTemplateRecord copyTemplateRecord2 = dpuFacade.createTemplateFromInstance(instanceRecord);

        assertNotNull(copyTemplateRecord2);
        assertEquals(instanceRecord.getName(), copyTemplateRecord2.getName());
        assertEquals(instanceRecord.getDescription(), copyTemplateRecord2.getDescription());
        assertEquals(instanceRecord.getJarPath(), copyTemplateRecord2.getJarPath());
        assertEquals(instanceRecord.getTemplate().getParent(), copyTemplateRecord2.getParent());
        assertNotSame(instanceRecord.getRawConf(), copyTemplateRecord2.getRawConf());
    }

    /**
     * Test of getAllTemplates method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetAllTemplates() {
        System.out.println("getAllTemplates");
        List<DPUTemplateRecord> templateRecords = dpuFacade.getAllTemplates();
        assertNotNull(templateRecords);
        long size = templateRecords.size();

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        dpuFacade.save(templateRecord);
        em.flush();
        List<DPUTemplateRecord> templateRecords1 = dpuFacade.getAllTemplates();
        assertNotNull(templateRecords1);
        assertTrue(size + 1 == templateRecords1.size());
        assertTrue(templateRecords1.contains(templateRecord));
    }

    /**
     * Test of getTemplate method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetTemplate() {
        System.out.println("getTemplate");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        dpuFacade.save(templateRecord);
        em.flush();
        assertNotNull(templateRecord);
        assertNotNull(templateRecord.getId());
        long id = templateRecord.getId();

        em.clear();
        DPUTemplateRecord templateRecord1 = dpuFacade.getTemplate(id);
        assertNotNull(templateRecord1);
        assertEquals(id, (long) templateRecord1.getId());
        assertEquals(templateRecord, templateRecord1);
    }

    /**
     * Test of save method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testSave_DPUTemplateRecord() {
        System.out.println("save");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        dpuFacade.save(templateRecord);
        em.flush();
        assertNotNull(templateRecord);
        assertNotNull(templateRecord.getId());
    }

    /**
     * Test of delete method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testDelete_DPUTemplateRecord() {
        System.out.println("delete");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        dpuFacade.save(templateRecord);
        em.flush();
        long id = templateRecord.getId();
        assertNotNull(templateRecord);
        assertNotNull(templateRecord.getId());
        dpuFacade.delete(templateRecord);
        DPUTemplateRecord templateRecord1 = dpuFacade.getTemplate(id);
        assertNull(templateRecord1);
    }

    /**
     * Test of getChildDPUs method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetChildDPUs() {
        System.out.println("getChildDPUs");
        DPUTemplateRecord parentTemplateRecord = dpuFacade.createTemplate("testParent", DPUType.EXTRACTOR);
        parentTemplateRecord.setDescription("parentTestDescription");
        parentTemplateRecord.setId(-52L);
        parentTemplateRecord.setJarDescription("parenttestJarDescription");
        parentTemplateRecord.setJarDirectory("parenttestJarDirectory");
        parentTemplateRecord.setJarName("parenttestJarName");

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");
        templateRecord.setParent(parentTemplateRecord);

        dpuFacade.save(parentTemplateRecord);
        dpuFacade.save(templateRecord);
        em.flush();

        List<DPUTemplateRecord> templateRecords = dpuFacade.getChildDPUs(parentTemplateRecord);
        assertNotNull(templateRecords);
        assertTrue(templateRecords.size() == 1);
        assertEquals(templateRecord.getId(), templateRecords.get(0).getId());
        assertEquals(templateRecord, templateRecords.get(0));
    }

    /**
     * Test of createInstanceFromTemplate method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testCreateInstanceFromTemplate() {
        System.out.println("createInstanceFromTemplate");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);
        assertEquals(templateRecord.getDescription(), instanceRecord.getDescription());
        assertEquals(templateRecord.getJarPath(), instanceRecord.getJarPath());
        assertEquals(templateRecord.getName(), instanceRecord.getName());
        assertEquals(templateRecord.getRawConf(), instanceRecord.getRawConf());
        assertEquals(templateRecord, instanceRecord.getTemplate());
        assertEquals(templateRecord.getType(), instanceRecord.getType());
    }

    /**
     * Test of getAllDPUInstances method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetAllDPUInstances() {
        System.out.println("getAllDPUInstances");
        List<DPUInstanceRecord> instanceRecords = dpuFacade.getAllDPUInstances();
        assertNotNull(instanceRecords);
        long size = instanceRecords.size();

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        em.flush();

        List<DPUInstanceRecord> instanceRecords1 = dpuFacade.getAllDPUInstances();
        assertNotNull(instanceRecords1);
        assertEquals(size + 1, instanceRecords1.size());
        assertTrue(instanceRecords1.contains(instanceRecord));
    }

    /**
     * Test of getDPUInstance method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetDPUInstance() {
        System.out.println("getDPUInstance");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        em.flush();
        em.clear();

        Long id = instanceRecord.getId();
        assertNotNull(id);

        DPUInstanceRecord instanceRecord1 = dpuFacade.getDPUInstance(id);
        assertNotNull(instanceRecord1);
        assertEquals(instanceRecord, instanceRecord1);
    }

    /**
     * Test of save method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testSave_DPUInstanceRecord() {
        System.out.println("save");

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = new DPUInstanceRecord();

        String rawConfig = "<xml><a>value</a></xml";

        instanceRecord.setName("testname");
        instanceRecord.setDescription("testdescription");
        instanceRecord.setRawConf(rawConfig);
        instanceRecord.setTemplate(templateRecord);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        assertNotNull(instanceRecord);
        assertNotNull(instanceRecord.getId());
        long id = instanceRecord.getId();

        em.flush();
        em.clear();
        DPUInstanceRecord instanceRecord1 = dpuFacade.getDPUInstance(id);
        assertNotNull(instanceRecord1);
        assertNotNull(instanceRecord1.getId());
        assertEquals(id, (long) instanceRecord1.getId());
        assertEquals(instanceRecord, instanceRecord1);
    }

    /**
     * Test of delete method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testDelete_DPUInstanceRecord() {

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = new DPUInstanceRecord();
        String rawConfig = "<xml><a>value</a></xml";

        instanceRecord.setName("testname");
        instanceRecord.setDescription("testdescription");
        instanceRecord.setRawConf(rawConfig);
        instanceRecord.setTemplate(templateRecord);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        assertNotNull(instanceRecord);
        assertNotNull(instanceRecord.getId());
        long id = instanceRecord.getId();

        em.flush();
        em.clear();
        dpuFacade.delete(instanceRecord);
        DPUInstanceRecord instanceRecord1 = dpuFacade.getDPUInstance(id);
        assertNull(instanceRecord1);
    }

    /**
     * Test of getAllDPURecords method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetAllDPURecords() {
        System.out.println("getAllDPURecords");

        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);

        Pipeline pipe = pipelineFacade.createPipeline();
        PipelineExecution exec = pipelineFacade.createExecution(pipe);

        MessageRecord messageRecord = new MessageRecord(new Date(), MessageRecordType.DPU_DEBUG, instanceRecord,
                exec, "testShortMessage", "testLongMessage");

        pipelineFacade.save(pipe);
        pipelineFacade.save(exec);
        em.flush();
        List<MessageRecord> messageRecords = dpuFacade.getAllDPURecords(exec);
        assertNotNull(messageRecords);
        long size = messageRecords.size();

        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        dpuFacade.save(messageRecord);
        em.flush();

        List<MessageRecord> messageRecords1 = dpuFacade.getAllDPURecords(exec);
        assertNotNull(messageRecords1);
        assertEquals(size + 1, messageRecords1.size());
        assertTrue(messageRecords1.contains(messageRecord));
    }

    /**
     * Test of getDPURecord method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testGetDPURecord() {
        System.out.println("getDPURecord");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);

        Pipeline pipe = pipelineFacade.createPipeline();
        PipelineExecution exec = pipelineFacade.createExecution(pipe);

        MessageRecord messageRecord = new MessageRecord(new Date(), MessageRecordType.DPU_DEBUG, instanceRecord,
                exec, "testShortMessage", "testLongMessage");

        pipelineFacade.save(pipe);
        pipelineFacade.save(exec);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        dpuFacade.save(messageRecord);
        assertNotNull(messageRecord.getId());
        long id = messageRecord.getId();
        em.flush();
        em.clear();

        MessageRecord messageRecord1 = dpuFacade.getDPURecord(id);
        assertNotNull(messageRecord1);
        assertEquals(messageRecord.getId(), messageRecord1.getId());
        assertEquals(messageRecord, messageRecord1);
    }

    /**
     * Test of save method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testSave_MessageRecord() {
        System.out.println("save");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);

        Pipeline pipe = pipelineFacade.createPipeline();
        PipelineExecution exec = pipelineFacade.createExecution(pipe);

        MessageRecord messageRecord = new MessageRecord(new Date(), MessageRecordType.DPU_DEBUG, instanceRecord,
                exec, "testShortMessage", "testLongMessage");

        pipelineFacade.save(pipe);
        pipelineFacade.save(exec);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        dpuFacade.save(messageRecord);

        assertNotNull(messageRecord.getId());
    }

    /**
     * Test of delete method, of class DPUFacade.
     */
    @Test
    @Transactional
    public void testDelete_MessageRecord() {
        System.out.println("delete");
        DPUTemplateRecord templateRecord = dpuFacade.createTemplate("testName", DPUType.EXTRACTOR);
        templateRecord.setDescription("testDescription");
        templateRecord.setId(-54L);
        templateRecord.setJarDescription("testJarDescription");
        templateRecord.setJarDirectory("testJarDirectory");
        templateRecord.setJarName("testJarName");

        DPUInstanceRecord instanceRecord = dpuFacade.createInstanceFromTemplate(templateRecord);

        Pipeline pipe = pipelineFacade.createPipeline();
        PipelineExecution exec = pipelineFacade.createExecution(pipe);

        MessageRecord messageRecord = new MessageRecord(new Date(), MessageRecordType.DPU_DEBUG, instanceRecord,
                exec, "testShortMessage", "testLongMessage");

        pipelineFacade.save(pipe);
        pipelineFacade.save(exec);
        dpuFacade.save(templateRecord);
        dpuFacade.save(instanceRecord);
        dpuFacade.save(messageRecord);
        assertNotNull(messageRecord.getId());
        long id = messageRecord.getId();
        em.flush();
        em.clear();
        List<MessageRecord> messageRecords = dpuFacade.getAllDPURecords(exec);
        assertNotNull(messageRecords);
        long size = messageRecords.size();

        dpuFacade.delete(messageRecord);
        em.flush();
        em.clear();

        MessageRecord messageRecord1 = dpuFacade.getDPURecord(id);
        assertNull(messageRecord1);

        List<MessageRecord> messageRecords1 = dpuFacade.getAllDPURecords(exec);
        assertNotNull(messageRecords1);
        assertEquals(size - 1, messageRecords1.size());
        assertFalse(messageRecords1.contains(messageRecord));
    }
}
