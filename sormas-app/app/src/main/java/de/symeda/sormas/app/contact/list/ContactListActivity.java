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

package de.symeda.sormas.app.contact.list;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.widget.AdapterView;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Random;

import androidx.lifecycle.ViewModelProviders;
import de.symeda.sormas.api.contact.FollowUpStatus;
import de.symeda.sormas.app.BaseListActivity;
import de.symeda.sormas.app.BaseListFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.component.menu.PageMenuItem;

public class ContactListActivity extends BaseListActivity {

    private FollowUpStatus statusFilters[] = new FollowUpStatus[]{FollowUpStatus.FOLLOW_UP, FollowUpStatus.COMPLETED,
            FollowUpStatus.CANCELED, FollowUpStatus.LOST, FollowUpStatus.NO_FOLLOW_UP};
    private ContactListViewModel model;

    public static void startActivity(Context context, FollowUpStatus listFilter) {
        BaseListActivity.startActivity(context, ContactListActivity.class, buildBundle(listFilter));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPreloader();
        adapter = new ContactListAdapter(R.layout.row_read_contact_list_item_layout, FollowUpStatus.FOLLOW_UP);
        model = ViewModelProviders.of(this).get(ContactListViewModel.class);
        model.getContacts().observe(this, contacts -> {
            ((ContactListAdapter) adapter).replaceAll(contacts);
            hidePreloader();
        });
        setOpenPageCallback(p -> {
            showPreloader();
            model.setFollowUpStatusAndReload(statusFilters[p.getKey()]);
        });
    }

    @Override
    public List<PageMenuItem> getPageMenuData() {
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
            FollowUpStatus listFilter = statusFilters[menuItem.getKey()];
            return ContactListFragment.newInstance(listFilter);
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getNewMenu().setTitle(R.string.action_new_contact);
        return true;
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_contacts_list;
    }

}
