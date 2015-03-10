package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.properties.DbRuntimeProperties;
import cz.cuni.mff.xrg.odcs.commons.app.properties.RuntimeProperty;

/**
 * Facade for fetching persisted entities.
 *
 * @author mvi
 */
@Transactional(readOnly = true)
public class RuntimePropertiesFacadeImpl implements RuntimePropertiesFacade {

    @Autowired
    private DbRuntimeProperties runtimePropertiesDao;

    @Override
    public List<RuntimeProperty> getAllRuntimeProperties() {
        return runtimePropertiesDao.getAll();
    }

    @Override
    public RuntimeProperty getByName(String name) {
        return runtimePropertiesDao.getByName(name);
    }

    @Transactional
    @Override
    public void save(RuntimeProperty property) {
        runtimePropertiesDao.save(property);
    }

    @Transactional
    @Override
    public void delete(RuntimeProperty property) {
        runtimePropertiesDao.delete(property);
    }
}
