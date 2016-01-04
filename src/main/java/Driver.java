import Summarizer.Group;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Created by brianzhao on 1/2/16.
 */
public class Driver {
    public static void main(String[] args) {

        File transcriptFolder = new File("TimssTranscripts");
        File[] files = transcriptFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) { //math and science directories
                File[] countryFolders = file.listFiles();
                for (File countryFolder : countryFolders) {
                    System.out.println(countryFolder.getPath());
                    if (countryFolder.isDirectory()) { //country folder directories
                        File[] actualTextFiles = countryFolder.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".txt");
                            }
                        });

                        for (File timmsFile : actualTextFiles) {
                            outputInfo(timmsFile);
                        }
                    }
                }
            }
        }
    }



    public static void outputInfo(File timmsFile){
        TimmsSummarizer timmsSummarizer = new TimmsSummarizer(timmsFile);
        List<Group> groups = timmsSummarizer.simpleFrequencySummary();
        timmsSummarizer.printWordInformation();
        TimmsSummarizer.printGroupListInfo(groups);
        timmsSummarizer.printHistogramInformation();
    }
}
