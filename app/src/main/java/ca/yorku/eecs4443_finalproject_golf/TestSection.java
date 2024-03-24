package ca.yorku.eecs4443_finalproject_golf;

import android.content.Context;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestSection {
    ArrayList<TestBundle> tests;
    String name;
    int size;
    TYPE type;
    ArrayList<TestResult> testResults;

    public TestSection(Context ctx, ArrayList<TestBundle> tests, int name, TYPE type) {
        this.tests = tests;
        this.name = ctx.getString(name);
        this.size = tests.size();
        this.type = type;

        testResults = new ArrayList<>();
    }

    public TestBundle getNextTest() {
        return tests.remove(0);
    }

    public boolean isTest() {
        return type == TYPE.TEST;
    }

    public void completeTest(TestBundle test, int attempts) {
        TestResult result = test.complete(attempts);
        testResults.add(result);
    }

    private String collectResults(Function<TestResult, String> mapper) {
        return testResults.stream().map(mapper).collect(Collectors.joining());
    }

    public String getCSV() {
        return collectResults(TestResult::toCSV);
    }

    public String getPrettyPrint() {
        return collectResults(TestResult::toString);
    }

    public void writeResults(Context ctx) {
        SharedPref.appendAndSaveCSVData(ctx, getCSV());
    }

    public enum TYPE {
        TUTORIAL,
        TEST
    }
}
