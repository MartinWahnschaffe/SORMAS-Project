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

package de.symeda.sormas.app.caze.edit;

import android.content.res.Resources;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.sample.Sample;
import de.symeda.sormas.app.core.adapter.databinding.OnListItemClickListener;
import de.symeda.sormas.app.databinding.FragmentFormListLayoutBinding;
import de.symeda.sormas.app.sample.edit.SampleEditActivity;
import de.symeda.sormas.app.sample.list.SampleListAdapter;
import de.symeda.sormas.app.sample.list.SampleListViewModel;

public class CaseEditSampleListFragment extends BaseEditFragment<FragmentFormListLayoutBinding, List<Sample>, Case> implements OnListItemClickListener {

    private SampleListAdapter adapter;
    private SampleListViewModel model;
    private LinearLayoutManager linearLayoutManager;

    public static CaseEditSampleListFragment newInstance(Case activityRootData) {
        return newInstance(CaseEditSampleListFragment.class, null, activityRootData);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((CaseEditActivity) getActivity()).showPreloader();
        adapter = new SampleListAdapter(R.layout.row_sample_list_item_layout, this);
        model = ViewModelProviders.of(this).get(SampleListViewModel.class);
        model.getSamples(getActivityRootData()).observe(this, samples -> {
            adapter.replaceAll(samples);
            adapter.notifyDataSetChanged();
            updateEmptyListHint(samples);
            ((CaseEditActivity) getActivity()).hidePreloader();
        });
    }

    @Override
    protected String getSubHeadingTitle() {
        Resources r = getResources();
        return r.getString(R.string.caption_case_samples);
    }

    @Override
    public List<Sample> getPrimaryData() {
        throw new UnsupportedOperationException("Sub list fragments don't hold their data");
    }

    @Override
    protected void prepareFragmentData() {

    }

    @Override
    public void onLayoutBinding(FragmentFormListLayoutBinding contentBinding) {
        linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        contentBinding.recyclerViewForList.setLayoutManager(linearLayoutManager);
        contentBinding.recyclerViewForList.setAdapter(adapter);
    }

    @Override
    public int getRootEditLayout() {
        return R.layout.fragment_root_list_form_layout;
    }

    @Override
    public int getEditLayout() {
        return R.layout.fragment_form_list_layout;
    }

    @Override
    public void onListItemClick(View view, int position, Object item) {
        Sample sample = (Sample) item;
        SampleEditActivity.startActivity(getActivity(), sample.getUuid());
    }

    @Override
    public boolean isShowSaveAction() {
        return false;
    }

    @Override
    public boolean isShowNewAction() {
        return true;
    }
}
