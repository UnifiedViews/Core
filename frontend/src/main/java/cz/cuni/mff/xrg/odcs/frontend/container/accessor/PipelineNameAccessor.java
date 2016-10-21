package cz.cuni.mff.xrg.odcs.frontend.container.accessor;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessorBase;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;

/**
 * Accessor used for containers that just list the pipelines.
 * 
 * @author Petyr
 */
public class PipelineNameAccessor extends ClassAccessorBase<Pipeline> {

    /**
     * Constructor.
     */
    public PipelineNameAccessor() {
        super(Pipeline.class);

        addNon(Long.class, "id", new ColumnGetter<Long>() {
            @Override
            public Long get(Pipeline object) {
                return object.getId();
            }
        });

        addNon(String.class, "name", new ColumnGetter<String>() {
            @Override
            public String get(Pipeline object) {
                String name = object.getName();
                return name.length() > Utils.getColumnMaxLenght() ? name.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : name;
            }
        });

        addNon(String.class, "description", new ColumnGetter<String>() {
            @Override
            public String get(Pipeline object) {
                String description = object.getDescription();
                if (description == null) {
                    return null;
                }
                return description.length() > Utils.getColumnMaxLenght() ? description.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : description;
            }
        });

    }
}
