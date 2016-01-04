import Summarizer.*;

import java.io.*;
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
          LinkedHashMap<String, String> timeToText = new LinkedHashMap<>();
        String fakeLastTime = null;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))){
            String line;
            while ((line= bufferedReader.readLine()) != null) {
                if (line.equals("�")) {
                    continue;
                }
                String[] lineArray = line.split("\t");
                if (lineArray.length == 1 &&
                        (lineArray[0].matches("\\d\\d:\\d\\d:\\d\\d\\s*$") ||
                                lineArray[0].matches(".*Form\\s*$")) ||
                                lineArray[0].matches("\\s*$")
                        ) {
                    continue;
                }
                if (lineArray.length != 3) {
                    System.out.println(lineArray.length);
                    System.out.println(line);
                    for (String string : lineArray) {
                        System.out.println(string);
                    }

                    System.out.println(inputFile.getPath());
                    throw new RuntimeException("The transcript file has an incorrect number of sections after " +
                            "splitting by tabs!!!");

                }

                String time = lineArray[0];
                fakeLastTime = time;
                String speaker = lineArray[1]; //unused
                String contentString = lineArray[2];
                timeToText.put(time, contentString);
            }
        } catch (IOException e) {
            e.printStackTrace();
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

        /**
         * debug related print statements
         */
        System.out.println("Creating simple frequency summary of Timms Video: " + this.timmsTranscriptFile.getName());
        System.out.println("Weight type: " + Constants.DEFAULT_WEIGHT_TYPE);
        System.out.println("Summary length is: " + (Constants.DEFAULT_SUMMARY_PROPORTION * 100) + "% of video length");
        System.out.println("Cutoff percentile for creating groups: " + (Constants.DEFAULT_CUTOFF * 100) + " percentile");
        System.out.println();
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


    public void printWordInformation() {
        if (!transcript.isAnalyzedYet()) {
            throw new RuntimeException("Can't generate histogram of transcript whose word counts haven't been analyzed yet");
        }
//        List<StringData> allWordsSortedByWeight = this.transcript.getAllStringData().internalStringDatas();
//        for (StringData stringData : allWordsSortedByWeight) {
//            System.out.println(stringData.getUnstemmedWord());
//            System.out.println("\tTf: " + stringData.getTf());
//            System.out.println("\tTfIdf: " + stringData.getTfIdf());
//        }

        System.out.println("All Words TF Values: ");
        List<StringData> allWordsSortedByTF = this.transcript.getAllStringData().sortByTfDescending();
        for (StringData stringData : allWordsSortedByTF) {
            System.out.println(stringData.getUnstemmedWord());
            System.out.println("\tTf: " + stringData.getTf());
        }

        System.out.println("\n\n");

        System.out.println("All Words TfIdf Values: ");
        List<StringData> allWordsSortedByTfIdf = this.transcript.getAllStringData().sortByTfIdfDescending();
        for (StringData stringData : allWordsSortedByTfIdf) {
            System.out.println(stringData.getUnstemmedWord());
            System.out.println("\tTf-Idf: " + stringData.getTfIdf());
        }
    }

    public void printHistogramInformation() {
        //find the highest importance value, then score everything relative to that
        double maxImportance = Double.MIN_VALUE;
        List<TimeRegion> timeRegions = transcript.getTimeRegions();
        for (TimeRegion timeRegion : timeRegions) {
            if (timeRegion.getImportance() > maxImportance) {
                maxImportance = timeRegion.getImportance();
            }
        }

        //sort all of the timeregions by time
        Collections.sort(timeRegions, TimeRegionComparators.startTimeComparator);


        for (TimeRegion timeRegion : timeRegions) {
            System.out.printf("%20s:", timeRegion.getStartTime());
            int numStars = computePercentage(timeRegion.getImportance(), maxImportance);
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < numStars/2; i++) {
                stars.append('*');
            }
            System.out.println(stars.toString());
        }
    }


    /**
     * returns an integer between 0 and 100 inclusive, representing the proportion the part is relative to the whole
     * eg: a part of 2 and a whole of 50 would return "4"
     * @param part
     * @param whole
     * @return
     */
    public static int computePercentage(double part, double whole) {
        double fraction = part/ whole;
        return (int) (fraction * 100);
    }


    public static void printGroupListInfo(List<Group> groups) {
        /**
         * debug related print statements
         */
        for (int i = 0; i < groups.size(); i++) {
            Group currentGroup = groups.get(i);
            System.out.println("Group " + i);
            System.out.println("Times: " + currentGroup.getStartTime() + "-" + currentGroup.getEndTime());
            System.out.println("Importance: " + currentGroup.getTotalImportance());
            System.out.println("[ \n\n" + groups.get(i) + "]\n\n");
        }
        System.out.println();
    }

}
