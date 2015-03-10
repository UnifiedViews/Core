package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import java.io.File;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.auth.SharedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.user.Organization;
import cz.cuni.mff.xrg.odcs.commons.app.user.OrganizationSharedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.OwnedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Representation of template for creating {@link DPUInstanceRecord}s. The
 * purpose of templating DPUs is to unburden user from manually edit and
 * configure all DPU properties when creating pipelines. Template's properties
 * are propagated to {@link DPUInstanceRecord} everytime it is created as an
 * instance of given {@link DPUTemplateRecord}. Reference to template is
 * preserved in each DPU instance, so that updates of configuration can be
 * propagated to template's children.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
@Entity
@Table(name = "dpu_template")
public class DPUTemplateRecord extends DPURecord
        implements OwnedEntity, SharedEntity {

    /**
     * Visibility in DPUTree.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "visibility")
    private ShareType shareType;

    /**
     * Description obtained from jar file manifest.
     */
    @Column(name = "jar_description")
    private String jarDescription;

    /**
     * DPURecord type, determined by associated jar file. It's transitive for
     * non-root templates.
     */
    @Enumerated(EnumType.ORDINAL)
    private DPUType type;

    /**
     * Name of directory where {@link #jarName} is located. It's transitive for
     * non-root templates.
     * 
     * @see cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig
     */
    @Column(name = "jar_directory")
    private String jarDirectory;

    /**
     * DPU's jar file name. It's transitive for non-root templates.
     * 
     * @see cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig
     */
    @Column(name = "jar_name")
    private String jarName;

    /**
     * Parent {@link DPUTemplateRecord}. If parent is set, this DPURecord is
     * rendered under its parent in DPU tree.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private DPUTemplateRecord parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    /**
     * Empty ctor for JPA.
     */
    public DPUTemplateRecord() {
    }

    /**
     * Creates new {@link DPUTemplateRecord}.
     * 
     * @param name
     *            Template name.
     * @param type
     *            {@link DPUType} of the template.
     */
    public DPUTemplateRecord(String name, DPUType type) {
        super(name);
        this.type = type;
    }

    /**
     * Copy constructor. New instance always has private shareType, no matter
     * what setting was in original DPU.
     * 
     * @param dpu
     */
    public DPUTemplateRecord(DPUTemplateRecord dpu) {
        super(dpu);
        shareType = ShareType.PRIVATE;
        type = dpu.type;
        parent = dpu.parent;

        if (parent == null) {
            jarDirectory = dpu.jarDirectory;
            jarName = dpu.jarName;
            jarDescription = dpu.jarDescription;
        } else {
            jarDirectory = null;
            jarName = null;
            jarDescription = null;
        }
    }

    /**
     * Create template from given instance.
     * 
     * @param dpuInstance
     */
    public DPUTemplateRecord(DPUInstanceRecord dpuInstance) {
        super(dpuInstance);
        this.shareType = ShareType.PRIVATE;
        this.type = dpuInstance.getType();

        // copy jarDescription from template of previous one ..
        this.jarDescription = dpuInstance.getTemplate() == null
                ? null
                : dpuInstance.getTemplate().getJarDescription();
    }

    /**
     * @param owner
     *            New template owner.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public ShareType getShareType() {
        return shareType;
    }

    /**
     * @param shareType
     *            New template ShareType.
     */
    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    /**
     * Description of used jar file. If the template is not root template
     * ask it's parent template for description and return it.
     * 
     * @return Description of jar file.
     */
    public String getJarDescription() {
        if (parent == null) {
            // top level DPU
            return jarDescription;
        } else {
            return parent.getJarDescription();
        }
    }

    /**
     * Set jar description. If the template is not root (ie. has parent)
     * then this call is ignored.
     * 
     * @param jarDescription
     *            New jar description.
     */
    public void setJarDescription(String jarDescription) {
        if (parent == null) {
            // top level DPU
            this.jarDescription = jarDescription;
        } else {
            // ignore ..
        }
    }

    /**
     * @return Template parent, null if there is no parent for this template.
     */
    public DPUTemplateRecord getParent() {
        return parent;
    }

    /**
     * Set template parent. If the template has parent and new is set then
     * nothing special happen.
     * If the dpu has parent and we set null, then the DPU copy jar information
     * from it's parent to it self.
     * If the dpu has no parent and we assign one, then the DPU's jar
     * informations will be read from new parent. The current DPU's jar
     * information of template will be in such case forgotten.
     * 
     * @param newParent
     *            New parent.
     */
    public void setParent(DPUTemplateRecord newParent) {

        if (this.parent == null && newParent != null) {
            // it was top, now it's not .. so it can take the values
            // from our new parent
            jarDirectory = null;
            jarName = null;
            jarDescription = null;
            type = null;
        } else if (this.parent != null && newParent == null) {
            // we was not top, now we are .. we need to take the valuse
            // from out parent and save them as ours
            jarDirectory = this.getParent().jarDirectory;
            jarName = this.getParent().jarName;
            jarDescription = this.getParent().jarDescription;
            type = this.getParent().type;
        } else {
            // null -> null = no change, we preserve our data
            // not null -> not null = no change, as we have nulls before and now
        }

        this.parent = newParent;
    }

    /**
     * If the template has no parent then it set type to this template, otherwise
     * recall this function on the parent.
     * 
     * @param type
     *            New type.
     */
    public void setType(DPUType type) {
        if (parent == null) {
            this.type = type;
        } else {
            parent.setType(type);
        }
    }

    @Override
    public DPUType getType() {
        if (parent == null) {
            return this.type;
        } else {
            return parent.getType();
        }
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
        instance = moduleFacade.getInstance(this);
    }

    @Override
    public String getJarPath() {
        if (parent == null) {
            // to level DPU
            if (jarDirectory == null || jarDirectory.isEmpty()) {
                return jarName;
            } else {
                return jarDirectory + File.separator + jarName;
            }
        } else {
            return parent.getJarPath();
        }
    }

    /**
     * @return name of jar-sub directory.
     */
    public String getJarDirectory() {
        if (parent == null) {
            // top level DPU
            return jarDirectory;
        } else {
            return parent.getJarDirectory();
        }
    }

    /**
     * Set jar directory for given DPU template. If the DPU has parent then
     * nothing happened.
     * 
     * @param jarDirectory
     */
    public void setJarDirectory(String jarDirectory) {
        if (parent == null) {
            // top level DPU
            this.jarDirectory = jarDirectory;
        } else {
            // ignore ..
        }
    }

    /**
     * @return name of given jar file.
     */
    public String getJarName() {
        if (parent == null) {
            // top level DPU
            return jarName;
        } else {
            return parent.getJarName();
        }
    }

    /**
     * Set jar name for given DPU template. If the DPU has parent then nothing
     * happened.
     * 
     * @param jarName
     */
    public void setJarName(String jarName) {
        if (parent == null) {
            // top level DPU
            this.jarName = jarName;
        } else {
            // ignore ..
        }
    }

    /**
     * @return true if DPU jar can be replaced, false otherwise.
     */
    public boolean jarFileReplacable() {
        return parent == null;
    }

}
