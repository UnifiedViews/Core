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
package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import org.eclipse.persistence.annotations.Index;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent the DPU instance pipeline placement in DB.
 *
 * @author Petyr
 * @author Jan Vojt
 */
@Entity
@Table(name = "dpu_instance")
@Index(name="ix_DPU_INSTANCE", columnNames = "dpu_id")
public class DPUInstanceRecord extends DPURecord {

    /**
     * Template used for creating this instance.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dpu_id")
    private DPUTemplateRecord template;

    /**
     * If true then this instance use owner template configuration.
     */
    @Column(name = "use_template_config", nullable = false)
    private boolean useTemplateConfig;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "dpuInstance")
    private Set<MessageRecord> messageRecords = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "dpuInstance")
    private Node node;

    /**
     * Empty constructor because of JPA.
     */
    public DPUInstanceRecord() {
    }

    /**
     * Copy constructor. Creates a copy of given <code>DPUInstanceRecord</code>.
     * Primary key {@link #id} of newly created object is <code>null</code>.
     * Copying is NOT propagated on {@link #template}, original reference is
     * preserved.
     *
     * @param dpuInstance
     */
    public DPUInstanceRecord(DPUInstanceRecord dpuInstance) {
        super(dpuInstance);
        template = dpuInstance.getTemplate();
        this.useTemplateConfig = dpuInstance.useTemplateConfig;
    }

    /**
     * Create new DPUInstanceRecord with given name and type.
     *
     * @param name
     */
    public DPUInstanceRecord(String name) {
        super(name);
        this.useTemplateConfig = false;
    }

    /**
     * Create instance based on given template.
     *
     * @param template
     */
    public DPUInstanceRecord(DPUTemplateRecord template) {
        // construct DPURecord
        super(template);
        // and set out variables
        this.template = template;
        this.useTemplateConfig = false;
    }

    /**
     * @return Used {@link DPUTemplateRecord}.
     */
    public DPUTemplateRecord getTemplate() {
        return template;
    }

    /**
     * @param template
     *            New {@link DPUTemplateRecord}.
     */
    public void setTemplate(DPUTemplateRecord template) {
        this.template = template;
    }

    /**
     * @return true if dpu should use template configuration, false if dpu should use instance configuration
     */
    public boolean isUseTemplateConfig() {
        return useTemplateConfig;
    }

    /**
     * @param useTemplateConfig
     *            true if dpu should use template configuration, false if dpu should use instance configuration
     */
    public void setUseTemplateConfig(boolean useTemplateConfig) {
        this.useTemplateConfig = useTemplateConfig;
    }

    @Override
    public DPUType getType() {
        return template.getType();
    }

    /**
     * Load DPU's instance from associated jar file.
     *
     * @param moduleFacade
     *            ModuleFacade used to load DPU.
     * @throws ModuleException
     */
    @Override
    public void loadInstance(ModuleFacade moduleFacade) throws ModuleException {
        instance = moduleFacade.getInstance(template);
    }

    @Override
    public String getJarPath() {
        return template.getJarPath();
    }

    public Set<MessageRecord> getMessageRecords() {
        return messageRecords;
    }

    public void setMessageRecords(Set<MessageRecord> messageRecords) {
        this.messageRecords = messageRecords;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

}
