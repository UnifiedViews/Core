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
package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.text.Collator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPURecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.transfer.ExportService;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.SimpleTreeFilter;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Tree showing available DPUs. Contains filters by accessibility and name. It
 * is possible to make nodes draggable and to add custom click listeners.
 * 
 * @author Bogo
 */
@org.springframework.stereotype.Component
@Scope("prototype")
public class DPUTree extends CustomComponent {

    private static final long serialVersionUID = -8635330869349339394L;

    public static final String MENU_NAME_PROPERTY = "menuName";

    VerticalLayout layoutTree;

    VerticalLayout mainLayout;

    Tree dpuTree;

    Button btnMinimize;

    Button btnExpand;

    Button buttonCreateDPU;

    private Button exportButton;

    GridLayout filterBar;

    boolean isExpandable = false;

    private Filter visibilityFilter;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private DPUCreate createDPU;

    @Autowired
    private ExportService exportService;

    @Autowired
    private PermissionUtils permissionUtils;

    private HorizontalLayout topLine;

    private Window.CloseListener createDPUCloseListener;

    private boolean isValid = true;

    /**
     * Creates new DPUTree.
     */
    public DPUTree() {
    }

    @PostConstruct
    private void initialize() {
        buildMainLayout();

        visibilityFilter = new Filter() {
            private static final long serialVersionUID = -1761725398586311364L;

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                if (itemId.getClass() != DPUTemplateRecord.class) {
                    return true;
                }
                DPUTemplateRecord dpu = (DPUTemplateRecord) itemId;
                if (dpu == null || dpu.getShareType() == null) {
                    return false;
                }
                return ShareType.PRIVATE.equals(dpu.getShareType());
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return true;
            }
        };

        createDPUCloseListener = new Window.CloseListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void windowClose(Window.CloseEvent e) {
                //refresh DPU tree after closing DPU Template creation dialog 
                refresh();
            }
        };

        setCompositionRoot(mainLayout);
    }

    /**
     * Builds layout.
     */
    private void buildMainLayout() {

        mainLayout = new VerticalLayout();

        layoutTree = new VerticalLayout();
        layoutTree.setSpacing(true);
        layoutTree.setImmediate(true);
        layoutTree.setHeight("100%");
        layoutTree.setMargin(true);
        mainLayout.setStyleName("dpuTreeLayout");

        //Expandable part of the component
        topLine = new HorizontalLayout();
        topLine.setWidth(100, Unit.PERCENTAGE);
        Label lblTree = new Label(Messages.getString("DPUTree.template.tree"));
        lblTree.setWidth(160, Unit.PIXELS);
        topLine.addComponent(lblTree);
        btnMinimize = new Button();
        btnMinimize.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = -5401352180513254192L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setTreeState(false);
            }
        });
        btnMinimize.setStyleName(BaseTheme.BUTTON_LINK);
        btnMinimize.setIcon(new ThemeResource("icons/collapse.svg"));
        btnMinimize.setDescription(Messages.getString("DPUTree.minimize.tree"));
        topLine.addComponent(btnMinimize);
        topLine.setExpandRatio(btnMinimize, 1.0f);
        btnExpand = new Button();
        btnExpand.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 2099560102850977680L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setTreeState(true);
            }
        });
        btnExpand.setStyleName(BaseTheme.BUTTON_LINK);
        btnExpand.setIcon(new ThemeResource("icons/expand.svg"));
        btnExpand.setDescription(Messages.getString("DPUTree.expand.tree"));
        btnExpand.setVisible(false);
        topLine.addComponent(btnExpand);
        topLine.setExpandRatio(btnExpand, 1.0f);
        topLine.setComponentAlignment(btnExpand, Alignment.TOP_RIGHT);
        topLine.setVisible(isExpandable);
        mainLayout.addComponent(topLine);

        buttonCreateDPU = new Button();
        buttonCreateDPU.setCaption(Messages.getString("DPUTree.create.dpu"));
        buttonCreateDPU.setHeight("25px");
        buttonCreateDPU.setWidth("180px");
        buttonCreateDPU
                .addClickListener(new Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        //Open the dialog for DPU Template creation
                        if (!UI.getCurrent().getWindows().contains(createDPU)) {
                            createDPU.initClean();
                            UI.getCurrent().addWindow(createDPU);
                            createDPU.removeCloseListener(createDPUCloseListener);
                            createDPU.addCloseListener(createDPUCloseListener);
                        } else {
                            createDPU.bringToFront();
                        }

                    }
                });

        exportButton = new Button(Messages.getString("DPUTree.export.dpu"));
        exportButton.setHeight("25px");
        exportButton.setWidth("180px");

        exportButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 6941128812967827740L;

            @Override
            public void buttonClick(ClickEvent event) {
                DPUTemplatesExport exportWindow = new DPUTemplatesExport(dpuFacade.getAllTemplates(), exportService);
                UI.getCurrent().addWindow(exportWindow);
            }
        });

        buttonCreateDPU.setVisible(isExpandable);
//        exportButton.setVisible(isExpandable);
        mainLayout.addComponent(buttonCreateDPU);
        mainLayout.addComponent(exportButton);

        // DPURecord tree filters
        filterBar = new GridLayout(2, 2);
        filterBar.setSpacing(false);

        CheckBox onlyMyDPU = new CheckBox();
        onlyMyDPU.setCaption(Messages.getString("DPUTree.private.only"));
        onlyMyDPU.setStyleName("private");
        onlyMyDPU.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 9200593350155429057L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                boolean onlyMy = (boolean) event.getProperty().getValue();
                Container.Filterable f = (Container.Filterable) dpuTree.getContainerDataSource();
                if (onlyMy) {
                    f.addContainerFilter(visibilityFilter);
                } else {
                    f.removeContainerFilter(visibilityFilter);
                }
            }
        });
        filterBar.addComponent(onlyMyDPU, 0, 0, 1, 0);

        TextField treeFilter = new TextField();
        treeFilter.setImmediate(false);
        treeFilter.setInputPrompt(Messages.getString("DPUTree.filter.tree"));
        treeFilter.addTextChangeListener(new FieldEvents.TextChangeListener() {
            private static final long serialVersionUID = -8569331871410134460L;

            SimpleTreeFilter filter = null;

            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                Container.Filterable f = (Container.Filterable) dpuTree
                        .getContainerDataSource();

                // Remove old filter
                if (filter != null) {
                    f.removeContainerFilter(filter);
                }

                // Set new filter
                filter = new SimpleTreeFilter(event.getText(), true, false);
                f.addContainerFilter(filter);

            }
        });

        filterBar.addComponent(treeFilter, 1, 1);
        filterBar.setSizeUndefined();
        layoutTree.addComponent(filterBar);
        layoutTree.setExpandRatio(filterBar, 0.05f);

        // DPURecord tree 
        dpuTree = new Tree() {
            public void setValue(Object newValue) throws Property.ReadOnlyException {
                if (isValid) {
                    super.setValue(newValue);
                } else {
                    isValid = true;
                }
            };

            @Override
            protected void setValue(Object newValue, boolean repaintIsNotNeeded) throws com.vaadin.data.Property.ReadOnlyException {
                if (isValid) {
                    super.setValue(newValue, repaintIsNotNeeded);
                } else {
                    isValid = true;
                }
            }
        };
        dpuTree.setImmediate(true);
        dpuTree.setHeight("100%");
        //	dpuTree.setHeight(600, Unit.PIXELS);
        dpuTree.setStyleName("dpuTree");
        dpuTree.setItemCaptionPropertyId(MENU_NAME_PROPERTY); // which property should be used to retrieve caption for tree item
        dpuTree.setItemStyleGenerator(new Tree.ItemStyleGenerator() {
            private static final long serialVersionUID = -3033372681114948667L;

            @Override
            public String getStyle(Tree source, Object itemId) {
                DPUTemplateRecord dpu = (DPUTemplateRecord) itemId;
                if (dpu.getShareType() == ShareType.PRIVATE) {
                    return "private";
                } else {
                    return "public";
                }
            }
        });
        ((HierarchicalContainer) dpuTree.getContainerDataSource()).setIncludeParentsWhenFiltering(true);
        ((HierarchicalContainer) dpuTree.getContainerDataSource()).addContainerProperty(MENU_NAME_PROPERTY, String.class, null);
        ((HierarchicalContainer) dpuTree.getContainerDataSource()).setItemSorter(new ItemSorter() {
            private static final long serialVersionUID = -3394104490891279840L;

            private Collator collator = Collator.getInstance(LocaleHolder.getLocale());

            @Override
            public void setSortProperties(Container.Sortable container, Object[] propertyId, boolean[] ascending) {
                //Ignore
            }

            @Override
            public int compare(Object itemId1, Object itemId2) {
                DPUTemplateRecord first = (DPUTemplateRecord) itemId1;
                DPUTemplateRecord second = (DPUTemplateRecord) itemId2;
                if (first.getId() == null || second.getId() == null) { // we dont compare first leaves under root of tree
                    return 0;
                }
                String firstName = StringUtils.isEmpty(first.getMenuName()) ? first.getName() : first.getMenuName();
                String secondName = StringUtils.isEmpty(second.getMenuName()) ? second.getName() : second.getMenuName();
                return collator.compare(firstName, secondName);
            }
        });

        layoutTree.addComponent(dpuTree);
        layoutTree.setComponentAlignment(dpuTree, Alignment.TOP_LEFT);
        layoutTree.setExpandRatio(dpuTree, 0.95f);
        mainLayout.addComponent(layoutTree);
    }

    /**
     * Fill DPU tree with data.
     */
    public void fillTree() {
        Container.Filterable f = (Container.Filterable) dpuTree.getContainerDataSource();
        Collection<Filter> filters = new LinkedList<>(f.getContainerFilters());
        f.removeAllContainerFilters();
        fillTree(dpuTree);
        for (Object itemId : dpuTree.rootItemIds()) {
            dpuTree.expandItemsRecursively(itemId);
        }
        for (Filter filter : filters) {
            f.addContainerFilter(filter);
        }
        setButtonsVisible();
    }

    /**
     * Adds custom ItemClickListener to the DPUTRee.
     * 
     * @param itemClickListener
     *            {@link ItemClickListener} to add
     *            to DPU tree.
     */
    public void addItemClickListener(
            ItemClickEvent.ItemClickListener itemClickListener) {
        dpuTree.addItemClickListener(itemClickListener);
    }

    public void setValue(Object newValue) {
        dpuTree.select(null);
        dpuTree.select(newValue);
        isValid = false;
    }

    public Object getValue() {
        return dpuTree.getValue();
    }

    /**
     * Reloads the contents of the DPUTree.
     */
    public void refresh() {
        fillTree();
        markAsDirty();
    }

    /**
     * Fills tree with available DPUs.
     * 
     * @param tree
     *            {@link Tree} to fill.
     */
    @SuppressWarnings("unchecked")
    private void fillTree(Tree tree) {
        tree.removeAllItems();

        Item item;
        DPURecord rootExtractor = addDPUToTreeRoot(Messages.getString("DPUTree.extractors"), tree);
        DPURecord rootTransformer = addDPUToTreeRoot(Messages.getString("DPUTree.transformers"), tree);
        DPURecord rootLoader = addDPUToTreeRoot(Messages.getString("DPUTree.loaders"), tree);
        DPURecord rootQuality = addDPUToTreeRoot(Messages.getString("DPUTree.quality"), tree);

        List<DPUTemplateRecord> dpus = dpuFacade.getAllTemplates();
        for (DPUTemplateRecord dpu : dpus) {
            addDPUToTree(dpu, tree, rootExtractor, rootTransformer, rootLoader, rootQuality);
        }

        for (Object itemId : tree.rootItemIds()) {
            tree.expandItemsRecursively(itemId);
        }

        ((HierarchicalContainer) tree.getContainerDataSource()).sort(null, null);
    }

    private DPURecord addDPUToTreeRoot(String name, Tree tree) {
        DPURecord dpuRecord = new DPUTemplateRecord(name, null);
        dpuRecord.setMenuName(name);
        Item item = tree.addItem(dpuRecord);
        item.getItemProperty(MENU_NAME_PROPERTY).setValue(name);

        return dpuRecord;
    }

    private void addDPUToTree(DPUTemplateRecord dpu, Tree tree, DPURecord rootExtractor, DPURecord rootTransformer, DPURecord rootLoader, DPURecord rootQuality) {
        if (dpu.getType() == null) { // we ignore DPU's without type
            return;
        }
        if (tree.containsId(dpu)) { // if DPU is already in tree, ignore it
            return;
        }
        Item item = tree.addItem(dpu);
        item.getItemProperty(MENU_NAME_PROPERTY).setValue(dpu.getMenuName());

        DPUTemplateRecord parent = dpu.getParent();
        if (parent != null) {
            addDPUToTree(parent, tree, rootExtractor, rootTransformer, rootLoader, rootQuality); // we must ensure that parent is already in tree before we set it as parent to tree
            tree.setParent(dpu, parent);
        } else {
            switch (dpu.getType()) {
                case EXTRACTOR:
                    tree.setParent(dpu, rootExtractor);
                    break;
                case TRANSFORMER:
                    tree.setParent(dpu, rootTransformer);
                    break;
                case LOADER:
                    tree.setParent(dpu, rootLoader);
                    break;
                case QUALITY:
                    tree.setParent(dpu, rootQuality);
                    break;
                default:
                    throw new IllegalArgumentException(Messages.getString("DPUTree.unknown") + dpu.getType());
            }
        }
    }

    private void setTreeState(boolean isStateExpanded) {
        btnMinimize.setVisible(isExpandable && isStateExpanded);
        btnExpand.setVisible(isExpandable && !isStateExpanded);
        buttonCreateDPU.setVisible(isExpandable && isStateExpanded && this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_CREATE));
        exportButton.setVisible(isExpandable && isStateExpanded && this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_EXPORT));
        layoutTree.setVisible(isStateExpanded);
        mainLayout.setSizeUndefined();
    }

    /**
     * Sets nodes of the tree drag-able.
     * 
     * @param dragable
     *            True if the nodes should be drag-able, false otherwise.
     */
    public void setDragable(boolean dragable) {
        if (dragable) {
            dpuTree.setDragMode(Tree.TreeDragMode.NODE);
        } else {
            dpuTree.setDragMode(Tree.TreeDragMode.NONE);
        }
    }

    /**
     * Set DPU tree expandable.
     * 
     * @param expandable
     */
    public void setExpandable(boolean expandable) {
        this.isExpandable = expandable;
        topLine.setVisible(isExpandable);
        setButtonsVisible();
    }

    private void setButtonsVisible() {
        this.buttonCreateDPU.setVisible(this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_CREATE));
        this.exportButton.setVisible(this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_EXPORT));
    }

    @Override
    public Collection<?> getListeners(Class<?> eventType) {
        if (eventType == ItemClickEvent.class) {
            return dpuTree.getListeners(eventType);
        }
        return super.getListeners(eventType);
    }
}
