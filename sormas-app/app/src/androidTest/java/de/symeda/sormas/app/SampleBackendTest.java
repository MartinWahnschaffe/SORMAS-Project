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

package de.symeda.sormas.app;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import junit.framework.TestResult;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import de.symeda.sormas.api.sample.SampleTestResultType;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.sample.Sample;
import de.symeda.sormas.app.backend.sample.SampleTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Mate Strysewske on 16.06.2017.
 */
@RunWith(AndroidJUnit4.class)
public class SampleBackendTest {

    @Rule
    public final ActivityTestRule<TestBackendActivity> testActivityRule = new ActivityTestRule<>(TestBackendActivity.class, false, true);

    @Before
    public void initTest() {
        TestHelper.initTestEnvironment(false);
    }

    @Test
    public void shouldCreateSample() {
        assertThat(DatabaseHelper.getSampleDao().queryForAll().size(), is(0));
        TestEntityCreator.createSample(null);

        assertThat(DatabaseHelper.getSampleDao().queryForAll().size(), is(1));
    }

    @Test
    public void shouldCreateSampleTest() {
        assertThat(DatabaseHelper.getSampleTestDao().queryForAll().size(), is(0));

        Sample sample = TestEntityCreator.createSample(null);
        TestEntityCreator.createSampleTest(sample);

        assertThat(DatabaseHelper.getSampleTestDao().queryForAll().size(), is(1));
    }

    @Test
    public void shouldMergeSamplesAsExpected() throws DaoException {
        Sample sample = TestEntityCreator.createSample(null);
        SampleTest sampleTest = TestEntityCreator.createSampleTest(sample);

        sample.setComment("AppSampleComment");
        sampleTest.setTestResult(SampleTestResultType.NEGATIVE);

        DatabaseHelper.getSampleDao().saveAndSnapshot(sample);
        DatabaseHelper.getSampleDao().accept(sample);
        DatabaseHelper.getSampleTestDao().saveAndSnapshot(sampleTest);
        DatabaseHelper.getSampleTestDao().accept(sampleTest);

        Sample mergeSample = (Sample) sample.clone();
        mergeSample.setAssociatedCase((Case) sample.getAssociatedCase().clone());
        mergeSample.setId(null);
        mergeSample.setComment("ServerSampleComment");

        SampleTest mergeSampleTest = (SampleTest) sampleTest.clone();
        mergeSampleTest.setId(null);
        mergeSampleTest.setTestResult(SampleTestResultType.POSITIVE);

        DatabaseHelper.getSampleDao().mergeOrCreate(mergeSample);
        DatabaseHelper.getSampleTestDao().mergeOrCreate(mergeSampleTest);

        Sample updatedSample = DatabaseHelper.getSampleDao().queryUuid(sample.getUuid());
        assertThat(updatedSample.getComment(), is("ServerSampleComment"));
        SampleTest updatedSampleTest = DatabaseHelper.getSampleTestDao().queryUuid(sampleTest.getUuid());
        assertThat(updatedSampleTest.getTestResult(), is(SampleTestResultType.POSITIVE));
    }

    @Test
    public void shouldAcceptAsExpected() throws DaoException {
        Sample sample = TestEntityCreator.createSample(null);
        assertThat(sample.isModified(), is(false));

        sample.setComment("NewSampleComment");

        DatabaseHelper.getSampleDao().saveAndSnapshot(sample);
        sample = DatabaseHelper.getSampleDao().queryUuid(sample.getUuid());

        assertThat(sample.isModified(), is(true));
        assertNotNull(DatabaseHelper.getSampleDao().querySnapshotByUuid(sample.getUuid()));

        DatabaseHelper.getSampleDao().accept(sample);
        sample = DatabaseHelper.getSampleDao().queryUuid(sample.getUuid());

        assertNull(DatabaseHelper.getSampleDao().querySnapshotByUuid(sample.getUuid()));
        assertThat(sample.isModified(), is(false));
    }

}
