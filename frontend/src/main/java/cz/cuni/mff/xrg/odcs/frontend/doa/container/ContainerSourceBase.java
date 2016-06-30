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
package cz.cuni.mff.xrg.odcs.frontend.doa.container;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

import java.util.ArrayList;
import java.util.List;

public class ContainerSourceBase<T extends DataObject> implements ContainerSource<T> {

    private List<T> dataItems;

    private ClassAccessor<T> classAccessor;

    public ContainerSourceBase(List<T> dataItems, ClassAccessor<T> classAccessor) {
        this.dataItems = dataItems;
        this.classAccessor = classAccessor;
    }

    @Override
    public int size() {
        return dataItems.size();
    }

    @Override
    public T getObject(Long id) {
        for (T item : dataItems) {
            if (item.getId().equals(id))
                return item;
        }

        throw new IllegalArgumentException("Item with id " + id.toString() + " not found");
    }

    @Override
    public T getObjectByIndex(int index) {
        return dataItems.get(index);
    }

    @Override
    public int indexOfId(Long itemId) {
        int index = 0;
        for (T item : dataItems) {
            if (item.getId().equals(itemId))
                return index;
            index++;
        }

        return -1;
    }

    @Override
    public boolean containsId(Long id) {
        try {
            getObject(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<Long> getItemIds(int startIndex, int numberOfItems) {
        List<Long> items = new ArrayList();

        for (int i = 0; i < dataItems.size(); i++) {
            if (i >= startIndex && (i - startIndex < numberOfItems))
                items.add(dataItems.get(i).getId());
        }

        return items;
    }

    @Override
    public ClassAccessor<T> getClassAccessor() {
        return classAccessor;
    }

    public void setDataItems(List<T> dataItems) {
        this.dataItems = dataItems;
    }
}
