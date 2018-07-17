package de.symeda.sormas.app.event.read.sub;

import android.os.Bundle;

import de.symeda.sormas.app.BaseReadFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.databinding.FragmentEventReadPersonInvolvedInfoLayoutBinding;
import de.symeda.sormas.app.databinding.FragmentPersonReadLayoutBinding;
import de.symeda.sormas.app.person.edit.PersonEditFragment;
import de.symeda.sormas.app.shared.EventParticipantFormNavigationCapsule;
import de.symeda.sormas.app.util.InfrastructureHelper;

public class EventParticipantReadFragment extends BaseReadFragment<FragmentEventReadPersonInvolvedInfoLayoutBinding, EventParticipant, EventParticipant> {

    private EventParticipant record;

    // Instance methods

    public static EventParticipantReadFragment newInstance(EventParticipantFormNavigationCapsule capsule, EventParticipant activityRootData) {
        return newInstance(EventParticipantReadFragment.class, capsule, activityRootData);
    }

    private void setUpFieldVisibilities(FragmentPersonReadLayoutBinding contentBinding) {
        InfrastructureHelper.initializeHealthFacilityDetailsFieldVisibility(contentBinding.personOccupationFacility, contentBinding.personOccupationFacilityDetails);
        PersonEditFragment.initializeCauseOfDeathDetailsFieldVisibility(contentBinding.personCauseOfDeath, contentBinding.personCauseOfDeathDisease, contentBinding.personCauseOfDeathDetails);
    }

    // Overrides

    @Override
    protected void prepareFragmentData(Bundle savedInstanceState) {
        record = getActivityRootData();
    }

    @Override
    public void onLayoutBinding(FragmentEventReadPersonInvolvedInfoLayoutBinding contentBinding) {
        contentBinding.setData(record);
    }

    @Override
    public void onAfterLayoutBinding(FragmentEventReadPersonInvolvedInfoLayoutBinding contentBinding) {
        setUpFieldVisibilities(contentBinding.eventParticipantPersonLayout);
    }

    @Override
    protected String getSubHeadingTitle() {
        return getResources().getString(R.string.caption_person_involved);
    }

    @Override
    public EventParticipant getPrimaryData() {
        return record;
    }

    @Override
    public int getReadLayout() {
        return R.layout.fragment_event_read_person_involved_info_layout;
    }

}
