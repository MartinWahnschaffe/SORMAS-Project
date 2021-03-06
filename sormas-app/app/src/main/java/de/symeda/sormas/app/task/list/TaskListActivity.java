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

package de.symeda.sormas.app.task.list;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.widget.AdapterView;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Random;

import androidx.lifecycle.ViewModelProviders;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.task.edit.TaskNewActivity;
import de.symeda.sormas.app.BaseListActivity;
import de.symeda.sormas.app.BaseListFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.component.menu.PageMenuItem;

public class TaskListActivity extends BaseListActivity {

    private TaskStatus statusFilters[] = new TaskStatus[]{TaskStatus.PENDING, TaskStatus.DONE, TaskStatus.NOT_EXECUTABLE};
    private TaskListViewModel model;

    public static void startActivity(Context context, TaskStatus listFilter) {
        BaseListActivity.startActivity(context, TaskListActivity.class, buildBundle(listFilter));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPreloader();
        adapter = new TaskListAdapter(R.layout.row_task_list_item_layout);
        model = ViewModelProviders.of(this).get(TaskListViewModel.class);
        model.getTasks().observe(this, tasks -> {
            ((TaskListAdapter) adapter).replaceAll(tasks);
            hidePreloader();
        });
        setOpenPageCallback(p -> {
            showPreloader();
            model.setTaskStatusAndReload(statusFilters[p.getKey()]);
        });
    }

    @Override
    public List<PageMenuItem> getPageMenuData(){
        return PageMenuItem.fromEnum(statusFilters, getContext());
    }

    @Override
    public int onNotificationCountChangingAsync(AdapterView parent, PageMenuItem menuItem, int position) {
        //TODO: Call database and retrieve notification count
        return (int) (new Random(DateTime.now().getMillis() * 1000).nextInt() / 10000000);
    }

    @Override
    protected BaseListFragment buildListFragment(PageMenuItem menuItem) {
        if (menuItem != null) {
            TaskStatus listFilter = statusFilters[menuItem.getKey()];
            return TaskListFragment.newInstance(listFilter);
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getNewMenu().setTitle(R.string.action_new_task);
        return true;
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_tasks_list;
    }

    @Override
    public void goToNewView() {
        TaskNewActivity.startActivity(getContext());
    }

    @Override
    protected boolean isEntryCreateAllowed() {
        return ConfigProvider.hasUserRight(UserRight.TASK_CREATE);
    }
}
