package cz.cuni.mff.xrg.odcs.frontend.gui.views;

/**
 * Interface for presenter that can change the application data.
 * 
 * @author Petyr
 */
public interface SavablePresenter extends Presenter {

    /**
     * Return true if there are unsaved changes in presenter.
     * 
     * @return If is modified
     */
    boolean isModified();

    /**
     * Save data in presenter.
     * It is called when isModified returns true and user decides to save the changes
     */
    void save();

}
