package ch.usz.c3pro.dataqueue.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ch.usz.c3pro.C3PRO;
import ch.usz.c3pro.dataqueue.DataQueue;

/**
 * C3PRO
 *
 * Created by manny Weber on 06/07/16.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This job will create the provided resource on the server at the FHIRServerURL.
 * It will persist, which means, the job will stay in the dataqueue until a network is available
 * the resurce is uploaded. If more than one Job is added to the dataqueue, they will be uploaded
 * FIFO. If no FHIRServerURL is provided, the serverURL from the C3PRO will be used.
 */
public class CreateResourceJob extends Job {
    private IBaseResource uploadResource;
    private String serverURL;
    /**localID is needed for persistence*/
    private long localID;

    /**
     * Enqueues the resource to be uploaded to the provided FHIRServer. The job will persist even
     * when app state changes.
     * */
    public CreateResourceJob(IBaseResource FHIRResource, String FHIRServerURL){
        super(new Params(Priority.MID).requireNetwork().persist().groupBy(DataQueue.UPLOAD_GROUP_TAG));
        uploadResource = FHIRResource;
        serverURL = FHIRServerURL;
        localID = -System.currentTimeMillis();
    }

    /**
     * Enqueues the resource to be uploaded to the FHIRServer defined in the C3PRO. The job will
     * persist even when app state changes.
     * */
    public CreateResourceJob(IBaseResource resource){
        this(resource, C3PRO.getDataQueue().getFHIRServerURL());
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        IGenericClient client = C3PRO.getFhirContext().newRestfulGenericClient(serverURL);
        MethodOutcome outcome = client.create().resource(uploadResource).prettyPrint().encodedJson().execute();
        //TODO decide what to do when upload does not return anything
        Log.d("SENDJOBS", "created resource with id "+outcome.getId().getValue());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
