package de.symeda.sormas.app.event;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.event.Event;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.component.SelectOrCreatePersonDialog;
import de.symeda.sormas.app.person.SyncPersonsTask;
import de.symeda.sormas.app.util.Callback;
import de.symeda.sormas.app.util.ParamCallback;

public class EventParticipantNewActivity extends AppCompatActivity {

    private String eventUuid;

    private EventParticipantNewPersonTab eventParticipantNewPersonTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle params = getIntent().getExtras();
        eventUuid = params.getString(EventEditActivity.KEY_EVENT_UUID);

        setContentView(R.layout.sormas_root_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getText(R.string.headline_new_eventParticipant));
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();


        eventParticipantNewPersonTab = new EventParticipantNewPersonTab();
        eventParticipantNewPersonTab.setArguments(params);
        ft.add(R.id.fragment_frame, eventParticipantNewPersonTab).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_action_bar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_action_help,false);
        menu.setGroupVisible(R.id.group_action_add,false);
        menu.setGroupVisible(R.id.group_action_save,true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                //Home/back button
                finish();

                return true;

            case R.id.action_save:
                try {
                    final EventParticipant eventParticipant = eventParticipantNewPersonTab.getData();

                    boolean eventParticipantDescReq =  eventParticipant.getInvolvementDescription()==null||eventParticipant.getInvolvementDescription().isEmpty();
                    boolean eventParticipantFirstNameReq =  eventParticipant.getPerson().getFirstName()==null||eventParticipant.getPerson().getFirstName().isEmpty();
                    boolean eventParticipantLastNameReq =  eventParticipant.getPerson().getLastName()==null||eventParticipant.getPerson().getLastName().isEmpty();

                    boolean validData = !eventParticipantDescReq
                            && !eventParticipantFirstNameReq
                            && !eventParticipantLastNameReq;

                    if(validData) {

                        List<Person> existingPersons = DatabaseHelper.getPersonDao().getAllByName(eventParticipant.getPerson().getFirstName(), eventParticipant.getPerson().getLastName());
                        if (existingPersons.size() > 0) {

                            AlertDialog.Builder dialogBuilder = new SelectOrCreatePersonDialog(this, eventParticipant.getPerson(), existingPersons, new ParamCallback() {
                                @Override
                                public void call(Object parameter) {
                                    if(parameter instanceof Person) {
                                        eventParticipant.setPerson((Person) parameter);
                                        savePersonAndEventParticipant(eventParticipant);
                                        showEventParticipantEditView(eventParticipant);
                                    }
                                }
                            });
                            AlertDialog newPersonDialog = dialogBuilder.create();
                            newPersonDialog.show();

                        } else {
                            savePersonAndEventParticipant(eventParticipant);
                            showEventParticipantEditView(eventParticipant);
                        }
                    }
                    else {
                        if(eventParticipantDescReq) {
                            Toast.makeText(this, "Not saved. Please specify the involvement description.", Toast.LENGTH_LONG).show();
                        }
                        else if(eventParticipantFirstNameReq) {
                            Toast.makeText(this, "Not saved. Please specify the first name.", Toast.LENGTH_LONG).show();
                        }
                        else if(eventParticipantLastNameReq) {
                            Toast.makeText(this, "Not saved. Please specify the last name.", Toast.LENGTH_LONG).show();
                        }
                    }

                    return true;
                } catch (Exception e) {
                    Toast.makeText(this, "Error while saving the event person. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

        }
        return super.onOptionsItemSelected(item);
    }


    private void savePersonAndEventParticipant(EventParticipant eventParticipant) {
        // save the person
        DatabaseHelper.getPersonDao().save(eventParticipant.getPerson());
        // set the given event
        Event event = DatabaseHelper.getEventDao().queryUuid(eventUuid);
        eventParticipant.setEvent(event);
        // save the contact
        DatabaseHelper.getEventParticipantDao().save(eventParticipant);

        new SyncPersonsTask().execute();
        new SyncEventParticipantsTask().execute();

        Toast.makeText(this, "Event person  saved", Toast.LENGTH_SHORT).show();


    }

    private void showEventParticipantEditView(EventParticipant eventParticipant) {
        Intent intent = new Intent(this, EventParticipantEditActivity.class);
        intent.putExtra(EventParticipant.UUID, eventParticipant.getUuid());
        startActivity(intent);
    }


}