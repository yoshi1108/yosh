package hoge;

import java.util.ArrayList;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class BatchJobEngine extends QuartzJobBean {
    private List<BatchJobIf> batchJobs_;

    public void setBatchJobs(BatchJobIf[] aBatchJobs) {
        batchJobs_ = new ArrayList<BatchJobIf>();
        for (int i = 0; i < aBatchJobs.length; i++) {
            batchJobs_.add(aBatchJobs[i]);
        }
    }
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        for (BatchJobIf batchJob : batchJobs_) {
            batchJob.executeJob();
        }
    }
}
