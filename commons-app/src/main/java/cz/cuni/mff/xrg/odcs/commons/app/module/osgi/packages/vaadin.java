package cz.cuni.mff.xrg.odcs.commons.app.module.osgi.packages;

/**
 * Contains list of Vaadin packages that will be exported from frontend.
 * 
 * @author Petyr
 */
public final class vaadin {

    /**
     * Used Vaadin version.
     */
    public static final String VERSION = "7.3.7";

    /**
     * List of OSGI packages to export. Does not start nor end with separator.
     */
    public static final String PACKAGES =
            "VAADIN.widgetsets;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs.5B88AE52043D503541FF84C6F9439C93;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs.617EF3F8DEF010596EABBFEBCDAEB4D6;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs.A626F958CF5599B10C85FED27BA47695;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs.BA77EB69753DFCF890DF2842B1818FC1;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs.F29C6FFB4F0F3E9CAB7B33C0D5049E63;version=\"" + VERSION + "\"," +
                    "VAADIN.widgetsets.com.vaadin.DefaultWidgetSet.deferredjs.FC14AE57E6E21B8995AE9DA8E418484C;version=\"" + VERSION + "\"," +
                    "VAADIN;version=\"" + VERSION + "\"," +
                    "com.vaadin;version=\"" + VERSION + "\"," +
                    "com.vaadin.annotations;version=\"" + VERSION + "\"," +
                    "com.vaadin.data;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.fieldgroup;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.converter;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.filter;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.sqlcontainer;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.sqlcontainer.connection;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.sqlcontainer.query;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.sqlcontainer.query.generator;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.util.sqlcontainer.query.generator.filter;version=\"" + VERSION + "\"," +
                    "com.vaadin.data.validator;version=\"" + VERSION + "\"," +
                    "com.vaadin.event;version=\"" + VERSION + "\"," +
                    "com.vaadin.event.dd;version=\"" + VERSION + "\"," +
                    "com.vaadin.event.dd.acceptcriteria;version=\"" + VERSION + "\"," +
                    "com.vaadin.navigator;version=\"" + VERSION + "\"," +
                    "com.vaadin.server;version=\"" + VERSION + "\"," +
                    "com.vaadin.server.communication;version=\"" + VERSION + "\"," +
                    "com.vaadin.server.themeutils;version=\"" + VERSION + "\"," +
                    "com.vaadin.server.widgetsetutils;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.components;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.components.calendar;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.components.calendar.event;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.components.calendar.handler;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.components.colorpicker;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.doc-files;version=\"" + VERSION + "\"," +
                    "com.vaadin.ui.themes;version=\"" + VERSION + "\"," +
                    "com.vaadin.util;version=\"" + VERSION + "\"," +
                    "com.vaadin;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.annotations;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.communication;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.extension;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.extension.javascriptmanager;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.absolutelayout;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.browserframe;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.button;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.calendar;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.checkbox;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.colorpicker;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.combobox;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.csslayout;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.customlayout;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.datefield;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.dd;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.draganddropwrapper;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.embedded;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.flash;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.form;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.gridlayout;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.image;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.label;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.link;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.menubar;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.optiongroup;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.orderedlayout;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.panel;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.popupview;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.progressindicator;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.slider;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.splitpanel;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.table;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.tabsheet;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.textarea;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.textfield;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.tree;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.treetable;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.twincolselect;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.ui;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.video;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.ui.window;version=\"" + VERSION + "\"," +
                    "com.vaadin.shared.util;version=\"" + VERSION + "\"," +
                    "com.vaadin.buildhelpers;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.expression;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.expression.exception;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.handler;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.parser;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.resolver;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.selector;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.tree;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.tree.controldirective;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.util;version=\"" + VERSION + "\"," +
                    "com.vaadin.sass.internal.visitor;version=\"" + VERSION + "\"," +
                    "com.wcs.wcslib.vaadin.widget.multifileupload.ui;version=\"1.7.1\"";
}
