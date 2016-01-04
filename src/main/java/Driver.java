import Summarizer.Group;

import java.io.File;
import java.util.List;

/**
 * Created by brianzhao on 1/2/16.
 */
public class Driver {
    public static void main(String[] args) {

        File transcriptFolder = new File("TimssTranscripts");
        File[] files = transcriptFolder.listFiles();
        for (File file : files) {
            System.out.println(file.getPath());
        }


//        TimmsSummarizer timmsSummarizer = new TimmsSummarizer(new File("/Users/brianzhao/Documents/IntellijProjects/VideoSumTimms/TimssTranscripts/Math/Math US1 transcript.txt"));
//        List<Group> groups = timmsSummarizer.simpleFrequencySummary();
//        timmsSummarizer.printWordInformation();
//        TimmsSummarizer.printGroupListInfo(groups);
//        timmsSummarizer.printHistogramInformation();
    }
}
