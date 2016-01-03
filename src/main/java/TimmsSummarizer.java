import Summarizer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by brianzhao on 12/23/15.
 */
public class TimmsSummarizer {
    private File timmsTranscriptFile;
    private String rawTranscript;
    private Transcript transcript;

    public TimmsSummarizer(File timmsTranscriptFile) {
        this.timmsTranscriptFile = timmsTranscriptFile;
        this.rawTranscript = fileToRawTranscriptString(this.timmsTranscriptFile);
        this.transcript = new Transcript(rawTranscript);
    }

    /**
     * @param inputFile File of timms transcript
     * @return string of raw transcript
     */
    private static String fileToRawTranscriptString(File inputFile) {
        if (!inputFile.exists()) {
            throw new RuntimeException("File does not exist!");
        }
        StringBuilder output = new StringBuilder();
        Scanner scanner = null;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LinkedHashMap<String, String> timeToText = new LinkedHashMap<>();
        String fakeLastTime = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] lineArray = line.split("\t");
            if (lineArray.length != 3) {
                throw new RuntimeException("The transcript file has an incorrect number of sections after " +
                        "splitting by tabs!!!");
            }

            String time = lineArray[0];
            fakeLastTime = time;
            String speaker = lineArray[1]; //unused
            String contentString = lineArray[2];
            timeToText.put(time, contentString);
        }

        fakeLastTime = TimeRegion.secondsToTimeString(TimeRegion.calculateSeconds(fakeLastTime) + 1);
        return timeToTextMappingToRawTranscript(timeToText, fakeLastTime);
    }

    /**
     * converts the linkedHashmap of key: timeString to value: word string that was said at that time into
     * a complete, properly formatted raw transcript of type String
     *
     * @param timeToText
     * @param videoEndTime
     * @return
     */
    public static String timeToTextMappingToRawTranscript(LinkedHashMap<String, String> timeToText, String videoEndTime) {
        StringBuilder rawTranscriptBuilder = new StringBuilder();
        Iterator<String> keyIterator = timeToText.keySet().iterator();
        if (!keyIterator.hasNext()) { //need to have transcript of at least 1 region
            return null;
        }

        String time = keyIterator.next(); //get time
        while (keyIterator.hasNext()) {
            if (!timeToText.get(time).isEmpty()) { //if the current string isn't empty
                String segmentStartTime = time;
                time = keyIterator.next();
                String segmentEndTime = time;
                String contents = timeToText.get(segmentStartTime).replaceAll("\n", " ").trim();
                rawTranscriptBuilder.append(segmentStartTime).append(Constants.TIME_REGION_DELIMITER).append(segmentEndTime).append('\n').append(contents).append('\n');
            } else {
                time = keyIterator.next();
            }
        }

        //do last time
        if (!timeToText.get(time).isEmpty()) { //if the current string isn't empty
            String contents = timeToText.get(time).replaceAll("\n", " ");
            rawTranscriptBuilder.append(time).append(Constants.TIME_REGION_DELIMITER).append(videoEndTime).append('\n').append(contents);
        }
        return rawTranscriptBuilder.toString();
    }

    public List<Group> simpleFrequencySummary() {
        SimpleFrequencySummary simpleFrequencySummary = new SimpleFrequencySummary(this.transcript);
        return simpleFrequencySummary.generateSummary();
    }


    public List<Group> simpleFrequencySummary(Double percentageOfTopwords,
                                              Double percentageOfVideo,
                                              Double cutOffValue,
                                              Weight weightType,
                                              Boolean normalizeOnDuration) {
        SimpleFrequencySummary simpleFrequencySummary = new SimpleFrequencySummary(this.transcript);
        return simpleFrequencySummary.generateSummary(
                percentageOfTopwords,
                percentageOfVideo,
                cutOffValue,
                weightType,
                normalizeOnDuration);
    }



    public static void main(String[] args) {
        TimmsSummarizer timmsSummarizer = new TimmsSummarizer(new File("/Users/brianzhao/Documents/IntellijProjects/VideoSumTimms/src/TimssTranscripts/Math/Math AU 1 transcript.txt"));
        List<Group> groups = timmsSummarizer.simpleFrequencySummary();
        for (int i = 0; i < groups.size(); i++) {
            Group currentGroup = groups.get(i);
            System.out.println("Group " + i + "\t" + currentGroup.getStartTime() + "-" + currentGroup.getEndTime());

            System.out.println(groups.get(i));
        }
    }


}
