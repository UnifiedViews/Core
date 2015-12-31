/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.commons.app.constraints;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUType;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionContextInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.OpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Edge;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Position;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;
import cz.cuni.mff.xrg.odcs.commons.app.user.*;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;

public class DatabaseConstraintsTest {

    private static EntityManagerFactory factory;

    private static Server dbServer;

    private static final String DB_URL = "jdbc:h2:mem:test_mem";

    @BeforeClass
    public static void init() throws SQLException {
        dbServer = Server.createTcpServer().start();

        Map properties = new HashMap();
        properties.put(JDBC_DRIVER, "org.h2.Driver");
        properties.put(JDBC_URL, DB_URL);
        factory = Persistence.createEntityManagerFactory("odcs", properties);

        String disableCryptography = ConfigProperty.CRYPTOGRAPHY_ENABLED.toString() + "=" + Boolean.FALSE.toString();
        AppConfig.loadFrom(new CharSequenceInputStream(disableCryptography, Charset.defaultCharset()));
    }

    @AfterClass
    public static void shutdown() {
        dbServer.shutdown();
    }

    @Test
    public void ON_DELETE_dpu_template_DELETE_dpu_instance() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                DPUTemplateRecord template = new DPUTemplateRecord("testTemplate", DPUType.LOADER);
                DPUInstanceRecord instance = new DPUInstanceRecord(template);
                template.getDerivedDPUs().add(instance);

                em.persist(template);
                em.persist(instance);

                em.getTransaction().commit();
                return template;
            }

        }.ensureReferencingInstanceDelete(DPUInstanceRecord.class, DPUTemplateRecord.class);
    }

    @Test
    public void ON_DELETE_dpu_instance_DELETE_exec_record() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                DPUInstanceRecord instance = new DPUInstanceRecord();
                MessageRecord messageRecord = new MessageRecord(null, null, instance, null, null, null);

                em.persist(instance);
                em.persist(messageRecord);

                em.getTransaction().commit();
                return instance;
            }

        }.ensureReferencingInstanceDelete(MessageRecord.class, DPUInstanceRecord.class);
    }

    @Test
    public void ON_DELETE_exec_pipeline_DELETE_exec_record() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                PipelineExecution pipelineExecution = new PipelineExecution();
                pipelineExecution.setOrderNumber(1l);
                MessageRecord messageRecord = new MessageRecord(null, null, null, pipelineExecution, null, null);

                em.persist(pipelineExecution);
                em.persist(messageRecord);

                em.getTransaction().commit();
                return pipelineExecution;
            }

        }.ensureReferencingInstanceDelete(MessageRecord.class, PipelineExecution.class);
    }

    @Test
    public void ON_DELETE_ppl_model_DELETE_exec_pipeline() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                Pipeline pipeline = new Pipeline();
                PipelineExecution pipelineExecution = new PipelineExecution(pipeline);
                pipelineExecution.setOrderNumber(1l);

                em.persist(pipeline);
                em.persist(pipelineExecution);

                em.getTransaction().commit();
                return pipeline;
            }

        }.ensureReferencingInstanceDelete(PipelineExecution.class, Pipeline.class);
    }

    @Test
    public void ON_DELETE_ppl_node_DELETE_exec_pipeline() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                DPUInstanceRecord instance = new DPUInstanceRecord();
                Node node = new Node(instance);
                PipelineExecution pipelineExecution = new PipelineExecution();
                pipelineExecution.setDebugNode(node);
                pipelineExecution.setOrderNumber(1l);

                em.persist(instance);
                em.persist(node);
                em.persist(pipelineExecution);

                em.getTransaction().commit();
                return node;
            }

        }.ensureReferencingInstanceDelete(PipelineExecution.class, Node.class);
    }

    @Test
    public void ON_DELETE_role_DELETE_usr_user_role() {

        EntityManager em = factory.createEntityManager();

        em.getTransaction().begin();
        RoleEntity role = new RoleEntity();
        role.setName("dummyRole");
        User user = new User();
        user.getRoles().add(role);
        user.setUsername("user_1");
        user.setExternalIdentifier("dummy");
        user.setPassword("pwd");

        em.persist(user);
        em.persist(role);
        em.getTransaction().commit();

        int usersBeforeRoleRemoval = getTableEntries(User.class, em).size();
        int rolesBeforeRoleRemoval = getTableEntries(RoleEntity.class, em).size();

        em.getTransaction().begin();
        em.remove(role);
        em.getTransaction().commit();

        Assert.assertEquals(usersBeforeRoleRemoval, getTableEntries(User.class, em).size());
        Assert.assertEquals(rolesBeforeRoleRemoval - 1, getTableEntries(RoleEntity.class, em).size());

        em.getTransaction().begin();
        em.refresh(user);
        Assert.assertTrue(user.getRoles().isEmpty());
    }

    //    @Test
    //    public void ON_DELETE_usr_user_DELETE_exec_pipeline() {
    //        new DeleteConstraintTest() {
    //
    //            @Override
    //            Object createReferencedInstance(EntityManager em) {
    //                em.getTransaction().begin();
    //                User user = new User();
    //                user.setPassword("pwd");
    //                user.setUsername("user_2");
    //                user.setExternalIdentifier("dummy");
    //                PipelineExecution pipelineExecution = new PipelineExecution();
    //                pipelineExecution.setOwner(user);
    //                pipelineExecution.setOrderNumber(1l);
    //
    //                em.persist(user);
    //                em.persist(pipelineExecution);
    //                em.getTransaction().commit();
    //
    //                return user;
    //            }
    //
    //        }.ensureReferencingInstanceDelete(PipelineExecution.class, User.class);
    //    }

    @Test
    public void ON_DELETE_user_actor_DELETE_exec_pipeline() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                UserActor actor = new UserActor();
                actor.setExternalId("dummy");
                actor.setName("dummy");
                Pipeline pipeline = new Pipeline();
                Schedule schedule = new Schedule();
                schedule.setPipeline(pipeline);
                schedule.setActor(actor);
                schedule.setPriority(0l);

                em.persist(pipeline);
                em.persist(actor);
                em.persist(schedule);

                em.getTransaction().commit();
                return actor;
            }

        }.ensureReferencingInstanceDelete(Schedule.class, UserActor.class);
    }

    @Test
    public void ON_DELETE_user_actor_DELETE_exec_schedule() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                UserActor actor = new UserActor();
                actor.setExternalId("dummy");
                actor.setName("dummy");
                Pipeline pipeline = new Pipeline();
                Schedule schedule = new Schedule();
                schedule.setPipeline(pipeline);
                schedule.setActor(actor);
                schedule.setPriority(0l);

                em.persist(pipeline);
                em.persist(actor);
                em.persist(schedule);

                em.getTransaction().commit();
                return actor;
            }

        }.ensureReferencingInstanceDelete(Schedule.class, UserActor.class);
    }

    @Test
    public void ON_DELETE_user_actor_DELETE_ppl_model() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                Pipeline pipeline = new Pipeline();
                UserActor actor = new UserActor();
                actor.setName("dummy");
                actor.setExternalId("dummy");
                pipeline.setActor(actor);

                em.persist(pipeline);
                em.persist(actor);
                em.getTransaction().commit();

                return actor;
            }

        }.ensureReferencingInstanceDelete(Pipeline.class, UserActor.class);
    }

    @Test
    public void ON_DELETE_ppl_model_DELETE_exec_schedule() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                Pipeline pipeline = new Pipeline();
                Schedule schedule = new Schedule();
                schedule.setPipeline(pipeline);
                schedule.setPriority(0l);

                em.persist(pipeline);
                em.persist(schedule);

                em.getTransaction().commit();
                return pipeline;
            }

        }.ensureReferencingInstanceDelete(Schedule.class, Pipeline.class);
    }

    @Test
    public void ON_DELETE_usr_user_DELETE_exec_schedule() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                Pipeline pipeline = new Pipeline();
                User user = new User();
                user.setUsername("user_3");
                user.setExternalIdentifier("dummy");
                user.setPassword("pwd");
                Schedule schedule = new Schedule();
                schedule.setOwner(user);
                schedule.setPipeline(pipeline);
                schedule.setPriority(0l);

                em.persist(pipeline);
                em.persist(user);
                em.persist(schedule);

                em.getTransaction().commit();
                return user;
            }

        }.ensureReferencingInstanceDelete(Schedule.class, User.class);
    }

    @Test
    public void ON_DELETE_usr_user_DELETE_ppl_model() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                Pipeline pipeline = new Pipeline();
                User user = new User();
                user.setUsername("user_4");
                user.setExternalIdentifier("dummy");
                user.setPassword("pwd");
                pipeline.setUser(user);

                em.persist(pipeline);
                em.persist(user);
                em.getTransaction().commit();

                return user;
            }

        }.ensureReferencingInstanceDelete(Pipeline.class, User.class);
    }

    @Test
    public void ON_DELETE_ppl_model_DELETE_ppl_graph() {
        new DeleteConstraintTest() {
            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                PipelineGraph pplGraph = new PipelineGraph();
                Pipeline pipeline = new Pipeline();
                pipeline.setGraph(pplGraph);

                em.persist(pplGraph);
                em.persist(pipeline);
                em.getTransaction().commit();

                return pipeline;
            }
        }.ensureReferencingInstanceDelete(PipelineGraph.class, Pipeline.class);
    }

    @Test
    public void ON_DELETE_ppl_graph_DELETE_ppl_node() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                DPUInstanceRecord instance = new DPUInstanceRecord();

                PipelineGraph pplGraph = new PipelineGraph();
                Pipeline pipeline = new Pipeline();
                pplGraph.setPipeline(pipeline);
                pipeline.setGraph(pplGraph);

                Node node = new Node();
                node.setDpuInstance(instance);
                pplGraph.addNode(node);

                em.persist(instance);
                em.persist(pipeline);
                em.persist(pplGraph);
                em.persist(node);

                em.getTransaction().commit();
                return pplGraph;
            }
        }.ensureReferencingInstanceDelete(Node.class, PipelineGraph.class);
    }

    @Test
    public void ON_DELETE_dpu_instance_DELETE_ppl_node() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                DPUInstanceRecord dpuInstance = new DPUInstanceRecord();
                Node node = new Node();
                node.setDpuInstance(dpuInstance);

                em.persist(dpuInstance);
                em.persist(node);
                em.getTransaction().commit();

                return dpuInstance;
            }

        }.ensureReferencingInstanceDelete(Node.class, DPUInstanceRecord.class);
    }

    // Note: the test is not needed, ppl_node is removed, ppl_position is dependant class
    //    @Test
    //    public void ON_DELETE_ppl_position_DELETE_ppl_node() {
    //        new DeleteConstraintTest() {
    //
    //            @Override
    //            Object createReferencedInstance(EntityManager em) {
    //                em.getTransaction().begin();
    //                DPUInstanceRecord dpuInstance = new DPUInstanceRecord();
    //                Position position = new Position();
    //                Node node = new Node();
    //                node.setDpuInstance(dpuInstance);
    //                node.setPosition(position);
    //
    //                em.persist(dpuInstance);
    //                em.persist(position);
    //                em.persist(node);
    //
    //                em.getTransaction().commit();
    //                return position;
    //            }
    //
    //        }.ensureReferencingInstanceDelete(Node.class, Position.class);
    //    }

    @Test
    public void ON_DELETE_ppl_graph_DELETE_ppl_edge() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                Pipeline pipeline = new Pipeline();
                PipelineGraph pplGraph = new PipelineGraph();
                pplGraph.setPipeline(pipeline);
                pipeline.setGraph(pplGraph);
                Edge edge = new Edge();
                edge.setGraph(pplGraph);

                em.persist(pipeline);
                em.persist(pplGraph);
                em.persist(edge);
                em.getTransaction().commit();

                return pplGraph;
            }

        }.ensureReferencingInstanceDelete(Edge.class, PipelineGraph.class);
    }

    //    @Test
    //    public void ON_DELETE_ppl_node_from_DELETE_ppl_edge() {
    //        new DeleteConstraintTest() {
    //
    //            @Override
    //            Object createReferencedInstance(EntityManager em) {
    //                em.getTransaction().begin();
    //                Node nodeFrom = new Node();
    //                DPUInstanceRecord dpuInstance = new DPUInstanceRecord();
    //                nodeFrom.setDpuInstance(dpuInstance);
    //                Edge edge = new Edge();
    //                edge.setFrom(nodeFrom);
    //
    //                em.persist(dpuInstance);
    //                em.persist(nodeFrom);
    //                em.persist(edge);
    //
    //                em.getTransaction().commit();
    //                return nodeFrom;
    //            }
    //        }.ensureReferencingInstanceDelete(Edge.class, Node.class);
    //    }

    //    @Test
    //    public void ON_DELETE_ppl_node_to_DELETE_ppl_edge() {
    //        new DeleteConstraintTest() {
    //
    //            @Override
    //            Object createReferencedInstance(EntityManager em) {
    //                em.getTransaction().begin();
    //                Node nodeTo = new Node();
    //                DPUInstanceRecord dpuInstance = new DPUInstanceRecord();
    //                nodeTo.setDpuInstance(dpuInstance);
    //                Edge edge = new Edge();
    //                edge.setTo(nodeTo);
    //
    //                em.persist(dpuInstance);
    //                em.persist(nodeTo);
    //                em.persist(edge);
    //
    //                em.getTransaction().commit();
    //                return nodeTo;
    //            }
    //        }.ensureReferencingInstanceDelete(Edge.class, Node.class);
    //    }

    @Test
    public void ON_DELETE_exec_schedule_DELETE_sch_sch_notification() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();

                Pipeline pipeline = new Pipeline();
                Schedule schedule = new Schedule();
                ScheduleNotificationRecord scheduleNotificationRecord = new ScheduleNotificationRecord();
                schedule.setNotification(scheduleNotificationRecord);
                schedule.setPipeline(pipeline);
                schedule.setPriority(0l);
                scheduleNotificationRecord.setSchedule(schedule);

                em.persist(pipeline);
                em.persist(schedule);
                em.persist(scheduleNotificationRecord);

                em.getTransaction().commit();
                return schedule;
            }

        }.ensureReferencingInstanceDelete(ScheduleNotificationRecord.class, Schedule.class);
    }

    @Test
    public void ON_DELETE_usr_user_DELETE_sch_usr_notification() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();

                User user = new User();
                UserNotificationRecord userNotificationRecord = new UserNotificationRecord();
                user.setNotification(userNotificationRecord);
                user.setUsername("user_5");
                user.setExternalIdentifier("dummy");
                user.setPassword("pwd");
                userNotificationRecord.setUser(user);

                em.persist(userNotificationRecord);
                em.persist(user);

                em.getTransaction().commit();
                return user;
            }

        }.ensureReferencingInstanceDelete(UserNotificationRecord.class, User.class);
    }

    @Test
    public void ON_DELETE_usr_user_DELETE_ppl_open_event() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();
                User user = new User();
                user.setUsername("user_6");
                user.setPassword("pwd");
                user.setExternalIdentifier("dummy");
                OpenEvent openEvent = new OpenEvent();
                openEvent.setTimestamp(new Date());
                openEvent.setUser(user);

                em.persist(user);
                em.persist(openEvent);

                em.getTransaction().commit();
                return user;
            }

        }.ensureReferencingInstanceDelete(OpenEvent.class, User.class);
    }

    @Test
    public void ON_DELETE_ppl_model_DELETE_ppl_open_event() {
        new DeleteConstraintTest() {

            @Override
            Object createReferencedInstance(EntityManager em) {
                em.getTransaction().begin();

                Pipeline pipeline = new Pipeline();
                OpenEvent openEvent = new OpenEvent();
                openEvent.setTimestamp(new Date());
                openEvent.setPipeline(pipeline);

                em.persist(pipeline);
                em.persist(openEvent);

                em.getTransaction().commit();
                return pipeline;
            }

        }.ensureReferencingInstanceDelete(OpenEvent.class, Pipeline.class);
    }

    @Test
    public void ON_DELETE_exec_context_pipeline_SET_context_id_IN_exec_pipeline_TO_NULL() {
        PipelineExecution exec_pipeline = new PipelineExecution();
        ExecutionContextInfo exec_context_pipeline = new ExecutionContextInfo(exec_pipeline);
        exec_pipeline.setContext(exec_context_pipeline);
        exec_pipeline.setOrderNumber(1l);

        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM exec_pipeline").executeUpdate();
        em.createNativeQuery("DELETE FROM exec_context_pipeline").executeUpdate();
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.persist(exec_pipeline);
        em.persist(exec_context_pipeline);
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.remove(exec_context_pipeline);
        em.getTransaction().commit();

        Assert.assertEquals(1, getTableEntries(PipelineExecution.class, em).size());
        Assert.assertEquals(0, getTableEntries(ExecutionContextInfo.class, em).size());
    }

    @Test
    public void ON_DELETE_exec_schedule_SET_schedule_id_IN_exec_pipeline_TO_NULL() {
        Pipeline pipeline = new Pipeline();
        Schedule schedule = new Schedule();
        schedule.setPipeline(pipeline);
        schedule.setPriority(0l);
        PipelineExecution pipelineExecution1 = new PipelineExecution();
        PipelineExecution pipelineExecution2 = new PipelineExecution();
        pipelineExecution1.setSchedule(schedule);
        pipelineExecution2.setSchedule(schedule);
        pipelineExecution1.setOrderNumber(1l);
        pipelineExecution2.setOrderNumber(1l);

        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM exec_schedule").executeUpdate();
        em.createNativeQuery("DELETE FROM exec_pipeline").executeUpdate();
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.persist(pipeline);
        em.persist(schedule);
        em.persist(pipelineExecution1);
        em.persist(pipelineExecution2);
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.remove(schedule);
        em.getTransaction().commit();

        Assert.assertEquals(2, getTableEntries(PipelineExecution.class, em).size());
        Assert.assertEquals(0, getTableEntries(Schedule.class, em).size());
    }

    @Test
    public void ON_DELETE_sch_email_SET_email_id_IN_usr_user_TO_NULL() {
        EmailAddress sch_email = new EmailAddress();
        User usr_user = new User();
        usr_user.setEmail(sch_email);
        usr_user.setUsername("user_7");
        usr_user.setExternalIdentifier("dummy");
        usr_user.setPassword("pwd");

        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        em.persist(sch_email);
        em.persist(usr_user);
        em.getTransaction().commit();

        int usersBeforeEmailRemoval = getTableEntries(User.class, em).size();
        int emailsBeforeEmailRemoval = getTableEntries(EmailAddress.class, em).size();

        em.getTransaction().begin();
        em.remove(sch_email);
        em.getTransaction().commit();

        Assert.assertEquals(usersBeforeEmailRemoval, getTableEntries(User.class, em).size());
        Assert.assertEquals(emailsBeforeEmailRemoval - 1, getTableEntries(EmailAddress.class, em).size());

        em.getTransaction().begin();
        em.refresh(usr_user);
        Assert.assertNull(usr_user.getEmail());
    }

    @Test
    public void dpu_template_parent_reference() {
        // on template parent deletion set reference in child to null

        DPUTemplateRecord template = new DPUTemplateRecord("testTemplateWithParent", DPUType.LOADER);
        DPUTemplateRecord parentTemplate = new DPUTemplateRecord("testParentTemplate", DPUType.LOADER);
        template.setParent(parentTemplate);

        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM dpu_template").executeUpdate();
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.persist(template);
        em.persist(parentTemplate);
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.remove(parentTemplate);
        em.getTransaction().commit();

        Assert.assertEquals(1, getTableEntries(DPUTemplateRecord.class, em).size());
        DPUTemplateRecord child = (DPUTemplateRecord) getTableEntries(DPUTemplateRecord.class, em).get(0);
        Assert.assertNull(child.getParent());
    }

    private List getTableEntries(Class entityClass, EntityManager em) {
        return em.createQuery("SELECT x FROM " + entityClass.getName() + " x", entityClass).getResultList();
    }

    private abstract class DeleteConstraintTest {
        void ensureReferencingInstanceDelete(Class referencingClass, Class referencedClass) {
            EntityManager em = factory.createEntityManager();

            int referencingClassInstancesBeforeCreate = getTableEntries(referencingClass, em).size();
            int referencedClassInstancesBeforeCreate = getTableEntries(referencedClass, em).size();

            Object referencedInstance = createReferencedInstance(em);

            Assert.assertEquals(referencingClassInstancesBeforeCreate + 1, getTableEntries(referencingClass, em).size());
            Assert.assertEquals(referencedClassInstancesBeforeCreate + 1, getTableEntries(referencedClass, em).size());

            em.getTransaction().begin();
            em.remove(referencedInstance);
            em.getTransaction().commit();
            Assert.assertEquals(referencingClassInstancesBeforeCreate, getTableEntries(referencingClass, em).size());
            Assert.assertEquals(referencedClassInstancesBeforeCreate, getTableEntries(referencedClass, em).size());

            em.close();
        }

        abstract Object createReferencedInstance(EntityManager em);
    }
}
