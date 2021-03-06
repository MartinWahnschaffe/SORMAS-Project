/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.component.menu;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.app.core.enumeration.StatusElaborator;
import de.symeda.sormas.app.core.enumeration.StatusElaboratorFactory;


public class PageMenuItem {

    private int key;
    private int notificationCount;
    private int iconResourceId;
    private String title;
    private String description;
    private boolean active;

    /**
     * key will be the index of the entry in the array
     */
    public static List<PageMenuItem> fromEnum(Enum[] values, Context context) {
        List<PageMenuItem> menuItems = new ArrayList<>();
        int key = 0;
        for (Enum value : values) {
            StatusElaborator elaborator = StatusElaboratorFactory.getElaborator(value);
            menuItems.add(new PageMenuItem(key, elaborator.getFriendlyName(context), value.toString(), elaborator.getIconResourceId(), false));
            key++;
        }
        return menuItems;
    }

    public PageMenuItem(int key, String title, String description, int iconResourceId, int notificationCount, boolean active) {
        this.key = key;
        this.notificationCount = notificationCount;
        this.iconResourceId = iconResourceId;
        this.title = title;
        this.description = description;
        this.active = active;
    }

    public PageMenuItem(int key, String title, String description, int iconResourceId, boolean active) {
        this.key = key;
        this.notificationCount = 0;
        this.iconResourceId = iconResourceId;
        this.title = title;
        this.description = description;
        this.active = active;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public void setIcon(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNotificationCount() {
        return this.notificationCount;
    }

    public int getIcon() {
        return this.iconResourceId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
